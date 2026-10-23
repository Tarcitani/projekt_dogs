package com.tarcitani.solomod.client;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.network.CinematicPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Cinemática de abate do Soberano:
 * <ul>
 *   <li>faixas pretas descem/sobem (formato cinema) e todo o HUD e a mão somem;</li>
 *   <li>a câmera vira suavemente para o chefe que se desintegra, com um leve zoom;</li>
 *   <li>letreiro: "SOBERANO DERROTADO" na faixa de cima e o nome do chefe na de baixo.</li>
 * </ul>
 * O jogador continua podendo andar; só a câmera é guiada (a mira real não muda).
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class BossCinematic {
    private BossCinematic() {}

    private static final int EASE = 20;

    private static int entityId = -1;
    private static long start;
    private static int duration;
    private static Component title = Component.empty();
    private static Vec3 focus;

    public static void start(CinematicPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        entityId = payload.entityId();
        start = mc.level.getGameTime();
        duration = payload.duration();
        title = payload.title();
        Entity boss = mc.level.getEntity(entityId);
        focus = boss != null ? boss.position().add(0, boss.getBbHeight() * 0.6, 0) : null;
    }

    private static float elapsed(float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (entityId < 0 || mc.level == null) return -1;
        float e = mc.level.getGameTime() - start + partial;
        return e >= duration ? -1 : e;
    }

    public static boolean active() {
        return elapsed(0) >= 0;
    }

    /** 0..1: entra nos primeiros 20 ticks, sai nos últimos 20. */
    private static float weight(float e) {
        float in = Mth.clamp(e / EASE, 0, 1), out = Mth.clamp((duration - e) / EASE, 0, 1);
        float w = Math.min(in, out);
        return w * w * (3 - 2 * w);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (entityId < 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (!active()) {
            entityId = -1;
            return;
        }
        // Enquanto o chefe existir, a câmera acompanha o corpo (ele se desfaz de cima para baixo).
        Entity boss = mc.level.getEntity(entityId);
        if (boss != null) focus = boss.position().add(0, boss.getBbHeight() * 0.6, 0);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        entityId = -1;
    }

    /** Some com todo o HUD (barra de vida, hotbar, mira, o HUD do mod...). */
    @SubscribeEvent
    public static void onGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (active()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onHand(RenderHandEvent event) {
        if (active()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onGui(RenderGuiEvent.Post event) {
        float e = elapsed(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        if (e < 0) return;
        GuiGraphics g = event.getGuiGraphics();
        Font font = Minecraft.getInstance().font;
        int w = g.guiWidth(), h = g.guiHeight();
        int bar = (int) (h * 0.14F * weight(e));
        g.fill(0, 0, w, bar, 0xFF000000);
        g.fill(0, h - bar, w, h, 0xFF000000);

        // Letreiro aparece depois que as faixas fecharam e some junto com elas.
        float text = Mth.clamp((e - 25) / 15F, 0, 1) * Mth.clamp((duration - e - 5) / 15F, 0, 1);
        if (text <= 0.02F) return;
        int alpha = Math.max(5, (int) (text * 255)) << 24;

        Component header = Component.translatable("cinematic.solomod.defeated").withStyle(ChatFormatting.BOLD);
        g.pose().pushPose();
        g.pose().translate(w / 2F, bar / 2F - 6, 0);
        g.pose().scale(1.6F, 1.6F, 1);
        g.drawString(font, header, -font.width(header) / 2, 0, alpha | 0xE8C547, false);
        g.pose().popPose();

        g.pose().pushPose();
        g.pose().translate(w / 2F, h - bar / 2F - 8, 0);
        g.pose().scale(2.0F, 2.0F, 1);
        g.drawString(font, title, -font.width(title) / 2, 0, alpha | 0xFFFFFF, true);
        g.pose().popPose();
    }

    /** Vira a câmera para o chefe (sem mudar para onde o jogador realmente olha). */
    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        float e = elapsed((float) event.getPartialTick());
        if (e < 0 || focus == null) return;
        float wgt = weight(e);
        Vec3 d = focus.subtract(event.getCamera().getPosition());
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90F;
        float pitch = (float) -(Mth.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * Mth.RAD_TO_DEG);
        event.setYaw(Mth.rotLerp(wgt, event.getYaw(), yaw));
        event.setPitch(Mth.lerp(wgt, event.getPitch(), pitch));
        event.setRoll(event.getRoll() + Mth.sin(e * 0.05F) * 1.5F * wgt);
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        float e = elapsed((float) event.getPartialTick());
        if (e >= 0) event.setFOV(event.getFOV() * (1 - 0.18F * weight(e)));
    }
}
