package com.tarcitani.solomod.worldgen;

import com.tarcitani.solomod.entity.ModEntities;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Os seis Santuários de Eco. Todos usam a mesma planta redonda de {@link SanctuaryPiece} (arena central, anel
 * interno, pátio, muralha com quatro portões e quatro pilares), mas cada um tem a própria paleta de blocos, a
 * própria guarnição e, na Catacumba, fica enterrado com um poço de escada até a superfície.
 *
 * <p>O nome serializado é o valor do campo {@code "sanctuary"} no JSON da estrutura e entra no NBT da peça:
 * não renomeie.
 */
public enum SanctuaryKind implements StringRepresentable {
    ASH_FORGE("ash_forge", false,
            new Palette(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.BASALT,
                    new BlockState[] {Blocks.BLACKSTONE.defaultBlockState(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                            Blocks.SMOOTH_BASALT.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState()},
                    Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                    Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState(), 4,
                    Blocks.LAVA.defaultBlockState(), null,
                    Blocks.POLISHED_BLACKSTONE, 2, Blocks.SOUL_SOIL.defaultBlockState(), Blocks.SOUL_FIRE.defaultBlockState(),
                    new BlockState[] {Blocks.ANVIL.defaultBlockState(), Blocks.ANVIL.defaultBlockState(),
                            Blocks.BLAST_FURNACE.defaultBlockState(), Blocks.BLAST_FURNACE.defaultBlockState()}),
            ModEntities.CINZAGRIM, ModEntities.LIVING_SLAG, ModEntities.WITHERED_SMITH, ModEntities.FORGE_SPARK),

    FORGOTTEN_LIBRARY("forgotten_library", false,
            new Palette(Blocks.DARK_OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.STONE_BRICKS,
                    new BlockState[] {Blocks.STONE_BRICKS.defaultBlockState(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                            Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), Blocks.CHISELED_STONE_BRICKS.defaultBlockState()},
                    Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS,
                    Blocks.STONE_BRICK_WALL.defaultBlockState(), 4,
                    null, Blocks.BOOKSHELF.defaultBlockState(),
                    Blocks.CHISELED_STONE_BRICKS, 2, Blocks.STONE_BRICK_WALL.defaultBlockState(), Blocks.LANTERN.defaultBlockState(),
                    new BlockState[] {Blocks.LECTERN.defaultBlockState(), Blocks.LECTERN.defaultBlockState(),
                            Blocks.CHISELED_BOOKSHELF.defaultBlockState(), Blocks.CHISELED_BOOKSHELF.defaultBlockState()}),
            ModEntities.ARCHIVIST, ModEntities.INK_SENTINEL, ModEntities.SPECTRAL_SCRIBE, ModEntities.FLYING_TOME),

    TIDE_SANCTUARY("tide_sanctuary", false,
            new Palette(Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.PRISMARINE,
                    new BlockState[] {Blocks.SAND.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(),
                            Blocks.PRISMARINE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState()},
                    Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE,
                    Blocks.PRISMARINE_WALL.defaultBlockState(), 4,
                    Blocks.WATER.defaultBlockState(), null,
                    Blocks.PRISMARINE_BRICKS, 2, Blocks.SEA_LANTERN.defaultBlockState(), null,
                    new BlockState[] {Blocks.DEAD_TUBE_CORAL_BLOCK.defaultBlockState(), Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState(),
                            Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState(), Blocks.DEAD_FIRE_CORAL_BLOCK.defaultBlockState()}),
            ModEntities.WEEPER, ModEntities.CHOIR_DROWNED, ModEntities.CORAL_CRAB, ModEntities.CORAL_JELLY),

    PETRIFIED_GARDEN("petrified_garden", false,
            new Palette(Blocks.MOSS_BLOCK, Blocks.ROOTED_DIRT, Blocks.MUD_BRICKS,
                    new BlockState[] {Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState(),
                            Blocks.MOSS_BLOCK.defaultBlockState(), Blocks.PACKED_MUD.defaultBlockState()},
                    Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS,
                    Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState(), 3,
                    Blocks.WATER.defaultBlockState(),
                    Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true),
                    Blocks.MOSSY_COBBLESTONE, 2, Blocks.SHROOMLIGHT.defaultBlockState(), null,
                    new BlockState[] {Blocks.FLOWERING_AZALEA.defaultBlockState(), Blocks.AZALEA.defaultBlockState(),
                            Blocks.FLOWERING_AZALEA.defaultBlockState(), Blocks.AZALEA.defaultBlockState()}),
            ModEntities.ROOT_MOTHER, ModEntities.THORN_SPROUT, ModEntities.MOSS_GOLEM, ModEntities.AMBER_WASP),

