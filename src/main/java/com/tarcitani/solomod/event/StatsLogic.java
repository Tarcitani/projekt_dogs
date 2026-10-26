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
 * {@link WeaponAbilities} centraliza habilidades de classe; {@link Parry} controla o escudo
 * e {@link ShieldSkills} as quatro técnicas de arma com escudo.
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

    /** Custos da árvore e mana por ativação das técnicas de classe. */
    public static final class Talents {
        private Talents() {}
        public static final int ROOT_COST = 1, BRANCH_COST = 2, TIER_COST = 3, ULTIMATE_COST = 5;
        public static final int THROW_MANA = 3, TELEPORT_MANA = 8, LEAP_MANA = 10;
        public static int techniqueMana(com.tarcitani.solomod.skill.WeaponTechnique technique) {
            return switch (technique) {
                case CRESCENT, RAPIER_DASH -> 5;
                case RISING, RAPIER_RETREAT -> 7;
                case CROSS, RAPIER_FLURRY, HAMMER_SWEEP -> 8;
                case HAMMER_LEAP, HAMMER_QUAKE -> LEAP_MANA;
            };
        }
    }

    /** Técnicas de classe com mana: dash/rajada/recuo, cortes da espada e salto/giro/onda do martelo; ticks, blocos e multiplicadores. */
    public static final class WeaponAbilities {
        private WeaponAbilities() {}

        public static final int RAPIER_USE_DURATION = 72000, RAPIER_CHARGE_TICKS = 30, RAPIER_COOLDOWN = 30;
        public static final double RAPIER_MIN_DISTANCE = 3, RAPIER_MAX_DISTANCE = 14, RAPIER_SPEED = 1.6;
        public static final float RAPIER_DAMAGE = 1.3F;
        public static final int FLURRY_TICKS = 32, FLURRY_COOLDOWN = 48, FLURRY_FIRST_HIT = 4, FLURRY_HIT_GAP = 10, FLURRY_HITS = 3;
        public static final float FLURRY_DAMAGE = 0.55F;
        public static final double FLURRY_RANGE = 4, FLURRY_CONE_DOT = 0.88;
        public static final int RETREAT_TICKS = 18, RETREAT_COOLDOWN = 38, RETREAT_HIT = 4, RETREAT_MOVE_TICKS = 4;
        public static final float RETREAT_DAMAGE = 1F;
        public static final double RETREAT_RANGE = 4, RETREAT_SPEED = 0.8;
        public static final int CRESCENT_COOLDOWN = 24, CRESCENT_WINDUP = 6, CRESCENT_TICKS = 18;
        public static final double CRESCENT_RANGE = 4.5, CRESCENT_PUSH = 0.65;
        public static final float CRESCENT_DAMAGE = 1.2F;
        public static final int RISING_COOLDOWN = 34, RISING_TICKS = 22;
        public static final float RISING_DAMAGE = 1.5F;
        public static final double RISING_RANGE = 5.0, RISING_LIFT = 0.7;
        public static final int CROSS_COOLDOWN = 42, CROSS_TICKS = 30, CROSS_SECOND_HIT = 16;
        public static final float CROSS_DAMAGE = 0.8F;
        public static final int DAGGER_THROW_COOLDOWN = 8, DAGGER_TELEPORT_COOLDOWN = 30;
        public static final double DAGGER_TELEPORT_RANGE = 64;
        public static final int HAMMER_COOLDOWN = 70, HAMMER_DIVE_TICK = 9, HAMMER_TIMEOUT = 240;
        public static final double HAMMER_RADIUS = 4.5, HAMMER_JUMP = 1.05, HAMMER_DIVE = -2.2;
        public static final float HAMMER_DAMAGE = 2F;
        public static final int HAMMER_DEBRIS_BLOCKS = 36;
        public static final double HAMMER_HEIGHT_CAP = 24, HAMMER_DAMAGE_PER_BLOCK = 0.12, HAMMER_RADIUS_PER_BLOCK = 0.08;
        public static final int HAMMER_SWEEP_TICKS = 28, HAMMER_SWEEP_COOLDOWN = 54, HAMMER_SWEEP_HIT = 10;
        public static final float HAMMER_SWEEP_DAMAGE = 1.6F;
        public static final double HAMMER_SWEEP_RANGE = 4.4, HAMMER_SWEEP_PUSH = 1.2;
        public static final int HAMMER_QUAKE_TICKS = 30, HAMMER_QUAKE_COOLDOWN = 64, HAMMER_QUAKE_HIT = 10;
        public static final float HAMMER_QUAKE_DAMAGE = 1.8F;
        public static final double HAMMER_QUAKE_RANGE = 8, HAMMER_QUAKE_STEP = 0.7, HAMMER_QUAKE_WIDTH = 1.25;
    }

    /** Rolamento: impulso no chão, recuperação e breve janela de esquiva de dano de combate. */
    public static final class Dodge {
        private Dodge() {}

        public static final int DOUBLE_TAP_MS = 280, MOVE_TICKS = 8, TOTAL_TICKS = 12, COOLDOWN_TICKS = 26;
        public static final int INVULNERABLE_FROM = 2, INVULNERABLE_UNTIL = 8;
        public static final double SPEED = 0.95;
        public static final float EXHAUSTION = 0.4F;
    }

    /** Escudo: janela ao levantar, recuperação e resistência; tempos em ticks (20 = 1 segundo). */
    public static final class Parry {
        private Parry() {}
        public static final int WINDOW_TICKS = 6, COOLDOWN_TICKS = 24, STUN_TICKS = 30;
        public static final int MULTIHIT_GRACE_TICKS = 2, DURABILITY = 672, DURABILITY_COST = 1;
        public static final double FRONT_DOT = 0.0;
    }

    /** Técnicas de uma mão com escudo: custo, recarga, preparo, duração, alcance, cone, dano e empurrão. */
    public static final class ShieldSkills {
        private ShieldSkills() {}
        public record Spec(int mana, int cooldown, int hitTick, int duration, double range,
                           double coneDot, float damage, double push) {}
        public static final Spec SWORD = new Spec(6, 44, 5, 16, 3.4, 0.35, 1.2F, 0.45);
        public static final Spec RAPIER = new Spec(5, 38, 4, 14, 4.8, 0.94, 1.35F, 0.2);
        public static final Spec DAGGER = new Spec(5, 42, 3, 15, 2.8, 0.8, 1.7F, 0.15);
        public static final Spec HAMMER = new Spec(10, 70, 9, 24, 3.0, 0.65, 1.6F, 1.0);
        public static final int RETREAT_TICKS = 4;
        public static final double RETREAT_SPEED = 0.3;
        public static Spec forWeapon(com.tarcitani.solomod.core.WeaponClass weapon) {
            return switch (weapon) {
                case LONGSWORD -> SWORD;
                case RAPIER -> RAPIER;
                case DAGGER -> DAGGER;
                case HAMMER -> HAMMER;
                default -> null;
            };
        }
    }

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
