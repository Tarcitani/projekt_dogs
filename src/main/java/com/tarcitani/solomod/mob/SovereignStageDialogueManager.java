package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.network.CinematicPayload;
import com.tarcitani.solomod.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Falas e efeitos de roteiro do Soberano (lore: docs/LORE.md, "A Dissonância"). O combate continua no
 * {@link SovereignBrain}; esta classe só decide <b>o que</b> ele diz e <b>quando</b>, e cuida da cena da morte.
 *
 * <ul>
 *   <li><b>Intro</b> (primeiro alvo, estágio 1) e <b>início de cada estágio</b> (fim da agonia): na barra de ação,
 *       porque fora da cinemática o HUD está visível.</li>
 *   <li><b>Transição</b> (começo da agonia, 80 ticks ajoelhado): no letreiro de baixo da cinemática de close.
 *       A transição para o estágio 6 também vaza partículas de glitch roxo ({@link #furyLeakTick}).</li>
 *   <li><b>Raio do Peito</b> e <b>exaustão</b> depois dele: barra de ação.</li>
 *   <li><b>Risada</b> ao matar um jogador: uma de três falas, no letreiro da cinemática de risada.</li>
 *   <li><b>Morte por glitch</b>: fala 1 no letreiro da cena final; a fala 2 troca o letreiro 50 ticks depois
 *       (sem reiniciar a cena) e, aos 100, todo o servidor recebe o aviso da Era VII. Durante a desintegração o
 *       corpo solta pó ciano e vermelho ({@link #glitchTick}), junto dos bipes de 8 bits de {@code Sovereign}.</li>
 * </ul>
 *
 * Os textos ficam em {@code lang/*.json} ({@code dialogue.solomod.sovereign.*}). Tarefas atrasadas são do servidor,
 * avançadas por {@link #tick} (chamado em {@code MobEvents}) e zeradas em {@link #clear}; não são salvas.
 */
public final class SovereignStageDialogueManager {
    private SovereignStageDialogueManager() {}

    private static final String KEY = "dialogue.solomod.sovereign.";
    /** Ticks entre a intro e a fala de início do estágio 1. */
    private static final int INTRO_TO_STAGE_ONE = 100;
    /** Na cena da morte: quando a fala 2 substitui a 1, e quando o servidor inteiro ouve o estalo. */
    private static final int DEATH_SECOND_LINE = 50, DEATH_BROADCAST = 100;
    /** Cores da Dissonância (ciano #00E5FF e vermelho #FF1744). */
    private static final DustParticleOptions GLITCH_CYAN = new DustParticleOptions(new Vector3f(0.0F, 0.9F, 1.0F), 2.2F);
    private static final DustParticleOptions GLITCH_RED = new DustParticleOptions(new Vector3f(1.0F, 0.09F, 0.27F), 2.2F);
    private static final DustParticleOptions GLITCH_PURPLE = new DustParticleOptions(new Vector3f(0.62F, 0.15F, 1.0F), 2.0F);
    private static final int LAUGH_LINES = 3;

    private record Pending(long due, Runnable action) {}

    private static final List<Pending> PENDING = new ArrayList<>();
    private static long now;

    // ------------------------------------------------------------------ textos

    /** Fala com o nome do Soberano na frente: "Soberano: «...»". */
    public static Component speech(Component line) {
        MutableComponent name = Component.translatable(KEY + "speaker").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        return name.append(Component.literal(": ").withStyle(ChatFormatting.DARK_PURPLE))
                .append(line.copy().withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
    }

    public static Component line(String id) {
        return Component.translatable(KEY + id);
    }

    /** Fala da transição ao SAIR do estágio {@code leaving} (1 a 5): vai no letreiro do close. */
    public static Component transitionLine(int leaving) {
        return line("stage." + leaving + ".transition");
    }

    public static Component laughLine(RandomSource random) {
        return line("laugh." + (1 + random.nextInt(LAUGH_LINES)));
    }

    // ------------------------------------------------------------------ gatilhos do SovereignBrain

    /** Primeiro alvo da luta (estágio 1): intro agora e a fala do estágio 1 logo depois. */
    public static void intro(Collection<ServerPlayer> audience) {
        say(audience, line("intro"));
        List<UUID> ids = ids(audience);
        MinecraftServer server = serverOf(audience);
        if (server == null) return;
        later(INTRO_TO_STAGE_ONE, () -> say(online(server, ids), line("stage.1.start")));
    }

    /** Fim da agonia: o poder do novo estágio desperta e ele anuncia (o estágio 6 fala pelo raio). */
    public static void stageStart(int stage, Collection<ServerPlayer> audience) {
        if (stage >= 2 && stage < SovereignBrain.STAGES) say(audience, line("stage." + stage + ".start"));
    }

    public static void beamFired(Collection<ServerPlayer> audience) {
        say(audience, line("beam"));
    }

    public static void beamExhausted(Collection<ServerPlayer> audience) {
        say(audience, line("exhausted"));
    }

    /**
     * Morte: a cena final já mostra a fala 1; aqui se agenda a troca para a fala 2 e o aviso a todo o servidor.
     * {@code header} é o mesmo cabeçalho da cena, reenviado com a fala 2 (o cliente só troca o texto).
     */
    public static void death(Entity sovereign, int duration, Component header, Collection<ServerPlayer> audience) {
        if (!(sovereign.level() instanceof ServerLevel level)) return;
        MinecraftServer server = level.getServer();
        int entityId = sovereign.getId();
        List<UUID> ids = ids(audience);
        later(DEATH_SECOND_LINE, () -> {
            CinematicPayload update = new CinematicPayload(entityId, duration, CinematicPayload.Kind.FINALE, header,
                    line("death.2"));
            for (ServerPlayer player : online(server, ids)) ModNetwork.sendCinematic(player, update);
        });
        later(DEATH_BROADCAST, () -> server.getPlayerList().broadcastSystemMessage(eraSevenBroadcast(), false));
    }

    /** "O Trono está vazio...": roxo, com bordas embaralhadas (obfuscated). */
    public static Component eraSevenBroadcast() {
        Component border = Component.literal("▓▓▓").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.OBFUSCATED);
        return Component.empty().append(border).append(Component.literal(" "))
                .append(Component.translatable("message.solomod.era_vii.broadcast")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .append(Component.literal(" ")).append(border);
    }

    // ------------------------------------------------------------------ efeitos

    /** Agonia que leva à Fúria (estágio 6): a melodia rasga e vaza glitch roxo pelo corpo. */
    public static void furyLeakTick(ServerLevel level, Entity sovereign, int t) {
        if (t % 2 != 0) return;
        double w = sovereign.getBbWidth() * 0.5;
        double y = sovereign.getY() + sovereign.getRandom().nextDouble() * sovereign.getBbHeight();
        Hazards.farParticles(level, GLITCH_PURPLE, sovereign.getX(), y, sovereign.getZ(), 10, w, 1.5, w, 0);
        Hazards.farParticles(level, ParticleTypes.REVERSE_PORTAL, sovereign.getX(), y, sovereign.getZ(), 6, w, 1.0, w, 0.3);
    }

    /** Desintegração: o corpo se separa em canais, ciano de um lado e vermelho do outro, cada vez mais. */
    public static void glitchTick(ServerLevel level, Entity sovereign, int ticks) {
        double w = sovereign.getBbWidth() * 0.5;
        int bursts = 1 + ticks / 25;
        for (int i = 0; i < bursts; i++) {
            double y = sovereign.getY() + sovereign.getRandom().nextDouble() * sovereign.getBbHeight();
            double shift = 0.6 + ticks * 0.01;
            Hazards.farParticles(level, GLITCH_CYAN, sovereign.getX() - shift, y, sovereign.getZ(), 8, w, 1.2, w, 0);
            Hazards.farParticles(level, GLITCH_RED, sovereign.getX() + shift, y, sovereign.getZ(), 8, w, 1.2, w, 0);
        }
    }

    // ------------------------------------------------------------------ agenda

    /** Chamado a cada tick do servidor (MobEvents). */
    public static void tick() {
        now++;
        Iterator<Pending> it = PENDING.iterator();
        List<Runnable> ready = new ArrayList<>();
        while (it.hasNext()) {
            Pending p = it.next();
            if (p.due() <= now) {
                ready.add(p.action());
                it.remove();
            }
        }
        ready.forEach(Runnable::run);
    }

    public static void clear() {
        PENDING.clear();
        now = 0;
    }

    /** Roda {@code action} daqui a {@code ticks} ticks de servidor (não sobrevive a reinício). */
    public static void schedule(int ticks, Runnable action) {
        later(ticks, action);
    }

    private static void later(int ticks, Runnable action) {
        PENDING.add(new Pending(now + ticks, action));
    }

    // ------------------------------------------------------------------ util

    private static void say(Collection<ServerPlayer> audience, Component line) {
        Component message = speech(line);
        for (ServerPlayer player : audience) player.displayClientMessage(message, true);
    }

    private static List<UUID> ids(Collection<ServerPlayer> audience) {
        List<UUID> ids = new ArrayList<>();
        for (ServerPlayer player : audience) ids.add(player.getUUID());
        return ids;
    }

    /** Quem da plateia ainda está online quando a tarefa atrasada roda. */
    private static List<ServerPlayer> online(MinecraftServer server, List<UUID> ids) {
        List<ServerPlayer> players = new ArrayList<>();
        for (UUID id : ids) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) players.add(player);
        }
        return players;
    }

    private static MinecraftServer serverOf(Collection<ServerPlayer> audience) {
        for (ServerPlayer player : audience) return player.getServer();
        return null;
    }
}
