package com.tarcitani.solomod.mob;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

/**
 * Animações de corpo inteiro tocadas pelo cliente (client/MobAnimations) sobre o modelo vanilla do mob.
 * O servidor só diz QUAL animação e por quantos ticks; a forma é calculada no cliente.
 */
public enum MobAnim {
    /** Só sincroniza a identidade (tier/elemento/fase), sem animação de ação. */
    NONE,
    /** Preparação de golpe: agacha e treme (telegrafa o ataque). */
    WINDUP,
    /** Bote curto do Lacaio: inclina para frente e estica. */
    LUNGE,
    /** Investida: corpo inclinado e esticado durante a corrida. */
    DASH,
    /** Sequência de golpes: torção de tronco de um lado para o outro. */
    COMBO,
    /** Esquiva/aparar: tomba para o lado. */
    DODGE,
    /** Conjuração: levita e gira devagar. */
    CAST,
    /** Salto: rodopio no ar. */
    LEAP,
    /** Impacto no chão: achata e volta. */
    SLAM,
    /** Rugido: incha e treme, inclinando para trás. */
    ROAR,
    /** Grito de alerta do Lacaio: joga a cabeça para trás. */
    HOWL,
    /** Teleporte: encolhe até sumir e reaparece. */
    BLINK,
    /** Invocação: sobe girando. */
    SUMMON,
    /** Troca de fase do Soberano: cresce, gira e treme. */
    PHASE,
    /** Sopro elemental: inclina para frente e vibra. */
    BREATH,
    /** Fim da Fúria: corpo caído para a frente, ofegante. */
    EXHAUSTED;

    private static final IntFunction<MobAnim> BY_ID =
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, MobAnim> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
}
