package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.entity.passive.PassiveEchoTriggers;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

/**
 * Guardião (SUB_BOSS): mago tático que flutua. Odeia corpo a corpo e controla a distância.
 *
 * <ul>
 *   <li>Distância: recua abaixo de 5 blocos, aproxima acima de 11, circula devagar entre os dois.</li>
 *   <li>Rajada Elemental: conjura e dispara 3 orbes mirando onde o alvo VAI estar.</li>
 *   <li>Repulsão: se o alvo colar nele, telegrafa um anel e explode empurrando para longe.</li>
 *   <li>Salto Esmagador: marca o ponto de queda no chão, salta e cai com uma explosão.</li>
 *   <li>Translocar: se o alvo foge ou fica longe por 5 s, teleporta para as costas dele.</li>
 *   <li>Invocação: uma vez, abaixo de 50% de vida, chama 2 Lacaios do mesmo elemento.</li>
 * </ul>
 *
 * <p><b>Fúria</b> (1/4 da vida, 20 s): para de fugir e parte para cima. Ataques exclusivos: Tempestade de
 * Orbes (espirais de orbes em todas as direções), Olhar Fulminante (raio do olho girando 360°) e
 * Translocação Frenética (três teleportes seguidos, cada um com uma explosão).
 */
final class GuardianBrain extends TierBrain {
    private final ServerBossEvent bar;
    private int strafeSide = 1;
    private int farTicks;
    private boolean summoned;

