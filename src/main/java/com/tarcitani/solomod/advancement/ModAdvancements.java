package com.tarcitani.solomod.advancement;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.ElementType;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Gerenciamento e concessão de conquistas (Advancements) do Solo Mod.
 */
public final class ModAdvancements {
    private ModAdvancements() {}

    public static final ResourceLocation ROOT = id("root");
    public static final ResourceLocation DEFEAT_NONE = id("defeat_sovereign_none");
    public static final ResourceLocation DEFEAT_FIRE = id("defeat_sovereign_fire");
    public static final ResourceLocation DEFEAT_WATER = id("defeat_sovereign_water");
    public static final ResourceLocation DEFEAT_EARTH = id("defeat_sovereign_earth");
    public static final ResourceLocation DEFEAT_AIR = id("defeat_sovereign_air");
    public static final ResourceLocation DEFEAT_ALL = id("defeat_all_sovereigns");

    /** Concede a conquista correspondente ao Soberano derrotado. */
    public static void awardBossDefeat(ServerPlayer player, ElementType element) {
        ResourceLocation advId = switch (element) {
            case NONE -> DEFEAT_NONE;
            case FIRE -> DEFEAT_FIRE;
            case WATER -> DEFEAT_WATER;
            case EARTH -> DEFEAT_EARTH;
            case AIR -> DEFEAT_AIR;
        };

        // Concede a conquista específica daquele Soberano
        grant(player, advId, "defeated");

        // Progresso no desafio mestre de derrotar todos os cinco Soberanos
        String masterCriterion = switch (element) {
            case NONE -> "sovereign_none";
            case FIRE -> "sovereign_fire";
            case WATER -> "sovereign_water";
            case EARTH -> "sovereign_earth";
            case AIR -> "sovereign_air";
        };
        grant(player, DEFEAT_ALL, masterCriterion);
    }

    private static void grant(ServerPlayer player, ResourceLocation id, String criterion) {
        if (player.server == null) return;
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
            if (!progress.isDone()) {
                player.getAdvancements().award(holder, criterion);
            }
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, path);
    }
}
