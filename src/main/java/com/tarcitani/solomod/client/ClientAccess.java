package com.tarcitani.solomod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Único ponto do código comum que toca classes de cliente.
 * Só deve ser chamado depois de checar {@code FMLEnvironment.dist == Dist.CLIENT}.
 */
public final class ClientAccess {
    private ClientAccess() {}

    public static Player localPlayer() {
        return Minecraft.getInstance().player;
    }
}
