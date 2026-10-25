package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.mob.Hazards;
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
 * Escriba Espectral (Biblioteca): fantasma de copista que flutua baixo e longe. Em vez de atirar, escreve uma runa no
 * chão sob o alvo, que explode 1,5 s depois (o anel mostra onde).
 */
public class SpectralScribe extends HoverShooter {
    public SpectralScribe(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.FOLLOW_RANGE, 28);
    }

    @Override
    protected ElementType element() {
        return ElementType.NONE;
    }

    @Override
    protected int shotInterval() {
        return 70;
    }

    @Override
    protected double minRange() {
        return 7.0;
    }

    @Override
    protected double maxRange() {
        return 12.0;
    }

    @Override
    protected double hoverHeight() {
        return 1.0;
    }

    @Override
    protected void shoot(ServerLevel server, LivingEntity target) {
        Hazards.sound(server, position(), SoundEvents.EVOKER_PREPARE_ATTACK, 0.8F, 1.4F);
        Hazards.add(new Hazards.Blast(server, this, ElementType.NONE, target.position(), 2.2, 30, damage(1.0F), 1));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }
}
