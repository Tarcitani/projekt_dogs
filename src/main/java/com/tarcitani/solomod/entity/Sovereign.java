package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.mob.Hazards;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
 * O modelo usa escala 7 (cerca de 31 blocos de altura, 36 com os braços erguidos); a caixa física de
 * combate é menor que o modelo de propósito (ver {@link #HITBOX_WIDTH}). A caixa de visibilidade engloba o
 * modelo inteiro para evitar cortes da câmera.
 *
 * <p><b>Morte</b>: desintegração diferente da dos Monarcas, ele morre BUGANDO ({@link #dissolveEffects}).
 *
 * <p><b>Imunidades</b>: fogo, lava e queimadura ({@code fireImmune} no registro e aqui), queda (os voos
 * curtos dele terminam em pouso) e sufocamento em paredes (a caixa gigante atravessa tetos e morros; os
 * olhos ficam a 22 blocos do chão).
 */
public class Sovereign extends TierMob {
    /** Vida base: 50 x 14 (BOSS) = 700. */
    public static final double BASE_HEALTH = 50;
    /** Dano base: 4 x 2.8 (BOSS) = 11.2 por golpe. */
    public static final double BASE_DAMAGE = 4;
    /** O modelo é desenhado nesta escala. */
    public static final float RENDER_SCALE = 7.0F;
    /**
     * Largura da caixa de colisão. O modelo tem 18 blocos de largura (36 com as asas), mas a busca de
     * entidades do Minecraft só olha seções a 2 blocos da região consultada: com mais de ~8 de largura o
     * jogador passa a errar golpes e flechas em certos ângulos. Também evita que o chefe fique preso em
     * qualquer terreno. Os pontos fora da caixa (braços, asas) não recebem dano.
     */
    public static final float HITBOX_WIDTH = 8.0F;
    /** Altura da caixa: do chão ao topo da cabeça (a coroa e os braços erguidos passam disso). */
    public static final float HITBOX_HEIGHT = 24.0F;
    /** Altura dos olhos: a cabeça do modelo (usada como origem de olhar e de efeitos). */
    public static final float EYE_HEIGHT = 22.0F;

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

    /**
     * A morte do Soberano é um GLITCH, não uma desintegração: o modelo trava em quadros, peças saltam de lugar,
     * ele pisca, as cores se separam (ver {@code TierMobModel.glitch} e {@code TierMobRenderer}), e aqui vêm as
     * faíscas, a estática roxa e os bipes picotados, cada vez mais densos até um estouro final.
     */
    @Override
    protected void dissolveEffects(ServerLevel server, int ticks) {
        float p = ticks / (float) DISSOLVE_TICKS;
        double w = getBbWidth() * 0.5;
        // Estática e faíscas em pontos aleatórios do corpo (o corpo gigante tem 24 blocos de altura).
        int bursts = 2 + (int) (6 * p);
        for (int i = 0; i < bursts; i++) {
            double y = getY() + random.nextDouble() * getBbHeight();
            Hazards.farParticles(server, ParticleTypes.ELECTRIC_SPARK, getX(), y, getZ(), 6, w, 1.5, w, 0.35);
            Hazards.farParticles(server, ParticleTypes.REVERSE_PORTAL, getX(), y, getZ(), 6, w, 1.5, w, 0.2);
        }
        if (ticks % 3 == 0) {
            // Bipe de 8 bits com altura aleatória: o "áudio picotado" de um programa travando.
            server.playSound(null, getX(), getY() + 10, getZ(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.HOSTILE,
                    3.0F, 0.5F + random.nextFloat() * 1.5F);
        }
        if (ticks % 11 == 0) {
            server.playSound(null, getX(), getY() + 10, getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE,
                    3.0F, 0.4F + random.nextFloat() * 0.8F);
        }
        if (ticks % 25 == 0) {
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 3.0F, 0.5F + p);
        }
    }

    /** Fogo (inclusive o das próprias bolas), lava e sufocamento nunca o ferem. */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_FALL)
                || super.isInvulnerableTo(source);
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
