package com.tarcitani.solomod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.core.StatType;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.skill.WeaponSkill;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Dados da arma modular, gravados direto no ItemStack como Data Component (1.21.1).
 *
 * <p>É um record imutável: para "alterar" uma arma, crie uma cópia com os métodos {@code with...}
 * e grave de volta com {@code stack.set(...)}. Records são obrigatórios aqui porque o vanilla compara
 * componentes por {@code equals/hashCode} (empilhamento, sincronização de inventário etc.).
 *
 * @param mobTier           tier do mob de origem do material
 * @param weaponClass       formato da arma (espadão, adaga, varinha...)
 * @param requiredStatType  stat de Maestria exigido
 * @param requiredStatValue valor mínimo do stat exigido
 * @param elementType       elemento da arma (NONE em armas NORMAL)
 * @param skillId           id da habilidade ativa ("none" quando não há)
 */
public record WeaponData(
        MobTier mobTier,
        WeaponClass weaponClass,
        StatType requiredStatType,
        int requiredStatValue,
        ElementType elementType,
        String skillId
) {
    /** Persistência (NBT do item). Campos opcionais deixam o formato tolerante a versões futuras. */
    public static final Codec<WeaponData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MobTier.CODEC.optionalFieldOf("mob_tier", MobTier.NORMAL).forGetter(WeaponData::mobTier),
            WeaponClass.CODEC.optionalFieldOf("weapon_class", WeaponClass.LONGSWORD).forGetter(WeaponData::weaponClass),
            StatType.CODEC.optionalFieldOf("required_stat", StatType.FOR).forGetter(WeaponData::requiredStatType),
            Codec.INT.optionalFieldOf("required_value", 0).forGetter(WeaponData::requiredStatValue),
            ElementType.CODEC.optionalFieldOf("element", ElementType.NONE).forGetter(WeaponData::elementType),
            Codec.STRING.optionalFieldOf("skill", WeaponSkill.NONE.id()).forGetter(WeaponData::skillId)
    ).apply(instance, WeaponData::new));

    /** Sincronização cliente/servidor (necessária para o tooltip no cliente). */
    public static final StreamCodec<ByteBuf, WeaponData> STREAM_CODEC = StreamCodec.composite(
            MobTier.STREAM_CODEC, WeaponData::mobTier,
            WeaponClass.STREAM_CODEC, WeaponData::weaponClass,
            StatType.STREAM_CODEC, WeaponData::requiredStatType,
            ByteBufCodecs.VAR_INT, WeaponData::requiredStatValue,
            ElementType.STREAM_CODEC, WeaponData::elementType,
            ByteBufCodecs.STRING_UTF8, WeaponData::skillId,
            WeaponData::new
    );

    /**
     * Construtor canônico com normalização: aplica as regras de tier para que nenhum comando ou loot
     * consiga criar uma arma inválida (ex.: NORMAL com elemento).
     */
    public WeaponData {
        if (requiredStatValue < 0) requiredStatValue = 0;
        if (!mobTier.allowsElement()) elementType = ElementType.NONE;
        if (!mobTier.allowsSkill() || skillId == null || skillId.isBlank()) skillId = WeaponSkill.NONE.id();
    }

    /**
     * Fábrica com os padrões do tier/classe: stat da classe, requisito do tier
     * e habilidade padrão (suprema para BOSS, a da classe para SUB_BOSS).
     */
    public static WeaponData of(WeaponClass weaponClass, MobTier tier, ElementType element) {
        String skill = switch (tier.skillLevel()) {
            case 2 -> WeaponSkill.ELEMENTAL_BURST.id();
            case 1 -> weaponClass.defaultSkillId();
            default -> WeaponSkill.NONE.id();
        };
        return new WeaponData(tier, weaponClass, weaponClass.defaultStat(), tier.defaultRequirement(), element, skill);
    }

    /** Usado quando um ModularWeaponItem não tem componente (ex.: pego do menu criativo). */
    public static WeaponData defaultFor(WeaponClass weaponClass) {
        return of(weaponClass, MobTier.NORMAL, ElementType.NONE);
    }

    public WeaponData withRequirement(int value) {
        return new WeaponData(mobTier, weaponClass, requiredStatType, value, elementType, skillId);
    }

    public WeaponData withSkill(String newSkillId) {
        return new WeaponData(mobTier, weaponClass, requiredStatType, requiredStatValue, elementType, newSkillId);
    }

    public WeaponSkill skill() {
        return WeaponSkill.byId(skillId);
    }

    public boolean hasSkill() {
        return skill() != WeaponSkill.NONE;
    }
}