    GuardianBrain(PathfinderMob mob, ElementType element) {
        super(mob, MobTier.SUB_BOSS, element);
        bar = new ServerBossEvent(mob.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
        addMove(new Volley());
        addMove(new Repulse());
        addMove(new LeapSlam());
        addMove(new Blink());
        addMove(new Summon());
        addRageMove(new OrbStorm());
        addRageMove(new EyeBeam());
        addRageMove(new FrenzyBlink());
    }

    @Override
    protected void onRageStart() {
        bar.setColor(BossEvent.BossBarColor.RED);
    }

    @Override
    protected void onRageEnd() {
        bar.setColor(BossEvent.BossBarColor.PURPLE);
    }

    @Override protected int thinkGap() { return 25; }
    @Override protected int meleeInterval() { return 20; }
    @Override protected float meleeElementChance() { return 0.40F; }

    @Override
    protected void engage() {
        double dist = mob.distanceTo(target);
        if (isRaging()) {
            chase(1.35);
            tryMelee(1.2F, SoundEvents.EVOKER_CAST_SPELL, 0.8F);
            return;
        }
        farTicks = dist > 9 ? farTicks + 1 : 0;
        if (dist < 5) {
            retreat(1.15);
        } else if (dist > 11) {
            chase(1.0);
        } else {
            if (random.nextInt(60) == 0) strafeSide = -strafeSide;
            strafe(0, 0.35F * strafeSide);
        }
        tryMelee(1.0F, SoundEvents.EVOKER_CAST_SPELL, 1.4F);
    }

    @Override
    public void passiveTick() {
        // Três motes do elemento orbitando o corpo.
        if (mob.tickCount % 2 == 0) {
            double r = 0.9 * mob.getScale();
            for (int k = 0; k < 3; k++) {
                double a = mob.tickCount * 0.15 + k * (2 * Math.PI / 3);
                Vec3 p = mob.position().add(Math.cos(a) * r, mob.getBbHeight() * 0.6 + Math.sin(a * 2) * 0.2, Math.sin(a) * r);
                level().sendParticles(Hazards.dust(element, 1.0F), p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
        }
        updateBossBar(bar, 40);
    }

    @Override
    public void onRemoved() {
        bar.removeAllPlayers();
    }

    @Override
    public void onDeath() {
        sound(SoundEvents.TOTEM_USE, 1.2F, 0.7F);
        particles(ParticleTypes.TOTEM_OF_UNDYING, center(), 40, 0.6, 0.4);
        bar.removeAllPlayers();
    }

    /** Rajada Elemental: 16 ticks conjurando, depois 3 orbes com mira preditiva. */
    private final class Volley extends Move {
        Volley() { super(70, 4); }

        @Override
        protected boolean usable(double dist) {
            return dist > 4 && dist < 22 && mob.getSensing().hasLineOfSight(target);
        }

        @Override
        protected void begin() {
            anim(MobAnim.CAST, 30);
            sound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.2F, 1.1F);
        }

        @Override
        protected boolean tick(int t) {
            mob.getLookControl().setLookAt(target, 30, 30);
            if (t == 16) PassiveEchoTriggers.volleyAimed(mob, target);
            if (t < 16) {
                SkillVfx.ring(level(), center(), 0.4 + t * 0.05, elementParticle(), 6);
            } else if (t == 16 || t == 22 || t == 28) {
                Vec3 from = mob.getEyePosition();
                double speed = 0.7;
                double travel = from.distanceTo(target.position()) / speed;
                Vec3 aim = target.position().add(target.getDeltaMovement().multiply(1, 0, 1).scale(travel))
                        .add(0, target.getBbHeight() * 0.5, 0);
                Vec3 vel = aim.subtract(from).normalize().scale(speed);
                Hazards.add(new Hazards.Orb(level(), mob, element, from, vel, baseDamage() * 1.2F, potency(), 60));
                sound(Hazards.castSound(element), 1.0F, 1.0F + (t - 16) * 0.03F);
            }
            return t < 30;
        }
    }

    /** Repulsão: anel telegrafado por 10 ticks e explosão que arremessa quem estiver perto. */
    private final class Repulse extends Move {
        private static final double RADIUS = 4.0;

        Repulse() { super(80, 6); }

        @Override
        protected boolean usable(double dist) {
            return dist < 3.5;
        }

        @Override
        protected void begin() {
            PassiveEchoTriggers.guardianRepulse(mob);
            anim(MobAnim.WINDUP, 10);
            sound(SoundEvents.GUARDIAN_ATTACK, 1.0F, 1.6F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < 10) {
                if (t % 2 == 0) SkillVfx.ring(level(), mob.position().add(0, 0.1, 0), RADIUS, Hazards.dust(element, 1.4F), 24);
                return true;
            }
            if (t == 10) {
                anim(MobAnim.SLAM, 10);
                sound(SoundEvents.WIND_CHARGE_BURST.value(), 1.5F, 0.8F);
                particles(ParticleTypes.EXPLOSION, center(), 3, 0.6, 0);
                SkillVfx.ring(level(), mob.position().add(0, 0.3, 0), RADIUS, elementParticle(), 30);
                for (LivingEntity e : enemiesWithin(RADIUS)) {
                    magicStrike(e, baseDamage());
                    Hazards.push(e, flatDirTo(e.position()).scale(1.4).add(0, 0.5, 0));
                }
            }
            return t < 14;
        }
    }

    /** Salto Esmagador: marca a queda, salta em parábola e explode ao tocar o chão. */
    private final class LeapSlam extends Move {
        private static final double RADIUS = 3.5;
        private Vec3 landing;

        LeapSlam() { super(160, 3); }

        @Override
        protected boolean usable(double dist) {
            return dist > 5 && dist < 16 && mob.onGround();
        }

        @Override
        protected void begin() {
            landing = target.position();
            anim(MobAnim.WINDUP, 10);
            sound(SoundEvents.HOGLIN_ANGRY, 1.2F, 0.8F);
        }

        @Override
        protected boolean tick(int t) {
            if (t % 3 == 0) SkillVfx.ring(level(), landing.add(0, 0.1, 0), RADIUS, Hazards.dust(element, 1.5F), 22);
            if (t < 10) {
                face(flatDirTo(landing));
                return true;
            }
            if (t == 10) {
                Vec3 d = landing.subtract(mob.position()).multiply(1, 0, 1);
                // No ar o mob perde 9% da velocidade horizontal por tick: ~9,4 x v percorridos em 20 ticks.
                double v = Math.min(1.6, d.length() / 9.4);
                Vec3 dir = d.normalize();
                mob.setDeltaMovement(dir.x * v, 0.85, dir.z * v);
                mob.hasImpulse = true;
                anim(MobAnim.LEAP, 20);
                sound(SoundEvents.BREEZE_JUMP, 1.2F, 0.6F);
                return true;
            }
            mob.resetFallDistance();
            if ((t > 14 && mob.onGround()) || t > 45) {
                anim(MobAnim.SLAM, 10);
                sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.5F, 0.8F);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.9F);
                particles(ParticleTypes.EXPLOSION, mob.position(), 4, 1.2, 0);
                SkillVfx.ring(level(), mob.position().add(0, 0.2, 0), RADIUS, elementParticle(), 30);
                for (LivingEntity e : enemiesWithin(RADIUS)) {
                    strike(e, baseDamage() * 1.5F, true);
                    Hazards.push(e, flatDirTo(e.position()).scale(0.5).add(0, 0.6, 0));
                }
                return false;
            }
            return true;
        }
    }