    BROKEN_WIND_TOWER("broken_wind_tower", false,
            new Palette(Blocks.SMOOTH_STONE, Blocks.POLISHED_DIORITE, Blocks.CALCITE,
                    new BlockState[] {Blocks.CALCITE.defaultBlockState(), Blocks.STONE.defaultBlockState(),
                            Blocks.ANDESITE.defaultBlockState(), Blocks.DIORITE.defaultBlockState()},
                    Blocks.POLISHED_DIORITE, Blocks.CALCITE,
                    Blocks.DIORITE_WALL.defaultBlockState(), 2,
                    null, null,
                    Blocks.POLISHED_DIORITE, 9, Blocks.LIGHTNING_ROD.defaultBlockState(), null,
                    new BlockState[] {Blocks.DIORITE_WALL.defaultBlockState(), Blocks.DIORITE_WALL.defaultBlockState(),
                            Blocks.DIORITE_WALL.defaultBlockState(), Blocks.DIORITE_WALL.defaultBlockState()}),
            ModEntities.HERALD, ModEntities.GRAY_HARPY, ModEntities.GALE_TOTEM, ModEntities.BREEZE_ELEMENTAL),

    WHISPERING_CATACOMB("whispering_catacomb", true,
            new Palette(Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_BRICKS,
                    new BlockState[] {Blocks.COBBLED_DEEPSLATE.defaultBlockState(), Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                            Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), Blocks.SCULK.defaultBlockState()},
                    Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS,
                    null, 6,
                    null, null,
                    Blocks.POLISHED_DEEPSLATE, 2, Blocks.SOUL_SOIL.defaultBlockState(), Blocks.SOUL_FIRE.defaultBlockState(),
                    new BlockState[] {Blocks.SKELETON_SKULL.defaultBlockState(), Blocks.COBWEB.defaultBlockState(),
                            Blocks.SKELETON_SKULL.defaultBlockState(), Blocks.COBWEB.defaultBlockState()}),
            ModEntities.ECHO_HUNTER, ModEntities.WHISPERING_BONES, ModEntities.CREEPING_SHADOW, ModEntities.WAILING_SHADE);

    public static final StringRepresentable.EnumCodec<SanctuaryKind> CODEC = StringRepresentable.fromEnum(SanctuaryKind::values);

    private final String name;
    private final boolean underground;
    private final Palette palette;
    private final Supplier<? extends EntityType<? extends Mob>> boss;
    private final Supplier<? extends EntityType<? extends Mob>> guardA;
    private final Supplier<? extends EntityType<? extends Mob>> guardB;
    private final Supplier<? extends EntityType<? extends Mob>> flyer;

    SanctuaryKind(String name, boolean underground, Palette palette, Supplier<? extends EntityType<? extends Mob>> boss,
                  Supplier<? extends EntityType<? extends Mob>> guardA, Supplier<? extends EntityType<? extends Mob>> guardB,
                  Supplier<? extends EntityType<? extends Mob>> flyer) {
        this.name = name;
        this.underground = underground;
        this.palette = palette;
        this.boss = boss;
        this.guardA = guardA;
        this.guardB = guardB;
        this.flyer = flyer;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** Enterrado: sala fechada com teto e poço de escada até a superfície. */
    public boolean underground() {
        return underground;
    }

    public Palette palette() {
        return palette;
    }

    public EntityType<? extends Mob> boss() {
        return boss.get();
    }

    /** Capanga principal (3 no pátio). */
    public EntityType<? extends Mob> guardA() {
        return guardA.get();
    }

    /** Capanga secundário (2 no pátio). */
    public EntityType<? extends Mob> guardB() {
        return guardB.get();
    }

    /** Voador (2 acima do pátio). */
    public EntityType<? extends Mob> flyer() {
        return flyer.get();
    }

    public static SanctuaryKind byName(String name) {
        return CODEC.byName(name, ASH_FORGE);
    }

    /**
     * Blocos de um santuário.
     *
     * @param arena       chão da arena central (e {@code arenaAlt}, 1 em 5)
     * @param ring        anel entre arena e pátio (ou pontes, quando há {@code moat})
     * @param yard        chão do pátio: o último bloco da lista é raro (1 em 20)
     * @param wall        muralha (e {@code wallAlt}, 1 em 6)
     * @param crenel      ameias no topo da muralha (null = sem ameias)
     * @param wallHeight  altura da muralha
     * @param moat        líquido do anel (null = anel seco, sem pontes)
     * @param lining      forro do lado de dentro da muralha, 2 de altura (null = nenhum)
     * @param pillar      pilares das diagonais, com {@code pillarHeight} de altura
     * @param pillarTop   bloco no topo do pilar
     * @param pillarFire  bloco acima do topo (fogo, lanterna...) ou null
     * @param decor       quatro enfeites da arena (oeste, leste, norte, sul)
     */
    public record Palette(Block arena, Block arenaAlt,
                          Block ring, BlockState[] yard,
                          Block wall, Block wallAlt,
                          @Nullable BlockState crenel, int wallHeight, @Nullable BlockState moat, @Nullable BlockState lining,
                          Block pillar, int pillarHeight, BlockState pillarTop,
                          @Nullable BlockState pillarFire, BlockState[] decor) {}
}
