package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.event.StatsLogic;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** C→S: solicita um nó por ID. Pontos, dependências e duplicatas são validados no servidor. */
public record UnlockTalentPayload(String id) implements CustomPacketPayload {
    public static final Type<UnlockTalentPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "unlock_talent"));
    public static final StreamCodec<ByteBuf, UnlockTalentPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128), UnlockTalentPayload::id, UnlockTalentPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(UnlockTalentPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
                    && StatsLogic.stats(player).unlockTalent(payload.id())) StatsLogic.onStatsChanged(player);
        });
    }
}
