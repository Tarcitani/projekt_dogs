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
 *   <li>{@code defeat_sovereign_<elemento>}: uma por forma do Soberano, critério {@code defeated}.</li>
 *   <li>{@code defeat_all_sovereigns}: desafio mestre com um critério {@code sovereign_<elemento>} por
 *       forma; a tela mostra o progresso (0/5).</li>
 * </ul>
 *
 * <p>Quem chama é {@code SovereignBrain.onDeath}, só para os jogadores que causaram dano ao chefe.
 * Elemento novo em {@link ElementType} exige os JSON e as chaves de lang correspondentes.
 */
public final class ModAdvancements {
    private ModAdvancements() {}

    public static final ResourceLocation DEFEAT_ALL = id("defeat_all_sovereigns");

    /** Concede a conquista do Soberano daquele elemento e o critério dele no desafio mestre. */
    public static void awardBossDefeat(ServerPlayer player, ElementType element) {
        String name = element.getSerializedName();
        grant(player, id("defeat_sovereign_" + name), "defeated");
        grant(player, DEFEAT_ALL, "sovereign_" + name);
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
