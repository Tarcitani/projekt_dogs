package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.component.ModDataComponents;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.entity.ThrownDagger;
import com.tarcitani.solomod.event.CombatEvents;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.mob.Hazards;
import com.tarcitani.solomod.network.HitFeedbackPayload;
import com.tarcitani.solomod.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Habilidade própria de cada FORMATO de arma, disponível em qualquer tier e sem custo de mana
 * (a habilidade de tier — Investida, Impacto, Explosão... — passa para agachar + clique direito):
 *
 * <ul>
 *   <li>Rapieira — Estocada Relâmpago (clique direito): dispara para a frente perfurando tudo no caminho.</li>
 *   <li>Adaga — Arremesso (clique direito): lança a adaga, que crava no chão ou no alvo. Clique direito de novo
 *       chama de volta: ela arranca do chão e volta girando, ferindo quem estiver no caminho.</li>
 *   <li>Machado — Salto Esmagador (clique direito): salta, mergulha e explode o chão num CRÍTICO em área.</li>
 *   <li>Espada longa — Giro da Claymore (ataque): todo golpe vira um giro de 360° que acerta todos em volta.</li>
 * </ul>
 */
public final class ClassAbility {
    private ClassAbility() {}

    private static final int LUNGE_TICKS = 6;
    private static final int LUNGE_COOLDOWN = 30;
    private static final int LEAP_COOLDOWN = 70;
    private static final int THROW_COOLDOWN = 8;

    private record Lunge(Vec3 dir, Set<Integer> hit, int[] left) {}

    private record Leap(long start, boolean[] dived) {}

    private static final Map<UUID, Lunge> LUNGES = new HashMap<>();
    private static final Map<UUID, Leap> LEAPS = new HashMap<>();
    private static final Map<UUID, Long> NO_FALL_UNTIL = new HashMap<>();

    /** Formatos cuja habilidade é no clique direito. */
    public static boolean hasRightClick(WeaponClass wc) {
        return wc == WeaponClass.RAPIER || wc == WeaponClass.DAGGER || wc == WeaponClass.AXE;
    }

    public static boolean hasAbility(WeaponClass wc) {
        return hasRightClick(wc) || wc == WeaponClass.LONGSWORD;
    }

    public static Component displayName(WeaponClass wc) {
        return Component.translatable("ability.solomod." + wc.getSerializedName());
    }

    public static boolean isThrown(ItemStack stack) {
        return stack.has(ModDataComponents.THROWN_DAGGER.get());
    }

    // ------------------------------------------------------------------ clique direito

