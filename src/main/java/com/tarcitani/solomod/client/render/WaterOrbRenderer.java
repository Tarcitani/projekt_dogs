package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.entity.WaterOrb;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D da esfera de água do Soberano no estilo do Minecraft: não é redonda, é um cubo dentro de cubo
 * dentro de cubo (como o cristal do End): núcleo ciano brilhante, cubo médio e casca externa translúcida,
 * cada um girando em eixos diferentes, com seis cubinhos de água em órbita. Texturas 16x16 de pixels grandes
 * (tools/gen_sovereign_effects.py).
 */
public final class WaterOrbRenderer extends EntityRenderer<WaterOrb> {
    private static final ResourceLocation SHELL = texture("water_orb");
    private static final ResourceLocation CORE = texture("water_orb_core");

    public WaterOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public void render(WaterOrb orb, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        float age = orb.tickCount + partial;
        float half = WaterOrb.SIZE * 0.5F;
        float pulse = 1F + Mth.sin(age * 0.3F) * 0.05F;
        pose.pushPose();
        pose.translate(0, orb.getBbHeight() * 0.5F, 0);

        // Núcleo: cubo claro girando na diagonal.
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(age * 6F));
        pose.mulPose(Axis.XP.rotationDegrees(35F + age * 3F));
        EffectMesh.cube(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CORE)), half * 0.44F * pulse, 255, 255, 255, 255);
        pose.popPose();

        // Cubo médio, em sentido contrário.
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-age * 4F + 45F));
        pose.mulPose(Axis.ZP.rotationDegrees(age * 2.5F));
        EffectMesh.cube(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(SHELL)), half * 0.72F, 255, 255, 255, 190);
        pose.popPose();

        // Casca externa translúcida, mais lenta.
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(age * 2.2F));
        EffectMesh.cube(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(SHELL)), half * pulse, 255, 255, 255, 140);
        pose.popPose();

        // Seis cubinhos de água em órbita inclinada.
        for (int i = 0; i < 6; i++) {
            float a = age * 0.07F + i * Mth.TWO_PI / 6;
            pose.pushPose();
            pose.translate(Mth.cos(a) * half * 1.5F, Mth.sin(a * 2) * half * 0.55F, Mth.sin(a) * half * 1.5F);
            pose.mulPose(Axis.YP.rotationDegrees(age * 8F + i * 50F));
            pose.mulPose(Axis.XP.rotationDegrees(age * 6F));
            EffectMesh.cube(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(SHELL)), half * 0.15F, 255, 255, 255, 235);
            pose.popPose();
        }
        pose.popPose();
        super.render(orb, yaw, partial, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(WaterOrb entity) {
        return SHELL;
    }
}
