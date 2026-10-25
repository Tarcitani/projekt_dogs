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

/**
 * A Carpideira: chefe do Santuário das Marés (Água). Figura de coral com véu de algas que chora o mar. Golpes:
 *
 * <ul>
 *   <li><b>Maré</b> (sempre): ergue os braços por 1 s e solta uma onda de 10 blocos (pule).</li>
 *   <li><b>Lágrimas</b> (sempre): 6 gotas grandes caem em volta do alvo em 1,5 a 2,5 s.</li>
 *   <li><b>Canto</b> (fase 2+): dois anéis de 10 bolhas que saem dela em todas as direções.</li>
 *   <li><b>Coro</b> (fase 3): chama 2 {@link ChoirDrowned} (no máximo 4 por perto).</li>
 * </ul>
 */
public class Weeper extends SanctuaryBoss {
    private static final int TIDE = 1;
    private static final int TEARS = 2;
    private static final int SONG = 3;
    private static final int CHOIR = 4;
    private static final int TIDE_WINDUP = 20;

    private int choirCooldown = 200;

    public Weeper(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 240)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ARMOR, 6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void registerGoals() {
        addGroundGoals(1.0);
    }

    @Override
    protected void customServerAiStep() {
        if (choirCooldown > 0) choirCooldown--;
        super.customServerAiStep();
    }

    @Override
    protected int chooseAttack(LivingEntity target, double dist, int phase) {
        if (phase >= 3 && choirCooldown <= 0 && countNearby(ChoirDrowned.class, 30) < 4) return CHOIR;
        return pick(dist < 10 ? TIDE : 0, TEARS, phase >= 2 ? SONG : 0);
    }

    @Override
    protected boolean tickAttack(ServerLevel server, int id, int t, @Nullable LivingEntity target) {
        switch (id) {
            case TIDE -> {
                if (t < TIDE_WINDUP && t % 2 == 0) {
                    SkillVfx.ring(server, position(), 10.0 * t / TIDE_WINDUP, Hazards.dust(ElementType.WATER, 1.6F), 30);
                } else if (t == TIDE_WINDUP + 2) {
                    Hazards.add(new Hazards.Shockwave(server, this, ElementType.WATER, position(), 10, 0.5, damage(1.0F), 2));
                    Hazards.sound(server, position(), SoundEvents.GENERIC_SPLASH, 2.0F, 0.6F);
                    server.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.3, getZ(), 60, 2, 0.2, 2, 0.3);
                }
                return t < 40;
            }
            case TEARS -> {
                if (t == 1 && target != null) {
                    Hazards.sound(server, position(), SoundEvents.ELDER_GUARDIAN_CURSE, 1.0F, 1.4F);
                    Vec3 c = target.position();
                    for (int i = 0; i < 6; i++) {
                        double a = random.nextDouble() * Math.PI * 2;
                        double r = i == 0 ? 0 : 2 + random.nextDouble() * 3.5;
                        Hazards.add(new Hazards.Blast(server, this, ElementType.WATER,
                                c.add(Math.cos(a) * r, 0, Math.sin(a) * r), 2.2, 30 + i * 4, damage(0.7F), 2));
                    }
                }
                return t < 56;
            }
            case SONG -> {
                if (t == 1) Hazards.sound(server, position(), SoundEvents.CONDUIT_ACTIVATE, 1.5F, 1.2F);
                if (t < 16 && t % 3 == 0) {
                    server.sendParticles(ParticleTypes.NOTE, getX(), getY() + 2.6, getZ(), 2, 0.5, 0.2, 0.5, 1);
                }
                if (t == 16 || t == 30) ring(server, t == 30 ? 0.3 : 0.0);
                return t < 40;
            }
            case CHOIR -> {
                if (t == 12) {
                    Hazards.sound(server, position(), SoundEvents.DROWNED_AMBIENT, 2.0F, 0.6F);
                    summon(server, ModEntities.CHOIR_DROWNED.get(), 2, 3.0, 0.2);
                    choirCooldown = 400;
                }
                return t < 40;
            }
            default -> {
                return false;
            }
        }
    }

    /** Anel de 10 bolhas na horizontal, saindo da altura do peito. */
    private void ring(ServerLevel server, double offset) {
        Vec3 from = position().add(0, 1.5, 0);
        for (int i = 0; i < 10; i++) {
            double a = offset + Math.PI * 2 * i / 10;
            Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
            Hazards.add(new Hazards.Orb(server, this, ElementType.WATER, from, dir.scale(0.55), damage(0.6F), 1, 40));
        }
        Hazards.sound(server, from, Hazards.castSound(ElementType.WATER), 1.5F, 0.9F);
    }

    @Override
    public float swing(float partialTick) {
        float t = attackTicks() + partialTick;
        return switch (attackId()) {
            case TIDE -> t < TIDE_WINDUP ? t / TIDE_WINDUP : Mth.clamp(1.0F - (t - TIDE_WINDUP) / 4.0F, 0.0F, 1.0F);
            case SONG, CHOIR -> Mth.clamp(t / 10.0F, 0.0F, 1.0F);
            default -> 0.0F;
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ELDER_GUARDIAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.DROWNED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }
}
