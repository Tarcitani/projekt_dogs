package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.event.ElementalCombat;
import com.tarcitani.solomod.event.StatsLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.tarcitani.solomod.core.StatType;

import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

/**
 * Habilidades ativas das armas. O {@code skillId} gravado no WeaponData aponta para uma destas.
 *
 * <p>Para criar uma nova: adicione uma constante com custo/recarga/INT mínima e implemente {@link #perform}.
 * A potência escala com o nível de habilidade do tier (menor = 1x, suprema = 2x).
 */
public enum WeaponSkill {
    //                id                 mana  recarga(ticks)  INT mín.
    NONE             ("none",             0,    0,              0),
    /** Investida: lança o jogador para frente e fere quem estiver no caminho. */
    DASH_STRIKE      ("dash_strike",      15,   60,             5),
    /** Impacto no chão: dano em área e empurrão ao redor. */
    GROUND_SLAM      ("ground_slam",      25,   100,            10),
    /** Projétil mágico instantâneo (raycast) que escala com INT. */
    MANA_BOLT        ("mana_bolt",        10,   30,             5),
    /** Suprema: explosão elemental em área que sempre aplica a passiva do elemento. */
    ELEMENTAL_BURST  ("elemental_burst",  40,   200,            20);

    private final String id;
    private final int manaCost;
    private final int cooldownTicks;
    private final int minInt;

    WeaponSkill(String id, int manaCost, int cooldownTicks, int minInt) {
        this.id = id;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
        this.minInt = minInt;
    }

    public String id() { return id; }
    public int manaCost() { return manaCost; }
    public int cooldownTicks() { return cooldownTicks; }
    public int minInt() { return minInt; }

    public Component displayName() {
        return Component.translatable("skill.solomod." + id);
    }

    public static WeaponSkill byId(String id) {
        for (WeaponSkill s : values()) {
            if (s.id.equalsIgnoreCase(id)) return s;
        }
        return NONE;
    }

    // ------------------------------------------------------------------ execução

    /**
     * Executa o efeito no servidor. Requisitos, mana e recarga já foram validados pelo item.
     * @return quantos alvos foram atingidos (só informativo)
     */
    public int perform(ServerPlayer player, WeaponData data) {
        ServerLevel level = player.serverLevel();
        float power = data.mobTier().skillLevel() >= 2 ? 2.0F : 1.0F;
        // Dano base da habilidade: dano da classe x tier x escalonamento do stat x potência.
        float base = (data.weaponClass().baseDamage() + 1.0F) * data.mobTier().damageMultiplier()
                * StatsLogic.weaponScaling(player, data) * power;
        DamageSource source = player.damageSources().indirectMagic(player, player);

        return switch (this) {
            case NONE -> 0;

            case DASH_STRIKE -> {
                Vec3 look = player.getLookAngle();
                Vec3 dir = new Vec3(look.x, 0, look.z).normalize();
                player.setDeltaMovement(dir.scale(1.6).add(0, 0.25, 0));
                player.hurtMarked = true; // força o envio da nova velocidade ao cliente
                AABB path = player.getBoundingBox().expandTowards(dir.scale(4.0)).inflate(1.0);
                int hits = 0;
                for (LivingEntity target : targets(level, player, path)) {
                    hits += hit(target, source, base, data, false) ? 1 : 0;
                }
                SkillVfx.line(level, player.position().add(0, 1, 0), player.position().add(dir.scale(4.0)).add(0, 1, 0),
                        ParticleTypes.SWEEP_ATTACK, 6);
                SkillVfx.line(level, player.position().add(0, 0.2, 0), player.position().add(dir.scale(4.0)),
                        SkillVfx.particleFor(data.elementType()), 16);
                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);
                yield hits;
            }

            case GROUND_SLAM -> {
                double radius = 3.5 + power;
                int hits = 0;
                for (LivingEntity target : targets(level, player, player.getBoundingBox().inflate(radius, 1.0, radius))) {
                    if (hit(target, source, base * 0.8F, data, false)) {
                        hits++;
                        target.knockback(0.8 * power, player.getX() - target.getX(), player.getZ() - target.getZ());
                    }
                }
                // Onda de choque: 3 anéis crescentes no chão + poeira.
                for (int ring = 1; ring <= 3; ring++) {
                    SkillVfx.ring(level, player.position().add(0, 0.1, 0), radius * ring / 3.0, ParticleTypes.CLOUD, 12 * ring);
                }
                SkillVfx.ring(level, player.position().add(0, 0.3, 0), radius, SkillVfx.particleFor(data.elementType()), 32);
                level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.2, player.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6F, 1.4F);
                yield hits;
            }

