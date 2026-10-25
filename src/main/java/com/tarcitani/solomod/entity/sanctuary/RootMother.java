package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.mob.Hazards;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Raiz-Mãe: chefe do Jardim Petrificado (Terra). Árvore-fóssil que anda devagar sobre as raízes. Golpes:
 *
 * <ul>
 *   <li><b>Raízes</b> (sempre): uma fileira de 7 raízes rompe o chão do pé dela até o alvo, uma atrás da outra.</li>
 *   <li><b>Tremor</b> (sempre): ergue os galhos por 1 s e solta um tremor de 10 blocos (pule).</li>
 *   <li><b>Casca</b> (fase 2+): por 4 s fecha a casca (lascas de madeira em volta) e sofre só 25% do dano.
 *       É a hora de recuar, não de bater.</li>
 *   <li><b>Brotos</b> (fase 3): chama 3 {@link ThornSprout} (no máximo 6 por perto).</li>
 * </ul>
 */
public class RootMother extends SanctuaryBoss {
    private static final int ROOTS = 1;
    private static final int QUAKE = 2;
    private static final int BARK = 3;
    private static final int SPROUTS = 4;
    private static final int QUAKE_WINDUP = 20;
    private static final int BARK_TICKS = 80;
    private static final float BARK_FACTOR = 0.25F;

    private Vec3 rootDir = Vec3.ZERO;
    private int sproutCooldown = 200;

    public RootMother(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300)
                .add(Attributes.ATTACK_DAMAGE, 11)
                .add(Attributes.MOVEMENT_SPEED, 0.14)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void registerGoals() {
        addGroundGoals(1.0);
    }

    @Override
    protected void customServerAiStep() {
        if (sproutCooldown > 0) sproutCooldown--;
        super.customServerAiStep();
    }

    @Override
    protected int chooseAttack(LivingEntity target, double dist, int phase) {
        if (phase >= 3 && sproutCooldown <= 0 && countNearby(ThornSprout.class, 30) < 6) return SPROUTS;
        return pick(dist < 18 ? ROOTS : 0, dist < 10 ? QUAKE : 0, phase >= 2 && random.nextInt(2) == 0 ? BARK : 0);
    }

    @Override
    protected void onAttackStart(int id, LivingEntity target) {
        rootDir = target.position().subtract(position()).multiply(1, 0, 1).normalize();
    }

    @Override
    protected boolean tickAttack(ServerLevel server, int id, int t, @Nullable LivingEntity target) {
        switch (id) {
            case ROOTS -> {
                if (t == 1) {
                    Hazards.sound(server, position(), SoundEvents.ROOTED_DIRT_BREAK, 2.0F, 0.5F);
                    for (int i = 1; i <= 7; i++) {
                        Vec3 p = position().add(rootDir.scale(i * 1.8));
                        Hazards.add(new Hazards.Blast(server, this, ElementType.EARTH, p, 1.5, 18 + i * 3, damage(0.9F), 2));
                    }
                }
                return t < 50;
            }
            case QUAKE -> {
                if (t < QUAKE_WINDUP && t % 2 == 0) {
                    SkillVfx.ring(server, position(), 10.0 * t / QUAKE_WINDUP, Hazards.dust(ElementType.EARTH, 1.6F), 30);
                } else if (t == QUAKE_WINDUP + 2) {
                    Hazards.add(new Hazards.Shockwave(server, this, ElementType.EARTH, position(), 10, 0.5, damage(1.0F), 2));
                    Hazards.sound(server, position(), SoundEvents.ROOTED_DIRT_BREAK, 2.0F, 0.4F);
                }
                return t < 38;
            }
            case BARK -> {
                if (t == 1) Hazards.sound(server, position(), SoundEvents.WOOD_PLACE, 2.0F, 0.5F);
                if (t % 4 == 0) {
                    server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                            getX(), getY() + 1.5, getZ(), 10, 1.0, 1.2, 1.0, 0.05);
                }
                return t < BARK_TICKS;
            }
            case SPROUTS -> {
                if (t == 12) {
                    Hazards.sound(server, position(), SoundEvents.SWEET_BERRY_BUSH_PLACE, 2.0F, 0.6F);
                    summon(server, ModEntities.THORN_SPROUT.get(), 3, 3.0, 0.2);
                    sproutCooldown = 360;
                }
                return t < 40;
            }
            default -> {
                return false;
            }
        }
    }

    /** Com a casca fechada, só 25% do dano passa. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (attackId() == BARK) {
            amount *= BARK_FACTOR;
            playSound(SoundEvents.WOOD_HIT, 1.0F, 0.6F);
        }
        return super.hurt(source, amount);
    }

    @Override
    public float swing(float partialTick) {
        float t = attackTicks() + partialTick;
        return switch (attackId()) {
            case QUAKE -> t < QUAKE_WINDUP ? t / QUAKE_WINDUP : Mth.clamp(1.0F - (t - QUAKE_WINDUP) / 4.0F, 0.0F, 1.0F);
            case ROOTS, SPROUTS -> Mth.clamp(t / 8.0F, 0.0F, 1.0F);
            default -> 0.0F;
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WOOD_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOOD_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOOD_BREAK;
    }
}
