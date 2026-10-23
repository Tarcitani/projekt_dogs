package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor -> cliente: a tela do jogador treme (terremoto do pisão, pouso do Soberano...). A força cai até
 * zero ao longo da duração; um novo tremor mais forte substitui o que está acabando.
 *
 * @param intensity força inicial (1 = pisão do Soberano; 0,3 é um tremor leve)
 * @param duration  duração em ticks
 */
public record ShakePayload(float intensity, int duration) implements CustomPacketPayload {
    public static final Type<ShakePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "shake"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ShakePayload::intensity,
            ByteBufCodecs.VAR_INT, ShakePayload::duration,
            ShakePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Só roda no cliente; a classe de cliente só é carregada quando este método executa. */
    public static void handle(ShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.ScreenShake.start(payload));
    }
}
