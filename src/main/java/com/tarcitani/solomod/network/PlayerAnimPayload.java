package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.skill.PlayerAnim;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Servidor -> cliente: um jogador começou uma animação de habilidade. */
public record PlayerAnimPayload(int entityId, PlayerAnim anim, int duration) implements CustomPacketPayload {
    public static final Type<PlayerAnimPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "player_anim"));

    public static final StreamCodec<ByteBuf, PlayerAnimPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayerAnimPayload::entityId,
            PlayerAnim.STREAM_CODEC, PlayerAnimPayload::anim,
            ByteBufCodecs.VAR_INT, PlayerAnimPayload::duration,
            PlayerAnimPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Só roda no cliente; a classe de cliente só é carregada quando este método executa. */
    public static void handle(PlayerAnimPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.PlayerAnimations.onPayload(payload));
    }
}
