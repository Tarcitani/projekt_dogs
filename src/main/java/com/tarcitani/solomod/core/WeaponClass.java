package com.tarcitani.solomod.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

/**
 * Classe (formato) da arma. Define o stat requerido padrão, os atributos vanilla base
 * e o "peso", que decide se a arma sofre penalidade de velocidade quando o requisito não é cumprido.
 *
 * <p>Os valores de dano/velocidade seguem a convenção vanilla: são somados ao dano base do jogador (1.0)
 * e à velocidade base de ataque (4.0). Ex.: espada de ferro = +5 dano, -2.4 velocidade.
 */
public enum WeaponClass implements StringRepresentable {
    //           nome          peso                  stat padrão    dano  veloc.  habilidade padrão
    GREATSWORD ("greatsword",  Weight.HEAVY,   StatType.FOR, 9.0F, -3.0F, "ground_slam"),
    AXE        ("axe",         Weight.HEAVY,   StatType.FOR, 8.0F, -3.1F, "ground_slam"),
    LONGSWORD  ("longsword",   Weight.MEDIUM,  StatType.FOR, 6.0F, -2.6F, "dash_strike"),
    DAGGER     ("dagger",      Weight.LIGHT,   StatType.AGI, 3.0F, -1.5F, "dash_strike"),
    RAPIER     ("rapier",      Weight.LIGHT,   StatType.AGI, 4.0F, -1.8F, "dash_strike"),
    KUNAI      ("kunai",       Weight.THROWN,  StatType.AGI, 2.5F, -1.2F, "dash_strike"),
    WAND       ("wand",        Weight.CHANNEL, StatType.INT, 1.5F, -2.0F, "mana_bolt");

    public enum Weight {
        HEAVY, MEDIUM, LIGHT, THROWN, CHANNEL;

        /** Armas Pesadas/Médias ficam mais lentas quando o jogador não cumpre o requisito. */
        public boolean penalizedWhenLocked() {
            return this == HEAVY || this == MEDIUM;
        }
    }

    public static final Codec<WeaponClass> CODEC = StringRepresentable.fromEnum(WeaponClass::values);
    private static final IntFunction<WeaponClass> BY_ID =
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, WeaponClass> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private final String serializedName;
    private final Weight weight;
    private final StatType defaultStat;
    private final float baseDamage;
    private final float baseSpeed;
    private final String defaultSkillId;

    WeaponClass(String serializedName, Weight weight, StatType defaultStat, float baseDamage, float baseSpeed,
                String defaultSkillId) {
        this.serializedName = serializedName;
        this.weight = weight;
        this.defaultStat = defaultStat;
        this.baseDamage = baseDamage;
        this.baseSpeed = baseSpeed;
        this.defaultSkillId = defaultSkillId;
    }

    public Weight weight() { return weight; }
    public StatType defaultStat() { return defaultStat; }
    public float baseDamage() { return baseDamage; }
    public float baseSpeed() { return baseSpeed; }
    public String defaultSkillId() { return defaultSkillId; }

    public Component displayName() {
        return Component.translatable("weapon_class.solomod." + serializedName);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static WeaponClass byName(String name) {
        for (WeaponClass c : values()) {
            if (c.serializedName.equalsIgnoreCase(name)) return c;
        }
        return null;
    }
}
