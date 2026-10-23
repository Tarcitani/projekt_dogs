package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.mob.Hazards;
import com.tarcitani.solomod.mob.SovereignBrain;
import com.tarcitani.solomod.mob.TierBrain;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Esfera de água do Soberano (modo água), com modelo 3D próprio em CUBOS, no estilo do Minecraft (ver
 * {@code WaterOrbRenderer}). Três jeitos:
 * <ul>
 *   <li>{@code ORBIT}: gira em volta do Soberano por um tempo (telegrafa o ataque);</li>
 *   <li>{@code HOMING}: depois persegue o jogador escolhido, curvando e acelerando (até 0,55 bloco/tick),
 *       e explode em respingo (raio 3,2) ao tocar alguém ou um bloco. Um golpe direto do jogador a estoura
 *       antes, sem dano: é a defesa;</li>
 *   <li>{@code WAVE}: só visual, rola em linha reta compondo a crista do Maremoto (o dano do Maremoto é
 *       calculado pelo ataque, não por elas).</li>
 * </ul>
 * Não é salva e roda só no servidor; o cliente desenha e solta bolhas.
 */
public final class WaterOrb extends Entity {
    public static final float SIZE = 2.0F;
    private static final double ORBIT_RADIUS = 7, ORBIT_HEIGHT = 12, SPEED = 0.55, SPLASH_RADIUS = 3.2;
    private static final int HOMING_LIFE = 160;

    private enum Mode { ORBIT, HOMING, WAVE }

    private Mode mode = Mode.HOMING;
    private UUID owner, target;
    private float damage;
    private int orbitTicks, waveLife;
    private double phase;
    private Vec3 velocity = Vec3.ZERO;
    private int age;

    public WaterOrb(EntityType<? extends WaterOrb> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    /** Esfera que gira em volta do Soberano por {@code orbitTicks} e depois persegue {@code target}. */
    public static void orbit(ServerLevel level, LivingEntity owner, LivingEntity target, double phase, int orbitTicks, float damage) {
        WaterOrb orb = new WaterOrb(ModEntities.WATER_ORB.get(), level);
        orb.mode = Mode.ORBIT;
        orb.owner = owner.getUUID();
        orb.target = target.getUUID();
        orb.phase = phase;
        orb.orbitTicks = orbitTicks;
        orb.damage = damage;
        orb.setPos(owner.position().add(Math.cos(phase) * ORBIT_RADIUS, ORBIT_HEIGHT, Math.sin(phase) * ORBIT_RADIUS));
        level.addFreshEntity(orb);
    }

    /** Esfera só visual da crista do Maremoto: anda {@code velocity} por tick durante {@code life} ticks. */
    public static void wave(ServerLevel level, LivingEntity owner, Vec3 pos, Vec3 velocity, int life) {
        WaterOrb orb = new WaterOrb(ModEntities.WATER_ORB.get(), level);
        orb.mode = Mode.WAVE;
        orb.owner = owner.getUUID();
        orb.velocity = velocity;
        orb.waveLife = life;
        orb.setPos(pos);
        level.addFreshEntity(orb);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override public boolean isPickable() { return mode != Mode.WAVE; }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}

    /** Um golpe direto do jogador estoura a esfera (sem dano para ninguém). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player || mode == Mode.WAVE) return false;
        if (level() instanceof ServerLevel server) pop(server, ParticleTypes.BUBBLE_POP, 30);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (level().isClientSide()) {
            if (age % 2 == 0) level().addParticle(ParticleTypes.BUBBLE, getX() + (random.nextDouble() - 0.5) * 2, getY() + (random.nextDouble() - 0.5) * 2,
                    getZ() + (random.nextDouble() - 0.5) * 2, 0, 0.05, 0);
            return;
        }
        ServerLevel server = (ServerLevel) level();
        if (mode == Mode.WAVE) {
            setPos(position().add(velocity));
            if (age >= waveLife) pop(server, ParticleTypes.SPLASH, 20);
            return;
        }
        Entity ownerEntity = owner == null ? null : server.getEntity(owner);
        if (!(ownerEntity instanceof LivingEntity boss) || !boss.isAlive()) {
            pop(server, ParticleTypes.BUBBLE_POP, 20);
            return;
        }
        // Regra dos poderes: fora do estágio da água (e do último), as esferas já lançadas somem.
        if (TierBrain.of(boss) instanceof SovereignBrain sovereign
                && sovereign.phase() != SovereignBrain.WATER_STAGE && sovereign.phase() != SovereignBrain.STAGES) {
            pop(server, ParticleTypes.BUBBLE_POP, 20);
            return;
        }
        if (mode == Mode.ORBIT) {
            double a = phase + age * 0.14;
            Vec3 c = boss.position().add(0, ORBIT_HEIGHT, 0);
            setPos(c.x + Math.cos(a) * ORBIT_RADIUS, c.y + Math.sin(age * 0.2 + phase) * 0.6, c.z + Math.sin(a) * ORBIT_RADIUS);
            if (age >= orbitTicks) {
                mode = Mode.HOMING;
                age = 0;
                Hazards.sound(server, position(), SoundEvents.DROWNED_SHOOT, 2.5F, 0.6F);
            }
            return;
        }
        homing(server, boss);
    }

    private void homing(ServerLevel server, LivingEntity boss) {
        Entity chased = target == null ? null : server.getEntity(target);
        if (age > HOMING_LIFE || !(chased instanceof LivingEntity victim) || !Hazards.isEnemy(boss, victim)) {
            pop(server, ParticleTypes.BUBBLE_POP, 20);
            return;
        }
        Vec3 want = victim.getBoundingBox().getCenter().subtract(position());
        // Curva até 10% por tick em direção ao alvo e acelera: dá para desviar de lado, não de costas.
        Vec3 dir = velocity.lengthSqr() < 1.0E-4 ? want.normalize() : velocity.normalize().lerp(want.normalize(), 0.1).normalize();
        velocity = dir.scale(Math.min(SPEED, velocity.length() + 0.04));
        AABB next = getBoundingBox().move(velocity);
        if (!level().noCollision(next) || !Hazards.enemiesIn(server, boss, next.inflate(0.2)).isEmpty()) {
            splash(server, boss);
            return;
        }
        setPos(position().add(velocity));
    }

    /** Respingo em área: dano d'água com a passiva do elemento e um empurrão para cima. */
    private void splash(ServerLevel server, LivingEntity boss) {
        Vec3 center = position();
        for (LivingEntity e : Hazards.enemiesIn(server, boss, new AABB(center, center).inflate(SPLASH_RADIUS))) {
            if (e.position().distanceToSqr(center) > SPLASH_RADIUS * SPLASH_RADIUS) continue;
            Hazards.hit(boss, e, damage, ElementType.WATER, 2, true);
            Hazards.push(e, e.position().subtract(center).multiply(1, 0, 1).normalize().scale(0.5).add(0, 0.55, 0));
        }
        Hazards.sound(server, center, SoundEvents.GENERIC_SPLASH, 2.5F, 0.7F);
        pop(server, ParticleTypes.SPLASH, 100);
    }

    private void pop(ServerLevel server, net.minecraft.core.particles.ParticleOptions particle, int count) {
        Hazards.farParticles(server, particle, getX(), getY(), getZ(), count, 1.2, 1.2, 1.2, 0.15);
        discard();
    }
}
