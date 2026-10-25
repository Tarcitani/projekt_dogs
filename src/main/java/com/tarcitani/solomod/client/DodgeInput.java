package com.tarcitani.solomod.client;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.event.StatsLogic.Dodge;
import com.tarcitani.solomod.network.DodgeRollPayload;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Dois pressionamentos reais do mesmo Ctrl em até 280 ms pedem rolamento; repetição da tecla não conta. */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT)
public final class DodgeInput {
    private DodgeInput() {}
    private static long lastPress = -1;
    private static int lastKey = -1;

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getKey() != GLFW.GLFW_KEY_LEFT_CONTROL && event.getKey() != GLFW.GLFW_KEY_RIGHT_CONTROL) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.isPaused() || !mc.isWindowActive()) {
            lastPress = -1;
            return;
        }
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        long now = Util.getMillis();
        if (lastPress >= 0 && lastKey == event.getKey() && now - lastPress <= Dodge.DOUBLE_TAP_MS) {
            lastPress = -1;
            var input = mc.player.input;
            PacketDistributor.sendToServer(new DodgeRollPayload((byte) Math.signum(input.forwardImpulse),
                    (byte) Math.signum(input.leftImpulse)));
        } else {
            lastPress = now;
            lastKey = event.getKey();
        }
    }

    @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { lastPress = -1; lastKey = -1; }
}
