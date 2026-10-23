package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

/**
 * Lacaio (NORMAL): pequeno, rápido e covarde. Sozinho é fraco; em bando fica corajoso.
 *
 * <ul>
 *   <li>Grito de alerta: ao ver o alvo ou apanhar, chama os mobs do mod em volta (16 blocos).</li>
 *   <li>Bando: com 2+ aliados no mesmo alvo, corre mais.</li>
 *   <li>Fuga: com menos de 30% de vida e sem bando, foge por 2,5 s (no máximo a cada 15 s).</li>
 *   <li>Bote: de 2,5 a 6 blocos, agacha (telegrafa) e salta mordendo.</li>
 * </ul>
 */
final class MinionBrain extends TierBrain {
    private int fleeTicks;
    private long nextFleeAt;
    private long nextHowlAt;

    MinionBrain(PathfinderMob mob, ElementType element) {
        super(mob, MobTier.NORMAL, element);
        addMove(new Lunge());
    }

    @Override protected int thinkGap() { return 30; }
    @Override protected int meleeInterval() { return 18; }
    @Override protected float meleeElementChance() { return 0.10F; }

    @Override
    protected void onEngage() {
        howl(target);
    }

    @Override
    protected void engage() {
        int pack = packSize();
        if (fleeTicks > 0) {
            fleeTicks--;
            retreat(1.4);
            return;
        }
        if (mob.getHealth() < mob.getMaxHealth() * 0.3F && pack < 2 && now() >= nextFleeAt) {
            fleeTicks = 50;
            nextFleeAt = now() + 300;
            anim(MobAnim.DODGE, 8);
            sound(SoundEvents.SILVERFISH_AMBIENT, 1.0F, 0.7F);
            retreat(1.4);
            return;
        }
        chase(pack >= 2 ? 1.25 : 1.0);
        tryMelee(1.0F, SoundEvents.FOX_BITE, 1.3F);
    }

    @Override
    public void onHurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker) howl(attacker);
    }

    @Override
    public void passiveTick() {
        // Fagulha discreta do elemento: o Lacaio quase não tem aura.
        if (element != ElementType.NONE && mob.tickCount % 30 == 0) {
            particles(elementParticle(), center(), 1, 0.2, 0.01);
        }
    }

    /** Joga a cabeça para trás e chama os aliados sem alvo para cima de quem o ameaçou. */
    private void howl(LivingEntity threat) {
        if (threat == null || now() < nextHowlAt) return;
        nextHowlAt = now() + 200;
        anim(MobAnim.HOWL, 14);
        sound(SoundEvents.WOLF_HOWL, 0.7F, 1.6F);
        for (PathfinderMob ally : allies(16)) {
            if (ally.getTarget() == null) ally.setTarget(threat);
        }
    }

    private int packSize() {
        int n = 0;
        for (PathfinderMob ally : allies(10)) if (ally.getTarget() == target) n++;
        return n;
    }

    /** Bote: 8 ticks agachado rosnando, depois salta; morde uma vez se encostar no alvo. */
    private final class Lunge extends Move {
        private boolean bit;

        Lunge() { super(80, 3); }

        @Override
        protected boolean usable(double dist) {
            return dist > 2.5 && dist < 6 && mob.onGround() && fleeTicks == 0;
        }

        @Override
        protected void begin() {
            bit = false;
            anim(MobAnim.WINDUP, 8);
            sound(SoundEvents.WOLF_GROWL, 0.9F, 1.5F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < 8) {
                face(flatDirTo(target.position()));
                return true;
            }
            if (t == 8) {
                Vec3 dir = flatDirTo(target.position());
                mob.setDeltaMovement(dir.x * 0.75, 0.38, dir.z * 0.75);
                mob.hasImpulse = true;
                anim(MobAnim.LUNGE, 12);
                sound(SoundEvents.PHANTOM_BITE, 0.8F, 1.6F);
                return true;
            }
            if (!bit && mob.getBoundingBox().inflate(0.4).intersects(target.getBoundingBox())) {
                bit = true;
                mob.swing(InteractionHand.MAIN_HAND);
                strike(target, baseDamage() * 1.3F, true);
                sound(SoundEvents.FOX_BITE, 1.0F, 1.1F);
            }
            return t < 22 && !(t > 11 && mob.onGround());
        }
    }
}
