package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.mob.Hazards;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Capanga da martelada: bate corpo a corpo e, de tempos em tempos, com o alvo perto, ergue a arma por 0,75 s (anel de
 * aviso crescente) e bate no chão, soltando uma onda de choque do elemento que se esquiva pulando.
 */
public abstract class Slammer extends SanctuaryMob {
    private static final int SLAM = 1;
    private static final int WINDUP = 15;
    private static final int RECOVER = 25;

    private int cooldown = slamCooldown();

    protected Slammer(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    protected abstract ElementType element();

    protected double slamRadius() {
        return 4.5;
    }

    protected int slamCooldown() {
        return 120;
    }

    protected double triggerRange() {
        return 6.0;
    }

    protected SoundEvent slamSound() {
        return SoundEvents.ANVIL_LAND;
    }

    @Override
    protected void registerGoals() {
        addGroundGoals(1.0);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        if (attackId() == 0) {
            if (cooldown > 0) cooldown--;
            if (cooldown <= 0 && target != null && distanceToSqr(target) < triggerRange() * triggerRange()) {
                setAttack(SLAM);
            }
            return;
        }
        int t = advanceAttack();
        getNavigation().stop();
        if (target != null) getLookControl().setLookAt(target, 40, 40);
        if (!(level() instanceof ServerLevel server)) return;
        if (t < WINDUP && t % 3 == 0) {
            SkillVfx.ring(server, position(), slamRadius() * t / WINDUP, Hazards.dust(element(), 1.2F), 18);
        } else if (t == WINDUP) {
            Hazards.add(new Hazards.Shockwave(server, this, element(), position(), slamRadius(), 0.45, damage(0.9F), 1));
            Hazards.sound(server, position(), slamSound(), 0.8F, 0.7F);
            server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.2, getZ(), 10, 0.8, 0.1, 0.8, 0.05);
        } else if (t >= WINDUP + RECOVER) {
            setAttack(0);
            cooldown = slamCooldown();
        }
    }

    @Override
    public float swing(float partialTick) {
        if (attackId() != SLAM) return 0.0F;
        float t = attackTicks() + partialTick;
        if (t < WINDUP) return t / WINDUP;
        return Mth.clamp(1.0F - (t - WINDUP) / 4.0F, 0.0F, 1.0F);
    }
}
