package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Golem de Musgo (Jardim): pedra coberta de musgo; bate devagar e a cada 6 s solta um tremor de terra de 5 blocos. */
public class MossGolem extends Slammer {
    public MossGolem(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 14;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50)
                .add(Attributes.ATTACK_DAMAGE, 8)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ARMOR, 6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    protected ElementType element() {
        return ElementType.EARTH;
    }

    @Override
    protected double slamRadius() {
        return 5.0;
    }

    @Override
    protected SoundEvent slamSound() {
        return SoundEvents.ROOTED_DIRT_BREAK;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STONE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STONE_BREAK;
    }
}
