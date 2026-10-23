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
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoloMod.MOD_ID);

    public static final Map<WeaponClass, DeferredItem<ModularWeaponItem>> WEAPONS = new EnumMap<>(WeaponClass.class);
    /** Ovos: tier -> elemento -> item. O de elemento NONE nasce sem elemento. */
    public static final Map<MobTier, Map<ElementType, DeferredItem<DeferredSpawnEggItem>>> EGGS = new EnumMap<>(MobTier.class);
    /** O Soberano não tem elemento: um ovo só (preto com manchas douradas). */
    public static final DeferredItem<DeferredSpawnEggItem> SOVEREIGN_EGG = ITEMS.register("sovereign_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SOVEREIGN, 0x1A1020, 0xE8C547, new Item.Properties()));

    static {
        for (WeaponClass wc : WeaponClass.values()) {
            WEAPONS.put(wc, ITEMS.register(wc.getSerializedName(), () -> new ModularWeaponItem(wc, new Item.Properties())));
        }
        egg(MobTier.NORMAL, "minion", ModEntities.MINION, 0x5B2A4A);
        egg(MobTier.OP, "elite", ModEntities.ELITE, 0x262433);
        egg(MobTier.SUB_BOSS, "guardian", ModEntities.GUARDIAN, 0x173A4D);
        egg(MobTier.BOSS, "monarch", ModEntities.MONARCH, 0x4A4540);
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
                output.accept(new ItemStack(SOVEREIGN_EGG.get()));
            })
            .build());

    /** Quatro armas sem duplicatas de tier/elemento; variantes continuam disponíveis pelo /modtest. */
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
