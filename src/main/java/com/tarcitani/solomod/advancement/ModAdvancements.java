package com.tarcitani.solomod.advancement;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.mob.SovereignStageDialogueManager;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Conquistas do Solo Mod em três abas (tecla L), alinhadas à lore (docs/LORE.md). Os JSON ficam em
 * {@code data/solomod/advancement/<aba>/} e são gerados por {@code tools/gen_advancements.py}.
 *
 * <p>Só a raiz da aba 1 ({@code born_in_silence}) usa um gatilho vanilla ({@code minecraft:tick}). Todos os outros
 * critérios usam {@code minecraft:impossible}: são os "gatilhos customizados" do mod, concedidos por este código
 * nos pontos do jogo onde o evento acontece. Contadores (cinco Monarcas, cinco linhagens, quinze Ecos, seis
 * guardiões) são critérios da própria conquista, então o progresso fica salvo junto dela, sem estado extra.
 *
 * <ul>
 *   <li><b>echo_song</b> (A Canção do Eco): {@link #onTierKill}, {@link #onPointsSpent},
 *       {@link #awardBossDefeat} (Monarcas), {@link #onRoyalFireballReflected}, {@link #onSovereignSeen},
 *       {@link #awardSovereignDefeat} (que agenda a secreta {@code error_sixth_voice} para 10 s depois).</li>
 *   <li><b>serene_echoes</b> (Ecos Serenos): {@link #onEchoBond}, {@link #onEchoExhausted},
 *       {@link #onEchoAstral}, {@link #onFrameJump}.</li>
 *   <li><b>seal_accomplices</b> (Os Cúmplices do Selo): {@link #onSanctuaryBossDefeat}.</li>
 * </ul>
 *
 * Conceder de novo um critério já concluído não faz nada, então os ganchos podem chamar sempre.
 */
public final class ModAdvancements {
    private ModAdvancements() {}

    private static final String SONG = "echo_song/", SERENE = "serene_echoes/", ACCOMPLICES = "seal_accomplices/";

    // Aba 1: A Canção do Eco
    public static final ResourceLocation TASTE_OF_VIOLENCE = id(SONG + "taste_of_violence");
    public static final ResourceLocation MUSCLE_MEMORY = id(SONG + "muscle_memory");
    public static final ResourceLocation BROKEN_GUARD = id(SONG + "broken_guard");
    public static final ResourceLocation RED_WHISPER = id(SONG + "red_whisper");
    public static final ResourceLocation SHATTERED_SEAL = id(SONG + "shattered_seal");
    public static final ResourceLocation MIRROR_FACE = id(SONG + "mirror_face");
    public static final ResourceLocation ABSOLUTE_DISSONANCE = id(SONG + "absolute_dissonance");
    public static final ResourceLocation ERROR_SIXTH_VOICE = id(SONG + "error_sixth_voice");
    /** Uma conquista por Monarca (pela forma: elemento do Monarca). A do Fogo exige ter rebatido uma Bola Real. */
    private static final Map<ElementType, ResourceLocation> MONARCHS = Map.of(
            ElementType.NONE, id(SONG + "first_strike"),
            ElementType.FIRE, id(SONG + "returned_fever"),
            ElementType.WATER, id(SONG + "empty_tide"),
            ElementType.EARTH, id(SONG + "severed_root"),
            ElementType.AIR, id(SONG + "usurper_fall"));

    // Aba 2: Ecos Serenos
    public static final ResourceLocation SOUL_TUNING_FORK = id(SERENE + "soul_tuning_fork");
    public static final ResourceLocation NECESSARY_SILENCE = id(SERENE + "necessary_silence");
    public static final ResourceLocation WHAT_IS_DEAD = id(SERENE + "what_is_dead_cannot_die");
    public static final ResourceLocation FIVE_LINEAGES = id(SERENE + "five_lineages_harmony");
    public static final ResourceLocation FRAME_JUMP = id(SERENE + "frame_jump");
    public static final ResourceLocation FIRST_CHOIR = id(SERENE + "the_first_choir");
    /**
     * Linhagem de cada forma da Fauna Pacífica (id da entidade → critério de {@code five_lineages_harmony}).
     * Forma nova: acrescente aqui, no gerador das conquistas e no critério de {@code the_first_choir}.
     */
    public static final Map<String, String> ECHO_LINEAGE = Map.ofEntries(
            Map.entry("spectral_scout", "none"), Map.entry("echo_runic_bat", "none"), Map.entry("relic_automaton", "none"),
            Map.entry("igneous_salamander", "fire"), Map.entry("golden_silica_scarab", "fire"), Map.entry("volcanic_ash_colt", "fire"),
            Map.entry("liquid_mana_axolotl", "water"), Map.entry("lotus_turtle_crab", "water"), Map.entry("abyssal_trench_remora", "water"),
            Map.entry("sylvan_mini_tank", "earth"), Map.entry("runic_boulder_golem", "earth"), Map.entry("amber_root_gazelle", "earth"),
            Map.entry("cloud_manta_ray", "air"), Map.entry("storm_sentinel_owl", "air"), Map.entry("dimensional_rift_moth", "air"));

    // Aba 3: Os Cúmplices do Selo
    public static final ResourceLocation LIE_OF_FIVE_THRONES = id(ACCOMPLICES + "lie_of_five_thrones");
    /** Chefe de Santuário (id da entidade) → conquista própria; o id também é o critério da raiz da aba. */
    private static final Map<String, ResourceLocation> SANCTUARY_BOSSES = Map.of(
            "cinzagrim", id(ACCOMPLICES + "chains_for_nobody"),
            "archivist", id(ACCOMPLICES + "weight_of_a_shadow"),
            "weeper", id(ACCOMPLICES + "song_of_guilt"),
            "root_mother", id(ACCOMPLICES + "stone_heart"),
            "herald", id(ACCOMPLICES + "swallowed_scream"),
            "echo_hunter", id(ACCOMPLICES + "faithful_to_the_end"));

    /** Depois da morte do Soberano, a secreta da Sexta Voz chega 10 s depois. */
    private static final int SIXTH_VOICE_DELAY = 200;
    private static final String DONE = "done";

    /** Quem já rebateu uma Bola de Fogo Real nesta sessão (vale para "Febre Devolvida"; não é salvo). */
    private static final Set<UUID> FEVER_REFLECTORS = new HashSet<>();

    // ------------------------------------------------------------------ aba 1

    /** Mob do mod morto pelo jogador: primeiro Lacaio, primeira Elite e Guardião morto depois da Fúria. */
    public static void onTierKill(ServerPlayer player, MobTier tier, boolean raging) {
        switch (tier) {
            case NORMAL -> grant(player, TASTE_OF_VIOLENCE, DONE);
            case OP -> grant(player, BROKEN_GUARD, DONE);
            case SUB_BOSS -> {
                if (raging) grant(player, RED_WHISPER, DONE);
            }
            default -> {}
        }
    }

    public static void onPointsSpent(ServerPlayer player) {
        grant(player, MUSCLE_MEMORY, DONE);
    }

    /** O jogador rebateu (ou o Escaravelho desviou para ele) uma Bola de Fogo Real. */
    public static void onRoyalFireballReflected(ServerPlayer player) {
        FEVER_REFLECTORS.add(player.getUUID());
    }

    /**
     * Monarca derrotado (chamado por {@code MonarchBrain.onDeath} para quem lutou): a conquista da forma e o
     * critério dela em "O Selo Partido". O Rei Vulcânico só conta como "Febre Devolvida" se o jogador rebateu uma
     * Bola de Fogo Real nesta sessão.
     */
    public static void awardBossDefeat(ServerPlayer player, ElementType element) {
        grant(player, SHATTERED_SEAL, "monarch_" + element.getSerializedName());
        if (element == ElementType.FIRE && !FEVER_REFLECTORS.contains(player.getUUID())) return;
        ResourceLocation monarch = MONARCHS.get(element);
        if (monarch != null) grant(player, monarch, DONE);
    }

    /** Frente a frente com o Soberano: ele mirou o jogador ou o jogador o feriu. */
    public static void onSovereignSeen(ServerPlayer player) {
        grant(player, MIRROR_FACE, DONE);
    }

    /** Soberano derrotado (para quem lutou): Dissonância Absoluta agora e a Sexta Voz 10 s depois. */
    public static void awardSovereignDefeat(ServerPlayer player) {
        grant(player, MIRROR_FACE, DONE);
        grant(player, ABSOLUTE_DISSONANCE, DONE);
        MinecraftServer server = player.server;
        UUID id = player.getUUID();
        SovereignStageDialogueManager.schedule(SIXTH_VOICE_DELAY, () -> {
            ServerPlayer online = server.getPlayerList().getPlayer(id);
            if (online != null) grant(online, ERROR_SIXTH_VOICE, DONE);
        });
    }

    // ------------------------------------------------------------------ aba 2

    /** Vínculo (ou reativação) de um Eco sereno: a raiz da aba, a linhagem dele e o registro da forma. */
    public static void onEchoBond(ServerPlayer player, Entity echo) {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(echo.getType()).getPath();
        grant(player, SOUL_TUNING_FORK, DONE);
        String lineage = ECHO_LINEAGE.get(path);
        if (lineage == null) return;
        grant(player, FIVE_LINEAGES, lineage);
        grant(player, FIRST_CHOIR, path);
    }

    public static void onEchoExhausted(ServerPlayer owner) {
        grant(owner, NECESSARY_SILENCE, DONE);
    }

    public static void onEchoAstral(ServerPlayer owner) {
        grant(owner, WHAT_IS_DEAD, DONE);
    }

    /** A Mariposa da Fenda tirou o jogador do Raio do Peito. */
    public static void onFrameJump(ServerPlayer player) {
        grant(player, FRAME_JUMP, DONE);
    }

    // ------------------------------------------------------------------ aba 3

    /** Chefe de Santuário derrotado (para quem lutou): a conquista dele e o critério na raiz da aba. */
    public static void onSanctuaryBossDefeat(ServerPlayer player, EntityType<?> type) {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
        ResourceLocation own = SANCTUARY_BOSSES.get(path);
        if (own == null) return;
        grant(player, own, DONE);
        grant(player, LIE_OF_FIVE_THRONES, path);
    }

    // ------------------------------------------------------------------ consultas

    /** O jogador já partiu o Selo (os cinco Monarcas)? É o que chama o Soberano no Anfiteatro. */
    public static boolean hasShatteredSeal(ServerPlayer player) {
        AdvancementHolder holder = player.server.getAdvancements().get(SHATTERED_SEAL);
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    // ------------------------------------------------------------------ util

    /** Fim do servidor: esquece quem rebateu bolas de fogo. */
    public static void clear() {
        FEVER_REFLECTORS.clear();
    }

    private static void grant(ServerPlayer player, ResourceLocation id, String criterion) {
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder != null) player.getAdvancements().award(holder, criterion);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, path);
    }
}
