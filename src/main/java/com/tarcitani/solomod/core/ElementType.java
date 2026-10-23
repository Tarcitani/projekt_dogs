package com.tarcitani.solomod.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;

import java.util.function.IntFunction;

/**
 * Elementos do mod e o ciclo de vantagem.
 *
 * <p>Ciclo: Água > Fogo > Ar > Terra > Água.
 * <ul>
 *   <li>Vantagem: 1.5x de dano e aplica o efeito passivo do elemento.</li>
 *   <li>Desvantagem ou mesmo elemento: 0.5x de dano.</li>
 *   <li>Pares "opostos" (Água x Ar, Fogo x Terra) ou {@link #NONE}: 1.0x (neutro).</li>
 * </ul>
 */
public enum ElementType implements StringRepresentable {
    NONE("none", ChatFormatting.GRAY),
    WATER("water", ChatFormatting.AQUA),
    EARTH("earth", ChatFormatting.DARK_GREEN),
    FIRE("fire", ChatFormatting.RED),
    AIR("air", ChatFormatting.WHITE);

    public static final float ADVANTAGE_MULTIPLIER = 1.5F;
    public static final float DISADVANTAGE_MULTIPLIER = 0.5F;
    public static final float NEUTRAL_MULTIPLIER = 1.0F;

    /** Codec para NBT/JSON (usa o nome serializado, ex.: "fire"). */
    public static final Codec<ElementType> CODEC = StringRepresentable.fromEnum(ElementType::values);

    private static final IntFunction<ElementType> BY_ID =
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    /** Codec de rede (envia apenas o ordinal como VarInt). */
    public static final StreamCodec<ByteBuf, ElementType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private final String serializedName;
    private final ChatFormatting color;

    ElementType(String serializedName, ChatFormatting color) {
        this.serializedName = serializedName;
        this.color = color;
    }

    /** O elemento que ESTE elemento vence no ciclo. */
    public ElementType beats() {
        return switch (this) {
            case WATER -> FIRE;
            case FIRE -> AIR;
            case AIR -> EARTH;
            case EARTH -> WATER;
            case NONE -> NONE;
        };
    }

    /** true se este elemento tem vantagem sobre {@code defender}. */
    public boolean hasAdvantageOver(ElementType defender) {
        return this != NONE && defender != NONE && this.beats() == defender;
    }

    /**
     * Multiplicador de dano de um ataque deste elemento contra {@code defender}.
     * Mesmo elemento conta como desvantagem, conforme a regra do design.
     */
    public float multiplierAgainst(ElementType defender) {
        if (this == NONE || defender == NONE) return NEUTRAL_MULTIPLIER;
        if (hasAdvantageOver(defender)) return ADVANTAGE_MULTIPLIER;
        if (defender.hasAdvantageOver(this) || defender == this) return DISADVANTAGE_MULTIPLIER;
        return NEUTRAL_MULTIPLIER;
    }

    public ChatFormatting color() {
        return color;
    }

    public Component displayName() {
        return Component.translatable("element.solomod." + serializedName).withStyle(color);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /** Busca tolerante a maiúsculas, usada pelos comandos. Retorna null se não existir. */
    public static ElementType byName(String name) {
        for (ElementType e : values()) {
            if (e.serializedName.equalsIgnoreCase(name) || e.name().equalsIgnoreCase(name)) return e;
        }
        return null;
    }
}
