package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.event.ElementalCombat;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ataques que vivem no mundo por alguns ticks, independentes do mob que os criou:
 * orbes elementais, áreas marcadas no chão (meteoros) e ondas de choque.
 *
 * <p>São simulados no servidor e desenhados com partículas: não precisam de entidade, modelo nem renderer.
 */
public final class Hazards {
    private Hazards() {}

    private static final List<Hazard> ACTIVE = new ArrayList<>();

    /** Um perigo ativo. {@code tick} devolve false quando terminou. */
    public interface Hazard {
        boolean tick();
    }

    public static void add(Hazard hazard) {
        ACTIVE.add(hazard);
    }

    /** Chamado a cada tick do servidor (MobEvents). Perigos podem criar outros durante o tick. */
    public static void tick() {
        if (ACTIVE.isEmpty()) return;
        List<Hazard> snapshot = new ArrayList<>(ACTIVE);
        ACTIVE.clear();
        for (Hazard h : snapshot) {
            if (h.tick()) ACTIVE.add(h);
        }
    }

    public static void clear() {
        ACTIVE.clear();
    }

    // ------------------------------------------------------------------ regras comuns

    /** Quem pode ser atingido: vivos, fora do criativo/espectador e que não sejam outros mobs do mod. */
    public static boolean isEnemy(LivingEntity owner, LivingEntity e) {
        if (e == owner || !e.isAlive() || e.hasData(ModAttachments.MOB_PROFILE)) return false;
        return !(e instanceof Player p) || (!p.isCreative() && !p.isSpectator());
    }

    public static List<LivingEntity> enemiesIn(ServerLevel level, LivingEntity owner, AABB box) {
        return level.getEntitiesOfClass(LivingEntity.class, box, e -> isEnemy(owner, e));
    }

    /**
     * Dano de um mob do mod. {@code magic} = elemental (ignora armadura).
     * Se o golpe entrar e houver elemento, aplica a passiva do elemento com a potência dada.
     */
    public static void hit(LivingEntity owner, LivingEntity victim, float damage, ElementType element, int potency,
                           boolean magic) {
        DamageSource source = magic ? owner.damageSources().indirectMagic(owner, owner)
                : owner.damageSources().mobAttack(owner);
        if (victim.hurt(source, damage) && potency > 0) {
            ElementalCombat.applyPassive(element, potency, victim);
        }
    }

    /** Empurrão que também chega ao cliente quando o alvo é um jogador. */
    public static void push(Entity e, Vec3 velocity) {
        e.push(velocity.x, velocity.y, velocity.z);
        e.hurtMarked = true;
    }

    // ------------------------------------------------------------------ visuais/sons por elemento

    /** Pó colorido na cor do elemento, usado para telegrafar áreas. */
    public static ParticleOptions dust(ElementType element, float size) {
        Integer rgb = element.color().getColor();
        int c = rgb == null ? 0xFFFFFF : rgb;
        return new DustParticleOptions(new Vector3f(((c >> 16) & 0xFF) / 255F, ((c >> 8) & 0xFF) / 255F, (c & 0xFF) / 255F), size);
    }

    /** Som de disparo do elemento. */
    public static SoundEvent castSound(ElementType element) {
        return switch (element) {
            case FIRE -> SoundEvents.BLAZE_SHOOT;
            case WATER -> SoundEvents.DROWNED_SHOOT;
            case AIR -> SoundEvents.BREEZE_SHOOT;
            case EARTH -> SoundEvents.EVOKER_FANGS_ATTACK;
            case NONE -> SoundEvents.SHULKER_SHOOT;
        };
    }

    /** Som de impacto do elemento. */
    public static SoundEvent impactSound(ElementType element) {
        return switch (element) {
            case FIRE -> SoundEvents.FIRECHARGE_USE;
            case WATER -> SoundEvents.GENERIC_SPLASH;
            case AIR -> SoundEvents.WIND_CHARGE_BURST.value();
            case EARTH -> SoundEvents.BASALT_BREAK;
            case NONE -> SoundEvents.AMETHYST_BLOCK_CHIME;
        };
    }

