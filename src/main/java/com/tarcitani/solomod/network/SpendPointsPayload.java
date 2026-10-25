package com.tarcitani.solomod.network;

import com.tarcitani.solomod.advancement.ModAdvancements;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.StatType;
import com.tarcitani.solomod.event.StatsLogic;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Cliente -> servidor: "gaste N pontos neste stat" (botões + da tela de Maestria).
 * O servidor revalida tudo: o cliente nunca define stats diretamente.
 */
public record SpendPointsPayload(StatType stat, int amount) implements CustomPacketPayload {
    public static final Type<SpendPointsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "spend_points"));

    public static final StreamCodec<ByteBuf, SpendPointsPayload> STREAM_CODEC = StreamCodec.composite(
            StatType.STREAM_CODEC, SpendPointsPayload::stat,
            ByteBufCodecs.VAR_INT, SpendPointsPayload::amount,
            SpendPointsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpendPointsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            int amount = Math.min(Math.max(payload.amount(), 1), 100); // limita abuso de pacote
            if (StatsLogic.stats(player).spendPoints(payload.stat(), amount) > 0) {
                ModAdvancements.onPointsSpent(player);
                StatsLogic.onStatsChanged(player);
            }
        });
    }
}
