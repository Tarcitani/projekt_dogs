package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.passive.EchoRunicBat;
import com.tarcitani.solomod.entity.passive.AbyssalTrenchRemora;
import com.tarcitani.solomod.entity.passive.AmberRootGazelle;
import com.tarcitani.solomod.entity.passive.CloudMantaRay;
import com.tarcitani.solomod.entity.passive.DimensionalRiftMoth;
import com.tarcitani.solomod.entity.passive.StormSentinelOwl;
import com.tarcitani.solomod.entity.passive.RunicBoulderGolem;
import com.tarcitani.solomod.entity.passive.SylvanMiniTank;
import com.tarcitani.solomod.entity.passive.GoldenSilicaScarab;
import com.tarcitani.solomod.entity.passive.LiquidManaAxolotl;
import com.tarcitani.solomod.entity.passive.LotusTurtleCrab;
import com.tarcitani.solomod.entity.passive.IgneousSalamander;
import com.tarcitani.solomod.entity.passive.VolcanicAshColt;
import com.tarcitani.solomod.entity.passive.PassiveEchoEntity;
import com.tarcitani.solomod.entity.passive.RelicAutomaton;
import com.tarcitani.solomod.entity.passive.SpectralScout;
import com.tarcitani.solomod.entity.sanctuary.*;
import com.tarcitani.solomod.mob.TierIdentity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entidades do mod: criaturas, Soberano, adaga e poderes 3D compartilhados pelos Monarcas e Soberano. */
public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, SoloMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<TierMob>> MINION =
            mob("minion", MobTier.NORMAL, 0.7F, 1.35F);
    public static final DeferredHolder<EntityType<?>, EntityType<TierMob>> ELITE =
            mob("elite", MobTier.OP, 0.9F, 2.6F);
    public static final DeferredHolder<EntityType<?>, EntityType<TierMob>> GUARDIAN =
            mob("guardian", MobTier.SUB_BOSS, 1.4F, 3.0F);
    public static final DeferredHolder<EntityType<?>, EntityType<TierMob>> MONARCH =
            mob("monarch", MobTier.BOSS, 2.4F, 4.2F);
    /** O chefe final: gigante, imune a fogo (tem o poder do Rei Vulcânico). */
    public static final DeferredHolder<EntityType<?>, EntityType<Sovereign>> SOVEREIGN =
            ENTITY_TYPES.register("sovereign", () -> EntityType.Builder.<Sovereign>of(Sovereign::new, MobCategory.MONSTER)
                    .sized(Sovereign.HITBOX_WIDTH, Sovereign.HITBOX_HEIGHT).eyeHeight(Sovereign.EYE_HEIGHT)
                    .fireImmune().clientTrackingRange(16).build("sovereign"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownDagger>> THROWN_DAGGER =
            ENTITY_TYPES.register("thrown_dagger", () -> EntityType.Builder.<ThrownDagger>of(ThrownDagger::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F).clientTrackingRange(8).updateInterval(1).build("thrown_dagger"));

    private static DeferredHolder<EntityType<?>, EntityType<TierMob>> mob(String name, MobTier tier, float width, float height) {
        return ENTITY_TYPES.register(name, () -> EntityType.Builder.<TierMob>of(
                        (type, level) -> new TierMob(type, level, tier), MobCategory.MONSTER)
                .sized(width, height).clientTrackingRange(tier == MobTier.BOSS ? 16 : 10).build(name));
    }

    public static final DeferredHolder<EntityType<?>, EntityType<RoyalFireball>> ROYAL_FIREBALL =
            ENTITY_TYPES.register("royal_fireball", () -> EntityType.Builder.<RoyalFireball>of(RoyalFireball::new, MobCategory.MISC)
                    .sized(TierIdentity.ROYAL_FIREBALL_SIZE, TierIdentity.ROYAL_FIREBALL_SIZE)
                    .fireImmune().clientTrackingRange(12).updateInterval(1).build("royal_fireball"));

    /** Tornado do Soberano e do Imperador dos Céus: funil 3D que persegue um jogador. Não é salvo no mundo. */
    public static final DeferredHolder<EntityType<?>, EntityType<SovereignTornado>> SOVEREIGN_TORNADO =
            ENTITY_TYPES.register("sovereign_tornado", () -> EntityType.Builder.<SovereignTornado>of(SovereignTornado::new, MobCategory.MISC)
                    .sized(SovereignTornado.WIDTH, SovereignTornado.HEIGHT).noSave().fireImmune()
                    .clientTrackingRange(12).updateInterval(1).build("sovereign_tornado"));

    /** Esfera do Soberano e do Leviatã: orbita o conjurador e depois persegue um jogador. Não é salva. */
    public static final DeferredHolder<EntityType<?>, EntityType<WaterOrb>> WATER_ORB =
            ENTITY_TYPES.register("water_orb", () -> EntityType.Builder.<WaterOrb>of(WaterOrb::new, MobCategory.MISC)
                    .sized(WaterOrb.SIZE, WaterOrb.SIZE).noSave().fireImmune()
                    .clientTrackingRange(12).updateInterval(1).build("water_orb"));

    /** Fauna Pacífica do Neutro (ver entity/passive). Só existe a forma do Neutro por enquanto. */
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralScout>> SPECTRAL_SCOUT =
            passive("spectral_scout", SpectralScout::new, 0.6F, 1.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<EchoRunicBat>> ECHO_RUNIC_BAT =
            passive("echo_runic_bat", EchoRunicBat::new, 0.5F, 0.5F);
    public static final DeferredHolder<EntityType<?>, EntityType<RelicAutomaton>> RELIC_AUTOMATON =
            passive("relic_automaton", RelicAutomaton::new, 1.2F, 1.0F);

    public static final DeferredHolder<EntityType<?>, EntityType<IgneousSalamander>> IGNEOUS_SALAMANDER =
            passive("igneous_salamander", IgneousSalamander::new, 1.0F, 0.6F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<GoldenSilicaScarab>> GOLDEN_SILICA_SCARAB =
            passive("golden_silica_scarab", GoldenSilicaScarab::new, 0.8F, 0.6F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<VolcanicAshColt>> VOLCANIC_ASH_COLT =
            passive("volcanic_ash_colt", VolcanicAshColt::new, 0.5F, 0.7F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<LiquidManaAxolotl>> LIQUID_MANA_AXOLOTL =
            passive("liquid_mana_axolotl", LiquidManaAxolotl::new, 0.6F, 0.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<LotusTurtleCrab>> LOTUS_TURTLE_CRAB =
            passive("lotus_turtle_crab", LotusTurtleCrab::new, 1.4F, 0.8F);
    public static final DeferredHolder<EntityType<?>, EntityType<AbyssalTrenchRemora>> ABYSSAL_TRENCH_REMORA =
            passive("abyssal_trench_remora", AbyssalTrenchRemora::new, 0.5F, 0.4F);

    public static final DeferredHolder<EntityType<?>, EntityType<SylvanMiniTank>> SYLVAN_MINI_TANK =
            passive("sylvan_mini_tank", SylvanMiniTank::new, 1.2F, 1.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<RunicBoulderGolem>> RUNIC_BOULDER_GOLEM =
            passive("runic_boulder_golem", RunicBoulderGolem::new, 0.8F, 0.8F);
    public static final DeferredHolder<EntityType<?>, EntityType<AmberRootGazelle>> AMBER_ROOT_GAZELLE =
            passive("amber_root_gazelle", AmberRootGazelle::new, 0.7F, 1.2F);

    public static final DeferredHolder<EntityType<?>, EntityType<CloudMantaRay>> CLOUD_MANTA_RAY =
            passive("cloud_manta_ray", CloudMantaRay::new, 1.2F, 0.4F);
    public static final DeferredHolder<EntityType<?>, EntityType<StormSentinelOwl>> STORM_SENTINEL_OWL =
            passive("storm_sentinel_owl", StormSentinelOwl::new, 0.6F, 0.7F);
    public static final DeferredHolder<EntityType<?>, EntityType<DimensionalRiftMoth>> DIMENSIONAL_RIFT_MOTH =
            passive("dimensional_rift_moth", DimensionalRiftMoth::new, 1.2F, 0.6F);

    private static <T extends PassiveEchoEntity> DeferredHolder<EntityType<?>, EntityType<T>> passive(
            String name, EntityType.EntityFactory<T> factory, float width, float height) {
        return passive(name, factory, width, height, false);
    }

    private static <T extends PassiveEchoEntity> DeferredHolder<EntityType<?>, EntityType<T>> passive(
            String name, EntityType.EntityFactory<T> factory, float width, float height, boolean fireImmune) {
        return ENTITY_TYPES.register(name, () -> {
            EntityType.Builder<T> builder = EntityType.Builder.of(factory, MobCategory.CREATURE)
                    .sized(width, height).clientTrackingRange(10);
            return (fireImmune ? builder.fireImmune() : builder).build(name);
        });
    }

    /** Santuário de Fogo (Forja Banhada em Cinzas): chefe e três mobs. Todos imunes a fogo (ver entity/sanctuary). */
    public static final DeferredHolder<EntityType<?>, EntityType<Cinzagrim>> CINZAGRIM =
            sanctuary("cinzagrim", Cinzagrim::new, 1.9F, 3.2F, 16);
    public static final DeferredHolder<EntityType<?>, EntityType<LivingSlag>> LIVING_SLAG =
            sanctuary("living_slag", LivingSlag::new, 0.9F, 1.0F, 10);
    public static final DeferredHolder<EntityType<?>, EntityType<ForgeSpark>> FORGE_SPARK =
            sanctuary("forge_spark", ForgeSpark::new, 0.6F, 0.6F, 10);
    public static final DeferredHolder<EntityType<?>, EntityType<WitheredSmith>> WITHERED_SMITH =
            sanctuary("withered_smith", WitheredSmith::new, 0.7F, 2.0F, 10);

    /** Demais Santuários de Eco (ver docs/GAMEPLAY.md): chefe e três capangas por santuário. */
    public static final DeferredHolder<EntityType<?>, EntityType<Archivist>> ARCHIVIST =
            sanctuary("archivist", Archivist::new, 0.9F, 2.6F, 16, false);
    public static final DeferredHolder<EntityType<?>, EntityType<FlyingTome>> FLYING_TOME =
            sanctuary("flying_tome", FlyingTome::new, 0.7F, 0.5F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InkSentinel>> INK_SENTINEL =
            sanctuary("ink_sentinel", InkSentinel::new, 0.9F, 1.8F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralScribe>> SPECTRAL_SCRIBE =
            sanctuary("spectral_scribe", SpectralScribe::new, 0.7F, 1.8F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<Weeper>> WEEPER =
            sanctuary("weeper", Weeper::new, 1.2F, 3.0F, 16, false);
    public static final DeferredHolder<EntityType<?>, EntityType<ChoirDrowned>> CHOIR_DROWNED =
            sanctuary("choir_drowned", ChoirDrowned::new, 0.6F, 1.95F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<CoralJelly>> CORAL_JELLY =
            sanctuary("coral_jelly", CoralJelly::new, 0.8F, 1.0F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<CoralCrab>> CORAL_CRAB =
            sanctuary("coral_crab", CoralCrab::new, 1.3F, 0.9F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<RootMother>> ROOT_MOTHER =
            sanctuary("root_mother", RootMother::new, 2.0F, 3.6F, 16, false);
    public static final DeferredHolder<EntityType<?>, EntityType<ThornSprout>> THORN_SPROUT =
            sanctuary("thorn_sprout", ThornSprout::new, 0.6F, 0.9F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MossGolem>> MOSS_GOLEM =
            sanctuary("moss_golem", MossGolem::new, 1.2F, 2.0F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<AmberWasp>> AMBER_WASP =
            sanctuary("amber_wasp", AmberWasp::new, 0.6F, 0.6F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<Herald>> HERALD =
            sanctuary("herald", Herald::new, 1.6F, 1.6F, 16, false);
    public static final DeferredHolder<EntityType<?>, EntityType<GrayHarpy>> GRAY_HARPY =
            sanctuary("gray_harpy", GrayHarpy::new, 0.9F, 1.0F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<GaleTotem>> GALE_TOTEM =
            sanctuary("gale_totem", GaleTotem::new, 0.9F, 2.4F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<BreezeElemental>> BREEZE_ELEMENTAL =
            sanctuary("breeze_elemental", BreezeElemental::new, 0.7F, 1.4F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<EchoHunter>> ECHO_HUNTER =
            sanctuary("echo_hunter", EchoHunter::new, 0.8F, 2.1F, 16, false);
    public static final DeferredHolder<EntityType<?>, EntityType<WailingShade>> WAILING_SHADE =
            sanctuary("wailing_shade", WailingShade::new, 0.7F, 1.6F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<WhisperingBones>> WHISPERING_BONES =
            sanctuary("whispering_bones", WhisperingBones::new, 0.6F, 1.95F, 10, false);
    public static final DeferredHolder<EntityType<?>, EntityType<CreepingShadow>> CREEPING_SHADOW =
            sanctuary("creeping_shadow", CreepingShadow::new, 0.7F, 1.2F, 10, false);

    private static <T extends Monster> DeferredHolder<EntityType<?>, EntityType<T>> sanctuary(
            String name, EntityType.EntityFactory<T> factory, float width, float height, int trackingRange) {
        return sanctuary(name, factory, width, height, trackingRange, true);
    }

    private static <T extends Monster> DeferredHolder<EntityType<?>, EntityType<T>> sanctuary(
            String name, EntityType.EntityFactory<T> factory, float width, float height, int trackingRange,
            boolean fireImmune) {
        return ENTITY_TYPES.register(name, () -> {
            EntityType.Builder<T> builder = EntityType.Builder.of(factory, MobCategory.MONSTER)
                    .sized(width, height).clientTrackingRange(trackingRange);
            return (fireImmune ? builder.fireImmune() : builder).build(name);
        });
    }

    public static EntityType<TierMob> forTier(MobTier tier) {
        return switch (tier) {
            case NORMAL -> MINION.get();
            case OP -> ELITE.get();
            case SUB_BOSS -> GUARDIAN.get();
            case BOSS -> MONARCH.get();
        };
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(ModEntities::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(MINION.get(), TierMob.attributes(MobTier.NORMAL).build());
        event.put(ELITE.get(), TierMob.attributes(MobTier.OP).build());
        event.put(GUARDIAN.get(), TierMob.attributes(MobTier.SUB_BOSS).build());
        event.put(MONARCH.get(), TierMob.attributes(MobTier.BOSS).build());
        event.put(SOVEREIGN.get(), Sovereign.sovereignAttributes().build());
        event.put(CINZAGRIM.get(), Cinzagrim.attributes().build());
        event.put(LIVING_SLAG.get(), LivingSlag.attributes().build());
        event.put(FORGE_SPARK.get(), ForgeSpark.attributes().build());
        event.put(WITHERED_SMITH.get(), WitheredSmith.attributes().build());
        event.put(ARCHIVIST.get(), Archivist.attributes().build());
        event.put(FLYING_TOME.get(), FlyingTome.attributes().build());
        event.put(INK_SENTINEL.get(), InkSentinel.attributes().build());
        event.put(SPECTRAL_SCRIBE.get(), SpectralScribe.attributes().build());
        event.put(WEEPER.get(), Weeper.attributes().build());
        event.put(CHOIR_DROWNED.get(), ChoirDrowned.attributes().build());
        event.put(CORAL_JELLY.get(), CoralJelly.attributes().build());
        event.put(CORAL_CRAB.get(), CoralCrab.attributes().build());
        event.put(ROOT_MOTHER.get(), RootMother.attributes().build());
        event.put(THORN_SPROUT.get(), ThornSprout.attributes().build());
        event.put(MOSS_GOLEM.get(), MossGolem.attributes().build());
        event.put(AMBER_WASP.get(), AmberWasp.attributes().build());
        event.put(HERALD.get(), Herald.attributes().build());
        event.put(GRAY_HARPY.get(), GrayHarpy.attributes().build());
        event.put(GALE_TOTEM.get(), GaleTotem.attributes().build());
        event.put(BREEZE_ELEMENTAL.get(), BreezeElemental.attributes().build());
        event.put(ECHO_HUNTER.get(), EchoHunter.attributes().build());
        event.put(WAILING_SHADE.get(), WailingShade.attributes().build());
        event.put(WHISPERING_BONES.get(), WhisperingBones.attributes().build());
        event.put(CREEPING_SHADOW.get(), CreepingShadow.attributes().build());
        event.put(SPECTRAL_SCOUT.get(), SpectralScout.attributes().build());
        event.put(ECHO_RUNIC_BAT.get(), EchoRunicBat.attributes().build());
        event.put(RELIC_AUTOMATON.get(), RelicAutomaton.attributes().build());
        event.put(IGNEOUS_SALAMANDER.get(), IgneousSalamander.attributes().build());
        event.put(GOLDEN_SILICA_SCARAB.get(), GoldenSilicaScarab.attributes().build());
        event.put(VOLCANIC_ASH_COLT.get(), VolcanicAshColt.attributes().build());
        event.put(LIQUID_MANA_AXOLOTL.get(), LiquidManaAxolotl.attributes().build());
        event.put(LOTUS_TURTLE_CRAB.get(), LotusTurtleCrab.attributes().build());
        event.put(ABYSSAL_TRENCH_REMORA.get(), AbyssalTrenchRemora.attributes().build());
        event.put(SYLVAN_MINI_TANK.get(), SylvanMiniTank.attributes().build());
        event.put(RUNIC_BOULDER_GOLEM.get(), RunicBoulderGolem.attributes().build());
        event.put(AMBER_ROOT_GAZELLE.get(), AmberRootGazelle.attributes().build());
        event.put(CLOUD_MANTA_RAY.get(), CloudMantaRay.attributes().build());
        event.put(STORM_SENTINEL_OWL.get(), StormSentinelOwl.attributes().build());
        event.put(DIMENSIONAL_RIFT_MOTH.get(), DimensionalRiftMoth.attributes().build());
    }
}