    public static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, pitch);
    }

    // ------------------------------------------------------------------ tipos

    /** Projétil elemental: voa em linha reta, explode no primeiro inimigo ou bloco. */
    public static final class Orb implements Hazard {
        private final ServerLevel level;
        private final LivingEntity owner;
        private final ElementType element;
        private final float damage;
        private final int potency;
        private Vec3 pos;
        private final Vec3 velocity;
        private int life;

        public Orb(ServerLevel level, LivingEntity owner, ElementType element, Vec3 pos, Vec3 velocity, float damage,
                   int potency, int life) {
            this.level = level;
            this.owner = owner;
            this.element = element;
            this.pos = pos;
            this.velocity = velocity;
            this.damage = damage;
            this.potency = potency;
            this.life = life;
        }

        @Override
        public boolean tick() {
            if (--life <= 0) return false;
            // Dois sub-passos por tick: o orbe é rápido e não pode atravessar um alvo.
            for (int step = 0; step < 2; step++) {
                pos = pos.add(velocity.scale(0.5));
                level.sendParticles(SkillVfx.particleFor(element), pos.x, pos.y, pos.z, 2, 0.08, 0.08, 0.08, 0.01);
                level.sendParticles(dust(element, 1.6F), pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

                AABB box = new AABB(pos, pos).inflate(0.45);
                List<LivingEntity> hits = enemiesIn(level, owner, box);
                if (!hits.isEmpty() || !level.noCollision(box)) {
                    for (LivingEntity e : hits) hit(owner, e, damage, element, potency, true);
                    level.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                    level.sendParticles(SkillVfx.particleFor(element), pos.x, pos.y, pos.z, 16, 0.4, 0.4, 0.4, 0.08);
                    sound(level, pos, impactSound(element), 1.0F, 1.1F);
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Área marcada no chão (meteoro): um anel externo mostra onde vai cair e um anel interno cresce
     * até encostar nele — quando encosta, explode. Dá tempo de sair.
     */
    public static final class Blast implements Hazard {
        private final ServerLevel level;
        private final LivingEntity owner;
        private final ElementType element;
        private final Vec3 center;
        private final double radius;
        private final int delay;
        private final float damage;
        private final int potency;
        private int age;

        public Blast(ServerLevel level, LivingEntity owner, ElementType element, Vec3 center, double radius, int delay,
                     float damage, int potency) {
            this.level = level;
            this.owner = owner;
            this.element = element;
            this.center = center.add(0, 0.15, 0);
            this.radius = radius;
            this.delay = delay;
            this.damage = damage;
            this.potency = potency;
        }

        @Override
        public boolean tick() {
            age++;
            if (age < delay) {
                if (age % 3 == 0) {
                    SkillVfx.ring(level, center, radius, dust(element, 1.2F), 20);
                    SkillVfx.ring(level, center, radius * age / delay, dust(element, 0.8F), 12);
                }
                // O "meteoro" descendo nos últimos 10 ticks.
                if (delay - age <= 10) {
                    double h = (delay - age) * 1.2;
                    level.sendParticles(SkillVfx.particleFor(element), center.x, center.y + h, center.z, 3, 0.2, 0.2, 0.2, 0);
                }
                return true;
            }
            for (LivingEntity e : enemiesIn(level, owner, new AABB(center, center).inflate(radius, 2, radius))) {
                if (e.position().distanceToSqr(center.x, e.getY(), center.z) <= radius * radius) {
                    hit(owner, e, damage, element, potency, true);
                    push(e, e.position().subtract(center).multiply(1, 0, 1).normalize().scale(0.5).add(0, 0.35, 0));
                }
            }
            level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 2, radius * 0.3, 0.2, radius * 0.3, 0);
            level.sendParticles(SkillVfx.particleFor(element), center.x, center.y + 0.5, center.z, 30, radius * 0.4, 0.4, radius * 0.4, 0.1);
            sound(level, center, SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.3F);
            sound(level, center, impactSound(element), 1.0F, 0.8F);
            return false;
        }
    }

    /**
     * Onda de choque: um anel que se expande pelo chão. Só atinge quem está no chão na borda do anel —
     * pular na hora certa esquiva.
     */
    public static final class Shockwave implements Hazard {
        private final ServerLevel level;
        private final LivingEntity owner;
        private final ElementType element;
        private final Vec3 center;
        private final double maxRadius;
        private final double speed;
        private final float damage;
        private final int potency;
        private final Set<Integer> alreadyHit = new HashSet<>();
        private double radius = 0.5;

        public Shockwave(ServerLevel level, LivingEntity owner, ElementType element, Vec3 center, double maxRadius,
                         double speed, float damage, int potency) {
            this.level = level;
            this.owner = owner;
            this.element = element;
            this.center = center.add(0, 0.1, 0);
            this.maxRadius = maxRadius;
            this.speed = speed;
            this.damage = damage;
            this.potency = potency;
        }

        @Override
        public boolean tick() {
            radius += speed;
            int points = (int) (radius * 10);
            SkillVfx.ring(level, center, radius, dust(element, 1.8F), points);
            SkillVfx.ring(level, center.add(0, 0.3, 0), radius, ParticleTypes.CRIT, points / 2);
            if (((int) (radius / speed)) % 3 == 0) sound(level, center, SoundEvents.RAVAGER_STEP, 0.6F, 0.6F);

            AABB box = new AABB(center, center).inflate(radius + 1, 1.5, radius + 1);
            for (LivingEntity e : enemiesIn(level, owner, box)) {
                double d = Math.sqrt(e.position().distanceToSqr(center.x, e.getY(), center.z));
                if (Math.abs(d - radius) < 0.9 && e.onGround() && alreadyHit.add(e.getId())) {
                    hit(owner, e, damage, element, potency, false);
                    push(e, e.position().subtract(center).multiply(1, 0, 1).normalize().scale(0.6).add(0, 0.45, 0));
                }
            }
            return radius < maxRadius;
        }
    }
}
