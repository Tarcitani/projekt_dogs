package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tarcitani.solomod.client.model.PassiveEchoModel;
import com.tarcitani.solomod.entity.passive.PassiveEchoEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Renderer genérico da Fauna Pacífica: uma malha e uma textura por forma (ver PassiveEchoModels).
 * A camada emissiva usa {@code <textura>_glow.png} (tons de cinza, como no resto do mod) tingida pela cor do
 * elemento e pulsando devagar. Ela se apaga na Exaustão Elemental e na Forma Astral, como manda a especificação.
 */
public class PassiveEchoRenderer<T extends PassiveEchoEntity> extends MobRenderer<T, PassiveEchoModel<T>> {
    private final ResourceLocation texture;
    private final ResourceLocation glow;
    private final int glowRgb;
    private final float scale;

    public PassiveEchoRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer,
                               ResourceLocation texture, float shadowRadius, int glowRgb, float scale) {
        super(context, new PassiveEchoModel<>(context.bakeLayer(layer)), shadowRadius);
        this.texture = texture;
        this.glow = ResourceLocation.fromNamespaceAndPath(texture.getNamespace(),
                texture.getPath().replace(".png", "_glow.png"));
        this.glowRgb = glowRgb;
        this.scale = scale;
        addLayer(new GlowLayer(this));
    }

    /** Cada forma tem seu tamanho: a malha é pequena e a escala dá variação de altura entre elas. */
    @Override
    protected void scale(T entity, PoseStack pose, float partial) {
        pose.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    private final class GlowLayer extends RenderLayer<T, PassiveEchoModel<T>> {
        GlowLayer(RenderLayerParent<T, PassiveEchoModel<T>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing,
                           float limbAmount, float partial, float age, float headYaw, float headPitch) {
            if (entity.isExhausted() || entity.isDormant()) return;
            // Pulsação lenta; o brilho é aditivo, então "apagar" = escurecer a cor.
            float k = 0.7F + 0.3F * Mth.sin(age * 0.12F);
            getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(glow)), 0xF000F0,
                    OverlayTexture.NO_OVERLAY, scale(glowRgb, k));
        }
    }

    private static int scale(int rgb, float k) {
        int r = (int) (((rgb >> 16) & 0xFF) * k), g = (int) (((rgb >> 8) & 0xFF) * k), b = (int) ((rgb & 0xFF) * k);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
