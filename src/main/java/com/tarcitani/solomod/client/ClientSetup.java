package com.tarcitani.solomod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.client.model.EliteModel;
import com.tarcitani.solomod.client.model.GuardianModel;
import com.tarcitani.solomod.client.model.MinionModel;
import com.tarcitani.solomod.client.model.MobMeshes;
import com.tarcitani.solomod.client.model.MonarchModel;
import com.tarcitani.solomod.client.model.PassiveEchoModels;
import com.tarcitani.solomod.client.model.SanctuaryModels;
import com.tarcitani.solomod.client.model.SovereignModel;
import com.tarcitani.solomod.client.render.PassiveEchoRenderer;
import com.tarcitani.solomod.client.render.SanctuaryRenderer;
import com.tarcitani.solomod.client.render.ThrownDaggerRenderer;
import com.tarcitani.solomod.entity.passive.AbyssalTrenchRemora;
import com.tarcitani.solomod.entity.passive.AmberRootGazelle;
import com.tarcitani.solomod.entity.passive.CloudMantaRay;
import com.tarcitani.solomod.entity.passive.DimensionalRiftMoth;
import com.tarcitani.solomod.entity.passive.StormSentinelOwl;
import com.tarcitani.solomod.entity.passive.RunicBoulderGolem;
import com.tarcitani.solomod.entity.passive.SylvanMiniTank;
import com.tarcitani.solomod.entity.passive.EchoRunicBat;
import com.tarcitani.solomod.entity.passive.LiquidManaAxolotl;
import com.tarcitani.solomod.entity.passive.LotusTurtleCrab;
import com.tarcitani.solomod.entity.passive.GoldenSilicaScarab;
import com.tarcitani.solomod.entity.passive.IgneousSalamander;
import com.tarcitani.solomod.entity.passive.VolcanicAshColt;
import com.tarcitani.solomod.entity.passive.RelicAutomaton;
import com.tarcitani.solomod.entity.passive.SpectralScout;
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
        event.registerLayerDefinition(layer("cinzagrim"), SanctuaryModels::cinzagrim);
        event.registerLayerDefinition(layer("living_slag"), SanctuaryModels::livingSlag);
        event.registerLayerDefinition(layer("forge_spark"), SanctuaryModels::forgeSpark);
        event.registerLayerDefinition(layer("withered_smith"), SanctuaryModels::witheredSmith);
        event.registerLayerDefinition(layer("archivist"), SanctuaryModels::archivist);
        event.registerLayerDefinition(layer("flying_tome"), SanctuaryModels::flyingTome);
        event.registerLayerDefinition(layer("ink_sentinel"), SanctuaryModels::inkSentinel);
        event.registerLayerDefinition(layer("spectral_scribe"), SanctuaryModels::spectralScribe);
        event.registerLayerDefinition(layer("weeper"), SanctuaryModels::weeper);
        event.registerLayerDefinition(layer("choir_drowned"), SanctuaryModels::choirDrowned);
        event.registerLayerDefinition(layer("coral_jelly"), SanctuaryModels::coralJelly);
        event.registerLayerDefinition(layer("coral_crab"), SanctuaryModels::coralCrab);
        event.registerLayerDefinition(layer("root_mother"), SanctuaryModels::rootMother);
        event.registerLayerDefinition(layer("thorn_sprout"), SanctuaryModels::thornSprout);
        event.registerLayerDefinition(layer("moss_golem"), SanctuaryModels::mossGolem);
        event.registerLayerDefinition(layer("amber_wasp"), SanctuaryModels::amberWasp);
        event.registerLayerDefinition(layer("herald"), SanctuaryModels::herald);
        event.registerLayerDefinition(layer("gray_harpy"), SanctuaryModels::grayHarpy);
        event.registerLayerDefinition(layer("gale_totem"), SanctuaryModels::galeTotem);
        event.registerLayerDefinition(layer("breeze_elemental"), SanctuaryModels::breezeElemental);
        event.registerLayerDefinition(layer("echo_hunter"), SanctuaryModels::echoHunter);
        event.registerLayerDefinition(layer("wailing_shade"), SanctuaryModels::wailingShade);
        event.registerLayerDefinition(layer("whispering_bones"), SanctuaryModels::whisperingBones);
        event.registerLayerDefinition(layer("creeping_shadow"), SanctuaryModels::creepingShadow);
        event.registerLayerDefinition(layer("spectral_scout"), PassiveEchoModels::spectralScout);
        event.registerLayerDefinition(layer("echo_runic_bat"), PassiveEchoModels::echoRunicBat);
        event.registerLayerDefinition(layer("relic_automaton"), PassiveEchoModels::relicAutomaton);
        event.registerLayerDefinition(layer("igneous_salamander"), PassiveEchoModels::igneousSalamander);
        event.registerLayerDefinition(layer("golden_silica_scarab"), PassiveEchoModels::goldenSilicaScarab);
        event.registerLayerDefinition(layer("volcanic_ash_colt"), PassiveEchoModels::volcanicAshColt);
        event.registerLayerDefinition(layer("liquid_mana_axolotl"), PassiveEchoModels::liquidManaAxolotl);
        event.registerLayerDefinition(layer("lotus_turtle_crab"), PassiveEchoModels::lotusTurtleCrab);
        event.registerLayerDefinition(layer("abyssal_trench_remora"), PassiveEchoModels::abyssalTrenchRemora);
        event.registerLayerDefinition(layer("sylvan_mini_tank"), PassiveEchoModels::sylvanMiniTank);
        event.registerLayerDefinition(layer("runic_boulder_golem"), PassiveEchoModels::runicBoulderGolem);
        event.registerLayerDefinition(layer("amber_root_gazelle"), PassiveEchoModels::amberRootGazelle);
        event.registerLayerDefinition(layer("cloud_manta_ray"), PassiveEchoModels::cloudMantaRay);
        event.registerLayerDefinition(layer("storm_sentinel_owl"), PassiveEchoModels::stormSentinelOwl);
        event.registerLayerDefinition(layer("dimensional_rift_moth"), PassiveEchoModels::dimensionalRiftMoth);
    }

    /** Textura de uma forma da Fauna Pacífica (textures/entity/passive/<nome>.png). */
    private static ResourceLocation passiveTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "textures/entity/passive/" + name + ".png");
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
        event.registerEntityRenderer(ModEntities.CINZAGRIM.get(), ctx -> new SanctuaryRenderer<>(ctx, "cinzagrim", 1.0F, 0xFF6A00, 1.0F));
        event.registerEntityRenderer(ModEntities.LIVING_SLAG.get(), ctx -> new SanctuaryRenderer<>(ctx, "living_slag", 0.5F, 0xFF8A1F, 1.2F));
        event.registerEntityRenderer(ModEntities.FORGE_SPARK.get(), ctx -> new SanctuaryRenderer<>(ctx, "forge_spark", 0.3F, 0xFFB347, 1.2F));
        event.registerEntityRenderer(ModEntities.WITHERED_SMITH.get(), ctx -> new SanctuaryRenderer<>(ctx, "withered_smith", 0.4F, 0xE0541A, 1.0F));
        event.registerEntityRenderer(ModEntities.ARCHIVIST.get(), ctx -> new SanctuaryRenderer<>(ctx, "archivist", 0.6F, 0xB38CFF, 1.0F));
        event.registerEntityRenderer(ModEntities.FLYING_TOME.get(), ctx -> new SanctuaryRenderer<>(ctx, "flying_tome", 0.3F, 0xC9A8FF, 1.0F));
        event.registerEntityRenderer(ModEntities.INK_SENTINEL.get(), ctx -> new SanctuaryRenderer<>(ctx, "ink_sentinel", 0.5F, 0x6A5AFF, 1.0F));
        event.registerEntityRenderer(ModEntities.SPECTRAL_SCRIBE.get(), ctx -> new SanctuaryRenderer<>(ctx, "spectral_scribe", 0.4F, 0xE6E0FF, 1.0F));
        event.registerEntityRenderer(ModEntities.WEEPER.get(), ctx -> new SanctuaryRenderer<>(ctx, "weeper", 0.8F, 0xFF8FA3, 1.0F));
        event.registerEntityRenderer(ModEntities.CHOIR_DROWNED.get(), ctx -> new SanctuaryRenderer<>(ctx, "choir_drowned", 0.4F, 0x7FE0F0, 1.0F));
        event.registerEntityRenderer(ModEntities.CORAL_JELLY.get(), ctx -> new SanctuaryRenderer<>(ctx, "coral_jelly", 0.4F, 0xFF9EC4, 1.0F));
        event.registerEntityRenderer(ModEntities.CORAL_CRAB.get(), ctx -> new SanctuaryRenderer<>(ctx, "coral_crab", 0.6F, 0xFFB27A, 1.0F));
        event.registerEntityRenderer(ModEntities.ROOT_MOTHER.get(), ctx -> new SanctuaryRenderer<>(ctx, "root_mother", 1.2F, 0xFFB300, 1.0F));
        event.registerEntityRenderer(ModEntities.THORN_SPROUT.get(), ctx -> new SanctuaryRenderer<>(ctx, "thorn_sprout", 0.3F, 0xE6FF7A, 1.0F));
        event.registerEntityRenderer(ModEntities.MOSS_GOLEM.get(), ctx -> new SanctuaryRenderer<>(ctx, "moss_golem", 0.6F, 0xFFC24A, 1.0F));
        event.registerEntityRenderer(ModEntities.AMBER_WASP.get(), ctx -> new SanctuaryRenderer<>(ctx, "amber_wasp", 0.3F, 0xFFB300, 1.0F));
        event.registerEntityRenderer(ModEntities.HERALD.get(), ctx -> new SanctuaryRenderer<>(ctx, "herald", 0.8F, 0x4FC3F7, 1.0F));
        event.registerEntityRenderer(ModEntities.GRAY_HARPY.get(), ctx -> new SanctuaryRenderer<>(ctx, "gray_harpy", 0.4F, 0xE0F7FF, 1.0F));
        event.registerEntityRenderer(ModEntities.GALE_TOTEM.get(), ctx -> new SanctuaryRenderer<>(ctx, "gale_totem", 0.5F, 0x9FE8FF, 1.0F));
        event.registerEntityRenderer(ModEntities.BREEZE_ELEMENTAL.get(), ctx -> new SanctuaryRenderer<>(ctx, "breeze_elemental", 0.3F, 0xC8F4FF, 1.0F));
        event.registerEntityRenderer(ModEntities.ECHO_HUNTER.get(), ctx -> new SanctuaryRenderer<>(ctx, "echo_hunter", 0.5F, 0xFFD54F, 1.0F));
        event.registerEntityRenderer(ModEntities.WAILING_SHADE.get(), ctx -> new SanctuaryRenderer<>(ctx, "wailing_shade", 0.4F, 0xA8F0E6, 1.0F));
        event.registerEntityRenderer(ModEntities.WHISPERING_BONES.get(), ctx -> new SanctuaryRenderer<>(ctx, "whispering_bones", 0.4F, 0x7AE0FF, 1.0F));
        event.registerEntityRenderer(ModEntities.CREEPING_SHADOW.get(), ctx -> new SanctuaryRenderer<>(ctx, "creeping_shadow", 0.4F, 0xB06BFF, 1.0F));
        event.registerEntityRenderer(ModEntities.SPECTRAL_SCOUT.get(), ctx -> new PassiveEchoRenderer<SpectralScout>(
                ctx, layer("spectral_scout"), passiveTexture("spectral_scout"), 0.3F, 0x00E5FF, 1.25F));
        event.registerEntityRenderer(ModEntities.ECHO_RUNIC_BAT.get(), ctx -> new PassiveEchoRenderer<EchoRunicBat>(
                ctx, layer("echo_runic_bat"), passiveTexture("echo_runic_bat"), 0.2F, 0x1E88E5, 1.3F));
        event.registerEntityRenderer(ModEntities.RELIC_AUTOMATON.get(), ctx -> new PassiveEchoRenderer<RelicAutomaton>(
                ctx, layer("relic_automaton"), passiveTexture("relic_automaton"), 0.5F, 0xD4AF37, 1.3F));
        event.registerEntityRenderer(ModEntities.IGNEOUS_SALAMANDER.get(), ctx -> new PassiveEchoRenderer<IgneousSalamander>(
                ctx, layer("igneous_salamander"), passiveTexture("igneous_salamander"), 0.4F, 0xFF5722, 1.3F));
        event.registerEntityRenderer(ModEntities.GOLDEN_SILICA_SCARAB.get(), ctx -> new PassiveEchoRenderer<GoldenSilicaScarab>(
                ctx, layer("golden_silica_scarab"), passiveTexture("golden_silica_scarab"), 0.3F, 0xFF8F00, 1.5F));
        event.registerEntityRenderer(ModEntities.VOLCANIC_ASH_COLT.get(), ctx -> new PassiveEchoRenderer<VolcanicAshColt>(
                ctx, layer("volcanic_ash_colt"), passiveTexture("volcanic_ash_colt"), 0.25F, 0xFF3D00, 1.5F));
        event.registerEntityRenderer(ModEntities.LIQUID_MANA_AXOLOTL.get(), ctx -> new PassiveEchoRenderer<LiquidManaAxolotl>(
                ctx, layer("liquid_mana_axolotl"), passiveTexture("liquid_mana_axolotl"), 0.3F, 0xE0F7FA, 1.4F));
        event.registerEntityRenderer(ModEntities.LOTUS_TURTLE_CRAB.get(), ctx -> new PassiveEchoRenderer<LotusTurtleCrab>(
                ctx, layer("lotus_turtle_crab"), passiveTexture("lotus_turtle_crab"), 0.6F, 0xE040FB, 1.3F));
        event.registerEntityRenderer(ModEntities.ABYSSAL_TRENCH_REMORA.get(), ctx -> new PassiveEchoRenderer<AbyssalTrenchRemora>(
                ctx, layer("abyssal_trench_remora"), passiveTexture("abyssal_trench_remora"), 0.2F, 0x00B8D4, 1.5F));
        event.registerEntityRenderer(ModEntities.SYLVAN_MINI_TANK.get(), ctx -> new PassiveEchoRenderer<SylvanMiniTank>(
                ctx, layer("sylvan_mini_tank"), passiveTexture("sylvan_mini_tank"), 0.5F, 0x689F38, 1.5F));
        event.registerEntityRenderer(ModEntities.RUNIC_BOULDER_GOLEM.get(), ctx -> new PassiveEchoRenderer<RunicBoulderGolem>(
                ctx, layer("runic_boulder_golem"), passiveTexture("runic_boulder_golem"), 0.3F, 0xFFB300, 1.7F));
        event.registerEntityRenderer(ModEntities.AMBER_ROOT_GAZELLE.get(), ctx -> new PassiveEchoRenderer<AmberRootGazelle>(
                ctx, layer("amber_root_gazelle"), passiveTexture("amber_root_gazelle"), 0.3F, 0xFFA000, 1.2F));
        event.registerEntityRenderer(ModEntities.CLOUD_MANTA_RAY.get(), ctx -> new PassiveEchoRenderer<CloudMantaRay>(
                ctx, layer("cloud_manta_ray"), passiveTexture("cloud_manta_ray"), 0.4F, 0xB0BEC5, 1.6F));
        event.registerEntityRenderer(ModEntities.STORM_SENTINEL_OWL.get(), ctx -> new PassiveEchoRenderer<StormSentinelOwl>(
                ctx, layer("storm_sentinel_owl"), passiveTexture("storm_sentinel_owl"), 0.3F, 0x7C4DFF, 1.6F));
        event.registerEntityRenderer(ModEntities.DIMENSIONAL_RIFT_MOTH.get(), ctx -> new PassiveEchoRenderer<DimensionalRiftMoth>(
                ctx, layer("dimensional_rift_moth"), passiveTexture("dimensional_rift_moth"), 0.2F, 0x00E5FF, 1.4F));
    }

    /** Só a adaga possui override de estado (arremessada). Armas não têm variantes elementais. */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
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
