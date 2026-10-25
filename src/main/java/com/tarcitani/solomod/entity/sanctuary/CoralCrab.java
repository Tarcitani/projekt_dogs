package com.tarcitani.solomod.entity.sanctuary;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Caranguejo de Coral (Marés): casco de coral grosso na frente. Golpes vindos de frente perdem 60% do dano (som de
 * escudo); pelos lados ou por trás ele apanha normal. Lento e duro.
 */
public class CoralCrab extends Bruiser {
    private static final float FRONT_REDUCTION = 0.4F;

    public CoralCrab(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ARMOR, 8)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getDirectEntity();
        if (attacker != null && level() instanceof ServerLevel) {
            Vec3 toAttacker = attacker.position().subtract(position()).multiply(1, 0, 1).normalize();
            Vec3 facing = Vec3.directionFromRotation(0, yBodyRot);
            if (toAttacker.dot(facing) > 0.5) {
                amount *= FRONT_REDUCTION;
                playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F);
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.TURTLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.TURTLE_DEATH;
    }
}
