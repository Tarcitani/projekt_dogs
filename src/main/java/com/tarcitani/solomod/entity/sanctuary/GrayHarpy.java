package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.mob.Hazards;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Harpia Cinzenta (Torre): circula alto e, em vez de atirar, mergulha. Grita (aviso), pausa 0,5 s mirando e desce em
 * linha reta até onde o alvo estava; acerta uma vez e volta a subir.
 */
public class GrayHarpy extends HoverShooter {
    private static final int DIVE = 1;
    private static final int AIM = 10;
    private static final int DIVE_END = 30;

    private Vec3 diveDir = Vec3.ZERO;
    private boolean hitDone;

    public GrayHarpy(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18)
                .add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected ElementType element() {
        return ElementType.AIR;
    }

    @Override
    protected int shotInterval() {
        return 80;
    }

    @Override
    protected double hoverHeight() {
        return 4.5;
    }

    @Override
    protected double minRange() {
        return 3.0;
    }

    @Override
    protected double maxRange() {
        return 8.0;
    }

    @Override
    protected void shoot(ServerLevel server, LivingEntity target) {
        setAttack(DIVE);
        hitDone = false;
        diveDir = target.getEyePosition().subtract(position()).normalize();
        Hazards.sound(server, position(), SoundEvents.PHANTOM_SWOOP, 1.0F, 1.3F);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (attackId() != DIVE || !(level() instanceof ServerLevel server)) return;
        int t = advanceAttack();
        if (t < AIM) {
            setDeltaMovement(getDeltaMovement().scale(0.5));
            server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.3, getZ(), 2, 0.2, 0.2, 0.2, 0.01);
            return;
        }
        setDeltaMovement(diveDir.scale(0.9));
        if (!hitDone) {
            for (LivingEntity e : Hazards.enemiesIn(server, this, getBoundingBox().inflate(0.5))) {
                Hazards.hit(this, e, damage(1.0F), ElementType.AIR, 1, false);
                hitDone = true;
            }
        }
        if (hitDone || verticalCollision || horizontalCollision || t >= DIVE_END) {
            setAttack(0);
            setDeltaMovement(0, 0.5, 0);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }
}
