package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.entity.RoyalFireball;
import com.tarcitani.solomod.mob.TierIdentity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D próprio da Bola Real: núcleo facetado incandescente, crosta vulcânica vazada,
 * dois anéis de plasma inclinados e fragmentos de magma em órbita. Não usa item nem arte de fireball
 * vanilla. Malha procedural e texturas geradas por tools/gen_royal_fireball.ps1; emissão em luz máxima.
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
        float radius = TierIdentity.ROYAL_FIREBALL_SIZE * 0.5F;
        pose.pushPose();
        pose.translate(0, ball.getBbHeight() * 0.5F, 0);
        pose.mulPose(Axis.YP.rotationDegrees(age * 3F));
        pose.mulPose(Axis.ZP.rotationDegrees(18F));
        float pulse = 1F + Mth.sin(age * 0.25F) * 0.035F;
        sphere(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CORE)), radius * 0.88F * pulse,
                16, 10, 255, 235, 160, 255);
        pose.mulPose(Axis.XP.rotationDegrees(age * 1.3F));
        sphere(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CRUST)), radius, 16, 10, 255, 255, 255, 255);

        for (int i = 0; i < 2; i++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(i == 0 ? 52F : -62F));
            pose.mulPose(Axis.YP.rotationDegrees(age * (i == 0 ? 4F : -3F)));
            ring(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CORE)), radius * (1.25F + i * 0.12F));
            pose.popPose();
        }
        for (int i = 0; i < 6; i++) {
            float a = age * 0.045F + i * Mth.TWO_PI / 6;
            pose.pushPose();
            pose.translate(Mth.cos(a) * radius * 1.3F, Mth.sin(a * 2) * radius * 0.45F, Mth.sin(a) * radius * 1.3F);
            pose.mulPose(Axis.ZP.rotationDegrees(age * 5F + i * 60F));
            sphere(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(CRUST)), radius * 0.15F, 6, 4, 255, 180, 100, 255);
            pose.popPose();
        }
        pose.popPose();
        super.render(ball, yaw, partial, pose, buffers, light);
    }

    private static void sphere(PoseStack pose, VertexConsumer buffer, float radius, int segments, int bands,
                               int r, int g, int b, int alpha) {
        for (int y = 0; y < bands; y++) {
            for (int x = 0; x < segments; x++) {
                sphereVertex(pose, buffer, radius, (float) x / segments, (float) y / bands, r, g, b, alpha);
                sphereVertex(pose, buffer, radius, (float) (x + 1) / segments, (float) y / bands, r, g, b, alpha);
                sphereVertex(pose, buffer, radius, (float) (x + 1) / segments, (float) (y + 1) / bands, r, g, b, alpha);
                sphereVertex(pose, buffer, radius, (float) x / segments, (float) (y + 1) / bands, r, g, b, alpha);
            }
        }
    }

    private static void sphereVertex(PoseStack pose, VertexConsumer buffer, float radius, float u, float v,
                                     int r, int g, int b, int alpha) {
        float a = u * Mth.TWO_PI, p = v * Mth.PI;
        float x = Mth.cos(a) * Mth.sin(p), y = Mth.cos(p), z = Mth.sin(a) * Mth.sin(p);
        vertex(pose, buffer, x * radius, y * radius, z * radius, u, v, r, g, b, alpha, x, y, z);
    }

    private static void ring(PoseStack pose, VertexConsumer buffer, float radius) {
        for (int i = 0; i < 48; i++) {
            float a = i * Mth.TWO_PI / 48, next = (i + 1) * Mth.TWO_PI / 48;
            float wave = 0.05F * Mth.sin(a * 5);
            vertex(pose, buffer, Mth.cos(a) * (radius - 0.06F), wave, Mth.sin(a) * (radius - 0.06F), 0, 0, 255, 160, 35, 180, 0, 1, 0);
            vertex(pose, buffer, Mth.cos(a) * (radius + 0.06F), wave, Mth.sin(a) * (radius + 0.06F), 0, 1, 255, 210, 90, 180, 0, 1, 0);
            vertex(pose, buffer, Mth.cos(next) * (radius + 0.06F), wave, Mth.sin(next) * (radius + 0.06F), 1, 1, 255, 210, 90, 180, 0, 1, 0);
            vertex(pose, buffer, Mth.cos(next) * (radius - 0.06F), wave, Mth.sin(next) * (radius - 0.06F), 1, 0, 255, 160, 35, 180, 0, 1, 0);
        }
    }

    private static void vertex(PoseStack pose, VertexConsumer buffer, float x, float y, float z, float u, float v,
                               int r, int g, int b, int alpha, float nx, float ny, float nz) {
        buffer.addVertex(pose.last().pose(), x, y, z).setColor(r, g, b, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose.last(), nx, ny, nz);
    }

    @Override public ResourceLocation getTextureLocation(RoyalFireball entity) { return CRUST; }
}
