package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.IntFunction;

/**
 * Servidor -> cliente: um momento de chefe. O cliente entra em modo cinemático: faixas pretas, HUD escondido,
 * câmera virando para o chefe e um letreiro (cabeçalho na faixa de cima, título na de baixo).
 *
 * @param entityId chefe (a câmera mira nele)
 * @param duration duração total em ticks
 * @param kind     tipo de cena (ver {@link Kind})
 * @param header   letreiro de cima (ex.: "MONARCA DERROTADO")
 * @param title    letreiro de baixo (nome do chefe ou a fala do estágio)
 */
public record CinematicPayload(int entityId, int duration, Kind kind, Component header, Component title)
        implements CustomPacketPayload {

    /** Não reordene: vai pela rede como ordinal. */
    public enum Kind {
        /** Monarca caiu: câmera no corpo que se desintegra, zoom leve. */
        DEFEAT,
        /** Estágio do Soberano: close no rosto (zoom forte) enquanto ele agoniza. */
        CLOSE_UP,
        /** Soberano caiu: como DEFEAT, e no fim abre os créditos. */
        FINALE,
        /** Soberano ri de um jogador que acabou de matar: close no rosto, como CLOSE_UP. */
        LAUGH;

        private static final IntFunction<Kind> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, Kind> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }

    public static final Type<CinematicPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "cinematic"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CinematicPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CinematicPayload::entityId,
            ByteBufCodecs.VAR_INT, CinematicPayload::duration,
            Kind.STREAM_CODEC, CinematicPayload::kind,
            ComponentSerialization.STREAM_CODEC, CinematicPayload::header,
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
