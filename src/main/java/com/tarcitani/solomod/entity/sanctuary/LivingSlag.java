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

/**
 * Escória Viva (Forja): poça de metal derretido que se levanta. Lenta e resistente; o golpe queima por 4 s e, ao
 * morrer, ela estoura em um pequeno clarão de brasa (aviso de 0,8 s, dá tempo de sair de perto).
 */
public class LivingSlag extends Bruiser {
    public LivingSlag(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ARMOR, 4)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    protected void onHit(LivingEntity target) {
        target.setRemainingFireTicks(80);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && random.nextInt(6) == 0) {
            level().addParticle(ParticleTypes.LAVA, getRandomX(0.5), getY() + 0.9, getRandomZ(0.5), 0, 0, 0);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel server) {
            Hazards.add(new Hazards.Blast(server, this, ElementType.FIRE, position(), 2.2, 16, damage(0.8F), 1));
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.MAGMA_CUBE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.MAGMA_CUBE_DEATH;
    }
}
