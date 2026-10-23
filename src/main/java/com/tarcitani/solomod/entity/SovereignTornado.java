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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tornado do Soberano (modo ar): uma entidade com modelo 3D próprio em CUBOS, no estilo do Minecraft (pilha de
 * anéis quadrados girando, ver {@code SovereignTornadoRenderer}) que persegue um jogador a 0,13 bloco/tick (mais lento que quem anda),
 * dura 11 s e lança para o alto quem encosta no pé dele. A velocidade de lançamento dá cerca de 11 blocos de
 * altura: o dano vem sobretudo da queda. Quem já foi lançado fica 3 s imune ao mesmo tornado.
 *
 * <p>Toda a lógica roda no servidor; o cliente só desenha e solta poeira. Não é salva (some ao recarregar o
 * mundo) e não tem colisão nem pode ser golpeada: o perigo é o pé dele, não o corpo.
 */
public final class SovereignTornado extends Entity {
    public static final double SPEED = 0.13, RADIUS = 2.2, LAUNCH = 1.45;
    public static final float WIDTH = 4.4F, HEIGHT = 14F;
    /** Duração em ticks (o renderer a usa para encolher o funil no fim). */
    public static final int LIFE = 220;
    private static final int REHIT = 60;

    private final Map<Integer, Integer> lastHit = new HashMap<>();
    private UUID owner, target;
    private float damage;

    public SovereignTornado(EntityType<? extends SovereignTornado> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    /** Cria um tornado do {@code owner} que persegue {@code target}. */
    public static void spawn(ServerLevel level, LivingEntity owner, LivingEntity target, Vec3 start, float damage) {
        SovereignTornado tornado = new SovereignTornado(ModEntities.SOVEREIGN_TORNADO.get(), level);
        tornado.owner = owner.getUUID();
        tornado.target = target.getUUID();
        tornado.damage = damage;
        tornado.setPos(start);
        level.addFreshEntity(tornado);
        Hazards.sound(level, start, SoundEvents.WIND_CHARGE_BURST.value(), 2.5F, 0.6F);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override public boolean isPickable() { return false; }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            // Poeira na base e nuvens soltas no alto, além do funil desenhado pelo renderer.
            for (int i = 0; i < 3; i++) {
                double a = random.nextDouble() * Math.PI * 2, r = 1.0 + random.nextDouble() * 1.8;
                level().addParticle(ParticleTypes.CLOUD, getX() + Math.cos(a) * r, getY() + 0.2, getZ() + Math.sin(a) * r,
                        Math.cos(a + 1.6) * 0.15, 0.08, Math.sin(a + 1.6) * 0.15);
            }
            return;
        }
        ServerLevel server = (ServerLevel) level();
        Entity ownerEntity = owner == null ? null : server.getEntity(owner);
        if (!(ownerEntity instanceof LivingEntity boss)) {
            discard();
            return;
        }
        // Regra dos poderes: fora do estágio do ar (e do último), os tornados já criados somem.
        boolean wrongStage = TierBrain.of(boss) instanceof SovereignBrain sovereign
                && sovereign.phase() != SovereignBrain.AIR_STAGE && sovereign.phase() != SovereignBrain.STAGES;
        if (tickCount > LIFE || !boss.isAlive() || wrongStage) {
            Hazards.farParticles(server, ParticleTypes.CLOUD, getX(), getY() + 3, getZ(), 40, 1.5, 3, 1.5, 0.2);
            discard();
            return;
        }
        Vec3 pos = position();
        if (target != null && server.getEntity(target) instanceof LivingEntity victim && Hazards.isEnemy(boss, victim)) {
            Vec3 d = victim.position().subtract(pos).multiply(1, 0, 1);
            if (d.lengthSqr() > 1.0E-4) pos = pos.add(d.normalize().scale(Math.min(SPEED, d.length())));
            // Acompanha o relevo: sobe e desce com o alvo.
            pos = new Vec3(pos.x, pos.y + Math.max(-0.3, Math.min(0.3, victim.getY() - pos.y)), pos.z);
        }
        setPos(pos);
        if (tickCount % 20 == 0) Hazards.sound(server, pos, SoundEvents.PHANTOM_FLAP, 2.5F, 0.5F);

        AABB box = new AABB(pos.x - RADIUS, pos.y - 1.5, pos.z - RADIUS, pos.x + RADIUS, pos.y + 4, pos.z + RADIUS);
        for (LivingEntity e : Hazards.enemiesIn(server, boss, box)) {
            Integer last = lastHit.get(e.getId());
            if (last != null && tickCount - last < REHIT) continue;
            lastHit.put(e.getId(), tickCount);
            Hazards.hit(boss, e, damage, ElementType.AIR, 0, true);
            Vec3 swirl = e.position().subtract(pos).multiply(1, 0, 1);
            swirl = swirl.lengthSqr() < 1.0E-4 ? Vec3.ZERO : swirl.normalize().scale(0.25);
            Hazards.push(e, new Vec3(swirl.x, LAUNCH, swirl.z));
            Hazards.sound(server, e.position(), SoundEvents.WIND_CHARGE_BURST.value(), 2F, 0.8F);
        }
    }
}
