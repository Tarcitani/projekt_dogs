package com.tarcitani.solomod.client;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.item.StarterGuide;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Capa exclusiva de Ecos do Despertar em inventário, mãos, chão e molduras.
 * Reconhece o conteúdo de StarterGuide, preservando nome personalizado e cópias vanilla.
 * Envolve o modelo já carregado de written_book, sem substituir assets do namespace minecraft;
 * demais livros e seus overrides conservam o modelo anterior, inclusive após recarregar recursos.
 * Não muda o item, a rede, a entrega única nem a interface vanilla de leitura/púlpito.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GuideBookModel {
    private static final ModelResourceLocation GUIDE = new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "item/echo_guide"),
            ModelResourceLocation.STANDALONE_VARIANT);
    private static final ModelResourceLocation WRITTEN_BOOK = ModelResourceLocation.inventory(
            ResourceLocation.withDefaultNamespace("written_book"));

    private GuideBookModel() {}

    @SubscribeEvent
    public static void register(ModelEvent.RegisterAdditional event) {
        event.register(GUIDE);
    }

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        BakedModel original = event.getModels().get(WRITTEN_BOOK);
        BakedModel cover = event.getModels().get(GUIDE);
        if (original != null && cover != null) event.getModels().put(WRITTEN_BOOK, new CoverOverride(original, cover));
    }

    private static final class CoverOverride extends BakedModelWrapper<BakedModel> {
        private final ItemOverrides overrides;

        private CoverOverride(BakedModel original, BakedModel cover) {
            super(original);
            overrides = new ItemOverrides() {
                @Override
                public @Nullable BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                                     @Nullable LivingEntity entity, int seed) {
                    if (StarterGuide.isGuide(stack)) {
                        BakedModel resolved = cover.getOverrides().resolve(cover, stack, level, entity, seed);
                        return resolved == null ? cover : resolved;
                    }
                    return original.getOverrides().resolve(original, stack, level, entity, seed);
                }
            };
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }
}
