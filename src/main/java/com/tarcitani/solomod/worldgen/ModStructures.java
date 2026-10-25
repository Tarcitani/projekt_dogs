package com.tarcitani.solomod.worldgen;

import com.tarcitani.solomod.SoloMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registros de geração de mundo: os Santuários de Eco ({@code sanctuary}) e os lugares colossais ({@code colossal}:
 * tronos e Anfiteatro). Qual deles nasce é escolhido no JSON da estrutura ({@code "sanctuary"} ou {@code "colossus"}).
 */
public final class ModStructures {
    private ModStructures() {}

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, SoloMod.MOD_ID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, SoloMod.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<SanctuaryStructure>> SANCTUARY =
            STRUCTURE_TYPES.register("sanctuary", () -> () -> SanctuaryStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SANCTUARY_PIECE =
            PIECE_TYPES.register("sanctuary", () -> (StructurePieceType.ContextlessType) SanctuaryPiece::new);
    /** Tronos dos Monarcas e o Anfiteatro do Selo Partido (qual: campo "colossus" do JSON). */
    public static final DeferredHolder<StructureType<?>, StructureType<ColossalStructure>> COLOSSAL =
            STRUCTURE_TYPES.register("colossal", () -> () -> ColossalStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> COLOSSAL_PIECE =
            PIECE_TYPES.register("colossal", () -> (StructurePieceType.ContextlessType) ColossalPiece::new);

    public static void register(IEventBus modBus) {
        STRUCTURE_TYPES.register(modBus);
        PIECE_TYPES.register(modBus);
    }
}
