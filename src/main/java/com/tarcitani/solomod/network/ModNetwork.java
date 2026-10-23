package com.tarcitani.solomod.network;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.skill.PlayerAnim;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registro de pacotes e helper de sincronização. */
public final class ModNetwork {
    private ModNetwork() {}

    private static final String PROTOCOL_VERSION = "1";

    /** Listener do mod bus (registrado em SoloMod). */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // Servidor -> cliente
        registrar.playToClient(SyncStatsPayload.TYPE, SyncStatsPayload.STREAM_CODEC, SyncStatsPayload::handle);
        registrar.playToClient(HitFeedbackPayload.TYPE, HitFeedbackPayload.STREAM_CODEC, HitFeedbackPayload::handle);
        registrar.playToClient(MobAnimPayload.TYPE, MobAnimPayload.STREAM_CODEC, MobAnimPayload::handle);
        registrar.playToClient(PlayerAnimPayload.TYPE, PlayerAnimPayload.STREAM_CODEC, PlayerAnimPayload::handle);
        registrar.playToClient(CinematicPayload.TYPE, CinematicPayload.STREAM_CODEC, CinematicPayload::handle);
        // Cliente -> servidor (botões da tela de Maestria, giro da espada longa)
        registrar.playToServer(SpendPointsPayload.TYPE, SpendPointsPayload.STREAM_CODEC, SpendPointsPayload::handle);
        registrar.playToServer(TestActionPayload.TYPE, TestActionPayload.STREAM_CODEC, TestActionPayload::handle);
        registrar.playToServer(SpinAttackPayload.TYPE, SpinAttackPayload.STREAM_CODEC, SpinAttackPayload::handle);
    }

    /**
     * Animação de habilidade de um jogador. {@code includeSelf = false} quando o próprio cliente já
     * começou a animação sozinho (giro da espada longa), para não reiniciá-la.
     */
    public static void sendPlayerAnim(ServerPlayer player, PlayerAnim anim, int duration, boolean includeSelf) {
        PlayerAnimPayload payload = new PlayerAnimPayload(player.getId(), anim, duration);
        if (includeSelf) PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
        else PacketDistributor.sendToPlayersTrackingEntity(player, payload);
    }

    /** Cinemática de abate do Soberano para os jogadores próximos. */
    public static void sendCinematic(ServerPlayer player, CinematicPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    /** Envia os stats atuais do servidor para o próprio jogador. */
    public static void syncStats(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncStatsPayload(player.getData(ModAttachments.PLAYER_STATS)));
    }

    /** Número de dano flutuante: vai para todos que enxergam o alvo. */
    public static void sendHit(Entity target, float amount, ElementType element, int kind) {
        PacketDistributor.sendToPlayersTrackingEntity(target, new HitFeedbackPayload(target.getId(), amount, element, kind));
    }

    /** Identidade/animação de um mob do mod: vai para todos que enxergam o mob. */
    public static void sendMobAnim(Entity mob, MobAnimPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(mob, payload);
    }

    /** Identidade de um mob para um único jogador (quando ele começa a enxergar o mob). */
    public static void sendMobAnim(ServerPlayer player, MobAnimPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
