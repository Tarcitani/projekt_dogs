package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Elite (OP): duelista. Mantém a guarda, circula o alvo enquanto o golpe recarrega e pune erros.
 *
 * <ul>
 *   <li>Guarda: enquanto recarrega, anda de lado a 2,5–4 blocos, trocando de lado de tempos em tempos.</li>
 *   <li>Aparar: 30% de chance de defender um golpe corpo a corpo de frente (a cada 4 s), desviar para o lado
 *       e contra-atacar logo em seguida.</li>
 *   <li>Investida: telegrafa a linha no chão por 0,7 s e dispara em linha reta, atropelando.</li>
 *   <li>Golpe Triplo: três cortes rápidos em leque, o último com o elemento.</li>
 * </ul>
 */
final class EliteBrain extends TierBrain {
    private int strafeSide = 1;
    private int strafeTimer;
    private long nextParryAt;
    private int riposteIn = -1;

    EliteBrain(PathfinderMob mob, ElementType element) {
        super(mob, MobTier.OP, element);
        addMove(new Dash());
        addMove(new Combo());
    }

    @Override protected int thinkGap() { return 20; }
    @Override protected int meleeInterval() { return 14; }
    @Override protected float meleeElementChance() { return 0.25F; }

    @Override
    protected void engage() {
        if (riposteIn >= 0 && riposteIn-- == 0) {
            meleeCooldown = 0;
            if (tryMelee(1.5F, SoundEvents.PLAYER_ATTACK_STRONG, 1.2F)) {
                particles(ParticleTypes.CRIT, target.getEyePosition(), 10, 0.3, 0.2);
            }
        }
        double dist = mob.distanceTo(target);
        if (meleeCooldown > 0 && dist < 6) {
            if (--strafeTimer <= 0) {
                strafeTimer = 30 + random.nextInt(30);
                strafeSide = -strafeSide;
            }
            float forward = dist < 2.5 ? -0.4F : dist > 4 ? 0.45F : 0F;
            strafe(forward, 0.5F * strafeSide);
            return;
        }
        chase(1.15);
        tryMelee(1.0F, SoundEvents.PLAYER_ATTACK_SWEEP, 1.4F);
    }

    @Override
    public boolean onIncomingDamage(DamageSource source, float amount) {
        if (isBusy() || now() < nextParryAt || random.nextFloat() > 0.30F) return false;
        // Só golpes corpo a corpo diretos, vindos da frente.
        if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) return false;
        Vec3 toAttacker = flatDirTo(attacker.position());
        if (mob.getLookAngle().multiply(1, 0, 1).normalize().dot(toAttacker) < 0.3) return false;

        nextParryAt = now() + 80;
        riposteIn = 6;
        anim(MobAnim.DODGE, 10);
        sound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.3F);
        sound(SoundEvents.ANVIL_LAND, 0.3F, 2.0F);
        particles(ParticleTypes.CRIT, mob.getEyePosition(), 12, 0.3, 0.3);
        Vec3 side = new Vec3(-toAttacker.z, 0, toAttacker.x).scale(random.nextBoolean() ? 0.6 : -0.6);
        Hazards.push(mob, side.add(0, 0.15, 0));
        return true;
    }

    @Override
    public void passiveTick() {
        if (mob.tickCount % 10 == 0) {
            SkillVfx.ring(level(), mob.position().add(0, 0.1, 0), 0.6 * mob.getScale(),
                    element == ElementType.NONE ? ParticleTypes.CRIT : elementParticle(), 6);
        }
    }

    /** Investida: 14 ticks mirando (linha telegrafada), 10 ticks correndo em linha reta. */
    private final class Dash extends Move {
        private static final int AIM = 14, RUN = 10;
        private final Set<Integer> hit = new HashSet<>();
        private Vec3 dir;

        Dash() { super(100, 3); }

        @Override
        protected boolean usable(double dist) {
            return dist > 4 && dist < 12 && mob.getSensing().hasLineOfSight(target);
        }

        @Override
        protected void begin() {
            hit.clear();
            anim(MobAnim.WINDUP, AIM);
            sound(SoundEvents.PIGLIN_BRUTE_ANGRY, 1.0F, 1.3F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < AIM) {
                dir = flatDirTo(target.position());
                face(dir);
                if (t % 2 == 0) {
                    Vec3 from = mob.position().add(0, 0.2, 0);
                    SkillVfx.line(level(), from, from.add(dir.scale(10)), Hazards.dust(element, 1.0F), 14);
                }
                return true;
            }
            if (t == AIM) {
                anim(MobAnim.DASH, RUN);
                sound(SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.0F, 1.2F);
            }
            face(dir);
            mob.setDeltaMovement(dir.x * 1.1, mob.getDeltaMovement().y, dir.z * 1.1);
            particles(elementParticle(), center(), 3, 0.3, 0.02);
            for (LivingEntity e : Hazards.enemiesIn(level(), mob, mob.getBoundingBox().inflate(0.5))) {
                if (hit.add(e.getId())) {
                    strike(e, baseDamage() * 1.6F, true);
                    Hazards.push(e, dir.scale(1.0).add(0, 0.3, 0));
                    sound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 0.9F);
                }
            }
            return t < AIM + RUN && !mob.horizontalCollision;
        }
    }

    /** Golpe Triplo: cortes nos ticks 6, 14 e 22, cada um avançando um pouco. */
    private final class Combo extends Move {
        Combo() { super(110, 4); }

        @Override
        protected boolean usable(double dist) {
            return dist < 3.2;
        }

        @Override
        protected void begin() {
            anim(MobAnim.COMBO, 26);
        }

        @Override
        protected boolean tick(int t) {
            Vec3 dir = flatDirTo(target.position());
            face(dir);
            if (t == 6 || t == 14 || t == 22) {
                int n = (t - 6) / 8;
                mob.swing(n == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                Hazards.push(mob, dir.scale(0.25));
                sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.9F + 0.2F * n);
                Vec3 front = center().add(dir.scale(1.4 * mob.getScale()));
                particles(ParticleTypes.SWEEP_ATTACK, front, 1, 0, 0);
                double reach = 2.6 * mob.getScale();
                for (LivingEntity e : enemiesWithin(reach)) {
                    if (dir.dot(flatDirTo(e.position())) > 0.35) strike(e, baseDamage() * 0.75F, n == 2);
                }
            }
            return t < 26;
        }
    }
}
