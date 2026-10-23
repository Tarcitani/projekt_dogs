package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.ElementType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor -> cliente: um acerto aconteceu (para o número de dano flutuante).
 *
 * @param entityId  entidade atingida
 * @param amount    dano final aplicado
 * @param element   elemento do golpe (NONE = físico/travado)
 * @param kind      0 = normal, 1 = vantagem elemental, 2 = desvantagem, 3 = habilidade, 4 = crítico
 */
public record HitFeedbackPayload(int entityId, float amount, ElementType element, int kind) implements CustomPacketPayload {
    public static final int NORMAL = 0, ADVANTAGE = 1, DISADVANTAGE = 2, SKILL = 3, CRIT = 4;

    public static final Type<HitFeedbackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "hit_feedback"));

    public static final StreamCodec<ByteBuf, HitFeedbackPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HitFeedbackPayload::entityId,
            ByteBufCodecs.FLOAT, HitFeedbackPayload::amount,
            ElementType.STREAM_CODEC, HitFeedbackPayload::element,
            ByteBufCodecs.VAR_INT, HitFeedbackPayload::kind,
            HitFeedbackPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Só roda no cliente; a classe de cliente só é carregada quando este método executa. */
    public static void handle(HitFeedbackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.DamageNumbers.onHit(payload));
    }
}
