package com.tarcitani.solomod.client;

import com.tarcitani.solomod.SoloMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/** Cópia no cliente do estado da Era VII (recebida por {@code EraStatePayload}); zera ao sair do mundo. */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientEraState {
    private ClientEraState() {}

    private static boolean sovereignDefeated;

    public static void set(boolean defeated) {
        sovereignDefeated = defeated;
    }

    public static boolean sovereignDefeated() {
        return sovereignDefeated;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        sovereignDefeated = false;
    }
}
