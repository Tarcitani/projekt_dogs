package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.client.ClientSetup;
import com.tarcitani.solomod.client.MobAnimations;
import com.tarcitani.solomod.client.model.MobMeshes;
import com.tarcitani.solomod.client.model.TierMobModel;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.entity.TierMob;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Renderer das criaturas. Cada elemento tem um modelo e uma textura COMPLETAMENTE diferentes
 * (Diabrete Ígneo, Medusa Abissal, Titã Ancestral...): o renderer troca de modelo a cada quadro conforme o
 * elemento sincronizado da criatura.
 *
 * <p>Camadas: textura base + brilho emissivo (olhos, gemas, chamas) na cor do elemento + aura de energia
 * vermelha enquanto o chefe está em Fúria. Durante a desintegração do Colosso o corpo fica translúcido.
 *
 * <p>{@link #single} é para criatura sem variantes (o Soberano): uma malha só, desenhada numa escala maior,
 * com o brilho passeando pelas cores dos cinco Monarcas.
 */
public class TierMobRenderer extends MobRenderer<TierMob, TierMobModel> {
    private record Variant(TierMobModel model, ResourceLocation texture, ResourceLocation glow) {}

    private static final ResourceLocation RAGE_SWIRL = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");

    /** Cores do brilho do Soberano, uma por Monarca (Colosso, fogo, água, terra, ar). */
    private static final int[] PRISM = {0xE8C547, 0xFF6A1F, 0x3FA9F5, 0x8BC34A, 0xE6F7FF};

    private final Map<ElementType, Variant> variants = new EnumMap<>(ElementType.class);
    private final int signatureColor;
    private final boolean elemental;
    private final float modelScale;
    private Variant current;

    /**
     * @param name           nome base da criatura ("minion"); as variantes são "minion_fire", "minion_water"...
     * @param modelFactory   classe de animação do tier (a mesma para todas as variantes)
     */
    public TierMobRenderer(EntityRendererProvider.Context context, String name, Function<ModelPart, TierMobModel> modelFactory,
                           float shadow, int signatureColor) {
        this(context, name, modelFactory, shadow, signatureColor, true, 1.0F);
    }

    /** Criatura sem variantes elementais: só a malha {@code name}, desenhada na escala dada. */
    public static TierMobRenderer single(EntityRendererProvider.Context context, String name,
                                         Function<ModelPart, TierMobModel> modelFactory, float shadow, float scale) {
        return new TierMobRenderer(context, name, modelFactory, shadow, PRISM[0], false, scale);
    }

    private TierMobRenderer(EntityRendererProvider.Context context, String name, Function<ModelPart, TierMobModel> modelFactory,
                            float shadow, int signatureColor, boolean elemental, float modelScale) {
        super(context, null, shadow);
        this.signatureColor = signatureColor;
        this.elemental = elemental;
        this.modelScale = modelScale;
        for (ElementType element : ElementType.values()) {
            // NONE vem primeiro: sem variantes, todos os elementos usam a malha base.
            if (!elemental && element != ElementType.NONE) {
                variants.put(element, variants.get(ElementType.NONE));
                continue;
            }
            String id = element == ElementType.NONE ? name : name + "_" + element.getSerializedName();
            TierMobModel model = modelFactory.apply(context.bakeLayer(ClientSetup.layer(id))).withDecor(MobMeshes.DECOR.getOrDefault(id, new String[0]));
            variants.put(element, new Variant(model, texture(id), texture(id + "_glow")));
        }
        current = variants.get(ElementType.NONE);
        this.model = current.model();
        addLayer(new GlowLayer(this));
        addLayer(new RageLayer(this));
    }

    private static ResourceLocation texture(String id) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "textures/entity/" + id + ".png");
    }

    @Override
    public void render(TierMob entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        current = variants.get(entity.element());
        this.model = current.model();
        super.render(entity, yaw, partial, pose, buffers, light);
    }

    @Override
    protected void scale(TierMob entity, PoseStack pose, float partial) {
        if (modelScale != 1.0F) pose.scale(modelScale, modelScale, modelScale);
    }

    @Override
    public ResourceLocation getTextureLocation(TierMob entity) {
        return variants.get(entity.element()).texture();
    }

    @Override
    protected RenderType getRenderType(TierMob entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (entity.dissolveProgress(0) > 0) return RenderType.entityTranslucent(getTextureLocation(entity));
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    /** Brilho que pulsa na cor do elemento (ou na cor-assinatura); vermelho e mais forte em Fúria. */
    private final class GlowLayer extends RenderLayer<TierMob, TierMobModel> {
        GlowLayer(RenderLayerParent<TierMob, TierMobModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, TierMob entity, float limbSwing,
                           float limbAmount, float partial, float age, float headYaw, float headPitch) {
            boolean rage = MobAnimations.isRaging(entity);
            ElementType element = entity.element();
            int rgb = rage ? 0xFF2A10 : !elemental ? prism(age)
                    : element == ElementType.NONE ? signatureColor : ClientSetup.tintFor(element);
            // Pulsação; o brilho é aditivo, então "apagar" = escurecer a cor.
            float k = (rage ? 0.85F + 0.15F * Mth.sin(age * 0.6F) : 0.7F + 0.3F * Mth.sin(age * 0.12F)) * getParentModel().alpha();
            getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(current.glow())), 0xF000F0,
                    OverlayTexture.NO_OVERLAY, scale(rgb, k));
        }
    }

    /** Aura de energia vermelha (a mesma técnica do creeper carregado) enquanto o chefe está em Fúria. */
    private final class RageLayer extends RenderLayer<TierMob, TierMobModel> {
        RageLayer(RenderLayerParent<TierMob, TierMobModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, TierMob entity, float limbSwing,
                           float limbAmount, float partial, float age, float headYaw, float headPitch) {
            if (!MobAnimations.isRaging(entity)) return;
            pose.pushPose();
            pose.scale(1.06F, 1.03F, 1.06F);
            float scroll = age * 0.02F;
            getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.energySwirl(RAGE_SWIRL, scroll % 1.0F, scroll * 0.5F % 1.0F)),
                    0xF000F0, OverlayTexture.NO_OVERLAY, scale(0xFF2010, 0.9F));
            pose.popPose();
        }
    }

    /** Passa de uma cor de Monarca para a próxima a cada 2 s. */
    private static int prism(float age) {
        float pos = age / 40F;
        int i = Math.floorMod((int) pos, PRISM.length);
        int a = PRISM[i], b = PRISM[(i + 1) % PRISM.length];
        float f = pos - (float) Math.floor(pos);
        int r = (int) Mth.lerp(f, (a >> 16) & 0xFF, (b >> 16) & 0xFF);
        int g = (int) Mth.lerp(f, (a >> 8) & 0xFF, (b >> 8) & 0xFF);
        int bl = (int) Mth.lerp(f, a & 0xFF, b & 0xFF);
        return (r << 16) | (g << 8) | bl;
    }

    private static int scale(int rgb, float k) {
        int r = (int) (((rgb >> 16) & 0xFF) * k), g = (int) (((rgb >> 8) & 0xFF) * k), b = (int) ((rgb & 0xFF) * k);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
