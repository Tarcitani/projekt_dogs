package com.tarcitani.solomod.event;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.StatType;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.network.ModNetwork;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Regras numéricas centrais (fórmulas de stats) e aplicação dos atributos vanilla.
 * Tudo que é "balanceamento" fica aqui para ser ajustado em um só lugar.
 */
public final class StatsLogic {
    private StatsLogic() {}

    // ------------------------------------------------------------------ fórmulas

    /** +0.25 de dano corpo a corpo por ponto de FOR (vale para qualquer ataque). */
    public static final double FOR_FLAT_DAMAGE = 0.25;
    /** +1% de velocidade de ataque por ponto de AGI. */
    public static final double AGI_ATTACK_SPEED = 0.01;
    /** +0.5% de velocidade de movimento por ponto de AGI (limitado a +50%). */
    public static final double AGI_MOVE_SPEED = 0.005;
    /** +1 de vida (meio coração) por ponto de VIT. */
    public static final double VIT_HEALTH = 1.0;
    /** -1% de dano recebido por ponto de VIT (limitado a 50%). */
    public static final float VIT_DAMAGE_REDUCTION = 0.01F;
    /** Com requisito cumprido: +2% de dano da arma por ponto no stat requerido. */
    public static final float WEAPON_STAT_SCALING = 0.02F;
    /** Penalidade de velocidade de ataque para armas Pesadas/Médias sem Maestria (-40%). */
    public static final double LOCKED_HEAVY_SPEED_PENALTY = -0.40;

    // ------------------------------------------------------------------ ids dos modificadores

    private static final ResourceLocation FOR_DAMAGE_ID = id("stat_for_damage");
    private static final ResourceLocation AGI_ATTACK_SPEED_ID = id("stat_agi_attack_speed");
    private static final ResourceLocation AGI_MOVE_SPEED_ID = id("stat_agi_move_speed");
    private static final ResourceLocation VIT_HEALTH_ID = id("stat_vit_health");
    private static final ResourceLocation LOCKED_PENALTY_ID = id("locked_weapon_penalty");

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, path);
    }

    // ------------------------------------------------------------------ consultas

    public static PlayerStatsAttachment stats(Player player) {
        return player.getData(ModAttachments.PLAYER_STATS);
    }

    /** true se o jogador cumpre o requisito de Maestria da arma. */
    public static boolean meetsRequirement(Player player, WeaponData data) {
        return stats(player).meets(data.requiredStatType(), data.requiredStatValue());
    }

    /**
     * Stat efetivo para escalonamento: stat base + bônus passivo do tier da arma na mão principal,
     * quando a arma usa esse stat. O bônus NÃO conta para destravar requisitos.
     */
    public static int effectiveStat(Player player, StatType type) {
        int value = stats(player).get(type);
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof ModularWeaponItem) {
            WeaponData data = ModularWeaponItem.getData(held);
            if (data.requiredStatType() == type && meetsRequirement(player, data)) {
                value += data.mobTier().statBonus();
            }
        }
        return value;
    }

    public static float weaponScaling(Player player, WeaponData data) {
        return 1.0F + effectiveStat(player, data.requiredStatType()) * WEAPON_STAT_SCALING;
    }

    public static float vitDamageMultiplier(Player player) {
        return 1.0F - Math.min(0.5F, stats(player).get(StatType.VIT) * VIT_DAMAGE_REDUCTION);
    }

    // ------------------------------------------------------------------ aplicação

    /**
     * Reaplica os modificadores de atributo dos stats.
     *
     * <p>Os modificadores de stat são PERMANENTES (salvos com a entidade): assim a vida máxima extra
     * já existe quando o vanilla carrega a vida atual do jogador no login, e ele não perde corações.
     * A penalidade da arma é TRANSIENTE porque depende do que está na mão.
     */
    public static void applyAttributes(ServerPlayer player) {
        PlayerStatsAttachment s = stats(player);

        setPermanent(player, Attributes.ATTACK_DAMAGE, FOR_DAMAGE_ID,
                s.get(StatType.FOR) * FOR_FLAT_DAMAGE, AttributeModifier.Operation.ADD_VALUE);
        setPermanent(player, Attributes.ATTACK_SPEED, AGI_ATTACK_SPEED_ID,
                s.get(StatType.AGI) * AGI_ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setPermanent(player, Attributes.MOVEMENT_SPEED, AGI_MOVE_SPEED_ID,
                Math.min(0.5, s.get(StatType.AGI) * AGI_MOVE_SPEED), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setPermanent(player, Attributes.MAX_HEALTH, VIT_HEALTH_ID,
                s.get(StatType.VIT) * VIT_HEALTH, AttributeModifier.Operation.ADD_VALUE);

        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());

        applyLockedWeaponPenalty(player);
    }

    /** Armas Pesadas/Médias sem Maestria: -40% de velocidade de ataque enquanto estiverem na mão. */
    public static void applyLockedWeaponPenalty(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed == null) return;

        ItemStack held = player.getMainHandItem();
        boolean penalize = false;
        if (held.getItem() instanceof ModularWeaponItem) {
            WeaponData data = ModularWeaponItem.getData(held);
            penalize = data.weaponClass().weight().penalizedWhenLocked() && !meetsRequirement(player, data);
        }

        if (penalize) {
            if (!speed.hasModifier(LOCKED_PENALTY_ID)) {
                speed.addTransientModifier(new AttributeModifier(LOCKED_PENALTY_ID, LOCKED_HEAVY_SPEED_PENALTY,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            speed.removeModifier(LOCKED_PENALTY_ID);
        }
    }

    private static void setPermanent(Player player, Holder<Attribute> attribute, ResourceLocation id,
                                     double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier current = instance.getModifier(id);
        if (current != null && current.amount() == amount && current.operation() == op) return; // nada mudou
        instance.removeModifier(id);
        if (amount != 0) instance.addPermanentModifier(new AttributeModifier(id, amount, op));
    }

    /** Chame sempre que os stats mudarem no servidor: reaplica atributos e sincroniza com o cliente. */
    public static void onStatsChanged(ServerPlayer player) {
        applyAttributes(player);
        ModNetwork.syncStats(player);
    }
}
