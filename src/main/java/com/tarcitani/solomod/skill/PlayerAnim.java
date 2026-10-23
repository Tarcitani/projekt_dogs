package com.tarcitani.solomod.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

/** Animações de corpo inteiro do jogador ao usar as habilidades das armas (client/PlayerAnimations). */
public enum PlayerAnim {
    /** Espada longa: giro completo de 360° com a claymore. */
    SPIN,
    /** Rapieira: corpo inclinado e esticado na estocada. */
    THRUST,
    /** Machado: mortal para a frente no ar. */
    LEAP_FLIP,
    /** Machado: impacto no chão (achata o corpo e treme a câmera de quem caiu). */
    SLAM;

    private static final IntFunction<PlayerAnim> BY_ID =
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, PlayerAnim> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
}
