package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.component.ModDataComponents;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.entity.ThrownDagger;
import com.tarcitani.solomod.event.CombatEvents;
import com.tarcitani.solomod.event.StatsLogic.WeaponAbilities;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.mob.Hazards;
import com.tarcitani.solomod.network.HitFeedbackPayload;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.network.CrescentSlashPayload;
import com.tarcitani.solomod.network.TechniqueSelectionPayload;
import com.tarcitani.solomod.network.WeaponMotionPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * Habilidade própria de cada FORMATO de arma, aprendida na árvore e com custo de mana em armas normais
 * (a habilidade avançada aprendida — Investida, Impacto, Explosão... — passa para agachar + clique direito):
 *
 * <ul>
 *   <li>Rapieira — R alterna dash carregado (3–14 blocos), Chuva de Agulhas (três acertos em cone)
 *       e Passo do Vento (estocada seguida de recuo com colisão).</li>
 *   <li>Adaga — Arremesso (clique direito): lança a adaga, que crava no chão ou no alvo. Clique direito de novo
 *       chama de volta; Shift + clique direito teleporta o dono para um ponto seguro junto dela e a recolhe.</li>
 *   <li>Martelo — Salto Esmagador: no chão salta; no ar mergulha imediatamente. A altura da queda aumenta
 *       dano e raio, com teto em 24 blocos. O machado legado continua iniciando só no chão.
 *       R alterna salto, Giro Demolidor circular e Ruptura Sísmica frontal que para em paredes/bordas.
 *       Impacto em área, blocos naturais saltando (mobGriefing), poeira e tremor.</li>
 *   <li>Espada longa — R seleciona Meia-Lua, Ascendente ou Cruzado; clique direito prepara por 6 ticks
 *       e executa a técnica. O Cruzado acerta duas vezes, com intervalo de 10 ticks.
 *       Seleção confirmada pelo servidor; todas compartilham a recarga do item.</li>
 * </ul>
 * O servidor guarda uma seleção por classe/sessão, valida carga, posse da adaga, destino e recargas. Ações em andamento são
 * canceladas ao morrer, trocar de arma/dimensão ou desconectar. Balanceamento em {@link WeaponAbilities}.
 */
public final class ClassAbility {
    private ClassAbility() {}

    private record Lunge(Vec3 dir, Set<Integer> hit, int[] left, double speed,
                         ItemStack weapon, ResourceKey<Level> dimension) {}

    private record Leap(long start, boolean[] dived, double[] peakY, ItemStack weapon, ResourceKey<Level> dimension) {}

    private record Crescent(long start, Vec3 dir, ItemStack weapon, ResourceKey<Level> dimension, WeaponTechnique technique) {}

    private record TechniqueAction(long start, Vec3 dir, ItemStack weapon, ResourceKey<Level> dimension,
                                   WeaponTechnique technique, Set<Integer> hit, Vec3[] waveOrigin, double[] waveLength) {}

    private static final Map<UUID, Lunge> LUNGES = new HashMap<>();
    private static final Map<UUID, Leap> LEAPS = new HashMap<>();
    private static final Map<UUID, Crescent> CRESCENTS = new HashMap<>();
    private static final Map<UUID, Long> NO_FALL_UNTIL = new HashMap<>();
    private static final Map<UUID, java.util.EnumMap<WeaponClass, WeaponTechnique>> TECHNIQUES = new HashMap<>();
    private static final Map<UUID, TechniqueAction> ACTIONS = new HashMap<>();

    public static WeaponTechnique technique(ServerPlayer player, WeaponClass weapon) {
        var selections = TECHNIQUES.get(player.getUUID());
        WeaponTechnique selected = selections == null ? WeaponTechnique.defaultFor(weapon) : selections.getOrDefault(weapon, WeaponTechnique.defaultFor(weapon));
        if (com.tarcitani.solomod.event.StatsLogic.stats(player).hasTalent(SkillTree.forTechnique(selected).id())) return selected;
        return WeaponTechnique.options(weapon).stream().filter(t -> com.tarcitani.solomod.event.StatsLogic.stats(player).hasTalent(SkillTree.forTechnique(t).id()))
                .findFirst().orElse(WeaponTechnique.defaultFor(weapon));
    }

    /** Troca apenas a seleção desta sessão; o cooldown continua sendo o do item. */
    public static void cycleTechnique(ServerPlayer player) {
        if (!canStart(player) || player.isUsingItem() || !(player.getMainHandItem().getItem() instanceof ModularWeaponItem item)
                || !WeaponTechnique.supports(item.weaponClass()) || ModularWeaponItem.getData(player.getMainHandItem()).weaponClass() != item.weaponClass()) return;
        var options = WeaponTechnique.options(item.weaponClass()).stream()
                .filter(t -> com.tarcitani.solomod.event.StatsLogic.stats(player).hasTalent(SkillTree.forTechnique(t).id())).toList();
        if (options.isEmpty()) return;
        WeaponTechnique old = technique(player, item.weaponClass());
        WeaponTechnique next = options.get((options.indexOf(old) + 1) % options.size());
        TECHNIQUES.computeIfAbsent(player.getUUID(), id -> new java.util.EnumMap<>(WeaponClass.class)).put(item.weaponClass(), next);
        PacketDistributor.sendToPlayer(player, new TechniqueSelectionPayload(next));
        player.displayClientMessage(Component.translatable("message.solomod.technique_selected", next.displayName()), true);
    }

    /** Formatos cuja habilidade é no clique direito. */
    public static boolean hasRightClick(WeaponClass wc) {
        return WeaponClass.AVAILABLE.contains(wc) || wc == WeaponClass.AXE;
    }

