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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * O Arauto: chefe da Torre dos Ventos Quebrados (Ar). Ave-tempestade que voa 5 blocos acima do alvo. Golpes:
 *
 * <ul>
 *   <li><b>Rajada</b> (sempre): abre as asas por 1 s (anel de aviso) e empurra para longe quem estiver a 9 blocos.</li>
 *   <li><b>Mergulho</b> (sempre): mira por 0,7 s e desce em linha reta até onde o alvo estava.</li>
 *   <li><b>Tempestade</b> (fase 2+): 3 descargas de vento caem em volta do alvo.</li>
 *   <li><b>Brisas</b> (fase 3): chama 2 {@link BreezeElemental} (no máximo 4 por perto).</li>
 * </ul>
 */
public class Herald extends SanctuaryBoss {
    private static final int GUST = 1;
    private static final int DIVE = 2;
    private static final int STORM = 3;
    private static final int BREEZES = 4;
    private static final int GUST_WINDUP = 20;
    private static final double GUST_RADIUS = 9.0;
    private static final int DIVE_AIM = 14;

    private final Set<Integer> diveHit = new HashSet<>();
    private Vec3 diveTarget = Vec3.ZERO;
    private int breezeCooldown = 200;

    public Herald(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE);
        setNoGravity(true);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 4)
                .add(Attributes.FOLLOW_RANGE, 48);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new HoverNearTargetGoal(this, 5, 11, 5.0, 0.06));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        addTargetGoals();
    }

    @Override
    protected boolean holdsStillWhileAttacking() {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        if (breezeCooldown > 0) breezeCooldown--;
        super.customServerAiStep();
    }

    @Override
    protected int chooseAttack(LivingEntity target, double dist, int phase) {
        if (phase >= 3 && breezeCooldown <= 0 && countNearby(BreezeElemental.class, 30) < 4) return BREEZES;
        return pick(dist < GUST_RADIUS ? GUST : 0, DIVE, phase >= 2 ? STORM : 0);
    }

    @Override
    protected void onAttackStart(int id, LivingEntity target) {
        diveHit.clear();
        diveTarget = target.position().add(0, 0.5, 0);
    }

    @Override
    protected boolean tickAttack(ServerLevel server, int id, int t, @Nullable LivingEntity target) {
        switch (id) {
            case GUST -> {
                setDeltaMovement(getDeltaMovement().scale(0.6));
                if (t < GUST_WINDUP && t % 2 == 0) {
                    SkillVfx.ring(server, position(), GUST_RADIUS * t / GUST_WINDUP, ParticleTypes.CLOUD, 30);
                } else if (t == GUST_WINDUP) {
                    Hazards.sound(server, position(), SoundEvents.WIND_CHARGE_BURST.value(), 2.0F, 0.6F);
                    AABB box = getBoundingBox().inflate(GUST_RADIUS, 6, GUST_RADIUS);
                    for (LivingEntity e : Hazards.enemiesIn(server, this, box)) {
                        if (e.distanceTo(this) > GUST_RADIUS + 2) continue;
                        Hazards.hit(this, e, damage(0.8F), ElementType.AIR, 2, false);
                        Vec3 away = e.position().subtract(position()).multiply(1, 0, 1).normalize();
                        Hazards.push(e, away.scale(1.4).add(0, 0.5, 0));
                    }
                    server.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
                }
                return t < 34;
            }
            case DIVE -> {
                if (t < DIVE_AIM) {
                    setDeltaMovement(getDeltaMovement().scale(0.5));
                    if (t == 1) Hazards.sound(server, position(), SoundEvents.PHANTOM_SWOOP, 2.0F, 0.6F);
                    Vec3 dir = diveTarget.subtract(position()).normalize();
                    for (int i = 1; i <= 8; i++) {
                        Vec3 p = position().add(dir.scale(i * 1.2));
                        server.sendParticles(Hazards.dust(ElementType.AIR, 1.0F), p.x, p.y, p.z, 1, 0, 0, 0, 0);
                    }
                    return true;
                }
                Vec3 dir = diveTarget.subtract(position());
                if (dir.lengthSqr() < 1.0 || verticalCollision || horizontalCollision || t > 40) {
                    setDeltaMovement(0, 0.6, 0);
                    return false;
                }
                setDeltaMovement(dir.normalize().scale(1.1));
                for (LivingEntity e : Hazards.enemiesIn(server, this, getBoundingBox().inflate(0.8))) {
                    if (diveHit.add(e.getId())) {
                        Hazards.hit(this, e, damage(1.3F), ElementType.AIR, 2, false);
                        Hazards.push(e, dir.normalize().scale(0.8).add(0, 0.4, 0));
                    }
                }
                return true;
            }
            case STORM -> {
                setDeltaMovement(getDeltaMovement().scale(0.6));
                if (t == 1 && target != null) {
                    Hazards.sound(server, position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 0.8F, 1.6F);
                    Vec3 c = target.position();
                    Hazards.add(new Hazards.Blast(server, this, ElementType.AIR, c, 2.6, 34, damage(0.9F), 2));
                    for (int i = 0; i < 2; i++) {
                        double a = random.nextDouble() * Math.PI * 2;
                        Hazards.add(new Hazards.Blast(server, this, ElementType.AIR,
                                c.add(Math.cos(a) * 3.5, 0, Math.sin(a) * 3.5), 2.6, 40, damage(0.9F), 2));
                    }
                }
                return t < 50;
            }
            case BREEZES -> {
                setDeltaMovement(getDeltaMovement().scale(0.6));
                if (t == 12) {
                    Hazards.sound(server, position(), SoundEvents.BREEZE_SHOOT, 2.0F, 0.6F);
                    summon(server, ModEntities.BREEZE_ELEMENTAL.get(), 2, 2.5, 0.0);
                    breezeCooldown = 400;
                }
                return t < 40;
            }
            default -> {
                return false;
            }
        }
    }

    /** Asas abertas (o modelo usa como "braço erguido") na rajada e na tempestade. */
    @Override
    public float swing(float partialTick) {
        float t = attackTicks() + partialTick;
        return switch (attackId()) {
            case GUST -> t < GUST_WINDUP ? t / GUST_WINDUP : Mth.clamp(1.0F - (t - GUST_WINDUP) / 4.0F, 0.0F, 1.0F);
            case STORM, BREEZES -> Mth.clamp(t / 10.0F, 0.0F, 1.0F);
            default -> 0.0F;
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    public float getVoicePitch() {
        return 0.5F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }
}
