package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** S→C: anima o rolamento aceito, incluindo sua direção real para cambalhotas laterais e para trás. */
public record RollAnimationPayload(int entityId, float dirX, float dirZ) implements CustomPacketPayload {
    public static final Type<RollAnimationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "roll_animation"));
    public static final StreamCodec<ByteBuf, RollAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RollAnimationPayload::entityId,
            ByteBufCodecs.FLOAT, RollAnimationPayload::dirX, ByteBufCodecs.FLOAT, RollAnimationPayload::dirZ, RollAnimationPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RollAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.PlayerAnimations.onRoll(payload));
    }
}