    public static boolean hasAbility(WeaponClass wc) {
        return hasRightClick(wc);
    }

    public static Component displayName(WeaponClass wc) {
        return Component.translatable("ability.solomod." + wc.getSerializedName());
    }

    public static boolean isThrown(ItemStack stack) {
        return stack.has(ModDataComponents.THROWN_DAGGER.get());
    }

    public static boolean canStart(ServerPlayer player) {
        UUID id = player.getUUID();
        return player.isAlive() && !player.isSpectator() && !player.isPassenger() && !player.isSleeping()
                && !DodgeRoll.isRolling(player) && !LUNGES.containsKey(id) && !LEAPS.containsKey(id) && !CRESCENTS.containsKey(id) && !ACTIONS.containsKey(id);
    }

    // ------------------------------------------------------------------ clique direito

    /** Executa a habilidade do formato. Devolve false se não pôde (recarga, no ar...). */
    public static boolean use(ServerPlayer player, ItemStack stack, ModularWeaponItem item) {
        if (!canStart(player) || player.getMainHandItem() != stack || stack.getItem() != item) return false;
        WeaponData data = ModularWeaponItem.getData(stack);
        if (data.weaponClass() != item.weaponClass()) return false;
        if (data.weaponClass() == WeaponClass.DAGGER && isThrown(stack)) {
            if (player.isShiftKeyDown()) return teleportToDagger(player, stack, item);
            recallDagger(player, stack);
            return true;
        }
        if (player.getCooldowns().isOnCooldown(item)) return false;
        switch (data.weaponClass()) {
            case RAPIER -> {
                WeaponTechnique technique = technique(player, WeaponClass.RAPIER);
                if (technique == WeaponTechnique.RAPIER_DASH) return false; // Uso contínuo vanilla.
                if (!SkillTree.consume(player, SkillTree.forTechnique(technique))) return false;
                startTechnique(player, stack, technique);
            }
            case DAGGER -> {
                if (!SkillTree.consume(player, SkillTree.classNode(player, item.weaponClass()))) return false;
                throwDagger(player, stack);
                player.getCooldowns().addCooldown(item, WeaponAbilities.DAGGER_THROW_COOLDOWN);
            }
            case AXE, HAMMER -> {
                if (item.weaponClass() == WeaponClass.HAMMER && technique(player, WeaponClass.HAMMER) != WeaponTechnique.HAMMER_LEAP) {
                    if (!grounded(player) || player.isInWaterOrBubble() || player.isInLava() || player.isFallFlying() || player.getAbilities().flying) return false;
                    if (!SkillTree.consume(player, SkillTree.classNode(player, item.weaponClass()))) return false;
                    startTechnique(player, stack, technique(player, WeaponClass.HAMMER));
                    break;
                }
                if (item.weaponClass() == WeaponClass.AXE && !grounded(player)) {
                    player.displayClientMessage(Component.translatable("message.solomod.need_ground").withStyle(ChatFormatting.RED), true);
                    return false;
                }
                if (player.isInWaterOrBubble() || player.isInLava() || player.isFallFlying() || player.getAbilities().flying) return false;
                if (!SkillTree.consume(player, SkillTree.classNode(player, item.weaponClass()))) return false;
                startLeap(player, stack);
                player.getCooldowns().addCooldown(item, WeaponAbilities.HAMMER_COOLDOWN);
            }
            case LONGSWORD -> {
                if (!SkillTree.consume(player, SkillTree.classNode(player, item.weaponClass()))) return false;
                startCrescent(player, stack);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ rapieira

    /** Distância desejada, também usada pelo HUD; o tempo válido é medido pelo uso do item no servidor. */
    public static double rapierDistance(int chargeTicks) {
        double charge = net.minecraft.util.Mth.clamp(chargeTicks / (double) WeaponAbilities.RAPIER_CHARGE_TICKS, 0, 1);
        return WeaponAbilities.RAPIER_MIN_DISTANCE
                + (WeaponAbilities.RAPIER_MAX_DISTANCE - WeaponAbilities.RAPIER_MIN_DISTANCE) * charge;
    }

    public static void tickRapierCharge(ServerPlayer player, ItemStack stack, int chargeTicks) {
        if (player.getMainHandItem() != stack || !canStart(player) || technique(player, WeaponClass.RAPIER) != WeaponTechnique.RAPIER_DASH) {
            player.stopUsingItem();
            return;
        }
        if (chargeTicks % 4 == 0) {
            Vec3 tip = player.getEyePosition().add(flat(player.getLookAngle(), player).scale(0.8));
            player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, tip.x, tip.y, tip.z,
                    2 + Math.min(chargeTicks, WeaponAbilities.RAPIER_CHARGE_TICKS) / 6, 0.12, 0.12, 0.12, 0.01);
        }
        if (chargeTicks == WeaponAbilities.RAPIER_CHARGE_TICKS) sound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F, 1.6F);
    }

    public static void releaseRapier(ServerPlayer player, ItemStack stack, int chargeTicks) {
        if (!canStart(player) || player.getMainHandItem() != stack || player.getUseItem() != stack
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND
                || !(stack.getItem() instanceof ModularWeaponItem item) || item.weaponClass() != WeaponClass.RAPIER
                || technique(player, WeaponClass.RAPIER) != WeaponTechnique.RAPIER_DASH
                || player.getCooldowns().isOnCooldown(item)) return;
        if (!SkillTree.consume(player, SkillTree.forTechnique(WeaponTechnique.RAPIER_DASH))) return;
        double distance = rapierDistance(chargeTicks);
        int ticks = (int) Math.ceil(distance / WeaponAbilities.RAPIER_SPEED);
        Vec3 dir = flat(player.getLookAngle(), player);
        LUNGES.put(player.getUUID(), new Lunge(dir, new HashSet<>(), new int[]{ticks}, distance / ticks,
                stack, player.level().dimension()));
        player.getCooldowns().addCooldown(item, WeaponAbilities.RAPIER_COOLDOWN);
        ModNetwork.sendPlayerAnim(player, PlayerAnim.THRUST, ticks + 4, true);
        ModNetwork.sendWeaponMotion(player, WeaponMotionPayload.Kind.RAPIER, player.getId(), player.position(),
                player.position().add(dir.scale(distance)), ticks);
        sound(player, SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.0F, 1.7F);
        sound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.9F);
        player.serverLevel().sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
    }

