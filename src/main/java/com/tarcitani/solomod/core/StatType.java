package com.tarcitani.solomod.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

/**
 * Os 4 atributos de Maestria do jogador.
 *
 * <ul>
 *   <li>FOR: dano físico base; requisito de armas Pesadas/Médias.</li>
 *   <li>AGI: velocidade de ataque e movimento; requisito de armas Leves/Lançáveis.</li>
 *   <li>INT: mana máxima; requisito de Habilidades Ativas e de Canalizadores.</li>
 *   <li>VIT: vida máxima e resistência a dano.</li>
 * </ul>
 */
public enum StatType implements StringRepresentable {
    FOR("for", ChatFormatting.RED),
    AGI("agi", ChatFormatting.GREEN),
    INT("int", ChatFormatting.BLUE),
    VIT("vit", ChatFormatting.GOLD);

    public static final Codec<StatType> CODEC = StringRepresentable.fromEnum(StatType::values);
    private static final IntFunction<StatType> BY_ID =
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, StatType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private final String serializedName;
    private final ChatFormatting color;

    StatType(String serializedName, ChatFormatting color) {
        this.serializedName = serializedName;
        this.color = color;
    }

    public ChatFormatting color() {
        return color;
    }

    /** Sigla curta, ex.: "AGI". */
    public Component shortName() {
        return Component.translatable("stat.solomod." + serializedName).withStyle(color);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static StatType byName(String name) {
        for (StatType s : values()) {
            if (s.serializedName.equalsIgnoreCase(name)) return s;
        }
        // Aceita os nomes por extenso em português também.
        return switch (name.toLowerCase()) {
            case "forca", "força", "str" -> FOR;
            case "agilidade", "dex" -> AGI;
            case "inteligencia", "inteligência" -> INT;
            case "vitalidade" -> VIT;
            default -> null;
        };
    }
}
