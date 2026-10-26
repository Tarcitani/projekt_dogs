package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.skill.ShieldAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** C→S: Z apenas pede a habilidade; equipamento, classe, alvo, custo e recarga são do servidor. */
public record ShieldAbilityPayload() implements CustomPacketPayload {
    public static final ShieldAbilityPayload INSTANCE = new ShieldAbilityPayload();
    public static final Type<ShieldAbilityPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "shield_ability"));
    public static final StreamCodec<ByteBuf, ShieldAbilityPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(ShieldAbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) ShieldAbility.request(player);
        });
    }
}
