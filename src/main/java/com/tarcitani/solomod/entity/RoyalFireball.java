package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.mob.Hazards;
import com.tarcitani.solomod.mob.TierBrain;
import com.tarcitani.solomod.mob.TierIdentity;
import com.tarcitani.solomod.mob.SovereignBrain;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Bola de fogo real do Rei Vulcânico e do Soberano: voo reto a 0,35 bloco/tick (7 blocos/s), sem
 * aceleração. Um golpe direto do jogador a rebate na direção da mira a 0,45 bloco/tick. Se voltar
 * ao conjurador, causa dano atribuído ao jogador e o deixa agachado por 100 ticks, salvo na Fúria
 * e durante rugidos/agonia. Na Fúria o dano continua valendo, mas não causa stun.
 * Modelo 3D próprio de 2,4 blocos (núcleo, crosta de magma e coroas orbitais), sem item vanilla.
 * Núcleo luminoso, espiral de chamas, fumaça e faíscas indicam o trajeto; impactos não destroem blocos.
 * Dono original, reflexão, dano e idade persistem em NBT; toda colisão/reflexão é decidida pelo servidor.
 */
public final class RoyalFireball extends Projectile {
    private UUID caster;
    private boolean reflected;
    private float damage = 12;
    private int age;

    public RoyalFireball(EntityType<? extends RoyalFireball> type, Level level) {
        super(type, level);
    }

    /** Dispara à altura do peito do alvo para permitir o rebote mesmo contra chefes gigantes. */
    public static void launch(ServerLevel level, LivingEntity boss, LivingEntity target, float damage) {
        RoyalFireball ball = new RoyalFireball(ModEntities.ROYAL_FIREBALL.get(), level);
        ball.caster = boss.getUUID();
        ball.setOwner(boss);
        ball.damage = damage;
        Vec3 aim = target.getEyePosition().add(0, -0.35, 0);
        Vec3 horizontal = aim.subtract(boss.position()).multiply(1, 0, 1).normalize();
        double radius = TierIdentity.ROYAL_FIREBALL_SIZE / 2;
        ball.setPos(boss.position().add(horizontal.scale(boss.getBbWidth() / 2 + radius + 0.4))
                .add(0, Math.max(0.15, Math.min(2.2, boss.getBbHeight() * 0.5) - radius), 0));
        ball.setDeltaMovement(aim.subtract(ball.getBoundingBox().getCenter()).normalize().scale(TierIdentity.ROYAL_FIREBALL_SPEED));
        level.addFreshEntity(ball);
        Hazards.sound(level, ball.position(), SoundEvents.BLAZE_SHOOT, 2F, 0.65F);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override public boolean isPickable() { return true; }
    @Override public float getPickRadius() { return 0.3F; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player
                || player.isSpectator() || !player.isAlive()) return false;
        if (level().isClientSide()) return true;
        reflected = true;
        setOwner(player);
        setDeltaMovement(player.getLookAngle().normalize().scale(TierIdentity.ROYAL_FIREBALL_RETURN_SPEED));
        hurtMarked = true;
        hasImpulse = true;
        burst(ParticleTypes.CRIT, 24);
        Hazards.sound((ServerLevel) level(), position(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.5F, 1.4F);
        return true;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || entity == getOwner()) return false;
        if (reflected && entity.getUUID().equals(caster)) return true;
        return getOwner() instanceof LivingEntity owner && Hazards.isEnemy(owner, living);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (++age > TierIdentity.ROYAL_FIREBALL_LIFE || !(level() instanceof ServerLevel server)
                    || caster == null || !(server.getEntity(caster) instanceof LivingEntity boss) || !boss.isAlive()) {
                discard();
                return;
            }
            TierBrain casterBrain = TierBrain.of(boss);
            if (casterBrain instanceof SovereignBrain && casterBrain.phase() != 2 && casterBrain.phase() != SovereignBrain.STAGES) {
                discard();
                return;
            }
            // Varredura pelo centro e raio real: a colisão acompanha o núcleo grande, inclusive no rebote.
            Vec3 center = getBoundingBox().getCenter();
            Vec3 nextCenter = center.add(getDeltaMovement());
            HitResult hit = level().clip(new ClipContext(center, nextCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level(), this, center, hit.getLocation(),
                    getBoundingBox().expandTowards(getDeltaMovement()), this::canHitEntity, getBbWidth() * 0.5F);
            if (entityHit != null) hit = entityHit;
            if (hit.getType() == HitResult.Type.MISS && level().getBlockCollisions(this,
                    getBoundingBox().move(getDeltaMovement()).deflate(0.1)).iterator().hasNext()) {
                // Um impacto lateral do volume não aparece no raio central.
                impact(null, server);
                return;
            }
            if (hit.getType() != HitResult.Type.MISS) {
                impact(hit, server);
                return;
            }
        } else {
            Vec3 center = getBoundingBox().getCenter();
            // Espiral contínua e rastro, sem trafegar dezenas de pacotes de partículas por tick.
            for (int i = 0; i < 10; i++) {
                double a = tickCount * 0.45 + Math.PI * 2 * i / 10;
                level().addParticle(ParticleTypes.FLAME, center.x + Math.cos(a) * 1.4,
                        center.y + Math.sin(a) * 1.4, center.z, 0, 0.01, 0);
            }
            Vec3 trail = getDeltaMovement().scale(-0.4);
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.x, center.y, center.z, trail.x, trail.y, trail.z);
            if (tickCount % 3 == 0) level().addParticle(ParticleTypes.LAVA, center.x, center.y, center.z, 0, 0, 0);
        }
        setPos(position().add(getDeltaMovement()));
        updateRotation();
    }

    private void impact(HitResult hit, ServerLevel server) {
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity victim) {
            if (reflected && victim.getUUID().equals(caster) && getOwner() instanceof Player player) {
                TierBrain brain = TierBrain.of(victim);
                if (brain != null) brain.stunFromFireball(TierIdentity.ROYAL_FIREBALL_STUN_TICKS);
                victim.hurt(damageSources().indirectMagic(this, player), damage);
            } else if (getOwner() instanceof LivingEntity owner) {
                Hazards.hit(owner, victim, damage, ElementType.FIRE, 3, true);
            }
        }
        burst(ParticleTypes.FLAME, 45);
        burst(ParticleTypes.LAVA, 12);
        burst(ParticleTypes.EXPLOSION, 2);
        Hazards.sound(server, position(), SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 1.1F);
        discard();
    }

    private void burst(net.minecraft.core.particles.ParticleOptions particle, int count) {
        Vec3 center = getBoundingBox().getCenter();
        ((ServerLevel) level()).sendParticles(particle, center.x, center.y, center.z, count, 0.8, 0.8, 0.8, 0.08);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (caster != null) tag.putUUID("Caster", caster);
        tag.putBoolean("Reflected", reflected);
        tag.putFloat("Damage", damage);
        tag.putInt("Age", age);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        caster = tag.hasUUID("Caster") ? tag.getUUID("Caster") : null;
        reflected = tag.getBoolean("Reflected");
        damage = tag.getFloat("Damage");
        age = tag.getInt("Age");
    }
}
