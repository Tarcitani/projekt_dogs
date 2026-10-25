package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor -> cliente: silêncio por {@code ticks} ticks (o sino da Torre dos Ventos Quebrados). O servidor renova
 * enquanto o jogador está perto; ao sair, o silêncio acaba sozinho.
 */
public record AmbientSilencePayload(int ticks) implements CustomPacketPayload {
    public static final Type<AmbientSilencePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "ambient_silence"));

    public static final StreamCodec<ByteBuf, AmbientSilencePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AmbientSilencePayload::ticks,
            AmbientSilencePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AmbientSilencePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.AmbientSilence.silenceFor(payload.ticks()));
    }
}