            case MANA_BOLT -> {
                // INT dá um bônus extra além do escalonamento normal da arma.
                float boltDamage = base + StatsLogic.stats(player).get(StatType.INT) * 0.3F;
                Vec3 start = player.getEyePosition();
                Vec3 end = start.add(player.getLookAngle().scale(24.0));
                // Para no primeiro bloco sólido: o raio não atravessa paredes.
                HitResult wall = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                if (wall.getType() != HitResult.Type.MISS) end = wall.getLocation();
                AABB sweep = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0);
                EntityHitResult result = ProjectileUtil.getEntityHitResult(player, start, end, sweep,
                        e -> e instanceof LivingEntity living && isValidTarget(player, living), 24.0 * 24.0);
                // Feixe visível até o alvo (ou até a parede).
                Vec3 beamEnd = result != null ? result.getLocation() : end;
                SkillVfx.line(level, start.add(player.getLookAngle().scale(0.8)), beamEnd, ParticleTypes.ENCHANT, 30);
                SkillVfx.line(level, start.add(player.getLookAngle().scale(0.8)), beamEnd, SkillVfx.particleFor(data.elementType()), 20);
                level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.8F, 1.6F);
                if (result != null && result.getEntity() instanceof LivingEntity target) {
                    yield hit(target, source, boltDamage, data, false) ? 1 : 0;
                }
                yield 0;
            }

            case ELEMENTAL_BURST -> {
                double radius = 6.0;
                int hits = 0;
                for (LivingEntity target : targets(level, player, player.getBoundingBox().inflate(radius, 2.0, radius))) {
                    hits += hit(target, source, base * 1.5F, data, true) ? 1 : 0;
                }
                ElementalCombat.spawnParticles(data.elementType(), player, 60);
                // Explosão em 4 anéis + coluna de partículas no centro.
                for (int ring = 1; ring <= 4; ring++) {
                    SkillVfx.ring(level, player.position().add(0, 0.2 + ring * 0.3, 0), radius * ring / 4.0,
                            SkillVfx.particleFor(data.elementType()), 16 * ring);
                }
                SkillVfx.line(level, player.position(), player.position().add(0, 4, 0), ParticleTypes.END_ROD, 20);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.7F, 1.0F);
                yield hits;
            }
        };
    }

    /** Aplica dano elemental e, se houver vantagem (ou se for forçado), a passiva do elemento. */
    private static boolean hit(LivingEntity target, DamageSource source, float damage, WeaponData data, boolean forcePassive) {
        float mult = ElementalCombat.multiplier(data, target);
        boolean hurt = target.hurt(source, damage * mult);
        if (hurt && (forcePassive || ElementalCombat.hasAdvantage(data, target))) {
            ElementalCombat.applyPassive(data.elementType(), data.mobTier().elementPotency(), target);
        }
        return hurt;
    }

    private static List<LivingEntity> targets(ServerLevel level, Player player, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, e -> isValidTarget(player, e));
    }

    /** Não acerta o próprio jogador, outros jogadores (sem PvP no MVP) nem armor stands. */
    private static boolean isValidTarget(Player player, LivingEntity e) {
        return e != player && e.isAlive() && !(e instanceof Player) && !(e instanceof ArmorStand);
    }
}
