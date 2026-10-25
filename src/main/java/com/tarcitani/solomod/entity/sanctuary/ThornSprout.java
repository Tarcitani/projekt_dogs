package com.tarcitani.solomod.entity.sanctuary;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Broto Espinhoso (Jardim): muda rápida coberta de espinhos; quem a acerta corpo a corpo leva 2 de dano de espinhos. */
public class ThornSprout extends Bruiser {
    private static final float THORNS = 2.0F;

    public ThornSprout(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        // Só corpo a corpo: a fonte direta é o próprio atacante (flechas e orbes não sofrem espinhos).
        if (hurt && source.getDirectEntity() instanceof LivingEntity attacker && attacker == source.getEntity()) {
            attacker.hurt(damageSources().thorns(this), THORNS);
        }
        return hurt;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SWEET_BERRY_BUSH_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GRASS_BREAK;
    }
}
