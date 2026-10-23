package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.mob.MobAnim;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor -> cliente: identidade de um mob do mod (tier/elemento/fase) e, opcionalmente, uma animação de ação.
 * Enviado quando o jogador passa a enxergar o mob e a cada ataque especial.
 *
 * @param entityId entidade
 * @param tier     tier do mob (define a animação ociosa)
 * @param element  elemento do mob
 * @param phase    fase do Monarca (1..3) ou estágio do Soberano (1..6); 1 para os demais
 * @param rage     chefe em Fúria (aura vermelha)
 * @param anim     animação a tocar ({@link MobAnim#NONE} = só identidade)
 * @param duration duração da animação em ticks
 */
public record MobAnimPayload(int entityId, MobTier tier, ElementType element, int phase, boolean rage, MobAnim anim,
                             int duration)
        implements CustomPacketPayload {
    public static final Type<MobAnimPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "mob_anim"));

    /** Escrito à mão: o composite do 1.21.1 vai só até 6 campos. */
    public static final StreamCodec<ByteBuf, MobAnimPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.VAR_INT.encode(buf, p.entityId());
                MobTier.STREAM_CODEC.encode(buf, p.tier());
                ElementType.STREAM_CODEC.encode(buf, p.element());
                ByteBufCodecs.VAR_INT.encode(buf, p.phase());
                ByteBufCodecs.BOOL.encode(buf, p.rage());
                MobAnim.STREAM_CODEC.encode(buf, p.anim());
                ByteBufCodecs.VAR_INT.encode(buf, p.duration());
            },
            buf -> new MobAnimPayload(ByteBufCodecs.VAR_INT.decode(buf), MobTier.STREAM_CODEC.decode(buf),
                    ElementType.STREAM_CODEC.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.BOOL.decode(buf),
                    MobAnim.STREAM_CODEC.decode(buf), ByteBufCodecs.VAR_INT.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Só roda no cliente; a classe de cliente só é carregada quando este método executa. */
    public static void handle(MobAnimPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.tarcitani.solomod.client.MobAnimations.onPayload(payload));
    }
}
