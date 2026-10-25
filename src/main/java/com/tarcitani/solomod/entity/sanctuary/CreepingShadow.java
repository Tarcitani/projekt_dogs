package com.tarcitani.solomod.entity.sanctuary;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Sombra Rastejante (Catacumba): rápida e invisível enquanto está a mais de 4 blocos do alvo; só a fumaça que solta
 * denuncia onde está. Aparece ao chegar perto.
 */
public class CreepingShadow extends Bruiser {
    private static final double REVEAL_DISTANCE_SQR = 4.0 * 4.0;

    public CreepingShadow(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 28);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        setInvisible(target != null && distanceToSqr(target) > REVEAL_DISTANCE_SQR);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && isInvisible() && random.nextInt(3) == 0) {
            level().addParticle(ParticleTypes.SMOKE, getRandomX(0.5), getY() + 0.3, getRandomZ(0.5), 0, 0.02, 0);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }
}
