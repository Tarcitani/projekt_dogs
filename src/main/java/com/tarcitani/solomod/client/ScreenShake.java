package com.tarcitani.solomod.client;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.network.ShakePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Tremor de câmera (ver {@link ShakePayload}): balança guinada, inclinação e rolagem com a força caindo até
 * zero. Só mexe na câmera; a mira real do jogador não muda.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ScreenShake {
    private ScreenShake() {}

    private static float intensity;
    private static long start;
    private static int duration;

    public static void start(ShakePayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        // Um tremor mais fraco não interrompe um mais forte que ainda está rolando.
        if (current(0) > payload.intensity()) return;
        intensity = payload.intensity();
        duration = Math.max(1, payload.duration());
        start = mc.level.getGameTime();
    }

    /** Força atual (0 quando acabou). */
    private static float current(float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || duration <= 0) return 0F;
        float elapsed = mc.level.getGameTime() - start + partial;
        return elapsed >= duration ? 0F : intensity * (1F - elapsed / duration);
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        float k = current((float) event.getPartialTick());
        if (k <= 0F) return;
        float t = Minecraft.getInstance().level.getGameTime() + (float) event.getPartialTick();
        event.setYaw(event.getYaw() + (float) Math.sin(t * 2.3F) * k * 1.4F);
        event.setPitch(event.getPitch() + (float) Math.cos(t * 3.1F) * k * 1.1F);
        event.setRoll(event.getRoll() + (float) Math.sin(t * 2.9F) * k * 2.0F);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        duration = 0;
    }
}
