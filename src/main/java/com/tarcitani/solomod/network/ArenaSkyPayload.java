package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor -> cliente: o céu costurado do Anfiteatro do Selo Partido vale por mais {@code ticks} ticks. O servidor
 * renova enquanto o jogador está na arena; ao sair, o céu volta ao normal sozinho.
 */
public record ArenaSkyPayload(int ticks) implements CustomPacketPayload {
    public static final Type<ArenaSkyPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "arena_sky"));

    public static final StreamCodec<ByteBuf, ArenaSkyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ArenaSkyPayload::ticks,
            ArenaSkyPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ArenaSkyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.ArenaSkyRenderer.showFor(payload.ticks()));
    }
}
