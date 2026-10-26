package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.sanctuary.SanctuaryBoss;
import com.tarcitani.solomod.event.StatsLogic.Parry;
import com.tarcitani.solomod.item.ParryShieldItem;
import com.tarcitani.solomod.mob.TierBrain;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aparada autoritativa de dano frontal de BOSS/SUB_BOSS (também chefes dos Santuários).
 * Usa a mão/item/tempo reais de uso do servidor; aceita magia atribuída ao chefe, mas nunca
 * ambiente, queda, espinhos ou fontes que ignoram invulnerabilidade. Cancela antes da armadura
 * e das passivas de Hazards.hit. Uma graça curtíssima cobre multihits e empurrões do mesmo impacto.
 * A recuperação do chefe é agendada para o próximo tick, sem interromper Move.tick por reentrância.
 * Fúria/transições continuam imunes ao stun de parry; o golpe ainda é defendido.
 */
public final class ParryGuard {
    private ParryGuard() {}
    private record Deflected(UUID boss, long until) {}
    private static final Map<UUID, Deflected> DEFLECTED = new HashMap<>();

    private static boolean isBoss(Entity entity) {
        if (entity instanceof SanctuaryBoss) return true;
        if (!(entity instanceof LivingEntity living) || !living.hasData(ModAttachments.MOB_PROFILE)) return false;
        MobTier tier = living.getData(ModAttachments.MOB_PROFILE).tier();
        return tier == MobTier.BOSS || tier == MobTier.SUB_BOSS;
    }

    /** Protege só o empurrão imediatamente posterior a uma aparada bem-sucedida. */
    public static boolean deflectedImpact(Entity entity) {
        Deflected state = DEFLECTED.get(entity.getUUID());
        return state != null && entity.level().getGameTime() < state.until();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        var source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.THORNS) || !(source.getEntity() instanceof LivingEntity boss)
                || !isBoss(boss)) return;
        Deflected recent = DEFLECTED.get(player.getUUID());
        if (deflectedImpact(player) && recent.boss().equals(boss.getUUID())) {
            event.setCanceled(true);
            return;
        }
        ItemStack shield = player.getUseItem();
        if (!player.isUsingItem() || !(shield.getItem() instanceof ParryShieldItem)
                || player.getItemInHand(player.getUsedItemHand()) != shield
                || !ClassAbility.canStart(player)
                || player.getTicksUsingItem() >= Parry.WINDOW_TICKS) return;
        Vec3 origin = source.getSourcePosition();
        if (origin == null) origin = boss.position();
        Vec3 toward = origin.subtract(player.position()).multiply(1, 0, 1);
        if (toward.lengthSqr() < 0.0001) toward = boss.position().subtract(player.position()).multiply(1, 0, 1);
        Vec3 front = Vec3.directionFromRotation(0, player.getYHeadRot());
        if (toward.lengthSqr() < 0.0001 || toward.normalize().dot(front) <= Parry.FRONT_DOT) return;

        event.setCanceled(true);
        DEFLECTED.put(player.getUUID(), new Deflected(boss.getUUID(),
                player.level().getGameTime() + Parry.MULTIHIT_GRACE_TICKS));
        var hand = player.getUsedItemHand();
        player.stopUsingItem();
        player.getCooldowns().addCooldown(shield.getItem(), Parry.COOLDOWN_TICKS);
        shield.hurtAndBreak(Parry.DURABILITY_COST, player, LivingEntity.getSlotForHand(hand));
        TierBrain brain = TierBrain.of(boss);
        if (brain != null) brain.onParried(Parry.STUN_TICKS);
        else if (boss instanceof SanctuaryBoss sanctuary) sanctuary.onParried(Parry.STUN_TICKS);
        Vec3 spark = player.getEyePosition().add(front.scale(0.8));
        player.serverLevel().sendParticles(ParticleTypes.CRIT, spark.x, spark.y, spark.z, 18, 0.25, 0.25, 0.25, 0.12);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.65F, 1.8F);
        player.displayClientMessage(Component.translatable("message.solomod.parry"), true);
    }

    @SubscribeEvent public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && !deflectedImpact(player)) DEFLECTED.remove(player.getUUID());
    }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { DEFLECTED.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) { DEFLECTED.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void onServerStopped(ServerStoppedEvent event) { DEFLECTED.clear(); }
}
