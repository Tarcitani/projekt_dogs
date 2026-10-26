package com.tarcitani.solomod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.network.CycleTechniquePayload;
import com.tarcitani.solomod.skill.WeaponTechnique;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** R alterna técnicas sem escudo; Z (livre no vanilla 1.21.1) solicita a habilidade de arma + escudo. */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT)
public final class WeaponControls {
    private WeaponControls() {}
    public static final KeyMapping CYCLE = new KeyMapping("key.solomod.cycle_sword", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, ClientSetup.KEY_CATEGORY);
    public static final KeyMapping SHIELD_SKILL = new KeyMapping("key.solomod.shield_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, ClientSetup.KEY_CATEGORY);
    private static final java.util.Map<WeaponClass, WeaponTechnique> SELECTED = new java.util.EnumMap<>(WeaponClass.class);
    public static WeaponTechnique selected(WeaponClass weapon) {
        WeaponTechnique selected = SELECTED.getOrDefault(weapon, WeaponTechnique.defaultFor(weapon));
        var player = Minecraft.getInstance().player;
        if (player == null) return selected;
        var stats = com.tarcitani.solomod.event.StatsLogic.stats(player);
        if (stats.hasTalent(com.tarcitani.solomod.skill.SkillTree.forTechnique(selected).id())) return selected;
        return WeaponTechnique.options(weapon).stream().filter(t -> stats.hasTalent(com.tarcitani.solomod.skill.SkillTree.forTechnique(t).id()))
                .findFirst().orElse(WeaponTechnique.defaultFor(weapon));
    }
    public static void accept(WeaponTechnique value) { SELECTED.put(value.weapon(), value); }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (CYCLE.consumeClick()) {
            if (mc.screen == null && mc.player != null && !mc.player.isUsingItem()
                    && !com.tarcitani.solomod.skill.ShieldAbility.hasShield(mc.player)
                    && mc.player.getMainHandItem().getItem() instanceof ModularWeaponItem item
                    && WeaponTechnique.supports(item.weaponClass())) PacketDistributor.sendToServer(CycleTechniquePayload.INSTANCE);
        }
        while (SHIELD_SKILL.consumeClick()) {
            if (mc.screen == null && mc.player != null && mc.player.isAlive() && !mc.player.isSpectator()
                    && com.tarcitani.solomod.skill.ShieldAbility.equipment(mc.player) != null) {
                PacketDistributor.sendToServer(com.tarcitani.solomod.network.ShieldAbilityPayload.INSTANCE);
            }
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { SELECTED.clear(); }
}
