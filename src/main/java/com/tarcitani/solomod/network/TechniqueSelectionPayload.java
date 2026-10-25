package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.skill.WeaponTechnique;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** S→C: técnica selecionada por classe nesta sessão; a classe vem do enum, nunca de um pedido do cliente. */
public record TechniqueSelectionPayload(WeaponTechnique technique) implements CustomPacketPayload {
    public static final Type<TechniqueSelectionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "technique_selection"));
    public static final StreamCodec<ByteBuf, TechniqueSelectionPayload> STREAM_CODEC = WeaponTechnique.STREAM_CODEC.map(TechniqueSelectionPayload::new, TechniqueSelectionPayload::technique);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(TechniqueSelectionPayload p, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.WeaponControls.accept(p.technique()));
    }
}
