package com.tarcitani.solomod.entity.passive;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

/**
 * Eventos de jogo da Fauna Pacífica (registrados em SoloMod). Hoje: a Mariposa da Fenda anula o dano de teleporte
 * das Pérolas do End enquanto tiver a carga do Passo de Fenda.
 */
public final class PassiveEchoEvents {
    private PassiveEchoEvents() {}

    @SubscribeEvent
    public static void onEnderPearl(EntityTeleportEvent.EnderPearl event) {
        if (event.getEntity() instanceof ServerPlayer player && PassiveEchoTriggers.negatePearlDamage(player)) {
            event.setAttackDamage(0.0F);
        }
    }
}
