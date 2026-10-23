package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.skill.ClassAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Cliente -> servidor: clique de ataque com a espada longa. O ataque vanilla é cancelado no cliente e
 * trocado por um giro de 360° que atinge todos em volta (validado no servidor).
 */
public record SpinAttackPayload() implements CustomPacketPayload {
    public static final SpinAttackPayload INSTANCE = new SpinAttackPayload();
    public static final Type<SpinAttackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "spin_attack"));
    public static final StreamCodec<ByteBuf, SpinAttackPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpinAttackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) ClassAbility.spinAttack(player);
        });
    }
}
