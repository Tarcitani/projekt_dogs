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
 * Pacote legado da espada longa, mantido com o mesmo identificador. O cliente atual usa o botão direito
 * vanilla; receber este pacote pede Corte em Meia-Lua, sujeito às mesmas validações e recarga.
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
