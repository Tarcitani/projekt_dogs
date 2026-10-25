package com.tarcitani.solomod.entity.sanctuary;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Capanga de corpo a corpo: anda até o alvo e bate; cada um aplica o próprio efeito no golpe ({@link #onHit}). */
public abstract class Bruiser extends SanctuaryMob {
    protected Bruiser(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        addGroundGoals(1.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) onHit(living);
        return hit;
    }

    /** Efeito extra de um golpe que acertou. */
    protected void onHit(LivingEntity target) {}
}
