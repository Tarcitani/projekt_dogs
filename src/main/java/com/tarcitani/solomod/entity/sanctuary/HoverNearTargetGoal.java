package com.tarcitani.solomod.entity.sanctuary;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Voo dos mobs sem gravidade dos santuários: fica de frente para o alvo, mantém a distância entre {@code min} e
 * {@code max} (aproxima quando longe, recua quando perto) e paira {@code height} blocos acima dos pés dele, com um
 * leve balanço. Durante um golpe especial ({@link SanctuaryMob#attackId()} != 0) não mexe no movimento.
 */
public class HoverNearTargetGoal extends Goal {
    private final SanctuaryMob mob;
    private final double min;
    private final double max;
    private final double height;
    private final double accel;

    public HoverNearTargetGoal(SanctuaryMob mob, double min, double max, double height, double accel) {
        this.mob = mob;
        this.min = min;
        this.max = max;
        this.height = height;
        this.accel = accel;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        mob.getLookControl().setLookAt(target, 30, 30);
        if (mob.attackId() != 0) return;
        Vec3 flat = target.position().subtract(mob.position()).multiply(1, 0, 1);
        double dist = flat.length();
        Vec3 move = Vec3.ZERO;
        if (dist > max) move = flat.normalize().scale(accel);
        else if (dist < min) move = flat.normalize().scale(-accel);
        double wantY = target.getY() + height + Math.sin(mob.tickCount * 0.1) * 0.4;
        move = move.add(0, Math.clamp((wantY - mob.getY()) * 0.03, -accel, accel), 0);
        mob.setDeltaMovement(mob.getDeltaMovement().scale(0.88).add(move));
    }
}
