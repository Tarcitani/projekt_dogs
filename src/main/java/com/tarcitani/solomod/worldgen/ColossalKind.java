package com.tarcitani.solomod.worldgen;

import com.tarcitani.solomod.core.ElementType;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

/**
 * Os lugares colossais (lore: docs/LORE.md, "Lugares colossais"): os cinco tronos dos Monarcas, onde a realidade
 * falha em volta do membro roubado, e o Anfiteatro do Selo Partido, a arena do Soberano no vazio do End.
 *
 * <p>Cada tipo diz o tamanho da caixa (meia-largura em x/z, quanto desce e quanto sobe a partir da base), como achar
 * a base ({@link Placement}) e qual Monarca nasce no trono. As formas em si ficam em {@link ColossalShapes}.
 * O nome serializado é o campo {@code "colossus"} no JSON da estrutura e entra no NBT da peça: não renomeie.
 *
 * <p>Limite do Minecraft: uma estrutura só é ligada aos pedaços de mundo a até 8 chunks do centro, então a
 * meia-largura fica abaixo de 128 blocos.
 */
public enum ColossalKind implements StringRepresentable {
    INVERTED_PENDULUM_CITADEL("inverted_pendulum_citadel", Placement.SURFACE, 52, 9, 62, ElementType.NONE),
    CARDIAC_FURNACE("cardiac_furnace", Placement.SURFACE, 44, 9, 62, ElementType.FIRE),
    DROWNED_SKY_TRENCH("drowned_sky_trench", Placement.OCEAN_FLOOR, 33, 4, 30, ElementType.WATER),
    AMBER_HOURS_CRADLE("amber_hours_cradle", Placement.SURFACE, 42, 10, 90, ElementType.EARTH),
    STATIC_TEMPEST_PANTHEON("static_tempest_pantheon", Placement.SURFACE, 47, 9, 75, ElementType.AIR),
    SHATTERED_SEAL_AMPHITHEATER("shattered_seal_amphitheater", Placement.SKY_VOID, 76, 15, 40, null);

    /** Como achar a base: na superfície, no fundo do mar (cubo seco) ou fixa no alto, sobre o vazio. */
    public enum Placement { SURFACE, OCEAN_FLOOR, SKY_VOID }

    public static final StringRepresentable.EnumCodec<ColossalKind> CODEC = StringRepresentable.fromEnum(ColossalKind::values);

    private final String name;
    private final Placement placement;
    private final int halfWidth;
    private final int below;
    private final int above;
    @Nullable
    private final ElementType monarch;

    ColossalKind(String name, Placement placement, int halfWidth, int below, int above, @Nullable ElementType monarch) {
        this.name = name;
        this.placement = placement;
        this.halfWidth = halfWidth;
        this.below = below;
        this.above = above;
        this.monarch = monarch;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public Placement placement() {
        return placement;
    }

    public int halfWidth() {
        return halfWidth;
    }

    public int below() {
        return below;
    }

    public int above() {
        return above;
    }

    /** Elemento do Monarca que nasce no trono; null no Anfiteatro (o Soberano é chamado, não gerado). */
    @Nullable
    public ElementType monarch() {
        return monarch;
    }

    public static ColossalKind byName(String name) {
        return CODEC.byName(name, INVERTED_PENDULUM_CITADEL);
    }
}
