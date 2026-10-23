package com.tarcitani.solomod.event;

import com.tarcitani.solomod.advancement.ModAdvancements;
import com.tarcitani.solomod.attachment.MobProfile;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.core.StatType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Ciclo de vida dos stats: XP por abate, regeneração de mana, atributos e sincronização. */
public final class PlayerEvents {
    private PlayerEvents() {}

    /** XP Global ao matar um mob do mod (qualquer mob com MobProfile). */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || !victim.hasData(ModAttachments.MOB_PROFILE)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        MobProfile profile = victim.getData(ModAttachments.MOB_PROFILE);
        PlayerStatsAttachment stats = StatsLogic.stats(player);
        int xp = profile.tier().xpReward();
        int gainedPoints = stats.addXp(xp);

        player.displayClientMessage(Component.translatable("message.solomod.xp_gain", xp, stats.getXpGlobal())
                .withStyle(ChatFormatting.YELLOW), true);
        if (gainedPoints > 0) {
            player.sendSystemMessage(Component.translatable("message.solomod.points_gain",
                    gainedPoints, stats.getPontosDisponiveis()).withStyle(ChatFormatting.GOLD));
        }
        if (profile.tier() == MobTier.BOSS) {
            ModAdvancements.awardBossDefeat(player, profile.element());
        }
        StatsLogic.onStatsChanged(player);
    }

    /**
     * A cada segundo: regenera mana (1 + INT/10) e sincroniza.
     * A cada 5 ticks: atualiza a penalidade da arma pesada (o jogador pode trocar de item a qualquer momento).
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.tickCount % 5 == 0) {
            StatsLogic.applyLockedWeaponPenalty(player);
        }
        if (player.tickCount % 20 == 0) {
            PlayerStatsAttachment stats = StatsLogic.stats(player);
            if (stats.getManaAtual() < stats.getMaxMana()) {
                stats.regenMana(1 + stats.get(StatType.INT) / 10);
                StatsLogic.onStatsChanged(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) StatsLogic.onStatsChanged(player);
    }

    /** Ao renascer o jogador é uma entidade nova: reaplica atributos e enche a vida com a VIT. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StatsLogic.onStatsChanged(player);
            if (!event.isEndConquered()) player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) StatsLogic.onStatsChanged(player);
    }
}
