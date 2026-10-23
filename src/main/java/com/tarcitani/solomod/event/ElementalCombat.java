package com.tarcitani.solomod.event;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.item.ModularWeaponItem;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Regras elementais compartilhadas pelo ataque corpo a corpo (CombatEvents) e pelas habilidades (WeaponSkill).
 */
public final class ElementalCombat {
    private ElementalCombat() {}

    /** Elemento "defensivo" de uma entidade: o do perfil de mob ou, para jogadores, o da arma na mão. */
    public static ElementType elementOf(LivingEntity entity) {
        if (entity.hasData(ModAttachments.MOB_PROFILE)) {
            return entity.getData(ModAttachments.MOB_PROFILE).element();
        }
        if (entity instanceof Player player && player.getMainHandItem().getItem() instanceof ModularWeaponItem) {
            return ModularWeaponItem.getData(player.getMainHandItem()).elementType();
        }
        return ElementType.NONE;
    }

    /**
     * Multiplicador elemental da arma contra o alvo.
     * Armas SUB_BOSS/BOSS têm elemento "puro": nunca caem abaixo de 1.0x.
     */
    public static float multiplier(WeaponData data, LivingEntity target) {
        float mult = data.elementType().multiplierAgainst(elementOf(target));
        if (data.mobTier().hasPureElement()) mult = Math.max(mult, ElementType.NEUTRAL_MULTIPLIER);
        return mult;
    }

    public static boolean hasAdvantage(WeaponData data, LivingEntity target) {
        return data.elementType().hasAdvantageOver(elementOf(target));
    }

    /**
     * Efeito passivo do elemento. {@code potency} vem do tier (1 = leve, 2 = forte, 3 = máximo)
     * e aumenta duração e nível do efeito.
     */
    public static void applyPassive(ElementType element, int potency, LivingEntity target) {
        if (element == ElementType.NONE || potency <= 0) return;
        int duration = 40 + 30 * potency;          // 3,5 s / 5 s / 6,5 s
        int amplifier = Math.max(0, potency - 1);  // nível I / II / III
        switch (element) {
            // Água: encharca e desacelera.
            case WATER -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
            // Fogo: queima por mais tempo conforme o tier.
            case FIRE -> target.igniteForSeconds(1.5F * potency + 1.0F);
            // Ar: arremessa para cima (levitação curta).
            case AIR -> target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 10 + 5 * potency, 0));
            // Terra: esmaga a guarda (fraqueza).
            case EARTH -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier));
            default -> {}
        }
        spawnParticles(element, target, 6 * potency);
    }

    public static void spawnParticles(ElementType element, LivingEntity at, int count) {
        if (!(at.level() instanceof ServerLevel level)) return;
        ParticleOptions particle = switch (element) {
            case WATER -> ParticleTypes.SPLASH;
            case FIRE -> ParticleTypes.FLAME;
            case AIR -> ParticleTypes.CLOUD;
            case EARTH -> ParticleTypes.CRIT;
            default -> ParticleTypes.ENCHANTED_HIT;
        };
        level.sendParticles(particle, at.getX(), at.getY() + at.getBbHeight() * 0.5, at.getZ(),
                count, at.getBbWidth() * 0.5, at.getBbHeight() * 0.4, at.getBbWidth() * 0.5, 0.05);
    }
}
