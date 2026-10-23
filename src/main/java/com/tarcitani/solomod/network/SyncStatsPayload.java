package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor -> cliente: cópia completa dos stats do jogador local.
 * O cliente precisa disso para pintar o tooltip (verde/vermelho) e mostrar a mana.
 */
public record SyncStatsPayload(PlayerStatsAttachment stats) implements CustomPacketPayload {
    public static final Type<SyncStatsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "sync_stats"));

    public static final StreamCodec<FriendlyByteBuf, SyncStatsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> payload.stats().write(buf),
            buf -> new SyncStatsPayload(PlayerStatsAttachment.read(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Executa no cliente: sobrescreve o attachment do jogador local. */
    public static void handle(SyncStatsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> context.player().getData(ModAttachments.PLAYER_STATS).copyFrom(payload.stats()));
    }
}
