package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.skill.ClassAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** C→S: pede a próxima técnica; servidor infere a arma na mão e valida estado, sem aceitar classe/índice do cliente. */
public record CycleTechniquePayload() implements CustomPacketPayload {
    public static final CycleTechniquePayload INSTANCE = new CycleTechniquePayload();
    public static final Type<CycleTechniquePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "cycle_technique"));
    public static final StreamCodec<ByteBuf, CycleTechniquePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(CycleTechniquePayload p, IPayloadContext context) {
        context.enqueueWork(() -> { if (context.player() instanceof ServerPlayer player) ClassAbility.cycleTechnique(player); });
    }
}
