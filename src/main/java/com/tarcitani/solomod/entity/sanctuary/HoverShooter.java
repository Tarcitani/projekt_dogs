package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.mob.Hazards;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Capanga voador: paira perto do alvo ({@link HoverNearTargetGoal}) e, com linha de visão, dispara a cada
 * {@link #shotInterval()} ticks. O disparo padrão é uma orbe do elemento; subclasses trocam por runa no chão,
 * mergulho etc. Sem gravidade e imune a queda.
 */
public abstract class HoverShooter extends SanctuaryMob {
    private int cooldown;

    protected HoverShooter(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setNoGravity(true);
        this.cooldown = shotInterval();
    }

    protected abstract ElementType element();

    protected int shotInterval() {
        return 50;
    }

    protected double minRange() {
        return 5.0;
    }

    protected double maxRange() {
        return 10.0;
    }

    protected double hoverHeight() {
        return 1.8;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new HoverNearTargetGoal(this, minRange(), maxRange(), hoverHeight(), 0.04));
        addTargetGoals();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        if (target == null || !(level() instanceof ServerLevel server)) return;
        if (--cooldown <= 0 && distanceTo(target) < maxRange() + 6 && hasLineOfSight(target)) {
            cooldown = shotInterval();
            shoot(server, target);
        }
    }

    /** Disparo padrão: uma orbe do elemento na direção dos olhos do alvo. */
    protected void shoot(ServerLevel server, LivingEntity target) {
        Vec3 from = position().add(0, getBbHeight() * 0.5, 0);
        Vec3 dir = target.getEyePosition().subtract(from).normalize();
        Hazards.sound(server, from, Hazards.castSound(element()), 0.8F, 1.3F);
        Hazards.add(new Hazards.Orb(server, this, element(), from, dir.scale(0.8), damage(1.0F), 1, 40));
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }
}
