package com.tarcitani.solomod.worldgen;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.mob.TierIdentity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Peça única de um lugar colossal. Em cada pedaço de mundo gerado, percorre só a interseção da caixa com aquele
 * pedaço e pede o bloco de cada ponto a {@link ColossalShapes} (posição relativa à base). Escreve direto em
 * coordenadas do mundo (sem rotação nem espelho: as formas já são simétricas).
 *
 * <p>Nos tronos nasce o Monarca do elemento certo, persistente, no ponto de {@link ColossalShapes#monarchSpot}. No
 * Berço das Horas de Âmbar, cada esfera guarda uma criatura congelada da Era II (sem IA, invulnerável e muda). O
 * Anfiteatro não gera o Soberano: ele é chamado por {@code StructureAmbientHandler} quando alguém que já partiu o
 * Selo chega ao centro.
 */
public class ColossalPiece extends StructurePiece {
    private static final String KIND_TAG = "Colossus";
    private static final String CENTER_TAG = "Center";

    private final ColossalKind kind;
    private final BlockPos center;

    public ColossalPiece(ColossalKind kind, BoundingBox box, BlockPos center) {
        super(ModStructures.COLOSSAL_PIECE.get(), 0, box);
        setOrientation(null);
        this.kind = kind;
        this.center = center;
    }

    public ColossalPiece(CompoundTag tag) {
        super(ModStructures.COLOSSAL_PIECE.get(), tag);
        this.kind = ColossalKind.byName(tag.getString(KIND_TAG));
        this.center = NbtUtils.readBlockPos(tag, CENTER_TAG).orElse(getBoundingBox().getCenter());
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString(KIND_TAG, kind.getSerializedName());
        tag.put(CENTER_TAG, NbtUtils.writeBlockPos(center));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BoundingBox box = getBoundingBox();
        int minX = Math.max(box.minX(), chunkBox.minX()), maxX = Math.min(box.maxX(), chunkBox.maxX());
        int minZ = Math.max(box.minZ(), chunkBox.minZ()), maxZ = Math.min(box.maxZ(), chunkBox.maxZ());
        int minY = Math.max(box.minY(), chunkBox.minY()), maxY = Math.min(box.maxY(), chunkBox.maxY());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockState state = ColossalShapes.at(kind, x - center.getX(), y - center.getY(), z - center.getZ());
                    if (state != null) level.setBlock(pos.set(x, y, z), state, 2);
                }
            }
        }
        ElementType monarch = kind.monarch();
        if (monarch != null) spawnMonarch(level, random, chunkBox, monarch);
        if (kind == ColossalKind.AMBER_HOURS_CRADLE) {
            for (int[] s : ColossalShapes.AMBER_SPHERES) spawnFrozen(level, chunkBox, s);
        }
    }

    private void spawnMonarch(WorldGenLevel level, RandomSource random, BoundingBox chunkBox, ElementType element) {
        int[] spot = ColossalShapes.monarchSpot(kind);
        BlockPos pos = center.offset(spot[0], spot[1], spot[2]);
        if (!chunkBox.isInside(pos)) return;
        TierMob monarch = ModEntities.MONARCH.get().create(level.getLevel());
        if (monarch == null) return;
        monarch.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        // O perfil vem antes do finalizeSpawn: com ele, a criatura não sorteia outro elemento.
        TierIdentity.apply(monarch, MobTier.BOSS, element);
        monarch.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        monarch.setPersistenceRequired();
        level.addFreshEntityWithPassengers(monarch);
    }

    /** Criatura da Era II presa no âmbar: sem IA, invulnerável, muda e persistente. */
    private void spawnFrozen(WorldGenLevel level, BoundingBox chunkBox, int[] sphere) {
        BlockPos pos = center.offset(sphere[0], sphere[1] - 1, sphere[2]);
        if (!chunkBox.isInside(pos)) return;
        Mob frozen = EntityType.WITHER_SKELETON.create(level.getLevel());
        if (frozen == null) return;
        frozen.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, sphere[0] * 10.0F, 0.0F);
        frozen.setNoAi(true);
        frozen.setInvulnerable(true);
        frozen.setSilent(true);
        frozen.setPersistenceRequired();
        level.addFreshEntity(frozen);
    }
}