    /** Executa a habilidade do formato. Devolve false se não pôde (recarga, no ar...). */
    public static boolean use(ServerPlayer player, ItemStack stack, ModularWeaponItem item) {
        WeaponData data = ModularWeaponItem.getData(stack);
        if (data.weaponClass() == WeaponClass.DAGGER && isThrown(stack)) {
            recallDagger(player, stack);
            return true;
        }
        if (player.getCooldowns().isOnCooldown(item)) return false;
        switch (data.weaponClass()) {
            case RAPIER -> {
                startLunge(player, data);
                player.getCooldowns().addCooldown(item, LUNGE_COOLDOWN);
            }
            case DAGGER -> {
                throwDagger(player, stack);
                player.getCooldowns().addCooldown(item, THROW_COOLDOWN);
            }
            case AXE -> {
                if (!player.onGround()) {
                    player.displayClientMessage(Component.translatable("message.solomod.need_ground").withStyle(ChatFormatting.RED), true);
                    return false;
                }
                startLeap(player, data);
                player.getCooldowns().addCooldown(item, LEAP_COOLDOWN);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ rapieira

    private static void startLunge(ServerPlayer player, WeaponData data) {
        Vec3 dir = flat(player.getLookAngle(), player);
        LUNGES.put(player.getUUID(), new Lunge(dir, new HashSet<>(), new int[]{LUNGE_TICKS}));
        ModNetwork.sendPlayerAnim(player, PlayerAnim.THRUST, LUNGE_TICKS + 4, true);
        sound(player, SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.0F, 1.7F);
        sound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.9F);
        player.serverLevel().sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
    }

    private static void tickLunge(ServerPlayer player, Lunge lunge) {
        ServerLevel level = player.serverLevel();
        WeaponData data = ModularWeaponItem.getData(player.getMainHandItem());
        Vec3 before = player.position().add(0, 1.0, 0);
        player.setDeltaMovement(lunge.dir().x * 1.6, Math.max(player.getDeltaMovement().y, 0.02), lunge.dir().z * 1.6);
        player.hurtMarked = true;
        // Rastro: linha de luz na altura do peito + fagulhas do elemento.
        Vec3 ahead = before.add(lunge.dir().scale(1.6));
        SkillVfx.line(level, before, ahead, ParticleTypes.END_ROD, 6);
        SkillVfx.line(level, before.add(0, -0.4, 0), ahead.add(0, -0.4, 0), SkillVfx.particleFor(data.elementType()), 6);

        float damage = attack(player) * 1.3F;
        for (LivingEntity target : targets(player, player.getBoundingBox().expandTowards(lunge.dir().scale(1.6)).inflate(1.0))) {
            if (lunge.hit().add(target.getId()) && target.hurt(player.damageSources().playerAttack(player), damage)) {
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        12, 0.3, 0.3, 0.3, 0.3);
                sound(player, SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.5F);
            }
        }
        if (--lunge.left()[0] <= 0) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.25, 1, 0.25));
            player.hurtMarked = true;
            LUNGES.remove(player.getUUID());
        }
    }

    // ------------------------------------------------------------------ adaga

    private static void throwDagger(ServerPlayer player, ItemStack stack) {
        ThrownDagger dagger = new ThrownDagger(player.level(), player, stack);
        dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.4F, 0.4F);
        player.level().addFreshEntity(dagger);
        stack.set(ModDataComponents.THROWN_DAGGER.get(), dagger.getUUID());
        player.swing(InteractionHand.MAIN_HAND, true);
        sound(player, SoundEvents.TRIDENT_THROW.value(), 1.0F, 1.6F);
    }

    private static void recallDagger(ServerPlayer player, ItemStack stack) {
        UUID id = stack.get(ModDataComponents.THROWN_DAGGER.get());
        Entity entity = id == null ? null : player.serverLevel().getEntity(id);
        if (entity instanceof ThrownDagger dagger && dagger.isAlive()) {
            if (dagger.isReturning()) return;
            dagger.recall();
            player.swing(InteractionHand.MAIN_HAND, true);
        } else {
            // A adaga ficou num chunk descarregado / outra dimensão: rematerializa na mão.
            stack.remove(ModDataComponents.THROWN_DAGGER.get());
            sound(player, SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.4F);
        }
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

    // ------------------------------------------------------------------ machado

    private static void startLeap(ServerPlayer player, WeaponData data) {
        Vec3 dir = flat(player.getLookAngle(), player);
        player.setDeltaMovement(dir.x * 0.55, 1.05, dir.z * 0.55);
        player.hurtMarked = true;
        long now = player.level().getGameTime();
        LEAPS.put(player.getUUID(), new Leap(now, new boolean[1]));
        NO_FALL_UNTIL.put(player.getUUID(), now + 100);
        ModNetwork.sendPlayerAnim(player, PlayerAnim.LEAP_FLIP, 12, true);
        sound(player, SoundEvents.BREEZE_JUMP, 1.0F, 0.8F);
        sound(player, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 0.7F);
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 12, 0.4, 0.05, 0.4, 0.05);
    }

    private static void tickLeap(ServerPlayer player, Leap leap) {
        long t = player.level().getGameTime() - leap.start();
        if (t >= 9 && !leap.dived()[0] && !player.onGround()) {
            // Mergulho: a queda é forçada, então o impacto é sempre seco e rápido.
            leap.dived()[0] = true;
            Vec3 m = player.getDeltaMovement();
            player.setDeltaMovement(m.x * 0.5, -2.2, m.z * 0.5);
            player.hurtMarked = true;
            sound(player, SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.0F, 0.6F);
        }
        if (leap.dived()[0]) {
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.2, 0.4, 0.2, 0.0);
        }
        if (t > 3 && player.onGround()) {
            LEAPS.remove(player.getUUID());
            slam(player);
        } else if (t > 80) {
            LEAPS.remove(player.getUUID());
        }
    }

    /** Impacto do Salto: crítico em área, estouro de terra e onda no chão. */
    private static void slam(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WeaponData data = ModularWeaponItem.getData(player.getMainHandItem());
        ElementType element = data.elementType();
        Vec3 center = player.position();
        double radius = 4.5;
        float damage = attack(player) * 2.0F;

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
        NO_FALL_UNTIL.put(player.getUUID(), level.getGameTime() + 10);
    }

    // ------------------------------------------------------------------ espada longa

    /** Ataque da espada longa: giro de 360° (vem do cliente, que cancela o golpe vanilla). */
    public static void spinAttack(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ModularWeaponItem) || ModularWeaponItem.getData(stack).weaponClass() != WeaponClass.LONGSWORD) {
            return;
        }
        float strength = player.getAttackStrengthScale(0.5F);
        if (strength < 0.7F || player.isSpectator()) return;
        player.resetAttackStrengthTicker();
        ServerLevel level = player.serverLevel();
        ElementType element = ModularWeaponItem.getData(stack).elementType();
        float damage = attack(player) * (0.2F + strength * strength * 0.8F);
        double radius = 3.4;

        for (LivingEntity target : targets(player, player.getBoundingBox().inflate(radius, 0.8, radius))) {
            if (target.distanceToSqr(player) > radius * radius) continue;
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                target.knockback(0.5, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
        }
        Vec3 c = player.position().add(0, 1.0, 0);
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x + Math.cos(a) * 2.0, c.y, c.z + Math.sin(a) * 2.0, 1, 0, 0, 0, 0);
        }
        SkillVfx.ring(level, c.add(0, -0.3, 0), 2.6, SkillVfx.particleFor(element), 24);
        sound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.7F);
        sound(player, SoundEvents.PLAYER_ATTACK_SWEEP, 0.8F, 1.1F);
        player.causeFoodExhaustion(0.1F);
        ModNetwork.sendPlayerAnim(player, PlayerAnim.SPIN, 8, false);
    }

    // ------------------------------------------------------------------ eventos

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Lunge lunge = LUNGES.get(player.getUUID());
        if (lunge != null) tickLunge(player, lunge);
        Leap leap = LEAPS.get(player.getUUID());
        if (leap != null) tickLeap(player, leap);
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
        UUID id = event.getEntity().getUUID();
        LUNGES.remove(id);
        LEAPS.remove(id);
        NO_FALL_UNTIL.remove(id);
    }

    // ------------------------------------------------------------------ helpers

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
