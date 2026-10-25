package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.skill.DodgeRoll;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Pedido C→S do duplo Ctrl: só eixos locais -1/0/1; o servidor decide direção, velocidade e recarga. */
public record DodgeRollPayload(byte forward, byte strafe) implements CustomPacketPayload {
    public static final Type<DodgeRollPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "dodge_roll"));
    public static final StreamCodec<ByteBuf, DodgeRollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, DodgeRollPayload::forward, ByteBufCodecs.BYTE, DodgeRollPayload::strafe, DodgeRollPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DodgeRollPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) DodgeRoll.request(player, payload.forward(), payload.strafe());
        });
    }
}
