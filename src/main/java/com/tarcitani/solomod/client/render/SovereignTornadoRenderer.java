package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.entity.SovereignTornado;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D do tornado do Soberano no estilo do Minecraft: uma pilha de 14 anéis QUADRADOS ocos (um por bloco
 * de altura) que crescem para cima, cada um girado a um ângulo e velocidade próprios, de modo que os cantos
 * formam uma espiral de degraus (como o vento do Breeze). Um pilar central escuro dá profundidade e blocos
 * de terra e pedra sobem em órbita. A textura é 16x16 de pixels grandes (tools/gen_sovereign_effects.py).
 */
public final class SovereignTornadoRenderer extends EntityRenderer<SovereignTornado> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID,
            "textures/entity/sovereign_tornado.png");
    private static final int LAYERS = 14;

    public SovereignTornadoRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SovereignTornado tornado, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        float age = tornado.tickCount + partial;
        // Surge do chão nos primeiros 0,75 s e se desfaz no último: o funil cresce e encolhe.
        float grow = Mth.clamp(age / 15F, 0F, 1F) * Mth.clamp((SovereignTornado.LIFE - age) / 15F, 0F, 1F);
        VertexConsumer wall = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        pose.pushPose();
        pose.scale(grow, grow, grow);
        // Pilar central escuro.
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-age * 14F));
        EffectMesh.box(pose, wall, -0.35F, 0F, -0.35F, 0.35F, LAYERS, 0.35F, 150, 165, 185, 200);
        pose.popPose();
        for (int i = 0; i < LAYERS; i++) {
            // Balanço maior quanto mais alto; cada anel gira diferente (e alternando o sentido).
            float sway = Mth.sin(age * 0.12F + i * 0.5F) * 0.05F * i;
            float swayZ = Mth.cos(age * 0.1F + i * 0.45F) * 0.05F * i;
            float spin = i * 17F + age * (7F + 0.6F * i) * (i % 2 == 0 ? 1F : -0.75F);
            pose.pushPose();
            pose.translate(sway, 0, swayZ);
            pose.mulPose(Axis.YP.rotationDegrees(spin));
            EffectMesh.squareRing(pose, wall, 0.9F + i * 0.2F, 0.5F, i, i + 0.92F, 255, 255, 255, 215);
            pose.popPose();
        }
        // Blocos de terra e pedra subindo em espiral: cubos pequenos girando.
        for (int i = 0; i < 9; i++) {
            float h = (age * 0.06F + i * 1.55F) % LAYERS;
            float a = age * (0.35F + 0.03F * i) + i * 2.1F;
            float r = 1.1F + h * 0.2F + 0.5F;
            pose.pushPose();
            pose.translate(Mth.cos(a) * r, h, Mth.sin(a) * r);
            pose.mulPose(Axis.YP.rotationDegrees(age * 12F + i * 40F));
            pose.mulPose(Axis.XP.rotationDegrees(age * 9F));
            EffectMesh.cube(pose, wall, 0.16F + 0.04F * (i % 3), 120, 100, 80, 255);
            pose.popPose();
        }
        pose.popPose();
        super.render(tornado, yaw, partial, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SovereignTornado entity) {
        return TEXTURE;
    }
}
