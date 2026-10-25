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
 * Todos os exemplares são Normal/sem elemento, inclusive ao ler componentes antigos. Campos
 * legados permanecem nos codecs para carregar mundos sem perder itens; habilidades vêm da árvore.
 *
 * @param mobTier           campo legado, normalizado para NORMAL
 * @param weaponClass       formato da arma (espadão, adaga, varinha...)
 * @param requiredStatType  stat de Maestria exigido
 * @param requiredStatValue valor mínimo do stat exigido
 * @param elementType       campo legado, normalizado para NONE
 * @param skillId           campo legado, normalizado para "none"; habilidades vêm da árvore
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
     * Converte também armas antigas em Normal/sem elemento, sem tocar em dados de mobs.
     */
    public WeaponData {
        mobTier = MobTier.NORMAL;
        elementType = ElementType.NONE;
        requiredStatType = weaponClass.defaultStat();
        requiredStatValue = MobTier.NORMAL.defaultRequirement();
        skillId = WeaponSkill.NONE.id();
    }

    /**
     * Fábrica compatível com chamadas antigas; tier/elemento são ignorados. Só gera arma Normal.
     */
    public static WeaponData of(WeaponClass weaponClass, MobTier tier, ElementType element) {
        return new WeaponData(MobTier.NORMAL, weaponClass, weaponClass.defaultStat(),
                MobTier.NORMAL.defaultRequirement(), ElementType.NONE, WeaponSkill.NONE.id());
    }

    /** Usado quando um ModularWeaponItem não tem componente (ex.: pego do menu criativo). */
    public static WeaponData defaultFor(WeaponClass weaponClass) {
        return of(weaponClass, MobTier.NORMAL, ElementType.NONE);
    }

    /** Compatibilidade de API: o construtor preserva apenas o requisito padrão Normal da classe. */
    public WeaponData withRequirement(int value) {
        return new WeaponData(mobTier, weaponClass, requiredStatType, value, elementType, skillId);
    }

    /** Compatibilidade de API: habilidades não são mais concedidas pelo ItemStack. */
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
