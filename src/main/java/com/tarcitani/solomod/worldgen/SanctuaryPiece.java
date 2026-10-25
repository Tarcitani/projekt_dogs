package com.tarcitani.solomod.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Peça única de um Santuário de Eco: plataforma redonda de 29x29 com arena central, anel (fosso com quatro pontes, ou
 * seco), pátio, muralha de quatro portões e quatro pilares nas diagonais. A paleta e a guarnição vêm do
 * {@link SanctuaryKind}. Um baú no pátio guarda o saque menor ({@code solomod:chests/<santuário>}); o grande vem do
 * chefe.
 *
 * <p>Coordenadas locais de 0 a {@link #SIZE}-1; y = 0 é o chão. Na superfície a peça preenche o terreno por baixo e
 * limpa o ar por cima. Enterrada (Catacumba), vira uma sala fechada de {@link #ROOM_HEIGHT} de altura, com teto, sem
 * portões, e um poço com escada no lado norte que sobe até a superfície. Os mobs nascem com a estrutura (só no
 * pedaço de mundo que contém o ponto) e ficam persistentes.
 */
public class SanctuaryPiece extends StructurePiece {
    public static final int SIZE = 29;
    /** Altura da caixa das versões na superfície (os pilares da Torre chegam a 12). */
    public static final int HEIGHT = 14;
    /** Altura interna da sala enterrada, do chão ao teto. */
    public static final int ROOM_HEIGHT = 8;
    private static final int CENTER = SIZE / 2;
    private static final double PLATFORM_RADIUS = 13.5;
    private static final double WALL_INNER = 12.0;
    private static final double RING_INNER = 6.0;
    private static final double RING_OUTER = 8.0;
    private static final String KIND_TAG = "Sanctuary";
    /** Lágrimas das Marés, árvores-nervo do Jardim (deslocamentos a partir do centro) e a torre fatiada. */
    static final int[][] TEARS = {{9, 0}, {-6, 7}, {-5, -8}};
    static final int[][] NERVE_TREES = {{-5, 10}, {5, -10}, {-10, -5}, {10, 6}};
    static final int TOWER_SEGMENTS = 7;
    static final int BELL_Y = 5 + TOWER_SEGMENTS;
    private static final int[][] PLUS = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final SanctuaryKind kind;

    public SanctuaryPiece(SanctuaryKind kind, BoundingBox box) {
        super(ModStructures.SANCTUARY_PIECE.get(), 0, box);
        // Sem orientação o espelho fica nulo e placeBlock quebra em blocos espelháveis (muros, escadas).
        // null = sem rotação nem espelho: a planta é simétrica.
        setOrientation(null);
        this.kind = kind;
    }

    public SanctuaryPiece(CompoundTag tag) {
        super(ModStructures.SANCTUARY_PIECE.get(), tag);
        this.kind = SanctuaryKind.byName(tag.getString(KIND_TAG));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString(KIND_TAG, kind.getSerializedName());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        SanctuaryKind.Palette p = kind.palette();
        int clearTop = kind.underground() ? ROOM_HEIGHT : HEIGHT;
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int dx = x - CENTER, dz = z - CENTER;
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > PLATFORM_RADIUS) continue;
                for (int y = 1; y < clearTop; y++) placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, chunkBox);
                buildColumn(level, random, chunkBox, p, x, z, dx, dz, r);
                if (kind.underground()) {
                    placeBlock(level, p.wall().defaultBlockState(), x, ROOM_HEIGHT, z, chunkBox);
                }
            }
        }
        buildPillars(level, chunkBox, p);
        buildDecor(level, chunkBox, p);
        buildRealityFault(level, chunkBox);
        if (kind.underground()) buildShaft(level, chunkBox, p);
        createChest(level, chunkBox, random, CENTER + 4, 1, CENTER - 10, chestLoot());
        spawnGarrison(level, random, chunkBox);
    }

    private ResourceKey<LootTable> chestLoot() {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("solomod", "chests/" + kind.getSerializedName()));
    }

    /** Chão (arena, anel, pátio) e, na borda, a muralha de uma coluna da plataforma. */
    private void buildColumn(WorldGenLevel level, RandomSource random, BoundingBox box, SanctuaryKind.Palette p,
                             int x, int z, int dx, int dz, double r) {
        boolean bridge = Math.abs(dx) <= 1 || Math.abs(dz) <= 1;
        BlockState below = p.wall().defaultBlockState();
        if (r >= RING_INNER && r < RING_OUTER && p.moat() != null && !bridge) {
            placeBlock(level, p.moat(), x, 0, z, box);
            fillColumnDown(level, below, x, -1, z, box);
            return;
        }
        BlockState floor;
        if (r < RING_INNER) {
            floor = (random.nextInt(5) == 0 ? p.arenaAlt() : p.arena()).defaultBlockState();
        } else if (r < RING_OUTER) {
            floor = p.ring().defaultBlockState();
        } else {
            BlockState[] yard = p.yard();
            int roll = random.nextInt(20);
            floor = roll == 0 ? yard[yard.length - 1] : yard[roll % (yard.length - 1)];
        }
        placeBlock(level, floor, x, 0, z, box);
        fillColumnDown(level, below, x, -1, z, box);

        if (r >= WALL_INNER) {
            // Na superfície os portões cardeais ficam abertos; enterrada, a sala é fechada (a entrada é o poço).
            boolean gate = !kind.underground() && (Math.abs(dx) <= 2 || Math.abs(dz) <= 2);
            if (gate) return;
            int height = kind.underground() ? ROOM_HEIGHT - 1 : p.wallHeight();
            for (int y = 1; y <= height; y++) {
                placeBlock(level, (random.nextInt(6) == 0 ? p.wallAlt() : p.wall()).defaultBlockState(), x, y, z, box);
            }
            if (p.crenel() != null && (x + z) % 2 == 0) placeBlock(level, p.crenel(), x, height + 1, z, box);
        } else if (r >= WALL_INNER - 1 && p.lining() != null && !(Math.abs(dx) <= 2 || Math.abs(dz) <= 2)) {
            placeBlock(level, p.lining(), x, 1, z, box);
            placeBlock(level, p.lining(), x, 2, z, box);
        }
    }

    /** Quatro pilares nas diagonais do pátio, com o topo e o "fogo" da paleta. */
    private void buildPillars(WorldGenLevel level, BoundingBox box, SanctuaryKind.Palette p) {
        int[][] spots = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] s : spots) {
            int x = CENTER + s[0], z = CENTER + s[1];
            for (int y = 1; y <= p.pillarHeight(); y++) placeBlock(level, p.pillar().defaultBlockState(), x, y, z, box);
            placeBlock(level, p.pillarTop(), x, p.pillarHeight() + 1, z, box);
            if (p.pillarFire() != null) placeBlock(level, p.pillarFire(), x, p.pillarHeight() + 2, z, box);
        }
    }

    /** Quatro enfeites em volta do centro da arena (oeste, leste, norte, sul). */
    private void buildDecor(WorldGenLevel level, BoundingBox box, SanctuaryKind.Palette p) {
        BlockState[] d = p.decor();
        placeBlock(level, d[0], CENTER - 4, 1, CENTER, box);
        placeBlock(level, d[1], CENTER + 4, 1, CENTER, box);
        placeBlock(level, d[2], CENTER, 1, CENTER - 4, box);
        placeBlock(level, d[3], CENTER, 1, CENTER + 4, box);
    }

    /**
     * Poço da sala enterrada: coluna de escada no lado norte (x = centro, z = 1) da sala até a superfície, com paredes
     * em volta, uma passagem para dentro da sala e uma lanterna de alma na boca do poço.
     */
    private void buildShaft(WorldGenLevel level, BoundingBox box, SanctuaryKind.Palette p) {
        int wx = getWorldX(CENTER, 1), wz = getWorldZ(CENTER, 1);
        int floorY = getWorldY(0);
        BlockState wall = p.wall().defaultBlockState();
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        // Passagem da escada para a sala.
        placeBlock(level, Blocks.AIR.defaultBlockState(), CENTER, 1, 2, box);
        placeBlock(level, Blocks.AIR.defaultBlockState(), CENTER, 2, 2, box);
        if (!box.isInside(new BlockPos(wx, floorY, wz))) return;
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz);
        for (int y = floorY + 1; y <= top; y++) {
            level.setBlock(new BlockPos(wx, y, wz), ladder, 2);
            level.setBlock(new BlockPos(wx, y, wz - 1), wall, 2);
            if (y > floorY + ROOM_HEIGHT) {
                level.setBlock(new BlockPos(wx - 1, y, wz), wall, 2);
                level.setBlock(new BlockPos(wx + 1, y, wz), wall, 2);
                level.setBlock(new BlockPos(wx, y, wz + 1), wall, 2);
            }
        }
        level.setBlock(new BlockPos(wx - 1, top + 1, wz), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(wx + 1, top + 1, wz), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(wx - 1, top + 2, wz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    // ------------------------------------------------------------------ a falha da realidade (lore: docs/LORE.md)

    /**
     * O que a falha da realidade deixou de "físico" em cada santuário. Os efeitos vivos (partículas, geada,
     * escuridão, silêncio) ficam em {@link StructureAmbientHandler}.
     */
    private void buildRealityFault(WorldGenLevel level, BoundingBox box) {
        switch (kind) {
            case ASH_FORGE -> buildForgeChains(level, box);
            case FORGOTTEN_LIBRARY -> buildEscherShelves(level, box);
            case TIDE_SANCTUARY -> buildTears(level, box);
            case PETRIFIED_GARDEN -> buildNerveTrees(level, box);
            case BROKEN_WIND_TOWER -> buildSlicedTower(level, box);
            case WHISPERING_CATACOMB -> buildRibsAndBlades(level, box);
        }
    }

    /** Forja: quatro correntes descem do céu sobre a arena e seguram bigornas paradas no ar. */
    private void buildForgeChains(WorldGenLevel level, BoundingBox box) {
        int[][] spots = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] s : spots) {
            for (int y = 7; y < HEIGHT; y++) placeBlock(level, Blocks.CHAIN.defaultBlockState(), CENTER + s[0], y, CENTER + s[1], box);
            spawnBlockDisplay(level, box, Blocks.ANVIL.defaultBlockState(), CENTER + s[0], 6, CENTER + s[1]);
        }
    }

    /** Biblioteca: ilhas de estantes penduradas de cabeça para baixo, com escada invertida e lanterna. */
    private void buildEscherShelves(WorldGenLevel level, BoundingBox box) {
        int[][] spots = {{-7, 0}, {7, 0}, {0, -7}, {0, 7}};
        BlockState stair = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.HALF, Half.TOP);
        for (int[] s : spots) {
            int x = CENTER + s[0], z = CENTER + s[1];
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), x + dx, 9, z + dz, box);
                    placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), x + dx, 8, z + dz, box);
                }
            }
            placeBlock(level, stair, x, 7, z, box);
            placeBlock(level, stair.setValue(StairBlock.FACING, Direction.SOUTH), x + 1, 7, z + 1, box);
            placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), x + 1, 7, z, box);
        }
    }

    /** Marés: três lágrimas gigantes de vidro paradas no ar sobre o pátio, com luz no núcleo. */
    private void buildTears(WorldGenLevel level, BoundingBox box) {
        BlockState glass = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        for (int[] s : TEARS) {
            int x = CENTER + s[0], z = CENTER + s[1];
            placeBlock(level, glass, x, 10, z, box);
            for (int[] d : PLUS) placeBlock(level, glass, x + d[0], 9, z + d[1], box);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) placeBlock(level, glass, x + dx, 8, z + dz, box);
            }
            placeBlock(level, Blocks.SEA_LANTERN.defaultBlockState(), x, 8, z, box);
            for (int[] d : PLUS) placeBlock(level, glass, x + d[0], 7, z + d[1], box);
        }
    }

    /** Jardim: quatro árvores de pedra em forma de nervo, com tronco fino e dendritos abertos no topo. */
    private void buildNerveTrees(WorldGenLevel level, BoundingBox box) {
        BlockState trunk = Blocks.POLISHED_TUFF.defaultBlockState();
        BlockState twig = Blocks.TUFF_BRICK_WALL.defaultBlockState();
        BlockState tip = Blocks.CHISELED_TUFF.defaultBlockState();
        for (int[] s : NERVE_TREES) {
            int x = CENTER + s[0], z = CENTER + s[1];
            for (int y = 1; y <= 5; y++) placeBlock(level, trunk, x, y, z, box);
            // Dendritos: galhos em diagonal subindo para fora, terminando num nó.
            int[][] branches = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] b : branches) {
                placeBlock(level, twig, x + b[0], 6, z + b[1], box);
                placeBlock(level, twig, x + 2 * b[0], 7, z + 2 * b[1], box);
                placeBlock(level, tip, x + 2 * b[0], 8, z + 2 * b[1], box);
            }
            placeBlock(level, trunk, x, 6, z, box);
            placeBlock(level, tip, x, 7, z, box);
        }
    }

    /** Torre: sete anéis desalinhados empilhados no ar sobre a arena e o sino no topo. */
    private void buildSlicedTower(WorldGenLevel level, BoundingBox box) {
        BlockState ring = Blocks.POLISHED_DIORITE.defaultBlockState();
        for (int i = 0; i < TOWER_SEGMENTS; i++) {
            int y = 5 + i;
            int ox = (i % 2 == 0 ? 1 : -1) * (i % 3 == 0 ? 0 : 1), oz = (i % 3) - 1;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0) placeBlock(level, ring, CENTER + ox + dx, y, CENTER + oz + dz, box);
                }
            }
        }
        placeBlock(level, Blocks.POLISHED_DIORITE.defaultBlockState(), CENTER, 4 + TOWER_SEGMENTS, CENTER, box);
        placeBlock(level, Blocks.BELL.defaultBlockState(), CENTER, BELL_Y, CENTER, box);
    }

    /** Catacumba: três costelas de osso cruzando a sala e uma parede de lâminas com crânios em volta. */
    private void buildRibsAndBlades(WorldGenLevel level, BoundingBox box) {
        for (int ribZ : new int[] {-6, 0, 6}) {
            for (int dx = -10; dx <= 10; dx++) {
                int y = 1 + (int) Math.round(6 * Math.sqrt(1 - (dx / 10.0) * (dx / 10.0)));
                placeBlock(level, Blocks.BONE_BLOCK.defaultBlockState(), CENTER + dx, y, CENTER + ribZ, box);
            }
        }
        // Lâminas (barras de ferro) com um crânio em cima, a cada 30°, longe do poço (norte) e do lado oposto.
        for (int deg = 0; deg < 360; deg += 30) {
            if (deg == 90 || deg == 270) continue;
            int x = CENTER + (int) Math.round(11 * Math.cos(Math.toRadians(deg)));
            int z = CENTER + (int) Math.round(11 * Math.sin(Math.toRadians(deg)));
            placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), x, 1, z, box);
            placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), x, 2, z, box);
            placeBlock(level, Blocks.SKELETON_SKULL.defaultBlockState(), x, 3, z, box);
        }
    }

    /**
     * Bloco parado no ar (entidade de exibição: não cai nem se quebra). Criada uma vez só, no pedaço de mundo que
     * contém o ponto. O estado do bloco vai pelo NBT ({@code block_state}), como no {@code /summon}.
     */
    private void spawnBlockDisplay(WorldGenLevel level, BoundingBox box, BlockState state, int x, int y, int z) {
        BlockPos pos = new BlockPos(getWorldX(x, z), getWorldY(y), getWorldZ(x, z));
        if (!box.isInside(pos)) return;
        Display.BlockDisplay display = EntityType.BLOCK_DISPLAY.create(level.getLevel());
        if (display == null) return;
        CompoundTag tag = new CompoundTag();
        tag.put("block_state", NbtUtils.writeBlockState(state));
        display.load(tag);
        display.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        level.addFreshEntity(display);
    }

    private void spawnGarrison(WorldGenLevel level, RandomSource random, BoundingBox box) {
        spawn(level, random, box, kind.boss(), CENTER, 1, CENTER);
        spawn(level, random, box, kind.guardA(), CENTER + 9, 1, CENTER + 3);
        spawn(level, random, box, kind.guardA(), CENTER - 9, 1, CENTER - 3);
        spawn(level, random, box, kind.guardA(), CENTER + 2, 1, CENTER + 10);
        spawn(level, random, box, kind.guardB(), CENTER - 10, 1, CENTER + 4);
        spawn(level, random, box, kind.guardB(), CENTER + 10, 1, CENTER - 4);
        spawn(level, random, box, kind.flyer(), CENTER + 7, 3, CENTER + 7);
        spawn(level, random, box, kind.flyer(), CENTER - 7, 3, CENTER - 7);
    }

    /** Cria o mob em coordenadas locais, uma vez só: apenas no pedaço de mundo que contém o ponto. */
    private void spawn(WorldGenLevel level, RandomSource random, BoundingBox box, EntityType<? extends Mob> type,
                       int x, int y, int z) {
        BlockPos pos = new BlockPos(getWorldX(x, z), getWorldY(y), getWorldZ(x, z));
        if (!box.isInside(pos)) return;
        Mob mob = type.create(level.getLevel());
        if (mob == null) return;
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        mob.setPersistenceRequired();
        level.addFreshEntityWithPassengers(mob);
    }
}
