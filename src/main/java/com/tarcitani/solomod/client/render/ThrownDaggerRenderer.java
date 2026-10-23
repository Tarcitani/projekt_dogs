package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tarcitani.solomod.entity.ThrownDagger;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Desenha a própria pilha da adaga (com a cor do elemento) apontada na direção do voo.
 * Voando ela gira de ponta a ponta; na volta gira mais rápido e ao contrário; cravada fica parada.
 */
public class ThrownDaggerRenderer extends EntityRenderer<ThrownDagger> {
    private final ItemRenderer itemRenderer;

    public ThrownDaggerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(ThrownDagger dagger, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        // Mesma orientação das flechas: depois destes dois giros, +X aponta para onde a adaga voa.
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial, dagger.yRotO, dagger.getYRot()) - 90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial, dagger.xRotO, dagger.getXRot())));
        if (!dagger.isStuck()) {
            float speed = dagger.isReturning() ? 55F : -38F;
            pose.mulPose(Axis.ZP.rotationDegrees((dagger.tickCount + partial) * speed));
        }
        // O desenho da adaga é diagonal (ponta em cima à direita): -45° deixa a ponta em +X.
        pose.mulPose(Axis.ZP.rotationDegrees(-45.0F));
        pose.scale(1.1F, 1.1F, 1.1F);
        itemRenderer.renderStatic(dagger.stack(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, pose,
                buffers, dagger.level(), dagger.getId());
        pose.popPose();
        super.render(dagger, yaw, partial, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownDagger dagger) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
