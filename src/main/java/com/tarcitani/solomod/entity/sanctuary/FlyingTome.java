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

/** Tomo Voador (Biblioteca): livro aberto que bate as capas como asas e dispara runas arcanas a cada 2 s. */
public class FlyingTome extends HoverShooter {
    public FlyingTome(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.FOLLOW_RANGE, 28);
    }

    @Override
    protected ElementType element() {
        return ElementType.NONE;
    }

    @Override
    protected int shotInterval() {
        return 40;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BOOK_PAGE_TURN;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BOOK_PUT;
    }
}
