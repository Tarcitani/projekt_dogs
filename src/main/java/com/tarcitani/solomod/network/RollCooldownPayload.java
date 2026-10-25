package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** S→C: ticks restantes da recarga do rolamento; zero limpa a barra após morte/troca de dimensão. */
public record RollCooldownPayload(int ticks) implements CustomPacketPayload {
    public static final Type<RollCooldownPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "roll_cooldown"));
    public static final StreamCodec<ByteBuf, RollCooldownPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RollCooldownPayload::ticks, RollCooldownPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(RollCooldownPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.SoloHud.rollCooldown(payload.ticks()));
    }
}
