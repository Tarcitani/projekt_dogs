package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.mob.Hazards;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Cinzagrim, o Ferreiro: chefe da Forja Banhada em Cinzas (Santuário de Fogo). Golem de basalto com um martelo de
 * bigorna. Golpes, todos telegrafados:
 *
 * <ul>
 *   <li><b>Martelada</b> (sempre): ergue o martelo por 1 s e bate no chão; onda de choque que se esquiva pulando.</li>
 *   <li><b>Investida</b> (fase 2+): marca uma linha, espera 0,8 s e atropela; para ao bater numa parede.</li>
 *   <li><b>Erupção</b> (fase 2+): quatro brasas caem em volta do alvo, uma sempre sobre ele.</li>
 *   <li><b>Fagulhas</b> (fase 3): chama duas {@link ForgeSpark} (no máximo 4 vivas por perto).</li>
 * </ul>
 */
public class Cinzagrim extends SanctuaryBoss {
    private static final int SLAM = 1;
    private static final int CHARGE = 2;
    private static final int ERUPT = 3;
    private static final int SUMMON = 4;

    private static final int SLAM_WINDUP = 20;
    private static final int CHARGE_AIM = 16;

    private final Set<Integer> chargeHit = new HashSet<>();
    private Vec3 chargeDir = Vec3.ZERO;
    private int summonCooldown = 200;

    public Cinzagrim(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 260)
                .add(Attributes.ATTACK_DAMAGE, 12)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ARMOR, 8)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void registerGoals() {
        addGroundGoals(1.0);
    }

    @Override
    protected void customServerAiStep() {
        if (summonCooldown > 0) summonCooldown--;
        super.customServerAiStep();
    }

    @Override
    protected int chooseAttack(LivingEntity target, double dist, int phase) {
        if (phase >= 3 && summonCooldown <= 0 && countNearby(ForgeSpark.class, 30) < 4) return SUMMON;
        return pick(dist < 9 ? SLAM : 0,
                phase >= 2 && dist > 6 && dist < 20 ? CHARGE : 0,
                phase >= 2 && dist < 24 ? ERUPT : 0);
    }

    @Override
    protected void onAttackStart(int id, LivingEntity target) {
        chargeHit.clear();
        chargeDir = target.position().subtract(position()).multiply(1, 0, 1).normalize();
    }

    @Override
    protected boolean tickAttack(ServerLevel server, int id, int t, @Nullable LivingEntity target) {
        return switch (id) {
            case SLAM -> slam(server, t);
            case CHARGE -> charge(server, t, target);
            case ERUPT -> erupt(server, t, target);
            case SUMMON -> summonSparks(server, t);
            default -> false;
        };
    }

    private boolean slam(ServerLevel server, int t) {
        if (t == 1) Hazards.sound(server, position(), SoundEvents.ANVIL_PLACE, 1.4F, 0.6F);
        if (t < SLAM_WINDUP && t % 2 == 0) {
            SkillVfx.ring(server, position(), 8.0 * t / SLAM_WINDUP, Hazards.dust(ElementType.FIRE, 1.6F), 28);
        } else if (t == SLAM_WINDUP + 2) {
            Hazards.add(new Hazards.Shockwave(server, this, ElementType.FIRE, position(), 9, 0.55, damage(1.0F), 2));
            Hazards.sound(server, position(), SoundEvents.ANVIL_LAND, 1.8F, 0.5F);
            Hazards.sound(server, position(), SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.8F);
            server.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.3, getZ(), 30, 1.5, 0.1, 1.5, 0.2);
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.3, getZ(), 4, 1.0, 0.1, 1.0, 0);
        }
        return t < 38;
    }

    private boolean charge(ServerLevel server, int t, @Nullable LivingEntity target) {
        if (t < CHARGE_AIM) {
            if (target != null) chargeDir = target.position().subtract(position()).multiply(1, 0, 1).normalize();
            setYRot((float) (Mth.atan2(chargeDir.z, chargeDir.x) * (180.0 / Math.PI)) - 90.0F);
            yBodyRot = getYRot();
            for (int i = 1; i <= 10; i++) {
                Vec3 p = position().add(chargeDir.scale(i * 1.4)).add(0, 0.1, 0);
                server.sendParticles(Hazards.dust(ElementType.FIRE, 1.2F), p.x, p.y, p.z, 1, 0.1, 0, 0.1, 0);
            }
            if (t == 1) Hazards.sound(server, position(), SoundEvents.RAVAGER_ROAR, 1.5F, 0.6F);
            return true;
        }
        setDeltaMovement(chargeDir.x, getDeltaMovement().y, chargeDir.z);
        hasImpulse = true;
        server.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.5, getZ(), 6, 0.6, 0.4, 0.6, 0.02);
        for (LivingEntity e : Hazards.enemiesIn(server, this, getBoundingBox().inflate(0.6))) {
            if (chargeHit.add(e.getId())) {
                Hazards.hit(this, e, damage(1.2F), ElementType.FIRE, 2, false);
                Hazards.push(e, chargeDir.add(0, 0.5, 0));
            }
        }
        if (horizontalCollision) {
            Hazards.sound(server, position(), SoundEvents.ANVIL_LAND, 1.0F, 0.5F);
            return false;
        }
        return t < 34;
    }

    private boolean erupt(ServerLevel server, int t, @Nullable LivingEntity target) {
        if (t == 1 && target != null) {
            Hazards.sound(server, position(), SoundEvents.FIRECHARGE_USE, 1.5F, 0.5F);
            Vec3 c = target.position();
            Hazards.add(new Hazards.Blast(server, this, ElementType.FIRE, c, 3.0, 40, damage(0.8F), 2));
            for (int i = 0; i < 3; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double r = 3.5 + random.nextDouble() * 2.5;
                Hazards.add(new Hazards.Blast(server, this, ElementType.FIRE,
                        c.add(Math.cos(angle) * r, 0, Math.sin(angle) * r), 3.0, 40 + i * 6, damage(0.8F), 2));
            }
        }
        return t < 56;
    }

    private boolean summonSparks(ServerLevel server, int t) {
        if (t == 12) {
            Hazards.sound(server, position(), SoundEvents.BLAZE_SHOOT, 1.5F, 0.5F);
            summon(server, ModEntities.FORGE_SPARK.get(), 2, 2.5, 2.5);
            summonCooldown = 400;
        }
        return t < 40;
    }

    /** Braço erguido para o modelo: martelada, erupção e invocação levantam o martelo; a investida não. */
    @Override
    public float swing(float partialTick) {
        float t = attackTicks() + partialTick;
        return switch (attackId()) {
            case SLAM -> t < SLAM_WINDUP ? t / SLAM_WINDUP : Mth.clamp(1.0F - (t - SLAM_WINDUP) / 4.0F, 0.0F, 1.0F);
            case ERUPT, SUMMON -> Mth.clamp(t / 10.0F, 0.0F, 1.0F);
            default -> 0.0F;
        };
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SMOKE, getRandomX(0.4), getY() + 3.0, getRandomZ(0.4), 0, 0.05, 0);
            if (getHealth() < getMaxHealth() * 0.33F && random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.FLAME, getRandomX(0.5), getY() + 1.0 + random.nextDouble() * 2,
                        getRandomZ(0.5), 0, 0.03, 0);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_DAMAGE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.55F + (random.nextFloat() - 0.5F) * 0.1F;
    }
}
