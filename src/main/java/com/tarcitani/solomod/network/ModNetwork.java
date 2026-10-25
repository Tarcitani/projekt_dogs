package com.tarcitani.solomod.network;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.skill.PlayerAnim;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registro do protocolo 5: stats com árvores, pedidos de desbloqueio e recarga de rolamento, além do combate. */
public final class ModNetwork {
    private ModNetwork() {}

    private static final String PROTOCOL_VERSION = "5";

    /** Listener do mod bus (registrado em SoloMod). */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // Servidor -> cliente
        registrar.playToClient(SyncStatsPayload.TYPE, SyncStatsPayload.STREAM_CODEC, SyncStatsPayload::handle);
        registrar.playToClient(HitFeedbackPayload.TYPE, HitFeedbackPayload.STREAM_CODEC, HitFeedbackPayload::handle);
        registrar.playToClient(MobAnimPayload.TYPE, MobAnimPayload.STREAM_CODEC, MobAnimPayload::handle);
        registrar.playToClient(PlayerAnimPayload.TYPE, PlayerAnimPayload.STREAM_CODEC, PlayerAnimPayload::handle);
        registrar.playToClient(CinematicPayload.TYPE, CinematicPayload.STREAM_CODEC, CinematicPayload::handle);
        registrar.playToClient(ShakePayload.TYPE, ShakePayload.STREAM_CODEC, ShakePayload::handle);
        registrar.playToClient(RollAnimationPayload.TYPE, RollAnimationPayload.STREAM_CODEC, RollAnimationPayload::handle);
        registrar.playToClient(CrescentSlashPayload.TYPE, CrescentSlashPayload.STREAM_CODEC, CrescentSlashPayload::handle);
        registrar.playToClient(WeaponMotionPayload.TYPE, WeaponMotionPayload.STREAM_CODEC, WeaponMotionPayload::handle);
        registrar.playToClient(TechniqueSelectionPayload.TYPE, TechniqueSelectionPayload.STREAM_CODEC, TechniqueSelectionPayload::handle);
        registrar.playToClient(RollCooldownPayload.TYPE, RollCooldownPayload.STREAM_CODEC, RollCooldownPayload::handle);
        // Cliente -> servidor (Maestria e pedido legado da espada longa; uso das armas segue o ciclo vanilla)
        registrar.playToServer(UnlockTalentPayload.TYPE, UnlockTalentPayload.STREAM_CODEC, UnlockTalentPayload::handle);
        registrar.playToServer(SpendPointsPayload.TYPE, SpendPointsPayload.STREAM_CODEC, SpendPointsPayload::handle);
        registrar.playToServer(TestActionPayload.TYPE, TestActionPayload.STREAM_CODEC, TestActionPayload::handle);
        registrar.playToServer(SpinAttackPayload.TYPE, SpinAttackPayload.STREAM_CODEC, SpinAttackPayload::handle);
        registrar.playToServer(DodgeRollPayload.TYPE, DodgeRollPayload.STREAM_CODEC, DodgeRollPayload::handle);
        registrar.playToServer(CycleTechniquePayload.TYPE, CycleTechniquePayload.STREAM_CODEC, CycleTechniquePayload::handle);
    }

    /** Mostra efeitos tanto para quem viu a partida quanto para quem está perto da chegada do teleporte. */
    public static void sendWeaponMotion(ServerPlayer player, WeaponMotionPayload.Kind kind, int entityId,
                                        net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to, float power) {
        WeaponMotionPayload payload = new WeaponMotionPayload(kind, entityId, from, to, power);
        for (ServerPlayer viewer : player.serverLevel().players()) {
            if (viewer == player || viewer.distanceToSqr(from) < 48 * 48 || viewer.distanceToSqr(to) < 48 * 48)
                PacketDistributor.sendToPlayer(viewer, payload);
        }
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

    /** Cinemática de chefe (morte do Monarca, agonia ou morte do Soberano) para um jogador. */
    public static void sendCinematic(ServerPlayer player, CinematicPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    /** Treme a tela de todos os jogadores a até {@code radius} blocos de {@code at}, mais fraco com a distância. */
    public static void sendShake(net.minecraft.server.level.ServerLevel level, net.minecraft.world.phys.Vec3 at,
                                 double radius, float intensity, int duration) {
        for (ServerPlayer player : level.players()) {
            double d = Math.sqrt(player.distanceToSqr(at));
            if (d > radius) continue;
            PacketDistributor.sendToPlayer(player, new ShakePayload(intensity * (float) (1.0 - 0.7 * d / radius), duration));
        }
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
