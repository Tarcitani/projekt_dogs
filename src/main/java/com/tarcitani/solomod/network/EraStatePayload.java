package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Servidor -> cliente: estado da Era VII ({@code era/EraSevenState}). Hoje só diz se o Soberano já caiu. */
public record EraStatePayload(boolean sovereignDefeated) implements CustomPacketPayload {
    public static final Type<EraStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "era_state"));

    public static final StreamCodec<ByteBuf, EraStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EraStatePayload::sovereignDefeated,
            EraStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EraStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.ClientEraState.set(payload.sovereignDefeated()));
    }
}
