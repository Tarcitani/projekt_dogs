package com.tarcitani.solomod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.client.model.EliteModel;
import com.tarcitani.solomod.client.model.GuardianModel;
import com.tarcitani.solomod.client.model.MinionModel;
import com.tarcitani.solomod.client.model.MobMeshes;
import com.tarcitani.solomod.client.model.MonarchModel;
import com.tarcitani.solomod.client.model.SovereignModel;
import com.tarcitani.solomod.client.render.ThrownDaggerRenderer;
import com.tarcitani.solomod.client.render.RoyalFireballRenderer;
import com.tarcitani.solomod.client.render.SovereignTornadoRenderer;
import com.tarcitani.solomod.client.render.WaterOrbRenderer;
import com.tarcitani.solomod.client.render.TierMobRenderer;
import com.tarcitani.solomod.component.ModDataComponents;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.Sovereign;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.item.ModItems;
import com.tarcitani.solomod.item.ModularWeaponItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/** Registros só de cliente (mod bus): teclas, HUD, cores dos itens, modelos e renderers das entidades. */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {}

    public static final String KEY_CATEGORY = "key.categories.solomod";

    /** K abre a tela de Maestria (configurável em Opções > Controles). */
    public static final KeyMapping OPEN_STATS = new KeyMapping("key.solomod.open_stats",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KEY_CATEGORY);

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS);
        event.register(WeaponControls.CYCLE);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "hud"), SoloHud::render);
    }

    /**
     * Tingimento das texturas: a camada 0 (cabo/lâmina) fica com a cor original;
     * a camada 1 (gume/runas/gema) recebe a cor do elemento da arma.
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        ItemLike[] weapons = ModItems.WEAPONS.values().stream().map(w -> (ItemLike) w.get()).toArray(ItemLike[]::new);
        event.register((stack, tintIndex) -> {
            if (tintIndex != 1) return 0xFFFFFFFF;
            WeaponData data = ModularWeaponItem.getData(stack);
            return FastColor.ARGB32.opaque(tintFor(data.elementType()));
        }, weapons);
    }

    // ------------------------------------------------------------------ criaturas e adaga

    /** Camada de modelo de uma variante ("minion", "minion_fire", "monarch_air"...). */
    public static ModelLayerLocation layer(String variant) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, variant), "main");
    }

    /** As 21 malhas: forma base + 4 elementos para cada uma das 4 criaturas, e o Soberano. */
    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (String variant : MobMeshes.VARIANTS) {
            event.registerLayerDefinition(layer(variant), () -> MobMeshes.get(variant));
        }
    }

    /** Cada criatura: classe de animação, sombra e cor-assinatura do brilho (quando não tem elemento); o Soberano é gigante e sem variantes. */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MINION.get(),
                ctx -> new TierMobRenderer(ctx, "minion", MinionModel::new, 0.4F, 0xFFD23C));
        event.registerEntityRenderer(ModEntities.ELITE.get(),
                ctx -> new TierMobRenderer(ctx, "elite", EliteModel::new, 0.6F, 0x8CFFF0));
        event.registerEntityRenderer(ModEntities.GUARDIAN.get(),
                ctx -> new TierMobRenderer(ctx, "guardian", GuardianModel::new, 0.8F, 0x9FF3FF));
        event.registerEntityRenderer(ModEntities.MONARCH.get(),
                ctx -> new TierMobRenderer(ctx, "monarch", MonarchModel::new, 1.6F, 0xFF8C28));
        event.registerEntityRenderer(ModEntities.SOVEREIGN.get(),
                ctx -> TierMobRenderer.single(ctx, "sovereign", SovereignModel::new, 2.4F, Sovereign.RENDER_SCALE));
        event.registerEntityRenderer(ModEntities.THROWN_DAGGER.get(), ThrownDaggerRenderer::new);
        event.registerEntityRenderer(ModEntities.ROYAL_FIREBALL.get(), RoyalFireballRenderer::new);
        event.registerEntityRenderer(ModEntities.SOVEREIGN_TORNADO.get(), SovereignTornadoRenderer::new);
        event.registerEntityRenderer(ModEntities.WATER_ORB.get(), WaterOrbRenderer::new);
    }

    /**
     * Propriedades que os modelos dos itens usam nos overrides:
     * <ul>
     *   <li>{@code solomod:element} (todas as armas): 0 = sem elemento, 1 água, 2 terra, 3 fogo, 4 ar —
     *       cada elemento tem uma textura/modelo próprio da arma.</li>
     *   <li>{@code solomod:thrown} (adaga): 1 enquanto ela está arremessada (contorno fantasma na mão).</li>
     * </ul>
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ResourceLocation element = ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "element");
            for (WeaponClass wc : WeaponClass.values()) {
                ItemProperties.register(ModItems.get(wc), element,
                        (stack, level, entity, seed) -> ModularWeaponItem.getData(stack).elementType().ordinal());
            }
            ItemProperties.register(ModItems.get(WeaponClass.DAGGER),
                    ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "thrown"),
                    (stack, level, entity, seed) -> stack.has(ModDataComponents.THROWN_DAGGER.get()) ? 1.0F : 0.0F);
        });
    }

    /** Paleta dos elementos (RGB), usada no item, no HUD e nos números de dano. */
    public static int tintFor(ElementType element) {
        return switch (element) {
            case WATER -> 0x3FA9F5;
            case FIRE -> 0xFF6A1F;
            case EARTH -> 0x8BC34A;
            case AIR -> 0xE6F7FF;
            case NONE -> 0xA8A8A8;
        };
    }
}
