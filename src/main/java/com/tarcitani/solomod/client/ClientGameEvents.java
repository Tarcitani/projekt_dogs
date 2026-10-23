package com.tarcitani.solomod.client;

import com.tarcitani.solomod.SoloMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Eventos de cliente: tela, HUD, dano e mundo. Cancela apenas os corações vanilla quando a HUD personalizada está visível. */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    @SubscribeEvent public static void onGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH) && SoloHud.visible()) event.setCanceled(true);
    }

    @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { SoloHud.reset(); }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (ClientSetup.OPEN_STATS.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new StatsScreen());
        }
        DamageNumbers.tick();
        SoloHud.tick();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            DamageNumbers.render(event.getPoseStack(), event.getCamera());
        }
    }
}