    private static void tickLunge(ServerPlayer player, Lunge lunge) {
        if (!validAction(player, lunge.weapon(), lunge.dimension()) || lunge.left()[0] <= 0) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.1, 1, 0.1));
            player.hurtMarked = true;
            LUNGES.remove(player.getUUID());
            return;
        }
        ServerLevel level = player.serverLevel();
        WeaponData data = ModularWeaponItem.getData(player.getMainHandItem());
        Vec3 before = player.position().add(0, 1.0, 0);
        Vec3 step = lunge.dir().scale(lunge.speed());
        // A própria colisão vanilla limita o dash; não atravessa paredes nem causa dano através delas.
        Vec3 allowed = Entity.collideBoundingBox(player, step, player.getBoundingBox(), level,
                level.getEntityCollisions(player, player.getBoundingBox().expandTowards(step)));
        if (allowed.lengthSqr() < 0.01) {
            player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
            player.hurtMarked = true;
            LUNGES.remove(player.getUUID());
            return;
        }
        player.setDeltaMovement(allowed.x, player.getDeltaMovement().y, allowed.z);
        player.hurtMarked = true;
        // Rastro: linha de luz na altura do peito + fagulhas do elemento.
        Vec3 ahead = before.add(allowed);
        SkillVfx.line(level, before, ahead, ParticleTypes.END_ROD, 6);
        SkillVfx.line(level, before.add(0, -0.4, 0), ahead.add(0, -0.4, 0), SkillVfx.particleFor(data.elementType()), 6);

        float damage = attack(player) * WeaponAbilities.RAPIER_DAMAGE;
        for (LivingEntity target : targets(player, player.getBoundingBox().expandTowards(allowed).inflate(1.0))) {
            if (player.hasLineOfSight(target) && lunge.hit().add(target.getId()) && target.hurt(player.damageSources().playerAttack(player), damage)) {
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        12, 0.3, 0.3, 0.3, 0.3);
                sound(player, SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.5F);
            }
        }
        lunge.left()[0]--;
    }

    // ------------------------------------------------------------------ adaga

    private static void throwDagger(ServerPlayer player, ItemStack stack) {
        ThrownDagger dagger = new ThrownDagger(player.level(), player, stack);
        dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.4F, 0.4F);
        player.level().addFreshEntity(dagger);
        stack.set(ModDataComponents.THROWN_DAGGER.get(), dagger.getUUID());
        player.swing(InteractionHand.MAIN_HAND, true);
        ModNetwork.sendPlayerAnim(player, PlayerAnim.DAGGER_THROW, 9, true);
        ModNetwork.sendWeaponMotion(player, WeaponMotionPayload.Kind.DAGGER_THROW, dagger.getId(), dagger.position(),
                dagger.position().add(dagger.getDeltaMovement().scale(10)), 1);
        sound(player, SoundEvents.TRIDENT_THROW.value(), 1.0F, 1.6F);
    }

    private static void recallDagger(ServerPlayer player, ItemStack stack) {
        UUID id = stack.get(ModDataComponents.THROWN_DAGGER.get());
        Entity entity = id == null ? null : player.serverLevel().getEntity(id);
        if (entity instanceof ThrownDagger dagger && dagger.isAlive() && dagger.getOwner() == player) {
            if (dagger.isReturning()) return;
            dagger.recall();
            player.swing(InteractionHand.MAIN_HAND, true);
            ModNetwork.sendPlayerAnim(player, PlayerAnim.DAGGER_RECALL, 10, true);
            ModNetwork.sendWeaponMotion(player, WeaponMotionPayload.Kind.DAGGER_RECALL, dagger.getId(), dagger.position(), player.getEyePosition(), 1);
        } else {
            // A adaga ficou num chunk descarregado / outra dimensão: rematerializa na mão.
            stack.remove(ModDataComponents.THROWN_DAGGER.get());
            sound(player, SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.4F);
        }
    }

    /** Shift escolhe o teleporte; a adaga só é recolhida depois de encontrar e alcançar um destino seguro. */
    private static boolean teleportToDagger(ServerPlayer player, ItemStack stack, ModularWeaponItem item) {
        if (player.getCooldowns().isOnCooldown(item)) return false;
        UUID id = stack.get(ModDataComponents.THROWN_DAGGER.get());
        Entity entity = id == null ? null : player.serverLevel().getEntity(id);
        if (!(entity instanceof ThrownDagger dagger) || !dagger.isAlive() || dagger.getOwner() != player
                || player.distanceToSqr(dagger) > WeaponAbilities.DAGGER_TELEPORT_RANGE * WeaponAbilities.DAGGER_TELEPORT_RANGE) {
            player.displayClientMessage(Component.translatable("message.solomod.dagger_unreachable").withStyle(ChatFormatting.RED), true);
            return false;
        }
        Vec3 destination = daggerDestination(player, dagger);
        if (destination == null) {
            player.displayClientMessage(Component.translatable("message.solomod.dagger_no_space").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!SkillTree.consume(player, SkillTree.byId("dagger.teleport"))) return false;
        ServerLevel level = player.serverLevel();
        Vec3 departure = player.position();
        ModNetwork.sendWeaponMotion(player, WeaponMotionPayload.Kind.DAGGER_BLINK, player.getId(), departure, destination, 1);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, departure.x, departure.y + 1, departure.z, 35, 0.4, 0.8, 0.4, 0.12);
        sound(player, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.55F, 1.8F);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.fallDistance = 0;
        daggerCaught(player, dagger);
        dagger.discard();
        player.getCooldowns().addCooldown(item, WeaponAbilities.DAGGER_TELEPORT_COOLDOWN);
        ModNetwork.sendPlayerAnim(player, PlayerAnim.DAGGER_BLINK, 10, true);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, destination.x, destination.y + 1, destination.z, 45, 0.4, 0.8, 0.4, 0.12);
        level.sendParticles(ParticleTypes.FLASH, destination.x, destination.y + 1, destination.z, 1, 0, 0, 0, 0);
        sound(player, SoundEvents.TRIDENT_RIPTIDE_1.value(), 0.9F, 1.8F);
        return true;
    }

    /** No voo aceita ar livre; junto ao chão/parede procura apoio sem ocupar blocos nem carregar chunks. */
    @Nullable
    private static Vec3 daggerDestination(ServerPlayer player, ThrownDagger blade) {
        ServerLevel level = player.serverLevel();
        Vec3 dagger = blade.position();
        if (!blade.isStuck() && clearDaggerDestination(player, dagger)) return dagger;
        for (double radius : new double[]{0, 0.8, 1.6}) {
            int samples = radius == 0 ? 1 : 8;
            for (int i = 0; i < samples; i++) {
                double angle = Math.PI * 2 * i / samples;
                double x = dagger.x + Math.cos(angle) * radius, z = dagger.z + Math.sin(angle) * radius;
                for (int dy : new int[]{0, 1, -1, 2, -2, -3}) {
                    BlockPos floor = BlockPos.containing(x, dagger.y + dy, z);
                    if (!level.hasChunkAt(floor) || level.isOutsideBuildHeight(floor)) continue;
                    BlockState state = level.getBlockState(floor);
                    var support = state.getCollisionShape(level, floor);
                    if (support.isEmpty() || !state.getFluidState().isEmpty() || state.is(BlockTags.FIRE)
                            || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS)
                            || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) continue;
                    double y = floor.getY() + support.max(net.minecraft.core.Direction.Axis.Y);
                    Vec3 point = new Vec3(x, y, z);
                    AABB box = player.getDimensions(Pose.STANDING).makeBoundingBox(point);
                    if (clearDaggerDestination(player, point)
                            && !level.noBlockCollision(player, box.move(0, -0.05, 0))) return point;
                }
            }
        }
        return null;
    }

    private static boolean clearDaggerDestination(ServerPlayer player, Vec3 destination) {
        ServerLevel level = player.serverLevel();
        // Usa a altura em pé mesmo quando o jogador segura Shift.
        AABB box = player.getDimensions(Pose.STANDING).makeBoundingBox(destination);
        if (box.minY < level.getMinBuildHeight() || box.maxY > level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(box)) return false;
        for (double x : new double[]{box.minX, box.maxX}) {
            for (double z : new double[]{box.minZ, box.maxZ}) {
                if (!level.hasChunkAt(BlockPos.containing(x, destination.y, z))) return false;
            }
        }
        return level.noCollision(player, box) && !level.containsAnyLiquid(box)
                && level.getBlockStates(box).noneMatch(state -> state.is(BlockTags.FIRE) || state.is(Blocks.CACTUS)
                || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE));
    }

    /** A adaga chegou de volta: tira o estado "fantasma" da pilha certa do inventário. */
    public static void daggerCaught(Player player, ThrownDagger dagger) {
        for (ItemStack stack : player.getInventory().items) {
            if (dagger.getUUID().equals(stack.get(ModDataComponents.THROWN_DAGGER.get()))) {
                stack.remove(ModDataComponents.THROWN_DAGGER.get());
                break;
            }
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RETURN,
                SoundSource.PLAYERS, 1.0F, 1.8F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(),
                SoundSource.PLAYERS, 0.6F, 1.6F);
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
        }
    }

    // ------------------------------------------------------------------ martelo / machado legado

    private static void startLeap(ServerPlayer player, ItemStack stack) {
        Vec3 dir = flat(player.getLookAngle(), player);
        boolean aerial = !grounded(player);
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(aerial ? velocity.x * 0.5 : dir.x * 0.55,
                aerial ? WeaponAbilities.HAMMER_DIVE : WeaponAbilities.HAMMER_JUMP,
                aerial ? velocity.z * 0.5 : dir.z * 0.55);
        player.hurtMarked = true;
        long now = player.level().getGameTime();
        LEAPS.put(player.getUUID(), new Leap(now, new boolean[]{aerial},
                new double[]{player.getY() + Math.min(player.fallDistance, WeaponAbilities.HAMMER_HEIGHT_CAP)}, stack, player.level().dimension()));
        NO_FALL_UNTIL.put(player.getUUID(), now + 100);
        ModNetwork.sendPlayerAnim(player, aerial ? PlayerAnim.HAMMER_DIVE : PlayerAnim.LEAP_FLIP,
                aerial ? WeaponAbilities.HAMMER_TIMEOUT : WeaponAbilities.HAMMER_DIVE_TICK, true);
        sound(player, SoundEvents.BREEZE_JUMP, 1.0F, 0.8F);
        sound(player, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 0.7F);
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 12, 0.4, 0.05, 0.4, 0.05);
    }

    private static void tickLeap(ServerPlayer player, Leap leap) {
        if (!validAction(player, leap.weapon(), leap.dimension())) {
            LEAPS.remove(player.getUUID());
            NO_FALL_UNTIL.remove(player.getUUID());
            return;
        }
        leap.peakY()[0] = Math.max(leap.peakY()[0], player.getY());
        NO_FALL_UNTIL.put(player.getUUID(), player.level().getGameTime() + 5);
        long t = player.level().getGameTime() - leap.start();
        if (t >= WeaponAbilities.HAMMER_DIVE_TICK && !leap.dived()[0] && !player.onGround()) {
            // Mergulho: a queda é forçada, então o impacto é sempre seco e rápido.
            leap.dived()[0] = true;
            Vec3 m = player.getDeltaMovement();
            player.setDeltaMovement(m.x * 0.5, WeaponAbilities.HAMMER_DIVE, m.z * 0.5);
            player.hurtMarked = true;
            sound(player, SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.0F, 0.6F);
            ModNetwork.sendPlayerAnim(player, PlayerAnim.HAMMER_DIVE, WeaponAbilities.HAMMER_TIMEOUT, true);
        }
        if (leap.dived()[0]) {
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.2, 0.4, 0.2, 0.0);
            Vec3 tail = player.position().add(0, 1.2, 0);
            SkillVfx.line(player.serverLevel(), tail, tail.add(0, 1.8, 0), ParticleTypes.CRIT, 6);
        }
        if ((leap.dived()[0] || t > 3) && grounded(player)) {
            LEAPS.remove(player.getUUID());
            if (leap.dived()[0]) slam(player, leap.peakY()[0] - player.getY());
        } else if (t > WeaponAbilities.HAMMER_TIMEOUT) {
            LEAPS.remove(player.getUUID());
        }
    }

    /** Impacto do Salto: crítico em área, estouro de terra e onda no chão. */
    private static void slam(ServerPlayer player, double fallenBlocks) {
        ServerLevel level = player.serverLevel();
        WeaponData data = ModularWeaponItem.getData(player.getMainHandItem());
        ElementType element = data.elementType();
        Vec3 center = player.position();
        double height = net.minecraft.util.Mth.clamp(fallenBlocks, 0, WeaponAbilities.HAMMER_HEIGHT_CAP);
        double radius = WeaponAbilities.HAMMER_RADIUS + height * WeaponAbilities.HAMMER_RADIUS_PER_BLOCK;
        float damage = attack(player) * (float) (WeaponAbilities.HAMMER_DAMAGE + height * WeaponAbilities.HAMMER_DAMAGE_PER_BLOCK);
        ModNetwork.sendWeaponMotion(player, WeaponMotionPayload.Kind.HAMMER_IMPACT, player.getId(), center, center, (float) (height / WeaponAbilities.HAMMER_HEIGHT_CAP));

        CombatEvents.withKind(HitFeedbackPayload.CRIT, () -> {
            for (LivingEntity target : targets(player, new AABB(center, center).inflate(radius, 2.0, radius))) {
                if (target.distanceToSqr(center) > radius * radius + 4) continue;
                if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                    Vec3 away = target.position().subtract(center).multiply(1, 0, 1).normalize().scale(0.5);
                    Hazards.push(target, away.add(0, 0.6, 0));
                    level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                            target.getZ(), 20, 0.3, 0.3, 0.3, 0.4);
                }
            }
        });

        BlockState ground = level.getBlockState(player.getOnPos());
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), center.x, center.y + 0.2, center.z,
                160, 1.8, 0.2, 1.8, 0.5);
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.3, center.z, 4, 1.2, 0.1, 1.2, 0);
        level.sendParticles(ParticleTypes.DUST_PLUME, center.x, center.y + 0.1, center.z, 40, 2.0, 0.1, 2.0, 0.05);
        // A onda ergue blocos naturais de verdade; o helper respeita mobGriefing e os recoloca ao pousar.
        Hazards.add(new Hazards.QuakeBlocks(level, player, center, radius, 0.55, WeaponAbilities.HAMMER_DEBRIS_BLOCKS + (int) height * 2));
        ModNetwork.sendShake(level, center, 22, 0.65F + (float) height * 0.025F, 18);
        // Onda que se expande pelo chão (só visual): 6 anéis, um por tick.
        Hazards.add(new Hazards.Hazard() {
            private int ring;

            @Override
            public boolean tick() {
                ring++;
                double r = ring * radius / 6;
                SkillVfx.ring(level, center.add(0, 0.15, 0), r, ParticleTypes.CLOUD, 10 + ring * 6);
                SkillVfx.ring(level, center.add(0, 0.3, 0), r, SkillVfx.particleFor(element), 6 + ring * 4);
                return ring < 6;
            }
        });

        sound(player, SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.6F, 0.8F);
        sound(player, SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.0F);
        sound(player, SoundEvents.PLAYER_ATTACK_CRIT, 1.4F, 0.7F);
        ModNetwork.sendPlayerAnim(player, PlayerAnim.SLAM, 10, true);
        player.swing(InteractionHand.MAIN_HAND, true);
        NO_FALL_UNTIL.put(player.getUUID(), level.getGameTime() + 10);
    }

    // ------------------------------------------------------------------ espada longa

    /** Compatibilidade com o pacote antigo: passa pelas mesmas validações do botão direito da meia-lua. */
    public static void spinAttack(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ModularWeaponItem item && item.weaponClass() == WeaponClass.LONGSWORD) {
            use(player, stack, item);
        }
    }

    private static void startCrescent(ServerPlayer player, ItemStack stack) {
        Vec3 direction = flat(player.getLookAngle(), player);
        WeaponTechnique technique = technique(player, WeaponClass.LONGSWORD);
        CRESCENTS.put(player.getUUID(), new Crescent(player.level().getGameTime(), direction,
                stack, player.level().dimension(), technique));
        player.getCooldowns().addCooldown(stack.getItem(), technique.cooldown());
        player.resetAttackStrengthTicker();
        ModNetwork.sendPlayerAnim(player, technique.animation(), technique.duration(), true);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new CrescentSlashPayload(player.getId(),
                (float) direction.x, (float) direction.z, ModularWeaponItem.getData(stack).elementType(), technique));
        sound(player, SoundEvents.PLAYER_ATTACK_STRONG, 0.7F, 0.65F);
        sound(player, SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 0.65F, 0.65F);
    }

    /** Um acerto por vítima por corte frontal (dois no Cruzado); linha de visão impede golpes através de paredes. */
    private static void tickCrescent(ServerPlayer player, Crescent crescent) {
        if (!validAction(player, crescent.weapon(), crescent.dimension())) {
            CRESCENTS.remove(player.getUUID());
            return;
        }
        int t = (int) (player.level().getGameTime() - crescent.start());
        if (t >= crescent.technique().duration()) {
            CRESCENTS.remove(player.getUUID());
            return;
        }
        if (t < WeaponAbilities.CRESCENT_WINDUP) {
            Vec3 blade = player.getEyePosition().add(crescent.dir().scale(0.8));
            SkillVfx.arc(player.serverLevel(), blade, crescent.dir(), 0.35 + t * 0.06, -0.8, 0.8,
                    ParticleTypes.ENCHANTED_HIT, 4);
            return;
        }
        ServerLevel level = player.serverLevel();
        ElementType element = ModularWeaponItem.getData(crescent.weapon()).elementType();
        double radius = crescent.technique().range();
        Vec3 center = player.position().add(0, 1, 0);
        if (t == WeaponAbilities.CRESCENT_WINDUP || (crescent.technique() == WeaponTechnique.CROSS && t == WeaponAbilities.CROSS_SECOND_HIT)) {
            player.swing(InteractionHand.MAIN_HAND, true);
            float damage = attack(player) * crescent.technique().damage();
            for (LivingEntity target : targets(player, player.getBoundingBox().inflate(radius, 0.8, radius))) {
                AABB bounds = target.getBoundingBox();
                double x = net.minecraft.util.Mth.clamp(player.getX(), bounds.minX, bounds.maxX) - player.getX();
                double z = net.minecraft.util.Mth.clamp(player.getZ(), bounds.minZ, bounds.maxZ) - player.getZ();
                Vec3 relative = bounds.getCenter().subtract(player.position()).multiply(1, 0, 1);
                if (x * x + z * z > radius * radius || crescent.dir().dot(relative) < 0 || !player.hasLineOfSight(target)) continue;
                if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                    target.knockback(WeaponAbilities.CRESCENT_PUSH, player.getX() - target.getX(), player.getZ() - target.getZ());
                    if (crescent.technique() == WeaponTechnique.RISING) Hazards.push(target, new Vec3(0, WeaponAbilities.RISING_LIFT, 0));
                    level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                            target.getZ(), 12, 0.2, 0.3, 0.2, 0.2);
                    level.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + target.getBbHeight() * 0.5,
                            target.getZ(), 1, 0, 0, 0, 0);
                }
            }
            SkillVfx.arc(level, center, crescent.dir(), radius * 0.85, -Math.PI / 2, Math.PI / 2,
                    ParticleTypes.END_ROD, 40);
            sound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.8F);
            sound(player, SoundEvents.TRIDENT_RIPTIDE_1.value(), 0.65F, 1.25F);
            ModNetwork.sendShake(level, player.position(), 10, 0.22F, 6);
            player.causeFoodExhaustion(0.1F);
        }
        double progress = (t - WeaponAbilities.CRESCENT_WINDUP)
                / (double) (crescent.technique().duration() - WeaponAbilities.CRESCENT_WINDUP - 1);
        double angle = -Math.PI / 2 + progress * Math.PI;
        SkillVfx.arc(level, center, crescent.dir(), radius * 0.7, angle - 0.18, angle + 0.18,
                SkillVfx.particleFor(element), 8);
    }

    // ------------------------------------------------------------------ técnicas adicionais (rapieira / martelo)

    private static void startTechnique(ServerPlayer player, ItemStack stack, WeaponTechnique technique) {
        Vec3 direction = flat(player.getLookAngle(), player);
        ACTIONS.put(player.getUUID(), new TechniqueAction(player.level().getGameTime(), direction, stack,
                player.level().dimension(), technique, new HashSet<>(), new Vec3[]{player.position()}, new double[]{0}));
        player.getCooldowns().addCooldown(stack.getItem(), technique.cooldown());
        player.resetAttackStrengthTicker();
        ModNetwork.sendPlayerAnim(player, technique.animation(), technique.duration(), true);
        WeaponMotionPayload.Kind kind = switch (technique) {
            case RAPIER_FLURRY -> WeaponMotionPayload.Kind.RAPIER_FLURRY;
            case RAPIER_RETREAT -> WeaponMotionPayload.Kind.RAPIER_RETREAT;
            case HAMMER_SWEEP -> WeaponMotionPayload.Kind.HAMMER_SWEEP;
            case HAMMER_QUAKE -> null; // Onda começa no acerto, depois de medir o caminho real.
            default -> throw new IllegalArgumentException("Técnica sem sequência: " + technique);
        };
        if (kind != null) ModNetwork.sendWeaponMotion(player, kind, player.getId(), player.position(),
                player.position().add(direction.scale(technique.range())), technique.duration());
        sound(player, technique.weapon() == WeaponClass.HAMMER ? SoundEvents.ARMOR_EQUIP_NETHERITE.value() : SoundEvents.TRIDENT_THROW.value(), 0.7F, 0.9F);
    }

    /** Acertos só no servidor, com intervalo vanilla entre estocadas, colisão no recuo e vítimas únicas por onda. */
    private static void tickTechnique(ServerPlayer player, TechniqueAction action) {
        int t = (int) (player.level().getGameTime() - action.start());
        if (!validAction(player, action.weapon(), action.dimension()) || t >= action.technique().duration()) {
            ACTIONS.remove(player.getUUID());
            return;
        }
        WeaponTechnique technique = action.technique();
        boolean heavy = technique.weapon() == WeaponClass.HAMMER;
        if (heavy && t <= WeaponAbilities.HAMMER_SWEEP_HIT && (!grounded(player) || player.isInWaterOrBubble()
                || player.isInLava() || player.isFallFlying() || player.getAbilities().flying)) {
            ACTIONS.remove(player.getUUID());
            return;
        }
        if (heavy || technique == WeaponTechnique.RAPIER_FLURRY) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.45, 1, 0.45));
            player.hurtMarked = true;
        }
        switch (technique) {
            case RAPIER_FLURRY -> {
                int elapsed = t - WeaponAbilities.FLURRY_FIRST_HIT;
                if (elapsed >= 0 && elapsed % WeaponAbilities.FLURRY_HIT_GAP == 0
                        && elapsed / WeaponAbilities.FLURRY_HIT_GAP < WeaponAbilities.FLURRY_HITS) {
                    strikeTechnique(player, action, false);
                    sound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 0.9F, 1.6F + elapsed * 0.01F);
                }
            }
            case RAPIER_RETREAT -> {
                if (t == WeaponAbilities.RETREAT_HIT) strikeTechnique(player, action, false);
                if (t > WeaponAbilities.RETREAT_HIT && t <= WeaponAbilities.RETREAT_HIT + WeaponAbilities.RETREAT_MOVE_TICKS) {
                    Vec3 step = action.dir().scale(-WeaponAbilities.RETREAT_SPEED);
                    Vec3 allowed = Entity.collideBoundingBox(player, step, player.getBoundingBox(), player.serverLevel(),
                            player.serverLevel().getEntityCollisions(player, player.getBoundingBox().expandTowards(step)));
                    player.setDeltaMovement(allowed.x, player.getDeltaMovement().y, allowed.z);
                    player.hurtMarked = true;
                } else if (t == WeaponAbilities.RETREAT_HIT + WeaponAbilities.RETREAT_MOVE_TICKS + 1) {
                    player.setDeltaMovement(player.getDeltaMovement().multiply(0.1, 1, 0.1));
                    player.hurtMarked = true;
                }
            }
            case HAMMER_SWEEP -> {
                if (t == WeaponAbilities.HAMMER_SWEEP_HIT) {
                    strikeTechnique(player, action, true);
                    ModNetwork.sendShake(player.serverLevel(), player.position(), 12, 0.3F, 8);
                    sound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.4F, 0.55F);
                }
            }
            case HAMMER_QUAKE -> {
                if (t == WeaponAbilities.HAMMER_QUAKE_HIT) {
                    Vec3 origin = player.position();
                    action.waveOrigin()[0] = origin;
                    action.waveLength()[0] = groundWaveLength(player, origin, action.dir());
                    ModNetwork.sendWeaponMotion(player, WeaponMotionPayload.Kind.HAMMER_QUAKE, player.getId(), origin,
                            origin.add(action.dir().scale(action.waveLength()[0])), (float) action.waveLength()[0]);
                    sound(player, SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.5F, 0.7F);
                    ModNetwork.sendShake(player.serverLevel(), origin, 18, 0.5F, 12);
                }
                if (t >= WeaponAbilities.HAMMER_QUAKE_HIT) tickGroundWave(player, action, t - WeaponAbilities.HAMMER_QUAKE_HIT);
            }
            default -> { }
        }
    }

    private static void strikeTechnique(ServerPlayer player, TechniqueAction action, boolean circular) {
        double range = action.technique().range();
        player.swing(InteractionHand.MAIN_HAND, true);
        for (LivingEntity target : targets(player, player.getBoundingBox().inflate(range, 1, range))) {
            Vec3 relative = target.getBoundingBox().getCenter().subtract(player.position()).multiply(1, 0, 1);
            AABB box = target.getBoundingBox();
            double dx = net.minecraft.util.Mth.clamp(player.getX(), box.minX, box.maxX) - player.getX();
            double dz = net.minecraft.util.Mth.clamp(player.getZ(), box.minZ, box.maxZ) - player.getZ();
            if (dx * dx + dz * dz > range * range || !player.hasLineOfSight(target)
                    || (!circular && action.dir().dot(relative.normalize()) < WeaponAbilities.FLURRY_CONE_DOT)) continue;
            if (target.hurt(player.damageSources().playerAttack(player), attack(player) * action.technique().damage())) {
                if (circular) target.knockback(WeaponAbilities.HAMMER_SWEEP_PUSH, player.getX() - target.getX(), player.getZ() - target.getZ());
                player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                        target.getZ(), 10, 0.15, 0.25, 0.15, 0.2);
                sound(player, SoundEvents.PLAYER_ATTACK_STRONG, 0.8F, circular ? 0.6F : 1.6F);
            }
        }
        player.causeFoodExhaustion(0.1F);
    }

    /** Mede só terreno apoiado e aproximadamente plano; para em paredes, líquidos, borda e chunks não carregados. */
    private static double groundWaveLength(ServerPlayer player, Vec3 origin, Vec3 dir) {
        ServerLevel level = player.serverLevel();
        double reach = WeaponAbilities.HAMMER_QUAKE_RANGE;
        var hit = level.clip(new net.minecraft.world.level.ClipContext(origin.add(0, 0.75, 0), origin.add(dir.scale(reach)).add(0, 0.75, 0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) reach = Math.max(0, hit.getLocation().distanceTo(origin.add(0, 0.75, 0)) - 0.35);
        double supported = 0;
        for (double distance = 0.5; distance <= reach; distance += 0.5) {
            Vec3 point = origin.add(dir.scale(distance));
            BlockPos floor = BlockPos.containing(point.x, origin.y - 0.25, point.z);
            if (!level.hasChunkAt(floor) || !level.getWorldBorder().isWithinBounds(floor)) break;
            BlockState state = level.getBlockState(floor);
            var shape = state.getCollisionShape(level, floor);
            if (shape.isEmpty() || !state.getFluidState().isEmpty()
                    || Math.abs(floor.getY() + shape.max(net.minecraft.core.Direction.Axis.Y) - origin.y) > 0.6) break;
            supported = distance;
        }
        return supported;
    }

    private static void tickGroundWave(ServerPlayer player, TechniqueAction action, int age) {
        double distance = (age + 1) * WeaponAbilities.HAMMER_QUAKE_STEP;
        if (distance > action.waveLength()[0] + WeaponAbilities.HAMMER_QUAKE_STEP || action.waveLength()[0] <= 0) return;
        Vec3 origin = action.waveOrigin()[0];
        Vec3 center = origin.add(action.dir().scale(Math.min(distance, action.waveLength()[0])));
        double width = WeaponAbilities.HAMMER_QUAKE_WIDTH;
        for (LivingEntity target : targets(player, new AABB(center, center).inflate(width, 2, width))) {
            Vec3 relative = target.getBoundingBox().getCenter().subtract(origin).multiply(1, 0, 1);
            double projection = relative.dot(action.dir());
            if (projection < 0 || projection > action.waveLength()[0] + 0.5 || !player.hasLineOfSight(target) || !action.hit().add(target.getId())) continue;
            if (target.hurt(player.damageSources().playerAttack(player), attack(player) * WeaponAbilities.HAMMER_QUAKE_DAMAGE))
                Hazards.push(target, action.dir().scale(0.4).add(0, 0.45, 0));
        }
        BlockPos floor = BlockPos.containing(center.x, center.y - 0.25, center.z);
        player.serverLevel().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, player.serverLevel().getBlockState(floor)),
                center.x, center.y + 0.15, center.z, 12, width * 0.5, 0.15, width * 0.5, 0.12);
    }

    // ------------------------------------------------------------------ eventos

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Lunge lunge = LUNGES.get(player.getUUID());
        if (lunge != null) tickLunge(player, lunge);
        Leap leap = LEAPS.get(player.getUUID());
        if (leap != null) tickLeap(player, leap);
        Crescent crescent = CRESCENTS.get(player.getUUID());
        if (crescent != null) tickCrescent(player, crescent);
        TechniqueAction action = ACTIONS.get(player.getUUID());
        if (action != null) tickTechnique(player, action);
        Long protectedUntil = NO_FALL_UNTIL.get(player.getUUID());
        if (protectedUntil != null && player.level().getGameTime() > protectedUntil) NO_FALL_UNTIL.remove(player.getUUID());
    }

    /** Sem dano de queda durante o Salto (nem logo depois do impacto). */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Long until = NO_FALL_UNTIL.get(player.getUUID());
        if (until != null && player.level().getGameTime() <= until) {
            event.setDistance(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearActions(event.getEntity().getUUID());
        TECHNIQUES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (WeaponClass weapon : WeaponClass.values()) if (WeaponTechnique.supports(weapon))
                PacketDistributor.sendToPlayer(player, new TechniqueSelectionPayload(technique(player, weapon)));
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        clearActions(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clearActions(player.getUUID());
    }

    @SubscribeEvent public static void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        LUNGES.clear(); LEAPS.clear(); CRESCENTS.clear(); ACTIONS.clear(); NO_FALL_UNTIL.clear(); TECHNIQUES.clear();
    }

    private static void clearActions(UUID id) {
        LUNGES.remove(id);
        LEAPS.remove(id);
        CRESCENTS.remove(id);
        ACTIONS.remove(id);
        NO_FALL_UNTIL.remove(id);
    }

    // ------------------------------------------------------------------ helpers

    private static boolean validAction(ServerPlayer player, ItemStack stack, ResourceKey<Level> dimension) {
        return player.isAlive() && !player.isSpectator() && !player.isPassenger() && !player.isSleeping()
                && player.getMainHandItem() == stack && player.level().dimension().equals(dimension);
    }

    private static boolean grounded(ServerPlayer player) {
        return player.onGround() && !player.serverLevel().noBlockCollision(player, player.getBoundingBox().move(0, -0.08, 0));
    }

    private static float attack(Player player) {
        return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static List<LivingEntity> targets(Player player, AABB area) {
        return player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !(e instanceof Player) && !(e instanceof ArmorStand));
    }

    private static Vec3 flat(Vec3 look, Player player) {
        Vec3 d = new Vec3(look.x, 0, look.z);
        if (d.lengthSqr() < 1.0E-4) d = Vec3.directionFromRotation(0, player.getYRot());
        return d.normalize();
    }

    private static void sound(Player player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}
