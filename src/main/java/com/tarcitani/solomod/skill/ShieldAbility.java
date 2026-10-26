package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.CombatEvents;
import com.tarcitani.solomod.event.StatsLogic;
import com.tarcitani.solomod.event.StatsLogic.ShieldSkills;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.network.HitFeedbackPayload;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.network.WeaponMotionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Z solicita a única habilidade permitida com arma + escudo, em qualquer ordem de mãos.
 * Aceita escudos com SHIELD_BLOCK (vanilla e do mod), as quatro classes atuais e seus visuais.
 * Servidor valida equipamento, Maestria, mana, recarga, chão e exclusão com outras ações.
 * Técnicas ficam disponíveis pela combinação, sem comprar outro nó. Guarda é baixada ao atacar;
 * a pose de apoio não concede bloqueio/invulnerabilidade. Trocar qualquer pilha cancela a ação.
 * Dano físico usa playerAttack, escala uma vez por CombatEvents e respeita paredes e hitboxes grandes.
 * A recarga é comum aos visuais da classe; estado transitório é limpo em morte/logout/dimensão/stop.
 * O pacote WeaponMotionPayload sincroniza selos, rastros e impactos animados, sem delegar dano ao cliente.
 */
public final class ShieldAbility {
    private ShieldAbility() {}
    public record Equipment(InteractionHand weaponHand, ItemStack weapon, ItemStack shield) {
        public WeaponClass weaponClass() { return ((ModularWeaponItem) weapon.getItem()).weaponClass(); }
    }
    private record Action(Equipment gear, long start, Vec3 direction, ResourceKey<Level> dimension) {}
    private static final Map<UUID, Action> ACTIVE = new HashMap<>();
    private static UUID applyingHit;

