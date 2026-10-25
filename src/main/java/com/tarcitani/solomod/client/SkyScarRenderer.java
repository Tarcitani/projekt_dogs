package com.tarcitani.solomod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tarcitani.solomod.SoloMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * A Cicatriz no Céu (lore: Era VII). Depois que o Soberano cai ({@link ClientEraState}), toda noite entre os ticks
 * {@link #START} e {@link #END} do dia (por volta da meia-noite) uma fenda fina aparece no zênite do Overworld, com
 * aberração cromática: um canal ciano e um vermelho desalinhados em volta de um núcleo branco, tremendo. Ela entra e
 * sai em {@link #FADE} ticks. Enquanto está aberta, caem fagulhas de estática silenciosas (sem som) bem acima do
 * jogador.
 *
 * <p>Desenhada logo depois do céu ({@code AFTER_SKY}), em coordenadas relativas à câmera, como o sol: fica sempre
 * no zênite, não importa onde o jogador esteja.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class SkyScarRenderer {
    private SkyScarRenderer() {}

    public static final int START = 17500, END = 18500, FADE = 150;
    /** Altura da fenda (blocos acima da câmera) e meia-largura de cada canal. */
    private static final float HEIGHT = 100F, CORE = 0.25F, CHANNEL = 0.6F, SHIFT = 0.9F;
    /** Pontos da fenda: um traço irregular de leste a oeste (fixo, para não "andar" entre quadros). */
    private static final float[][] SPINE = spine();

    private static float[][] spine() {
        RandomSource random = RandomSource.create(0x50B3_A1L);
        int n = 14;
        float[][] points = new float[n][2];
        for (int i = 0; i < n; i++) {
            points[i][0] = -48F + 96F * i / (n - 1);
            points[i][1] = (random.nextFloat() - 0.5F) * 9F;
        }
        return points;
    }

    /** 0..1: quanto a fenda está aberta agora (0 fora da janela, de dia, fora do Overworld ou antes do Soberano). */
    public static float openness(ClientLevel level, float partial) {
        if (!ClientEraState.sovereignDefeated() || level.dimension() != Level.OVERWORLD) return 0F;
        float time = (level.getDayTime() % 24000L) + partial;
        if (time < START || time > END) return 0F;
        return Mth.clamp(Math.min(time - START, END - time) / FADE, 0F, 1F);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float open = openness(mc.level, partial);
        if (open <= 0F) return;

        float time = mc.level.getGameTime() + partial;
        float flicker = 0.8F + 0.2F * Mth.sin(time * 0.9F) * Mth.sin(time * 0.37F);
        float jitter = Mth.sin(time * 2.3F) * 0.25F;
        Matrix4f view = event.getModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = open * flicker;
        // Canais desalinhados (ciano para um lado, vermelho para o outro) e o núcleo branco por cima.
        crack(buffer, view, -SHIFT + jitter, CHANNEL, 0.0F, 0.9F, 1.0F, alpha * 0.8F);
        crack(buffer, view, SHIFT - jitter, CHANNEL, 1.0F, 0.09F, 0.27F, alpha * 0.8F);
        crack(buffer, view, 0F, CORE, 1.0F, 1.0F, 1.0F, alpha);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** Um canal da fenda: faixa de meia-largura {@code width} seguindo {@link #SPINE}, deslocada {@code shift} em x. */
    private static void crack(BufferBuilder buffer, Matrix4f view, float shift, float width,
                              float r, float g, float b, float a) {
        for (int i = 0; i < SPINE.length - 1; i++) {
            float x0 = SPINE[i][0] + shift, z0 = SPINE[i][1];
            float x1 = SPINE[i + 1][0] + shift, z1 = SPINE[i + 1][1];
            float dx = x1 - x0, dz = z1 - z0;
            float len = Mth.sqrt(dx * dx + dz * dz);
            // As pontas afinam: a fenda é mais larga no meio.
            float taper = 1F - Math.abs((i + 0.5F) / (SPINE.length - 1) - 0.5F) * 1.6F;
            float px = -dz / len * width * taper, pz = dx / len * width * taper;
            buffer.addVertex(view, x0 + px, HEIGHT, z0 + pz).setColor(r, g, b, a);
            buffer.addVertex(view, x1 + px, HEIGHT, z1 + pz).setColor(r, g, b, a);
            buffer.addVertex(view, x1 - px, HEIGHT, z1 - pz).setColor(r, g, b, a);
            buffer.addVertex(view, x0 - px, HEIGHT, z0 - pz).setColor(r, g, b, a);
        }
    }

    /** Estática silenciosa: fagulhas que caem bem acima do jogador enquanto a fenda está aberta. */
    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        float open = openness(mc.level, 0F);
        if (open <= 0F || mc.level.random.nextFloat() > open * 0.6F) return;
        RandomSource random = mc.level.random;
        double x = mc.player.getX() + (random.nextDouble() - 0.5) * 24;
        double y = mc.player.getY() + 18 + random.nextDouble() * 10;
        double z = mc.player.getZ() + (random.nextDouble() - 0.5) * 24;
        mc.level.addParticle(random.nextBoolean() ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.END_ROD,
                x, y, z, 0, -0.05, 0);
    }
}
