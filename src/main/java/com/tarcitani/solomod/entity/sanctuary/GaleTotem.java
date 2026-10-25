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

/**
 * Totem do Vendaval (Torre): pilar de pedra entalhada que não sai do lugar. A cada 4 s, com alguém a menos de 7
 * blocos, solta uma rajada em anel de 6 blocos que empurra para longe (pule para esquivar).
 */
public class GaleTotem extends Slammer {
    public GaleTotem(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override
    protected ElementType element() {
        return ElementType.AIR;
    }

    @Override
    protected double slamRadius() {
        return 6.0;
    }

    @Override
    protected int slamCooldown() {
        return 80;
    }

    @Override
    protected double triggerRange() {
        return 7.0;
    }

    @Override
    protected SoundEvent slamSound() {
        return SoundEvents.WIND_CHARGE_BURST.value();
    }

    @Override
    public boolean isPushable() {
        return false;
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
