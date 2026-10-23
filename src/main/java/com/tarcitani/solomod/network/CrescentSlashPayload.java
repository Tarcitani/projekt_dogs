package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.skill.WeaponTechnique;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** S→C: rastro volumétrico da técnica da espada, com direção travada, elemento e técnica definidos pelo servidor. */
public record CrescentSlashPayload(int entityId, float dirX, float dirZ, ElementType element, WeaponTechnique technique) implements CustomPacketPayload {
    public static final Type<CrescentSlashPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "crescent_slash"));
    public static final StreamCodec<ByteBuf, CrescentSlashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CrescentSlashPayload::entityId,
            ByteBufCodecs.FLOAT, CrescentSlashPayload::dirX, ByteBufCodecs.FLOAT, CrescentSlashPayload::dirZ,
            ElementType.STREAM_CODEC, CrescentSlashPayload::element,
            WeaponTechnique.STREAM_CODEC, CrescentSlashPayload::technique, CrescentSlashPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CrescentSlashPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.WeaponEffects.start(payload));
    }
}
