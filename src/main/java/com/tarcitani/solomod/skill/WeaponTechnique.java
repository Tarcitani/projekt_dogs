package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.event.StatsLogic.WeaponAbilities;
import com.tarcitani.solomod.core.WeaponClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

/** Técnicas por classe, alternadas por R e confirmadas pelo servidor. Acrescente constantes no fim. */
public enum WeaponTechnique {
    CRESCENT(WeaponClass.LONGSWORD), RISING(WeaponClass.LONGSWORD), CROSS(WeaponClass.LONGSWORD),
    RAPIER_DASH(WeaponClass.RAPIER), RAPIER_FLURRY(WeaponClass.RAPIER), RAPIER_RETREAT(WeaponClass.RAPIER),
    HAMMER_LEAP(WeaponClass.HAMMER), HAMMER_SWEEP(WeaponClass.HAMMER), HAMMER_QUAKE(WeaponClass.HAMMER);

    private final WeaponClass weapon;
    WeaponTechnique(WeaponClass weapon) { this.weapon = weapon; }
    public WeaponClass weapon() { return weapon; }
    public static java.util.List<WeaponTechnique> options(WeaponClass weapon) {
        return java.util.Arrays.stream(values()).filter(t -> t.weapon == weapon).toList();
    }
    public static WeaponTechnique defaultFor(WeaponClass weapon) {
        return switch (weapon) {
            case LONGSWORD -> CRESCENT;
            case RAPIER -> RAPIER_DASH;
            case HAMMER -> HAMMER_LEAP;
            default -> throw new IllegalArgumentException("Arma sem seletor de técnica: " + weapon);
        };
    }
    public static boolean supports(WeaponClass weapon) { return weapon == WeaponClass.LONGSWORD || weapon == WeaponClass.RAPIER || weapon == WeaponClass.HAMMER; }

    public static final StreamCodec<ByteBuf, WeaponTechnique> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), Enum::ordinal);
    public Component displayName() { return Component.translatable((weapon == WeaponClass.LONGSWORD ? "sword_technique.solomod." : "weapon_technique.solomod.") + name().toLowerCase(java.util.Locale.ROOT)); }
    public int duration() { return switch (this) {
        case CRESCENT -> WeaponAbilities.CRESCENT_TICKS;
        case RISING -> WeaponAbilities.RISING_TICKS;
        case CROSS -> WeaponAbilities.CROSS_TICKS;
        case RAPIER_DASH -> WeaponAbilities.RAPIER_CHARGE_TICKS;
        case RAPIER_FLURRY -> WeaponAbilities.FLURRY_TICKS;
        case RAPIER_RETREAT -> WeaponAbilities.RETREAT_TICKS;
        case HAMMER_LEAP -> WeaponAbilities.HAMMER_TIMEOUT;
        case HAMMER_SWEEP -> WeaponAbilities.HAMMER_SWEEP_TICKS;
        case HAMMER_QUAKE -> WeaponAbilities.HAMMER_QUAKE_TICKS;
    }; }
    public int cooldown() { return switch (this) {
        case CRESCENT -> WeaponAbilities.CRESCENT_COOLDOWN;
        case RISING -> WeaponAbilities.RISING_COOLDOWN;
        case CROSS -> WeaponAbilities.CROSS_COOLDOWN;
        case RAPIER_DASH -> WeaponAbilities.RAPIER_COOLDOWN;
        case RAPIER_FLURRY -> WeaponAbilities.FLURRY_COOLDOWN;
        case RAPIER_RETREAT -> WeaponAbilities.RETREAT_COOLDOWN;
        case HAMMER_LEAP -> WeaponAbilities.HAMMER_COOLDOWN;
        case HAMMER_SWEEP -> WeaponAbilities.HAMMER_SWEEP_COOLDOWN;
        case HAMMER_QUAKE -> WeaponAbilities.HAMMER_QUAKE_COOLDOWN;
    }; }
    public float damage() { return switch (this) {
        case CRESCENT -> WeaponAbilities.CRESCENT_DAMAGE;
        case RISING -> WeaponAbilities.RISING_DAMAGE;
        case CROSS -> WeaponAbilities.CROSS_DAMAGE;
        case RAPIER_DASH -> WeaponAbilities.RAPIER_DAMAGE;
        case RAPIER_FLURRY -> WeaponAbilities.FLURRY_DAMAGE;
        case RAPIER_RETREAT -> WeaponAbilities.RETREAT_DAMAGE;
        case HAMMER_LEAP -> WeaponAbilities.HAMMER_DAMAGE;
        case HAMMER_SWEEP -> WeaponAbilities.HAMMER_SWEEP_DAMAGE;
        case HAMMER_QUAKE -> WeaponAbilities.HAMMER_QUAKE_DAMAGE;
    }; }
    public double range() { return switch (this) {
        case CRESCENT, CROSS -> WeaponAbilities.CRESCENT_RANGE;
        case RISING -> WeaponAbilities.RISING_RANGE;
        case RAPIER_DASH -> WeaponAbilities.RAPIER_MAX_DISTANCE;
        case RAPIER_FLURRY -> WeaponAbilities.FLURRY_RANGE;
        case RAPIER_RETREAT -> WeaponAbilities.RETREAT_RANGE;
        case HAMMER_LEAP -> WeaponAbilities.HAMMER_RADIUS;
        case HAMMER_SWEEP -> WeaponAbilities.HAMMER_SWEEP_RANGE;
        case HAMMER_QUAKE -> WeaponAbilities.HAMMER_QUAKE_RANGE;
    }; }
    public PlayerAnim animation() { return switch (this) {
        case CRESCENT -> PlayerAnim.CRESCENT;
        case RISING -> PlayerAnim.RISING_CUT;
        case CROSS -> PlayerAnim.CROSS_CUT;
        case RAPIER_DASH -> PlayerAnim.THRUST;
        case RAPIER_FLURRY -> PlayerAnim.RAPIER_FLURRY;
        case RAPIER_RETREAT -> PlayerAnim.RAPIER_RETREAT;
        case HAMMER_LEAP -> PlayerAnim.LEAP_FLIP;
        case HAMMER_SWEEP -> PlayerAnim.HAMMER_SWEEP;
        case HAMMER_QUAKE -> PlayerAnim.HAMMER_QUAKE;
    }; }
}
