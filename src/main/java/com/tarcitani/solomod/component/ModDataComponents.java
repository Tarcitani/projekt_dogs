package com.tarcitani.solomod.component;

import com.tarcitani.solomod.SoloMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

/** Registro dos Data Components do mod. */
public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SoloMod.MOD_ID);

    /**
     * {@code solomod:weapon_data}.
     * persistent = salva no disco; networkSynchronized = vai para o cliente (tooltip).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponData>> WEAPON_DATA =
            DATA_COMPONENTS.register("weapon_data", () -> DataComponentType.<WeaponData>builder()
                    .persistent(WeaponData.CODEC)
                    .networkSynchronized(WeaponData.STREAM_CODEC)
                    .build());

    /**
     * {@code solomod:thrown_dagger}: presente enquanto a adaga está fora da mão (UUID da entidade arremessada).
     * O item continua no slot como "fantasma" para que o clique direito possa chamá-la de volta.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> THROWN_DAGGER =
            DATA_COMPONENTS.register("thrown_dagger", () -> DataComponentType.<UUID>builder()
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC)
                    .build());

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
