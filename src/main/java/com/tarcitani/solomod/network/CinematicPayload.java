package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor -> cliente: o Soberano caiu. O cliente entra em modo cinemático: faixas pretas, HUD escondido,
 * câmera virando para o chefe que se desintegra e o letreiro do abate.
 *
 * @param entityId chefe derrotado (a câmera mira nele)
 * @param duration duração total em ticks
 * @param title    nome do chefe para o letreiro
 */
public record CinematicPayload(int entityId, int duration, Component title) implements CustomPacketPayload {
    public static final Type<CinematicPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "cinematic"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CinematicPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CinematicPayload::entityId,
            ByteBufCodecs.VAR_INT, CinematicPayload::duration,
            ComponentSerialization.STREAM_CODEC, CinematicPayload::title,
            CinematicPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Só roda no cliente; a classe de cliente só é carregada quando este método executa. */
    public static void handle(CinematicPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.BossCinematic.start(payload));
    }
}
