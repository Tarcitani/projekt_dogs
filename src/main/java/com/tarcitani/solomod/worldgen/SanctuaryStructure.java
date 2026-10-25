package com.tarcitani.solomod.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Estrutura de um Santuário de Eco. O JSON escolhe qual com {@code "sanctuary": "<nome>"} (ver {@link SanctuaryKind}).
 * Mede a superfície no centro do chunk e cria uma única {@link SanctuaryPiece}:
 * <ul>
 *   <li>na superfície: chão uma casa abaixo dela; não nasce abaixo do nível do mar;</li>
 *   <li>enterrada: chão {@link #BURY_DEPTH} blocos abaixo da superfície, e a caixa sobe até ela por causa do poço.</li>
 * </ul>
 * Biomas e raridade ficam em data/solomod/worldgen (structure, structure_set) e nas tags {@code has_structure/<nome>}.
 */
public class SanctuaryStructure extends Structure {
    public static final MapCodec<SanctuaryStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            SanctuaryKind.CODEC.fieldOf("sanctuary").forGetter(s -> s.kind)
    ).apply(i, SanctuaryStructure::new));

    /** Profundidade do chão da sala enterrada, abaixo da superfície. */
    public static final int BURY_DEPTH = 22;

    private final SanctuaryKind kind;

    public SanctuaryStructure(StructureSettings settings, SanctuaryKind kind) {
        super(settings);
        this.kind = kind;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        int surface = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        if (surface <= context.chunkGenerator().getSeaLevel()) return Optional.empty();
        int half = SanctuaryPiece.SIZE / 2;
        BoundingBox box;
        if (kind.underground()) {
            int floor = surface - BURY_DEPTH;
            if (floor - 2 <= context.heightAccessor().getMinBuildHeight()) return Optional.empty();
            box = new BoundingBox(x - half, floor, z - half, x + half, surface + 4, z + half);
        } else {
            box = new BoundingBox(x - half, surface - 1, z - half, x + half, surface - 1 + SanctuaryPiece.HEIGHT, z + half);
        }
        BoundingBox finalBox = box;
        return Optional.of(new GenerationStub(new BlockPos(x, surface, z),
                builder -> builder.addPiece(new SanctuaryPiece(kind, finalBox))));
    }

    public SanctuaryKind kind() {
        return kind;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SANCTUARY.get();
    }
}
