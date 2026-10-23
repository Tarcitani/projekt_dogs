package com.tarcitani.solomod.attachment;

import com.tarcitani.solomod.SoloMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Registro dos Data Attachments do mod. */
public final class ModAttachments {
    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SoloMod.MOD_ID);

    /**
     * Stats do jogador.
     * serialize(CODEC) = salva com a entidade; copyOnDeath() = não perde progresso ao morrer.
     * A sincronização com o cliente é manual (SyncStatsPayload), pois o sync automático de
     * attachments só chegou em versões posteriores do NeoForge.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerStatsAttachment>> PLAYER_STATS =
            ATTACHMENT_TYPES.register("player_stats", () -> AttachmentType.builder(() -> new PlayerStatsAttachment())
                    .serialize(PlayerStatsAttachment.CODEC)
                    .copyOnDeath()
                    .build());

    /** Tier/elemento de um mob. Só existe nos mobs marcados (use hasData para testar). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobProfile>> MOB_PROFILE =
            ATTACHMENT_TYPES.register("mob_profile", () -> AttachmentType.builder(() -> MobProfile.NONE)
                    .serialize(MobProfile.CODEC)
                    .build());

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
