package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.mob.Hazards;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * O Arquivista: chefe da Biblioteca do Esquecimento (Santuário Neutro). Espectro de bibliotecário que flutua a 3
 * blocos de altura, longe do alvo. Golpes:
 *
 * <ul>
 *   <li><b>Rajada de Páginas</b> (sempre): leque de 5 runas arcanas depois de 0,7 s.</li>
 *   <li><b>Selo Rúnico</b> (sempre): 3 selos no chão em volta do alvo, um sob ele; explodem em 1,5 s.</li>
 *   <li><b>Cópias</b> (fase 2+): cria 2 ilusões idênticas por 20 s. Elas só soltam páginas fracas, morrem com um
 *       golpe e <b>não têm sombra</b>: é assim que se acha o verdadeiro.</li>
 *   <li><b>Salto de Página</b> (fase 3): some em fumaça e reaparece atrás do alvo, já disparando.</li>
 * </ul>
 */
public class Archivist extends SanctuaryBoss {
    private static final EntityDataAccessor<Boolean> ILLUSION =
            SynchedEntityData.defineId(Archivist.class, EntityDataSerializers.BOOLEAN);
    private static final int VOLLEY = 1;
    private static final int SEALS = 2;
    private static final int COPIES = 3;
    private static final int BLINK = 4;
    private static final int ILLUSION_LIFETIME = 400;
    private static final int VOLLEY_AT = 14;
    private static final int BLINK_AT = 12;

    private int lifetime;

    public Archivist(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE);
        setNoGravity(true);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ARMOR, 4)
                .add(Attributes.FOLLOW_RANGE, 40);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ILLUSION, false);
    }

    @Override
    public boolean isIllusion() {
        return entityData.get(ILLUSION);
    }

    @Override
    protected boolean showsBossBar() {
        return !isIllusion();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new HoverNearTargetGoal(this, 6, 12, 3.0, 0.05));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        addTargetGoals();
    }

    @Override
    protected void customServerAiStep() {
        if (isIllusion() && ++lifetime > ILLUSION_LIFETIME) {
            vanish();
            return;
        }
        super.customServerAiStep();
    }

    @Override
    protected int chooseAttack(LivingEntity target, double dist, int phase) {
        if (isIllusion()) return VOLLEY;
        if (phase >= 3 && random.nextInt(3) == 0) return BLINK;
        boolean noCopies = level().getEntitiesOfClass(Archivist.class, getBoundingBox().inflate(40), Archivist::isIllusion).isEmpty();
        return pick(VOLLEY, SEALS, phase >= 2 && noCopies ? COPIES : 0);
    }

    @Override
    protected boolean tickAttack(ServerLevel server, int id, int t, @Nullable LivingEntity target) {
        return switch (id) {
            case VOLLEY -> volley(server, t, target, VOLLEY_AT, 30);
            case SEALS -> seals(server, t, target);
            case COPIES -> copies(server, t, target);
            case BLINK -> blink(server, t, target);
            default -> false;
        };
    }

    private boolean volley(ServerLevel server, int t, @Nullable LivingEntity target, int fireAt, int end) {
        if (t < fireAt && t % 3 == 0) {
            server.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.5, getZ(), 12, 0.6, 0.6, 0.6, 0.5);
        }
        if (t == fireAt && target != null) {
            Hazards.sound(server, position(), SoundEvents.EVOKER_CAST_SPELL, 1.4F, 1.2F);
            Vec3 from = position().add(0, 1.4, 0);
            Vec3 aim = target.getEyePosition().subtract(from).normalize();
            float dmg = damage(isIllusion() ? 0.3F : 0.7F);
            for (int i = -2; i <= 2; i++) {
                Vec3 dir = aim.yRot(i * 0.18F);
                Hazards.add(new Hazards.Orb(server, this, ElementType.NONE, from, dir.scale(0.7), dmg, 1, 50));
            }
        }
        return t < end;
    }

    private boolean seals(ServerLevel server, int t, @Nullable LivingEntity target) {
        if (t == 1 && target != null) {
            Hazards.sound(server, position(), SoundEvents.EVOKER_PREPARE_ATTACK, 1.4F, 0.8F);
            Vec3 c = target.position();
            Hazards.add(new Hazards.Blast(server, this, ElementType.NONE, c, 2.6, 30, damage(0.9F), 2));
            for (int i = 0; i < 2; i++) {
                double a = random.nextDouble() * Math.PI * 2;
                Hazards.add(new Hazards.Blast(server, this, ElementType.NONE,
                        c.add(Math.cos(a) * 4, 0, Math.sin(a) * 4), 2.6, 36, damage(0.9F), 2));
            }
        }
        return t < 45;
    }

    private boolean copies(ServerLevel server, int t, @Nullable LivingEntity target) {
        if (t < 15) {
            server.sendParticles(ParticleTypes.WITCH, getX(), getY() + 1.2, getZ(), 6, 0.6, 0.8, 0.6, 0.05);
        } else if (t == 15) {
            Hazards.sound(server, position(), SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.5F, 1.0F);
            for (int i = 0; i < 2; i++) {
                Archivist copy = ModEntities.ARCHIVIST.get().create(server);
                if (copy == null) continue;
                copy.entityData.set(ILLUSION, true);
                copy.xpReward = 0;
                double a = Math.PI * i + random.nextDouble();
                copy.moveTo(getX() + Math.cos(a) * 4, getY(), getZ() + Math.sin(a) * 4, getYRot(), 0);
                copy.setHealth(1.0F);
                copy.setTarget(target);
                copy.setPersistenceRequired();
                server.addFreshEntity(copy);
                server.sendParticles(ParticleTypes.POOF, copy.getX(), copy.getY() + 1, copy.getZ(), 15, 0.4, 0.6, 0.4, 0.02);
            }
        }
        return t < 30;
    }

    private boolean blink(ServerLevel server, int t, @Nullable LivingEntity target) {
        if (t == 1) {
            Hazards.sound(server, position(), SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, 1.4F, 1.0F);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 20, 0.5, 0.8, 0.5, 0.02);
        }
        if (t == BLINK_AT && target != null) {
            Vec3 behind = target.position().subtract(target.getLookAngle().multiply(1, 0, 1).normalize().scale(3.5)).add(0, 2.5, 0);
            teleportTo(behind.x, behind.y, behind.z);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 20, 0.5, 0.8, 0.5, 0.02);
        }
        return t <= BLINK_AT || volley(server, t - BLINK_AT, target, 10, 24);
    }

    /** Ilusões não deixam saque: somem em fumaça. */
    @Override
    public void die(DamageSource source) {
        if (isIllusion()) {
            vanish();
            return;
        }
        super.die(source);
    }

    private void vanish() {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.POOF, getX(), getY() + 1, getZ(), 20, 0.4, 0.7, 0.4, 0.02);
            Hazards.sound(server, position(), SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.8F, 1.6F);
        }
        discard();
    }

    @Override
    public boolean shouldBeSaved() {
        return !isIllusion() && super.shouldBeSaved();
    }

    @Override
    public float swing(float partialTick) {
        float t = attackTicks() + partialTick;
        return attackId() == 0 ? 0.0F : Mth.clamp(t / 10.0F, 0.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.EVOKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }
}
