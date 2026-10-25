package com.tarcitani.solomod.item;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.TierMob;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Itens do mod: uma arma por classe (solomod:greatsword, solomod:dagger...), um ovo por criatura e elemento,
 * o ovo do Soberano e os rostos dos seis chefes em pixel art. A aba Solo Mod reúne criaturas;
 * a aba Armas contém só rapieira, espada longa, adaga e martelo, uma unidade neutra de cada.
 */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SoloMod.MOD_ID);
    /** Rostos das conquistas; arte e modelos gerados por tools/gen_boss_icons.py. */
    public static final DeferredItem<Item> MONARCH_NONE_HEAD = ITEMS.registerSimpleItem("monarch_none_head");
    public static final DeferredItem<Item> MONARCH_FIRE_HEAD = ITEMS.registerSimpleItem("monarch_fire_head");
    public static final DeferredItem<Item> MONARCH_WATER_HEAD = ITEMS.registerSimpleItem("monarch_water_head");
    public static final DeferredItem<Item> MONARCH_EARTH_HEAD = ITEMS.registerSimpleItem("monarch_earth_head");
    public static final DeferredItem<Item> MONARCH_AIR_HEAD = ITEMS.registerSimpleItem("monarch_air_head");
    /** Ícones da conquista do Soberano: "???" antes de derrotá-lo e a cabeça dele depois. Só aparecem nas conquistas. */
    public static final DeferredItem<Item> UNKNOWN_BOSS = ITEMS.registerSimpleItem("unknown_boss");
    public static final DeferredItem<Item> SOVEREIGN_HEAD = ITEMS.registerSimpleItem("sovereign_head");
    /** Era VII: cai do Soberano. Indestrutível e sem uso por enquanto (gancho da próxima atualização). */
    public static final DeferredItem<DissonanceShardItem> DISSONANCE_SHARD = ITEMS.register("dissonance_shard",
            () -> new DissonanceShardItem(new Item.Properties()));
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoloMod.MOD_ID);

    public static final Map<WeaponClass, DeferredItem<ModularWeaponItem>> WEAPONS = new EnumMap<>(WeaponClass.class);
    /** Ovos: tier -> elemento -> item. O de elemento NONE nasce sem elemento. */
    public static final Map<MobTier, Map<ElementType, DeferredItem<DeferredSpawnEggItem>>> EGGS = new EnumMap<>(MobTier.class);
    /** O Soberano não tem elemento: um ovo só (preto com manchas douradas). */
    public static final DeferredItem<DeferredSpawnEggItem> SOVEREIGN_EGG = ITEMS.register("sovereign_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SOVEREIGN, 0x1A1020, 0xE8C547, new Item.Properties()));

    /** Ovos da Fauna Pacífica (cor base e manchas, sem elemento gravado). Neutro, Fogo e Água. */
    public static final Map<String, DeferredItem<DeferredSpawnEggItem>> PASSIVE_EGGS = new LinkedHashMap<>();

    /** Ovos dos Santuários de Eco (chefe e mobs), na ordem de exibição na aba. */
    public static final Map<String, DeferredItem<DeferredSpawnEggItem>> SANCTUARY_EGGS = new LinkedHashMap<>();

    static {
        for (WeaponClass wc : WeaponClass.values()) {
            WEAPONS.put(wc, ITEMS.register(wc.getSerializedName(), () -> new ModularWeaponItem(wc, new Item.Properties())));
        }
        egg(MobTier.NORMAL, "minion", ModEntities.MINION, 0x5B2A4A);
        egg(MobTier.OP, "elite", ModEntities.ELITE, 0x262433);
        egg(MobTier.SUB_BOSS, "guardian", ModEntities.GUARDIAN, 0x173A4D);
        egg(MobTier.BOSS, "monarch", ModEntities.MONARCH, 0x4A4540);
        sanctuaryEgg("cinzagrim", ModEntities.CINZAGRIM, 0x2B2220, 0xFF6A00);
        sanctuaryEgg("living_slag", ModEntities.LIVING_SLAG, 0x3A1F14, 0xFF9A1F);
        sanctuaryEgg("forge_spark", ModEntities.FORGE_SPARK, 0xFF7A1A, 0xFFE08A);
        sanctuaryEgg("withered_smith", ModEntities.WITHERED_SMITH, 0x4A4038, 0xE0541A);
        sanctuaryEgg("archivist", ModEntities.ARCHIVIST, 0x2A2238, 0xB38CFF);
        sanctuaryEgg("flying_tome", ModEntities.FLYING_TOME, 0x5A3A22, 0xD9C9A3);
        sanctuaryEgg("ink_sentinel", ModEntities.INK_SENTINEL, 0x1B1B26, 0x4A3AA8);
        sanctuaryEgg("spectral_scribe", ModEntities.SPECTRAL_SCRIBE, 0x6E6A8F, 0xE6E0FF);
        sanctuaryEgg("weeper", ModEntities.WEEPER, 0x1F4E5F, 0xFF8FA3);
        sanctuaryEgg("choir_drowned", ModEntities.CHOIR_DROWNED, 0x2E6B63, 0xF07A8A);
        sanctuaryEgg("coral_jelly", ModEntities.CORAL_JELLY, 0xFF9EC4, 0x7FE0F0);
        sanctuaryEgg("coral_crab", ModEntities.CORAL_CRAB, 0xD9534F, 0xF7D08A);
        sanctuaryEgg("root_mother", ModEntities.ROOT_MOTHER, 0x4A3A2A, 0x8BC34A);
        sanctuaryEgg("thorn_sprout", ModEntities.THORN_SPROUT, 0x4C7A2B, 0xC9E07A);
        sanctuaryEgg("moss_golem", ModEntities.MOSS_GOLEM, 0x6B6B6B, 0x5E8C31);
        sanctuaryEgg("amber_wasp", ModEntities.AMBER_WASP, 0xFFB300, 0x3A2A10);
        sanctuaryEgg("herald", ModEntities.HERALD, 0xCFD8DC, 0x4FC3F7);
        sanctuaryEgg("gray_harpy", ModEntities.GRAY_HARPY, 0x78808A, 0xE8E0D0);
        sanctuaryEgg("gale_totem", ModEntities.GALE_TOTEM, 0x8A8F99, 0x9FE8FF);
        sanctuaryEgg("breeze_elemental", ModEntities.BREEZE_ELEMENTAL, 0xD6F3FF, 0x6FC8E8);
        sanctuaryEgg("echo_hunter", ModEntities.ECHO_HUNTER, 0x1A1A1F, 0xFFD54F);
        sanctuaryEgg("wailing_shade", ModEntities.WAILING_SHADE, 0x3A3F55, 0xA8F0E6);
        sanctuaryEgg("whispering_bones", ModEntities.WHISPERING_BONES, 0xC8C0AA, 0x5A6B7A);
        sanctuaryEgg("creeping_shadow", ModEntities.CREEPING_SHADOW, 0x101014, 0x7A2BFF);
        passiveEgg("spectral_scout", ModEntities.SPECTRAL_SCOUT, 0x1A1A24, 0x00E5FF);
        passiveEgg("echo_runic_bat", ModEntities.ECHO_RUNIC_BAT, 0x3B4249, 0x1E88E5);
        passiveEgg("relic_automaton", ModEntities.RELIC_AUTOMATON, 0x5A5D60, 0xD4AF37);
        passiveEgg("igneous_salamander", ModEntities.IGNEOUS_SALAMANDER, 0x211A1E, 0xFF5722);
        passiveEgg("golden_silica_scarab", ModEntities.GOLDEN_SILICA_SCARAB, 0xFFD54F, 0xFF8F00);
        passiveEgg("volcanic_ash_colt", ModEntities.VOLCANIC_ASH_COLT, 0x423D3B, 0xFF3D00);
        passiveEgg("liquid_mana_axolotl", ModEntities.LIQUID_MANA_AXOLOTL, 0x80DEEA, 0xE0F7FA);
        passiveEgg("lotus_turtle_crab", ModEntities.LOTUS_TURTLE_CRAB, 0x004D40, 0xE040FB);
        passiveEgg("abyssal_trench_remora", ModEntities.ABYSSAL_TRENCH_REMORA, 0x90A4AE, 0x00B8D4);
        passiveEgg("sylvan_mini_tank", ModEntities.SYLVAN_MINI_TANK, 0x3E2723, 0x689F38);
        passiveEgg("runic_boulder_golem", ModEntities.RUNIC_BOULDER_GOLEM, 0x4E342E, 0xFFB300);
        passiveEgg("amber_root_gazelle", ModEntities.AMBER_ROOT_GAZELLE, 0x8D6E63, 0xFFA000);
        passiveEgg("cloud_manta_ray", ModEntities.CLOUD_MANTA_RAY, 0xE1F5FE, 0xB0BEC5);
        passiveEgg("storm_sentinel_owl", ModEntities.STORM_SENTINEL_OWL, 0x37474F, 0x7C4DFF);
        passiveEgg("dimensional_rift_moth", ModEntities.DIMENSIONAL_RIFT_MOTH, 0x120024, 0x00E5FF);
    }

    private static void sanctuaryEgg(String name, Supplier<? extends EntityType<? extends Mob>> type, int bodyColor, int spotColor) {
        SANCTUARY_EGGS.put(name, ITEMS.register(name + "_spawn_egg",
                () -> new DeferredSpawnEggItem(type, bodyColor, spotColor, new Item.Properties())));
    }

    private static void passiveEgg(String name, Supplier<? extends EntityType<? extends Mob>> type, int bodyColor, int spotColor) {
        PASSIVE_EGGS.put(name, ITEMS.register(name + "_spawn_egg",
                () -> new DeferredSpawnEggItem(type, bodyColor, spotColor, new Item.Properties())));
    }

    /** Ovo com o elemento gravado nos dados da entidade (lido por TierMob.readAdditionalSaveData). */
    private static void egg(MobTier tier, String name, Supplier<? extends EntityType<TierMob>> type, int bodyColor) {
        Map<ElementType, DeferredItem<DeferredSpawnEggItem>> byElement = new EnumMap<>(ElementType.class);
        for (ElementType element : ElementType.values()) {
            String id = element == ElementType.NONE ? name + "_spawn_egg" : name + "_" + element.getSerializedName() + "_spawn_egg";
            int spots = element == ElementType.NONE ? 0xD4D4D4 : spotColor(element);
            byElement.put(element, ITEMS.register(id, () -> {
                CompoundTag tag = new CompoundTag();
                tag.putString("id", SoloMod.MOD_ID + ":" + name);
                tag.putString(TierMob.EGG_ELEMENT_TAG, element.getSerializedName());
                return new DeferredSpawnEggItem(type, bodyColor, spots,
                        new Item.Properties().component(DataComponents.ENTITY_DATA, CustomData.of(tag)));
            }));
        }
        EGGS.put(tier, byElement);
    }

    private static int spotColor(ElementType element) {
        return switch (element) {
            case WATER -> 0x3FA9F5;
            case EARTH -> 0x8BC34A;
            case FIRE -> 0xFF6A1F;
            case AIR -> 0xE6F7FF;
            case NONE -> 0xD4D4D4;
        };
    }

    /**
     * Aba "Solo Mod": ovos de todas as criaturas; as armas ficam em uma aba separada.
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("solomod", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.solomod"))
            .icon(() -> new ItemStack(SOVEREIGN_EGG.get()))
            .displayItems((params, output) -> {
                for (MobTier tier : MobTier.values()) {
                    EGGS.get(tier).values().forEach(egg -> output.accept(new ItemStack(egg.get())));
                }
                PASSIVE_EGGS.values().forEach(egg -> output.accept(new ItemStack(egg.get())));
                SANCTUARY_EGGS.values().forEach(egg -> output.accept(new ItemStack(egg.get())));
                output.accept(new ItemStack(SOVEREIGN_EGG.get()));
                output.accept(new ItemStack(DISSONANCE_SHARD.get()));
            })
            .build());

    /** Quatro armas normais sem variantes de tier/elemento; /modtest também gera somente essas versões. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEAPONS_TAB = TABS.register("weapons", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.solomod.weapons"))
            .icon(() -> new ItemStack(get(WeaponClass.HAMMER)))
            .displayItems((params, output) -> WeaponClass.AVAILABLE.forEach(wc ->
                    output.accept(ModularWeaponItem.create(get(wc), WeaponData.of(wc, MobTier.NORMAL, ElementType.NONE)))))
            .build());

    public static ModularWeaponItem get(WeaponClass wc) {
        return WEAPONS.get(wc).get();
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
