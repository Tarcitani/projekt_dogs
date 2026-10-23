package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * O Soberano: chefe final da versão de testes. Há eras os cinco Monarcas se uniram para derrotá-lo; ele
 * voltou com o poder de todos eles. Modelo gigante com um pedaço de cada Monarca (tools/mob_variants.py).
 *
 * <p>É um {@link TierMob} de tier BOSS sem elemento, mas com cérebro próprio
 * ({@link com.tarcitani.solomod.mob.SovereignBrain}): 6 estágios de vida, close cinemático a cada estágio
 * e Fúria sem fim no último. Ao morrer, desintegra como os Monarcas e os jogadores veem os créditos.
 *
 * <p>Os atributos daqui são a BASE; {@code TierIdentity} soma o multiplicador do tier BOSS por cima
 * (vida 14x, dano 2.8x, +12 de armadura).
 * O modelo usa escala 7 (cinco vezes a escala visual anterior de 1,4); a caixa física de combate
 * continua separada. A caixa de visibilidade engloba o modelo inteiro para evitar cortes da câmera.
 */
public class Sovereign extends TierMob {
    /** Vida base: 50 x 14 (BOSS) = 700. */
    public static final double BASE_HEALTH = 50;
    /** Dano base: 4 x 2.8 (BOSS) = 11.2 por golpe. */
    public static final double BASE_DAMAGE = 4;
    /** O modelo é desenhado nesta escala (a caixa de colisão já é a do gigante). */
    public static final float RENDER_SCALE = 7.0F;

    @Override
    public net.minecraft.world.phys.AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(18, 32, 18);
    }

    public Sovereign(EntityType<? extends TierMob> type, Level level) {
        super(type, level, MobTier.BOSS);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder sovereignAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 48)
                .add(Attributes.ARMOR, 4)
                .add(Attributes.STEP_HEIGHT, 2.0);
    }

    /** O Soberano não tem forma elemental: ele usa todos os elementos nos ataques. */
    @Override
    public ElementType randomElement() {
        return ElementType.NONE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public float getVoicePitch() {
        return 0.5F + (random.nextFloat() - 0.5F) * 0.1F;
    }
}
