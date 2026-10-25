package com.tarcitani.solomod.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tarcitani.solomod.SoloMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * O céu costurado do Anfiteatro do Selo Partido ({@code ArenaSkyPayload}). Enquanto o jogador está na arena, cinco
 * retalhos de céu dos lugares roubados (pedra, fogo, mar, âmbar e tempestade, um por Monarca) aparecem em volta,
 * presos por bordas de falha: um contorno ciano (#00E5FF) e um vermelho (#FF1744) desalinhados, tremendo, como
 * um modelo em wireframe que travou. Desenhado logo depois do céu, relativo à câmera.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ArenaSkyRenderer {
    private ArenaSkyRenderer() {}

    private static final float DISTANCE = 100F, HALF = 22F, EDGE = 0.7F, SHIFT = 0.9F;
    /** Cores dos retalhos, na ordem dos Monarcas (Colosso, Rei Vulcânico, Leviatã, Titã, Imperador). */
    private static final int[] PATCHES = {0x8C8F94, 0xD2511E, 0x245FA6, 0xE0A030, 0xD7DCF5};

    private static long visibleUntil;

    public static void showFor(int ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) visibleUntil = mc.level.getGameTime() + ticks;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        visibleUntil = 0;
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.getGameTime() >= visibleUntil) return;
        float time = mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Matrix4f view = event.getModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        BufferBuilder fill = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < PATCHES.length; i++) {
            Vector3f[] c = corners(i, 0F);
            int rgb = PATCHES[i];
            quad(fill, view, c[0], c[1], c[2], c[3], ((rgb >> 16) & 0xFF) / 255F, ((rgb >> 8) & 0xFF) / 255F,
                    (rgb & 0xFF) / 255F, 0.8F);
        }
        BufferUploader.drawWithShader(fill.buildOrThrow());

        // Bordas de falha: somam luz por cima dos retalhos.
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        BufferBuilder edges = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float jitter = Mth.sin(time * 1.9F) * 0.4F;
        for (int i = 0; i < PATCHES.length; i++) {
            outline(edges, view, corners(i, -SHIFT + jitter), 0.0F, 0.9F, 1.0F);
            outline(edges, view, corners(i, SHIFT - jitter), 1.0F, 0.09F, 0.27F);
        }
        BufferUploader.drawWithShader(edges.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** Cantos do retalho {@code i}: um quadrado de frente para a câmera, em volta do céu, deslocado {@code shift}. */
    private static Vector3f[] corners(int i, float shift) {
        float yaw = (float) Math.toRadians(i * 72 + 15);
        float pitch = (float) Math.toRadians(i % 2 == 0 ? 22 : 40);
        Vector3f dir = new Vector3f(Mth.cos(yaw) * Mth.cos(pitch), Mth.sin(pitch), Mth.sin(yaw) * Mth.cos(pitch));
        Vector3f right = new Vector3f(-Mth.sin(yaw), 0, Mth.cos(yaw));
        Vector3f up = new Vector3f(dir).cross(right).normalize();
        Vector3f center = new Vector3f(dir).mul(DISTANCE).add(new Vector3f(right).mul(shift));
        Vector3f r = new Vector3f(right).mul(HALF), u = new Vector3f(up).mul(HALF);
        return new Vector3f[] {
                new Vector3f(center).sub(r).sub(u), new Vector3f(center).add(r).sub(u),
                new Vector3f(center).add(r).add(u), new Vector3f(center).sub(r).add(u)};
    }

    /** As quatro arestas do retalho como faixas finas. */
    private static void outline(BufferBuilder buffer, Matrix4f view, Vector3f[] c, float r, float g, float b) {
        for (int k = 0; k < 4; k++) {
            Vector3f a = c[k], e = c[(k + 1) % 4];
            Vector3f along = new Vector3f(e).sub(a).normalize();
            Vector3f normal = new Vector3f(a).normalize().cross(along).mul(EDGE);
            quad(buffer, view, new Vector3f(a).sub(normal), new Vector3f(e).sub(normal), new Vector3f(e).add(normal),
                    new Vector3f(a).add(normal), r, g, b, 0.95F);
        }
    }

    private static void quad(BufferBuilder buffer, Matrix4f view, Vector3f a, Vector3f b, Vector3f c, Vector3f d,
                             float r, float g, float bl, float alpha) {
        buffer.addVertex(view, a.x, a.y, a.z).setColor(r, g, bl, alpha);
        buffer.addVertex(view, b.x, b.y, b.z).setColor(r, g, bl, alpha);
        buffer.addVertex(view, c.x, c.y, c.z).setColor(r, g, bl, alpha);
        buffer.addVertex(view, d.x, d.y, d.z).setColor(r, g, bl, alpha);
    }
}
