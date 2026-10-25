package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.event.StatsLogic.Dodge;
import com.tarcitani.solomod.network.RollAnimationPayload;
import com.tarcitani.solomod.network.RollCooldownPayload;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rolamento pelo duplo Ctrl: pedido direcional do cliente, movimento e recarga controlados pelo servidor.
 * Só começa com apoio físico, sem veículo, voo, natação, uso de item ou outra habilidade em andamento.
 * Move por 8 ticks e recupera até 12; entre os ticks 2 e 7 esquiva de dano de combate. Queda, ambiente,
 * void e fontes que ignoram invulnerabilidade continuam valendo. Não muda a hitbox nem destrói blocos.
 * A direção real é enviada aos observadores; a recarga vai ao dono para a barra sobre o XP vanilla.
 * Valores em {@link Dodge}.
 */
public final class DodgeRoll {
    private DodgeRoll() {}

    private record Rolling(long start, Vec3 direction, ResourceKey<Level> dimension) {}
    private static final Map<UUID, Rolling> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> READY_AT = new HashMap<>();

    public static boolean isRolling(ServerPlayer player) { return ACTIVE.containsKey(player.getUUID()); }

    public static void request(ServerPlayer player, byte forward, byte strafe) {
        if (Math.abs((int) forward) > 1 || Math.abs((int) strafe) > 1 || !ClassAbility.canStart(player)
                || player.isUsingItem() || player.isInWaterOrBubble() || player.isInLava() || player.isFallFlying()
                || player.getAbilities().flying || !player.onGround()
                || player.serverLevel().noBlockCollision(player, player.getBoundingBox().move(0, -0.08, 0))) return;
        long now = player.level().getGameTime();
        if (now < READY_AT.getOrDefault(player.getUUID(), 0L)) {
            PacketDistributor.sendToPlayer(player, new RollCooldownPayload((int) (READY_AT.get(player.getUUID()) - now)));
            player.displayClientMessage(Component.translatable("message.solomod.roll_cooldown"), true);
            return;
        }
        Vec3 front = Vec3.directionFromRotation(0, player.getYRot());
        Vec3 left = new Vec3(front.z, 0, -front.x);
        Vec3 direction = front.scale(forward).add(left.scale(strafe));
        if (direction.lengthSqr() < 0.01) direction = front;
        direction = direction.normalize();
        ACTIVE.put(player.getUUID(), new Rolling(now, direction, player.level().dimension()));
        READY_AT.put(player.getUUID(), now + Dodge.COOLDOWN_TICKS);
        PacketDistributor.sendToPlayer(player, new RollCooldownPayload(Dodge.COOLDOWN_TICKS));
        player.setSprinting(false);
        player.causeFoodExhaustion(Dodge.EXHAUSTION);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new RollAnimationPayload(player.getId(), (float) direction.x, (float) direction.z));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7F, 0.65F);
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();
        Rolling roll = ACTIVE.get(id);
        if (roll == null) {
            if (player.level().getGameTime() >= READY_AT.getOrDefault(id, Long.MAX_VALUE)) READY_AT.remove(id);
            return;
        }
        int age = (int) (player.level().getGameTime() - roll.start());
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping()
                || player.isInWaterOrBubble() || player.isInLava() || player.isFallFlying() || player.getAbilities().flying
                || !roll.dimension().equals(player.level().dimension()) || age >= Dodge.TOTAL_TICKS) {
            stop(player);
            return;
        }
        player.setSprinting(false);
        player.stopUsingItem();
        Vec3 step = age < Dodge.MOVE_TICKS
                ? roll.direction().scale(Dodge.SPEED * Math.sin(Math.PI * (age + 0.5) / Dodge.MOVE_TICKS)) : Vec3.ZERO;
        Vec3 allowed = Entity.collideBoundingBox(player, step, player.getBoundingBox(), player.level(),
                player.level().getEntityCollisions(player, player.getBoundingBox().expandTowards(step)));
        player.setDeltaMovement(allowed.x, player.getDeltaMovement().y, allowed.z);
        player.hurtMarked = true;
        if (age < Dodge.MOVE_TICKS && age % 2 == 0) {
            var ground = player.level().getBlockState(player.getOnPos());
            if (!ground.isAir()) player.serverLevel().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    player.getX(), player.getY() + 0.12, player.getZ(), 6, 0.25, 0.08, 0.25, 0.03);
        }
    }

    /** Janela curta de esquiva; nunca cancela /kill, void, queda ou queimaduras ambientais. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Rolling roll = ACTIVE.get(player.getUUID());
        if (roll == null || !roll.dimension().equals(player.level().dimension())) return;
        long age = player.level().getGameTime() - roll.start();
        var source = event.getSource();
        if (age < Dodge.INVULNERABLE_FROM || age >= Dodge.INVULNERABLE_UNTIL
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(DamageTypeTags.IS_FALL)) return;
        if (source.getEntity() instanceof LivingEntity || source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.IS_EXPLOSION)) event.setCanceled(true);
    }

    private static void stop(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
        player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
        player.hurtMarked = true;
    }

    private static void clear(UUID id) { ACTIVE.remove(id); READY_AT.remove(id); }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { clear(event.getEntity().getUUID()); }
    @SubscribeEvent public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        clear(event.getEntity().getUUID());
        if (event.getEntity() instanceof ServerPlayer player) PacketDistributor.sendToPlayer(player, new RollCooldownPayload(0));
    }
    @SubscribeEvent public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player.getUUID());
            PacketDistributor.sendToPlayer(player, new RollCooldownPayload(0));
        }
    }
    @SubscribeEvent public static void onServerStopped(ServerStoppedEvent event) { ACTIVE.clear(); READY_AT.clear(); }
}
