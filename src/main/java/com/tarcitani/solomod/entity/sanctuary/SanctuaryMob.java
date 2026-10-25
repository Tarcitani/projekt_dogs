package com.tarcitani.solomod.entity.sanctuary;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base dos mobs dos Santuários de Eco (masmorras opcionais, fora da linha Monarcas -> Soberano). Eles NÃO usam o
 * perfil de tier ({@code TierIdentity}) nem os cérebros de {@code mob/}: cada um decide seus golpes na própria
 * classe. Em troca, todos compartilham o golpe sincronizado que o modelo usa para animar ({@link #swing}), e
 * {@code Hazards.isEnemy} os trata como aliados entre si (a onda de choque do chefe não fere os capangas).
 *
 * <p>Bases prontas: {@link Bruiser} (corpo a corpo com efeito no golpe), {@link Slammer} (martelada telegrafada),
 * {@link HoverShooter} (paira e atira) e {@link SanctuaryBoss} (chefe com fases e barra de vida).
 */
public abstract class SanctuaryMob extends Monster {
    /** Golpe em andamento (0 = nenhum); o significado de cada número é da subclasse. */
    private static final EntityDataAccessor<Integer> ATTACK =
            SynchedEntityData.defineId(SanctuaryMob.class, EntityDataSerializers.INT);
    /** Ticks desde o início do golpe atual. */
    private static final EntityDataAccessor<Integer> ATTACK_TICKS =
            SynchedEntityData.defineId(SanctuaryMob.class, EntityDataSerializers.INT);

    protected SanctuaryMob(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK, 0);
        builder.define(ATTACK_TICKS, 0);
    }

    public final int attackId() {
        return entityData.get(ATTACK);
    }

    public final int attackTicks() {
        return entityData.get(ATTACK_TICKS);
    }

    /** Inicia (ou encerra, com 0) um golpe e zera o contador. */
    protected final void setAttack(int id) {
        entityData.set(ATTACK, id);
        entityData.set(ATTACK_TICKS, 0);
    }

    /** Avança o contador do golpe atual e devolve o novo valor. */
    protected final int advanceAttack() {
        int t = attackTicks() + 1;
        entityData.set(ATTACK_TICKS, t);
        return t;
    }

    /** Dano base (atributo) vezes o fator. */
    protected final float damage(float factor) {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * factor;
    }

    /**
     * Metas de quem anda e bate: nada na água, corpo a corpo (pausado durante um golpe especial), passeia, olha em
     * volta e mira jogadores (e quem o atacou).
     */
    protected final void addGroundGoals(double speed) {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, speed, true) {
            @Override
            public boolean canUse() {
                return attackId() == 0 && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return attackId() == 0 && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        addTargetGoals();
    }

    protected final void addTargetGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 0..1: quanto o braço da arma está erguido, para o modelo animar. Padrão: sempre baixo. */
    public float swing(float partialTick) {
        return 0.0F;
    }

    /** 0..1: quanto o mob está ajoelhado (a reverência do Caçador de Ecos). Padrão: de pé. */
    public float kneel(float partialTick) {
        return 0.0F;
    }

    /** Ilusões (cópias do Arquivista) são desenhadas sem sombra. */
    public boolean isIllusion() {
        return false;
    }

    public static boolean isSanctuary(Entity entity) {
        return entity instanceof SanctuaryMob;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
