package com.tarcitani.solomod.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

/** Animações das armas e técnicas com escudo (client/PlayerAnimations); ordinais antigos são preservados. */
public enum PlayerAnim {
    /** Espada longa: giro completo de 360° com a claymore. */
    SPIN,
    /** Rapieira: corpo inclinado e esticado na estocada. */
    THRUST,
    /** Martelo e machado legado: mortal para a frente no ar. */
    LEAP_FLIP,
    /** Martelo e machado legado: impacto no chão (achata o corpo e treme a câmera de quem caiu). */
    SLAM,
    /** Espada longa: preparação e varredura horizontal de meia-lua, sem girar o corpo inteiro. */
    CRESCENT,
    /** Martelo: mergulho com a arma erguida para o impacto. */
    HAMMER_DIVE,
    /** Adaga: arremesso com o braço esticado. */
    DAGGER_THROW,
    /** Adaga: puxada do braço ao chamar a arma de volta. */
    DAGGER_RECALL,
    /** Adaga: desaparecimento e reaparição no teleporte. */
    DAGGER_BLINK,
    /** Esquiva: corpo recolhido em cambalhota na direção do deslocamento. */
    ROLL,
    /** Espada longa: golpe vertical ascendente de duas mãos. */
    RISING_CUT,
    /** Espada longa: dois cortes diagonais em sequência. */
    CROSS_CUT,
    /** Rapieira: três estocadas precisas, alinhadas aos acertos do servidor. */
    RAPIER_FLURRY,
    /** Rapieira: estocada e passo rápido para trás. */
    RAPIER_RETREAT,
    /** Martelo: giro de duas mãos com varredura circular. */
    HAMMER_SWEEP,
    /** Martelo: preparação sobre a cabeça e golpe que abre uma onda no chão. */
    HAMMER_QUAKE,
    /** Técnica de uma mão; a classe e a mão da arma vêm do equipamento sincronizado. */
    SHIELD_STRIKE;

    private static final IntFunction<PlayerAnim> BY_ID =
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, PlayerAnim> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
}
