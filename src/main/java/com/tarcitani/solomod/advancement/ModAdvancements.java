package com.tarcitani.solomod.advancement;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.ElementType;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Conquistas (Advancements) do Solo Mod, na aba própria "Solo Mod" (tecla L).
 *
 * <p>Os JSON ficam em {@code data/solomod/advancement/}. Todos os critérios, menos o da raiz, usam o
 * gatilho {@code minecraft:impossible}: só este código os concede.
 * <ul>
 *   <li>{@code root}: concedida no primeiro tick (gatilho {@code minecraft:tick}) para a aba aparecer.</li>
 *   <li>{@code defeat_monarch_<elemento>}: uma por forma do Monarca, critério {@code defeated}.</li>
 *   <li>{@code defeat_all_monarchs}: desafio mestre com um critério {@code monarch_<elemento>} por
 *       forma; a tela mostra o progresso (0/5).</li>
 *   <li>{@code broken_seal}: SECRETA (oculta até ser ganha). Vem junto com a anterior e insinua que os
 *       Monarcas mantinham algo selado — o Soberano.</li>
 *   <li>{@code sovereign_unknown}: oculta e mostrada como "???" (ícone de interrogações) assim que os cinco
 *       Monarcas caem; é concedida e REVOGADA pelo código. Ao derrotar o Soberano ela some e aparece
 *       {@code defeat_sovereign}, com a cabeça dele.</li>
 *   <li>{@code defeat_sovereign}: SECRETA. Derrotar o Soberano (o fim da versão de testes).</li>
 * </ul>
 *
 * <p>Quem chama é {@code MonarchBrain.onDeath} e {@code SovereignBrain.onDeath}, só para os jogadores que
 * causaram dano ao chefe. Elemento novo em {@link ElementType} exige os JSON e as chaves de lang correspondentes.
 */
public final class ModAdvancements {
    private ModAdvancements() {}

    public static final ResourceLocation DEFEAT_ALL = id("defeat_all_monarchs");
    public static final ResourceLocation BROKEN_SEAL = id("broken_seal");
    public static final ResourceLocation DEFEAT_SOVEREIGN = id("defeat_sovereign");
    public static final ResourceLocation SOVEREIGN_UNKNOWN = id("sovereign_unknown");

    /**
     * Concede a conquista do Monarca daquele elemento e o critério dele no desafio mestre. Com os cinco,
     * revela a conquista secreta.
     */
    public static void awardBossDefeat(ServerPlayer player, ElementType element) {
        String name = element.getSerializedName();
        grant(player, id("defeat_monarch_" + name), "defeated");
        grant(player, DEFEAT_ALL, "monarch_" + name);
        AdvancementHolder all = player.server.getAdvancements().get(DEFEAT_ALL);
        if (all != null && player.getAdvancements().getOrStartProgress(all).isDone()) {
            grant(player, BROKEN_SEAL, "sealed");
            // O "???" só vale enquanto o Soberano não caiu (senão voltaria depois de revogado).
            if (!isDone(player, DEFEAT_SOVEREIGN)) grant(player, SOVEREIGN_UNKNOWN, "seen");
        }
    }

    private static boolean isDone(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    /** Derrotar o Soberano também quebra o selo (para a conquista ter o pai visível na árvore). */
    public static void awardSovereignDefeat(ServerPlayer player) {
        grant(player, BROKEN_SEAL, "sealed");
        AdvancementHolder unknown = player.server.getAdvancements().get(SOVEREIGN_UNKNOWN);
        if (unknown != null) player.getAdvancements().revoke(unknown, "seen");
        grant(player, DEFEAT_SOVEREIGN, "defeated");
    }

    /** {@code award} já ignora critério concluído, então conceder de novo não faz nada. */
    private static void grant(ServerPlayer player, ResourceLocation id, String criterion) {
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder != null) player.getAdvancements().award(holder, criterion);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, path);
    }
}
