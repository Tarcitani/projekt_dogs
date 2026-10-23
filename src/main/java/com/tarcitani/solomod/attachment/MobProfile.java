package com.tarcitani.solomod.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;

/**
 * Perfil de um mob "do mod": tier e elemento do alvo.
 *
 * <p>Enquanto não existem entidades próprias, qualquer mob vira "mob do mod" ao receber este
 * attachment (via {@code /modtest mob ...}). Só mobs com perfil concedem XP Global.
 */
public record MobProfile(MobTier tier, ElementType element) {
    public static final MobProfile NONE = new MobProfile(MobTier.NORMAL, ElementType.NONE);

    public static final Codec<MobProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MobTier.CODEC.optionalFieldOf("tier", MobTier.NORMAL).forGetter(MobProfile::tier),
            ElementType.CODEC.optionalFieldOf("element", ElementType.NONE).forGetter(MobProfile::element)
    ).apply(instance, MobProfile::new));
}
