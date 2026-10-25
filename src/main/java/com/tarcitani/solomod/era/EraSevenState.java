package com.tarcitani.solomod.era;

import com.tarcitani.solomod.network.ModNetwork;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Estado da Era VII (lore: docs/LORE.md, "A Dissonância e a Era VII"), salvo no mundo (pasta {@code data/} do
 * Overworld). Hoje guarda só {@code isSovereignDefeated}: vira verdadeiro na primeira morte do Soberano e não volta
 * sozinho (o {@code /modtest era} liga e desliga para testes).
 *
 * <p>O cliente recebe a flag por {@code EraStatePayload} ao entrar no mundo e sempre que ela muda: é ela que acende a
 * Cicatriz no Céu ({@code client/SkyScarRenderer}). O Caçador de Ecos lê a flag no servidor para a reverência.
 */
public final class EraSevenState extends SavedData {
    private static final String NAME = "solomod_era";
    private static final String SOVEREIGN_DEFEATED = "SovereignDefeated";

    private boolean sovereignDefeated;

    private EraSevenState() {}

    private static EraSevenState load(CompoundTag tag, HolderLookup.Provider registries) {
        EraSevenState state = new EraSevenState();
        state.sovereignDefeated = tag.getBoolean(SOVEREIGN_DEFEATED);
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(SOVEREIGN_DEFEATED, sovereignDefeated);
        return tag;
    }

    private static EraSevenState get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(EraSevenState::new, EraSevenState::load, null), NAME);
    }

    public static boolean isSovereignDefeated(MinecraftServer server) {
        return get(server).sovereignDefeated;
    }

    /** Muda a flag, salva e avisa todos os clientes online. */
    public static void setSovereignDefeated(MinecraftServer server, boolean defeated) {
        EraSevenState state = get(server);
        if (state.sovereignDefeated == defeated) return;
        state.sovereignDefeated = defeated;
        state.setDirty();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) ModNetwork.syncEra(player, defeated);
    }

    /** Ao entrar no mundo: o cliente precisa saber se a Cicatriz já existe. */
    public static void syncTo(ServerPlayer player) {
        ModNetwork.syncEra(player, isSovereignDefeated(player.server));
    }
}
