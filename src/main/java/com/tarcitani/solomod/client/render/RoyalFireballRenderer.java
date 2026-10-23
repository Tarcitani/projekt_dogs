package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.entity.RoyalFireball;
import com.tarcitani.solomod.mob.TierIdentity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D da Bola Real no estilo do Minecraft: nada redondo. Um cubo de magma incandescente no centro,
 * uma casca de basalto com lava girando em volta, dois anéis quadrados de chama inclinados e seis blocos
 * de magma em órbita. Texturas 16x16 de pixels grandes (tools/gen_royal_fireball.ps1).
 */
public final class RoyalFireballRenderer extends EntityRenderer<RoyalFireball> {
    private static final ResourceLocation CORE = texture("royal_fireball_core");
    private static final ResourceLocation CRUST = texture("royal_fireball");

    public RoyalFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public void render(RoyalFireball ball, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        float age = ball.tickCount + partial;
        float half = TierIdentity.ROYAL_FIREBALL_SIZE * 0.5F;
        float pulse = 1F + Mth.sin(age * 0.25F) * 0.04F;
        pose.pushPose();
        pose.translate(0, ball.getBbHeight() * 0.5F, 0);

        // Núcleo: cubo de magma girando na diagonal.
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(age * 5F));
        pose.mulPose(Axis.XP.rotationDegrees(25F + age * 2F));
        EffectMesh.cube(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CORE)), half * 0.5F * pulse, 255, 255, 255, 255);
        pose.popPose();

        // Casca de basalto com lava: cubo maior, girando em sentido contrário.
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(45F - age * 3F));
        pose.mulPose(Axis.ZP.rotationDegrees(age * 1.5F));
        EffectMesh.cube(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CRUST)), half * 0.62F, 255, 255, 255, 230);
        pose.popPose();

        // Dois anéis quadrados de chama, inclinados em planos diferentes e girando em velocidades opostas.
        for (int i = 0; i < 2; i++) {
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(i == 0 ? 70F : -20F));
            pose.mulPose(Axis.ZP.rotationDegrees(i == 0 ? 0F : 60F));
            pose.mulPose(Axis.YP.rotationDegrees(age * (i == 0 ? 6F : -4F)));
            EffectMesh.squareRing(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CORE)),
                    half * (1.1F + i * 0.1F), 0.12F, -0.06F, 0.06F, 255, 255, 255, 200);
            pose.popPose();
        }

        // Seis blocos de magma em órbita.
        for (int i = 0; i < 6; i++) {
            float a = age * 0.06F + i * Mth.TWO_PI / 6;
            pose.pushPose();
            pose.translate(Mth.cos(a) * half * 1.25F, Mth.sin(a * 2) * half * 0.4F, Mth.sin(a) * half * 1.25F);
            pose.mulPose(Axis.YP.rotationDegrees(age * 9F + i * 60F));
            EffectMesh.cube(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CRUST)), half * 0.16F, 255, 255, 255, 255);
            pose.popPose();
        }
        pose.popPose();
        super.render(ball, yaw, partial, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(RoyalFireball entity) {
        return CRUST;
    }
}
