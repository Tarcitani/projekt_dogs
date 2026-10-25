package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.client.ClientSetup;
import com.tarcitani.solomod.client.model.SanctuaryModel;
import com.tarcitani.solomod.entity.sanctuary.SanctuaryMob;
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
 * Renderer dos mobs dos Santuários de Eco: malha e textura por criatura e uma camada de brilho ({@code _glow.png},
 * tons de cinza) tingida na cor do santuário e pulsando. A escala dá o tamanho final. Ilusões não têm sombra.
 */
public class SanctuaryRenderer<T extends SanctuaryMob> extends MobRenderer<T, SanctuaryModel<T>> {
    private final ResourceLocation texture;
    private final ResourceLocation glow;
    private final int glowRgb;
    private final float scale;
    private final float baseShadow;

    public SanctuaryRenderer(EntityRendererProvider.Context context, String name, float shadowRadius, int glowRgb,
                             float scale) {
        super(context, new SanctuaryModel<>(context.bakeLayer(ClientSetup.layer(name))), shadowRadius * scale);
        this.texture = texture(name);
        this.glow = texture(name + "_glow");
        this.glowRgb = glowRgb;
        this.scale = scale;
        this.baseShadow = shadowRadius * scale;
        addLayer(new GlowLayer(this));
    }

    private static ResourceLocation texture(String id) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "textures/entity/sanctuary/" + id + ".png");
    }

    /** Ilusões (cópias do Arquivista) não projetam sombra: é a pista para achar o verdadeiro. */
    @Override
    public void render(T entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        this.shadowRadius = entity.isIllusion() ? 0.0F : baseShadow;
        super.render(entity, yaw, partial, pose, buffers, light);
    }

    @Override
    protected void scale(T entity, PoseStack pose, float partial) {
        pose.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    private final class GlowLayer extends RenderLayer<T, SanctuaryModel<T>> {
        GlowLayer(RenderLayerParent<T, SanctuaryModel<T>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing,
                           float limbAmount, float partial, float age, float headYaw, float headPitch) {
            if (entity.isInvisible()) return;
            float k = 0.75F + 0.25F * Mth.sin(age * 0.15F);
            getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(glow)), 0xF000F0,
                    OverlayTexture.NO_OVERLAY, tint(glowRgb, k));
        }
    }

    private static int tint(int rgb, float k) {
        int r = (int) (((rgb >> 16) & 0xFF) * k), g = (int) (((rgb >> 8) & 0xFF) * k), b = (int) ((rgb & 0xFF) * k);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
