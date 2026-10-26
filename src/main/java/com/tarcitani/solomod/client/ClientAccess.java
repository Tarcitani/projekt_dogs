package com.tarcitani.solomod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Único ponto do código comum que toca classes de cliente.
 * Só deve ser chamado depois de checar {@code FMLEnvironment.dist == Dist.CLIENT}.
 * Expõe também os rótulos das teclas configuráveis usados nos tooltips comuns.
 */
public final class ClientAccess {
    private ClientAccess() {}

    public static Player localPlayer() {
        return Minecraft.getInstance().player;
    }

    public static net.minecraft.network.chat.Component shieldAbilityKey() {
        return WeaponControls.SHIELD_SKILL.getTranslatedKeyMessage();
    }

    public static boolean rapierDashSelected() {
        return WeaponControls.selected(com.tarcitani.solomod.core.WeaponClass.RAPIER)
                == com.tarcitani.solomod.skill.WeaponTechnique.RAPIER_DASH;
    }

    public static net.minecraft.network.chat.Component techniqueHint(com.tarcitani.solomod.core.WeaponClass weapon) {
        return net.minecraft.network.chat.Component.translatable("tooltip.solomod.technique_selected",
                WeaponControls.selected(weapon).displayName(), WeaponControls.CYCLE.getTranslatedKeyMessage());
    }
}