    public static boolean isShield(ItemStack stack) { return stack.canPerformAction(ItemAbilities.SHIELD_BLOCK); }
    public static boolean hasShield(Player player) { return isShield(player.getMainHandItem()) || isShield(player.getOffhandItem()); }
    public static Equipment equipment(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack weapon = player.getItemInHand(hand);
            ItemStack shield = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (weapon.getItem() instanceof ModularWeaponItem item && WeaponClass.AVAILABLE.contains(item.weaponClass()) && isShield(shield))
                return new Equipment(hand, weapon, shield);
        }
        return null;
    }
    public static boolean isActive(ServerPlayer player) { return ACTIVE.containsKey(player.getUUID()); }
    /** Só expõe a arma secundária durante a chamada síncrona de dano desta habilidade. */
    public static ItemStack strikingWeapon(ServerPlayer player) {
        Action action = ACTIVE.get(player.getUUID());
        return player.getUUID().equals(applyingHit) && action != null ? action.gear().weapon() : ItemStack.EMPTY;
    }
    public static Component name(WeaponClass weapon) { return Component.translatable("shield_skill.solomod." + weapon.getSerializedName()); }

    public static void request(ServerPlayer player) {
        Equipment gear = equipment(player);
        if (gear == null || !ClassAbility.canStart(player) || !grounded(player)
                || player.isInWaterOrBubble() || player.isInLava() || player.isFallFlying() || player.getAbilities().flying
                || player.isUsingItem() && player.getUseItem() != gear.shield() || ClassAbility.isThrown(gear.weapon())) return;
        var item = (ModularWeaponItem) gear.weapon().getItem();
        var data = ModularWeaponItem.getData(gear.weapon());
        if (data.weaponClass() != item.weaponClass()) return;
        if (!StatsLogic.meetsRequirement(player, data)) {
            player.displayClientMessage(Component.translatable("message.solomod.skill_locked", data.requiredStatValue(),
                    data.requiredStatType().shortName().getString()).withStyle(ChatFormatting.RED), true);
            return;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            player.displayClientMessage(Component.translatable("message.solomod.shield_skill_cooldown"), true);
            return;
        }
        ShieldSkills.Spec spec = ShieldSkills.forWeapon(item.weaponClass());
        if (!StatsLogic.stats(player).tryConsumeMana(spec.mana())) {
            player.displayClientMessage(Component.translatable("message.solomod.no_mana", StatsLogic.stats(player).getManaAtual(), spec.mana())
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        player.stopUsingItem();
        player.setSprinting(false);
        ACTIVE.put(player.getUUID(), new Action(gear, player.level().getGameTime(),
                Vec3.directionFromRotation(0, player.getYRot()), player.level().dimension()));
        item.applyCooldown(player, spec.cooldown());
        StatsLogic.onStatsChanged(player);
        ModNetwork.sendPlayerAnim(player, PlayerAnim.SHIELD_STRIKE, spec.duration(), true);
        var kind = switch (item.weaponClass()) {
            case LONGSWORD -> WeaponMotionPayload.Kind.SHIELD_SWORD;
            case RAPIER -> WeaponMotionPayload.Kind.SHIELD_RAPIER;
            case DAGGER -> WeaponMotionPayload.Kind.SHIELD_DAGGER;
            case HAMMER -> WeaponMotionPayload.Kind.SHIELD_HAMMER;
            default -> throw new IllegalStateException("Classe sem técnica de escudo");
        };
        ModNetwork.sendWeaponMotion(player, kind, player.getId(), player.position(),
                player.position().add(ACTIVE.get(player.getUUID()).direction().scale(spec.range())), spec.duration());
        player.displayClientMessage(Component.translatable("message.solomod.shield_skill_used", name(item.weaponClass())), true);
    }

    private static boolean grounded(ServerPlayer player) {
        return player.onGround() && !player.serverLevel().noBlockCollision(player, player.getBoundingBox().move(0, -0.08, 0));
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Action action = ACTIVE.get(player.getUUID());
        if (action == null) return;
        Equipment gear = equipment(player);
        int age = (int) (player.level().getGameTime() - action.start());
        ShieldSkills.Spec spec = ShieldSkills.forWeapon(action.gear().weaponClass());
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping()
                || gear == null || gear.weaponHand() != action.gear().weaponHand()
                || gear.weapon() != action.gear().weapon() || gear.shield() != action.gear().shield()
                || !action.dimension().equals(player.level().dimension()) || !grounded(player)
                || player.isInWaterOrBubble() || player.isInLava() || player.isFallFlying() || player.getAbilities().flying
                || age >= spec.duration()) {
            ACTIVE.remove(player.getUUID());
            if (action.gear().weaponClass() == WeaponClass.DAGGER && age >= spec.hitTick()
                    && age <= spec.hitTick() + ShieldSkills.RETREAT_TICKS) stopRetreat(player);
            return;
        }
        player.setSprinting(false);
        player.stopUsingItem();
        if (age == spec.hitTick()) strike(player, action, spec);
        if (gear.weaponClass() == WeaponClass.DAGGER && age >= spec.hitTick() && age < spec.hitTick() + ShieldSkills.RETREAT_TICKS) {
            Vec3 step = action.direction().scale(-ShieldSkills.RETREAT_SPEED);
            // Recuo curto com colisão; interrompe antes de bordas, líquidos e chunks não carregados.
            var floor = net.minecraft.core.BlockPos.containing(player.position().add(step).add(0, -0.15, 0));
            if (player.serverLevel().hasChunkAt(floor) && player.serverLevel().getFluidState(floor).isEmpty()
                    && !player.serverLevel().noBlockCollision(player, player.getBoundingBox().move(step).move(0, -0.1, 0))) {
                Vec3 allowed = Entity.collideBoundingBox(player, step, player.getBoundingBox(), player.level(),
                        player.level().getEntityCollisions(player, player.getBoundingBox().expandTowards(step)));
                player.setDeltaMovement(allowed.x, player.getDeltaMovement().y, allowed.z);
                player.hurtMarked = true;
            } else {
                stopRetreat(player);
            }
        }
        if (gear.weaponClass() == WeaponClass.DAGGER && age == spec.hitTick() + ShieldSkills.RETREAT_TICKS) stopRetreat(player);
    }

    private static void stopRetreat(ServerPlayer player) {
        player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
        player.hurtMarked = true;
    }

    private static Vec3 closestPoint(LivingEntity target, Vec3 origin) {
        var box = target.getBoundingBox();
        return new Vec3(Mth.clamp(origin.x, box.minX, box.maxX), Mth.clamp(origin.y, box.minY, box.maxY),
                Mth.clamp(origin.z, box.minZ, box.maxZ));
    }

    private static void strike(ServerPlayer player, Action action, ShieldSkills.Spec spec) {
        Equipment gear = action.gear();
        Vec3 eye = player.getEyePosition();
        var targets = player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(spec.range(), 1.0, spec.range()),
                e -> e != player && e.isAlive() && !(e instanceof Player) && !(e instanceof ArmorStand));
        targets.sort(Comparator.comparingDouble(e -> closestPoint(e, eye).distanceToSqr(eye)));
        // Atributos da arma só entram na mão principal; compensa a base da classe na secundária.
        float damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                + (gear.weaponHand() == InteractionHand.OFF_HAND ? gear.weaponClass().baseDamage() : 0)) * spec.damage();
        player.swing(gear.weaponHand(), true);
        for (LivingEntity target : targets) {
            Vec3 nearest = closestPoint(target, eye);
            Vec3 relative = nearest.subtract(eye);
            Vec3 flat = relative.multiply(1, 0, 1);
            if (relative.lengthSqr() > spec.range() * spec.range()
                    || flat.lengthSqr() > 0.001 && flat.normalize().dot(action.direction()) < spec.coneDot()
                    || player.serverLevel().clip(new ClipContext(eye, nearest, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) continue;
            boolean[] hit = {false};
            applyingHit = player.getUUID();
            try {
                CombatEvents.withKind(HitFeedbackPayload.SKILL,
                        () -> hit[0] = target.hurt(player.damageSources().playerAttack(player), damage));
            } finally {
                applyingHit = null;
            }
            if (hit[0]) {
                target.knockback(spec.push(), -action.direction().x, -action.direction().z);
                player.serverLevel().sendParticles(ParticleTypes.CRIT, nearest.x, nearest.y, nearest.z, 5, 0.12, 0.12, 0.12, 0.03);
            }
            if (gear.weaponClass() == WeaponClass.RAPIER || gear.weaponClass() == WeaponClass.DAGGER) break;
        }
        Vec3 start = eye.add(0, -0.4, 0);
        if (gear.weaponClass() == WeaponClass.LONGSWORD) {
            SkillVfx.arc(player.serverLevel(), start, action.direction(), 2, -1, 1, ParticleTypes.CRIT, 12);
        } else if (gear.weaponClass() == WeaponClass.HAMMER) {
            Vec3 impact = player.position().add(action.direction().scale(1.5)).add(0, 0.15, 0);
            SkillVfx.arc(player.serverLevel(), impact, action.direction(), 1.2, -1, 1, ParticleTypes.CLOUD, 10);
            ModNetwork.sendShake(player, 0.18F, 5);
        } else {
            SkillVfx.line(player.serverLevel(), start.add(action.direction().scale(0.6)),
                    start.add(action.direction().scale(spec.range())), ParticleTypes.CRIT, 10);
        }
        player.level().playSound(null, player.blockPosition(), gear.weaponClass() == WeaponClass.HAMMER
                ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { ACTIVE.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { ACTIVE.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void death(LivingDeathEvent event) { ACTIVE.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); applyingHit = null; }
}