    /** Translocar: some, reaparece às costas do alvo. */
    private final class Blink extends Move {
        Blink() { super(140, 5); }

        @Override
        protected boolean usable(double dist) {
            return dist > 14 || farTicks > 100;
        }

        @Override
        protected void begin() {
            PassiveEchoTriggers.blinkPrepared(mob, target);
            anim(MobAnim.BLINK, 12);
            sound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.7F);
            particles(ParticleTypes.PORTAL, center(), 40, 0.5, 0.5);
        }

        @Override
        protected boolean tick(int t) {
            if (t == 6) {
                Vec3 back = target.getLookAngle().multiply(1, 0, 1).normalize().scale(-3);
                Vec3 dest = target.position().add(back);
                if (!mob.randomTeleport(dest.x, dest.y, dest.z, true)) {
                    dest = target.position().subtract(back);
                    mob.randomTeleport(dest.x, dest.y, dest.z, true);
                }
                face(flatDirTo(target.position()));
                sound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.2F);
                particles(ParticleTypes.PORTAL, center(), 40, 0.5, 0.5);
                particles(elementParticle(), center(), 15, 0.5, 0.05);
                farTicks = 0;
                meleeCooldown = 0;
            }
            return t < 12;
        }
    }

    /** Invocação (uma vez por luta): sobe girando e chama 2 Lacaios. */
    private final class Summon extends Move {
        Summon() { super(20 * 60 * 10, 100); }

        @Override
        protected boolean usable(double dist) {
            return !summoned && mob.getHealth() < mob.getMaxHealth() * 0.5F;
        }

        @Override
        protected void begin() {
            summoned = true;
            anim(MobAnim.SUMMON, 30);
            sound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.5F, 0.9F);
        }

        @Override
        protected boolean tick(int t) {
            double a = t * 0.5;
            Vec3 p = mob.position().add(Math.cos(a) * 1.5, t * 0.08, Math.sin(a) * 1.5);
            particles(elementParticle(), p, 2, 0.05, 0);
            if (t == 30) {
                summon(MobTier.NORMAL, 2);
                sound(SoundEvents.EVOKER_CAST_SPELL, 1.5F, 0.8F);
            }
            return t < 30;
        }
    }

    // ------------------------------------------------------------------ ataques da Fúria

    /** Tempestade de Orbes: a cada 6 ticks, um anel de 10 orbes que gira um pouco a cada leva (espiral). */
    private final class OrbStorm extends Move {
        OrbStorm() { super(120, 4); }

        @Override
        protected void begin() {
            anim(MobAnim.CAST, 60);
            sound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.5F, 0.7F);
        }

        @Override
        protected boolean tick(int t) {
            if (t > 0 && t % 6 == 0) {
                Vec3 from = center();
                for (int i = 0; i < 10; i++) {
                    double a = Math.PI * 2 * i / 10 + t * 0.25;
                    Vec3 vel = new Vec3(Math.cos(a), 0, Math.sin(a)).scale(0.45);
                    Hazards.add(new Hazards.Orb(level(), mob, element, from, vel, baseDamage() * 0.7F, potency(), 40));
                }
                sound(Hazards.castSound(element), 1.0F, 0.8F + t * 0.01F);
            }
            return t < 60;
        }
    }

    /** Olhar Fulminante: mira 12 ticks e depois o raio do olho dá uma volta completa ao redor do Oráculo. */
    private final class EyeBeam extends Move {
        private static final double LENGTH = 14;
        private double startAngle;

        EyeBeam() { super(140, 4); }

        @Override
        protected void begin() {
            Vec3 d = flatDirTo(target.position());
            startAngle = Math.atan2(d.z, d.x);
            anim(MobAnim.CAST, 50);
            sound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 1.2F);
        }

        @Override
        protected boolean tick(int t) {
            double angle = t < 12 ? startAngle : startAngle + (t - 12) / 38.0 * Math.PI * 2;
            Vec3 dir = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            face(dir);
            Vec3 from = mob.getEyePosition().add(0, -mob.getBbHeight() * 0.3, 0);
            Vec3 to = from.add(dir.scale(LENGTH));
            if (t < 12) {
                if (t % 2 == 0) SkillVfx.line(level(), from, to, Hazards.dust(element, 0.8F), 14);
                return true;
            }
            SkillVfx.line(level(), from, to, elementParticle(), 18);
            SkillVfx.line(level(), from, to, Hazards.dust(element, 1.8F), 10);
            if (t % 3 == 0) {
                for (LivingEntity e : enemiesWithin(LENGTH)) {
                    Vec3 p = e.position().add(0, e.getBbHeight() * 0.5, 0);
                    double along = p.subtract(from).dot(dir);
                    if (along < 0 || along > LENGTH) continue;
                    Vec3 closest = from.add(dir.scale(along));
                    if (closest.distanceTo(p) < 1.3 + e.getBbWidth() / 2) magicStrike(e, baseDamage() * 0.6F);
                }
            }
            if (t % 5 == 0) sound(SoundEvents.GUARDIAN_ATTACK, 1.0F, 1.6F);
            return t < 50;
        }
    }

    /** Translocação Frenética: três vezes seguidas some, reaparece nas costas do alvo e explode. */
    private final class FrenzyBlink extends Move {
        FrenzyBlink() { super(100, 5); }

        @Override
        protected boolean tick(int t) {
            int local = t % 14;
            if (t / 14 >= 3) return false;
            if (local == 0) {
                anim(MobAnim.BLINK, 8);
                sound(SoundEvents.ENDERMAN_TELEPORT, 1.2F, 0.6F);
                particles(ParticleTypes.PORTAL, center(), 30, 0.5, 0.5);
            }
            if (local == 4) {
                Vec3 back = target.getLookAngle().multiply(1, 0, 1).normalize().scale(-2.5);
                Vec3 dest = target.position().add(back);
                mob.randomTeleport(dest.x, dest.y, dest.z, true);
                face(flatDirTo(target.position()));
            }
            if (local == 8) {
                anim(MobAnim.SLAM, 6);
                sound(SoundEvents.WIND_CHARGE_BURST.value(), 1.5F, 0.6F);
                particles(ParticleTypes.EXPLOSION, center(), 2, 0.5, 0);
                SkillVfx.ring(level(), mob.position().add(0, 0.3, 0), 3.5, elementParticle(), 24);
                for (LivingEntity e : enemiesWithin(3.5)) {
                    magicStrike(e, baseDamage() * 0.9F);
                    Hazards.push(e, flatDirTo(e.position()).scale(1.0).add(0, 0.4, 0));
                }
            }
            return true;
        }
    }
}
