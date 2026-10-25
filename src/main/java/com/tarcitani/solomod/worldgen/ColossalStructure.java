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
 * Estrutura de um lugar colossal ({@link ColossalKind}): os tronos dos Monarcas e o Anfiteatro do Selo Partido.
 * O JSON escolhe qual com {@code "colossus": "<nome>"}. A base depende do tipo:
 * <ul>
 *   <li>superfície: o chão fica uma casa abaixo da superfície; não nasce abaixo do nível do mar;</li>
 *   <li>fundo do mar: o chão do cubo seco é o leito do oceano, e precisa de pelo menos {@link #MIN_TRENCH_DEPTH} de
 *       água por cima (senão o teto de vidro furaria a superfície);</li>
 *   <li>vazio: base fixa em {@link #VOID_BASE_Y} (o Anfiteatro paira sobre o vazio do End), ou mais alto se houver
 *       ilha embaixo.</li>
 * </ul>
 */
public class ColossalStructure extends Structure {
    public static final MapCodec<ColossalStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            ColossalKind.CODEC.fieldOf("colossus").forGetter(s -> s.kind)
    ).apply(i, ColossalStructure::new));

    private static final int MIN_TRENCH_DEPTH = 32;
    public static final int VOID_BASE_Y = 110;

    private final ColossalKind kind;

    public ColossalStructure(StructureSettings settings, ColossalKind kind) {
        super(settings);
        this.kind = kind;
    }

    public ColossalKind kind() {
        return kind;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        int sea = context.chunkGenerator().getSeaLevel();
        int base;
        switch (kind.placement()) {
            case SURFACE -> {
                int surface = height(context, x, z, Heightmap.Types.WORLD_SURFACE_WG);
                if (surface <= sea) return Optional.empty();
                base = surface - 1;
            }
            case OCEAN_FLOOR -> {
                int floor = height(context, x, z, Heightmap.Types.OCEAN_FLOOR_WG);
                if (sea - floor < MIN_TRENCH_DEPTH) return Optional.empty();
                base = floor;
            }
            default -> {
                int top = height(context, x, z, Heightmap.Types.WORLD_SURFACE_WG);
                base = Math.max(VOID_BASE_Y, top + 40);
            }
        }
        int max = context.heightAccessor().getMaxBuildHeight() - 1;
        if (base + kind.above() > max || base - kind.below() <= context.heightAccessor().getMinBuildHeight()) {
            return Optional.empty();
        }
        int w = kind.halfWidth();
        BoundingBox box = new BoundingBox(x - w, base - kind.below(), z - w, x + w, base + kind.above(), z + w);
        BlockPos center = new BlockPos(x, base, z);
        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(new ColossalPiece(kind, box, center))));
    }

    private static int height(GenerationContext context, int x, int z, Heightmap.Types type) {
        return context.chunkGenerator().getFirstOccupiedHeight(x, z, type, context.heightAccessor(), context.randomState());
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.COLOSSAL.get();
    }
}
