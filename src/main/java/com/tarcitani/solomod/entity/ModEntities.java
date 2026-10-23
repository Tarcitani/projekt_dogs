package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.mob.TierIdentity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entidades do mod: criaturas, Soberano, adaga arremessada e bola de fogo real rebatível. */
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
                    .sized(3.4F, 6.6F).fireImmune().clientTrackingRange(16).build("sovereign"));

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
    }
}
