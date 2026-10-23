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
 * Tier de origem: define a qualidade do material (e portanto da arma) pelo mob de onde veio.
 * Também é usado no próprio mob para decidir quanto XP Global ele concede.
 *
 * <ul>
 *   <li>NORMAL: só dano base e velocidade. Sem elemento, sem habilidade.</li>
 *   <li>OP (Elite): mais dano, bônus passivo de stat e passiva elemental leve.</li>
 *   <li>SUB_BOSS: dano elemental puro (o elemento ignora a trava de "mesmo elemento") e habilidade menor.</li>
 *   <li>BOSS: status máximos, habilidade suprema e passiva elemental no nível máximo.</li>
 * </ul>
 */
public enum MobTier implements StringRepresentable {
    //          nome        dano  +stat  potência elem.  nível hab.  req. padrão  XP    cor
    NORMAL  ("normal",   1.00F, 0,     0,              0,          5,           10,   ChatFormatting.GRAY),
    OP      ("op",       1.25F, 3,     1,              0,          15,          40,   ChatFormatting.GREEN),
    SUB_BOSS("sub_boss", 1.50F, 5,     2,              1,          30,          150,  ChatFormatting.LIGHT_PURPLE),
    BOSS    ("boss",     2.00F, 10,    3,              2,          50,          500,  ChatFormatting.GOLD);

    public static final Codec<MobTier> CODEC = StringRepresentable.fromEnum(MobTier::values);
    private static final IntFunction<MobTier> BY_ID =
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, MobTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private final String serializedName;
    /** Multiplicador aplicado ao dano final da arma. */
    private final float damageMultiplier;
    /** Pontos extras no stat requerido da arma enquanto ela está na mão (só para escalonamento, não destrava requisito). */
    private final int statBonus;
    /** 0 = sem elemento; 1 = leve; 2 = forte; 3 = máximo. Controla chance/duração/nível do efeito passivo. */
    private final int elementPotency;
    /** 0 = sem habilidade; 1 = habilidade menor; 2 = habilidade suprema. */
    private final int skillLevel;
    /** Valor de requisito sugerido quando a arma é gerada sem valor explícito. */
    private final int defaultRequirement;
    /** XP Global concedido ao matar um mob deste tier. */
    private final int xpReward;
    private final ChatFormatting color;

    MobTier(String serializedName, float damageMultiplier, int statBonus, int elementPotency, int skillLevel,
            int defaultRequirement, int xpReward, ChatFormatting color) {
        this.serializedName = serializedName;
        this.damageMultiplier = damageMultiplier;
        this.statBonus = statBonus;
        this.elementPotency = elementPotency;
        this.skillLevel = skillLevel;
        this.defaultRequirement = defaultRequirement;
        this.xpReward = xpReward;
        this.color = color;
    }

    public float damageMultiplier() { return damageMultiplier; }
    public int statBonus() { return statBonus; }
    public int elementPotency() { return elementPotency; }
    public int skillLevel() { return skillLevel; }
    public int defaultRequirement() { return defaultRequirement; }
    public int xpReward() { return xpReward; }
    public ChatFormatting color() { return color; }

    public boolean allowsElement() { return elementPotency > 0; }
    public boolean allowsSkill() { return skillLevel > 0; }

    /** SUB_BOSS e BOSS: o elemento é "puro" e nunca sofre a penalidade de 0.5x. */
    public boolean hasPureElement() { return this == SUB_BOSS || this == BOSS; }

    public Component displayName() {
        return Component.translatable("tier.solomod." + serializedName).withStyle(color);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static MobTier byName(String name) {
        for (MobTier t : values()) {
            if (t.serializedName.equalsIgnoreCase(name) || t.name().equalsIgnoreCase(name)) return t;
        }
        return null;
    }
}
