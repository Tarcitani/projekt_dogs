package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.entity.passive.PassiveEchoTriggers;
import com.tarcitani.solomod.advancement.ModAdvancements;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.entity.RoyalFireball;
import com.tarcitani.solomod.entity.SovereignTornado;
import com.tarcitani.solomod.entity.WaterOrb;
import com.tarcitani.solomod.mob.TierIdentity.MonarchPowers;
import com.tarcitani.solomod.network.CinematicPayload;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Monarca (BOSS): os cinco reis elementais (Colosso Rei e suas formas). Gigante, lento e implacável. Luta em
 * três fases e se adapta ao jogador.
 *
 * <p><b>Fases</b> (por vida): 1 acima de 60% · 2 até 25% · 3 abaixo. A troca para a fase 2 é um rugido
 * invulnerável de 2 s que arremessa todos em volta e convoca 2 Elites. A fase 3 começa com a FÚRIA
 * (ver {@link TierBrain}): barra vermelha, tela escura, aura que queima quem fica colado, 4 Lacaios e
 * 20 s de ataques exclusivos — Marcha Sísmica, Meteoro Colossal, Investida Sem Fim e Anel de Erupções.
 *
 * <p><b>Invocação</b>: em qualquer fase convoca 3 Lacaios do seu elemento (até 6 vivos por perto).
 *
 * <p><b>Leitura do jogador</b>: conta golpes de perto e de longe (com decaimento). Quem luta à distância
 * atrai Vórtice e Chuva Elemental; quem cola atrai Golpe Sísmico e Sopro.
 *
 * <p><b>Ataques</b>:
 * O Rei Vulcânico usa Bolas de Fogo Reais desde a fase 1 (também na Fúria): projéteis lentos rebatíveis.
 * Fora da Fúria, devolver ao chefe o deixa agachado se recuperando por 5 s. A aura de dano e as
 * trocas de fase ficam suspensas na recuperação e na risada após matar um jogador.
 * Investidas de Fogo convertem partes do piso natural em magma (respeitando mobGriefing).
 * Cada rei também domina os poderes do seu elemento usados pelo Soberano, em todas as fases e na Fúria:
 * neutro = Punho do Colosso (Golpe Sísmico com empurrão); fogo = Genki-dama;
 * água = Maremoto e Esferas de Água; terra = Raízes, Pisão e Pulão; ar = quatro Tornados 3D.
 * Os alcances/danos dessas versões são centralizados em {@link MonarchPowers}.
 * <ul>
 *   <li>Golpe Sísmico: ergue os braços e esmaga; onda de choque no chão — pule para esquivar.</li>
 *   <li>Chuva Elemental: marca círculos no chão (mais por fase) que explodem após um tempo.</li>
 *   <li>Investida Devastadora: 1/2/3 investidas seguidas; bater numa parede o deixa atordoado.</li>
 *   <li>Vórtice (fase 2+): puxa todos por 2 s e detona.</li>
 *   <li>Sopro Elemental: varre um cone à frente com o elemento.</li>
 *   <li>Convocar Lacaios: ergue os braços e chama 3 Diabretes do seu elemento.</li>
 * </ul>
 *
 * <p><b>Morte</b>: cinemática para todos a até 64 blocos. A conquista do Monarca
 * ({@link ModAdvancements#awardBossDefeat}) vai só para quem causou dano a ele e ainda está online, e só
 * se ele for a criatura Monarca ({@code solomod:monarch}); mob vanilla marcado como BOSS não conta. A lista de
 * participantes não é salva: se o chunk descarregar no meio da luta, ela recomeça vazia.
 */
final class MonarchBrain extends TierBrain {
    private final ServerBossEvent bar;
    private final PhaseShift phaseShift = new PhaseShift();
    /** Jogadores que causaram dano nesta luta (UUID, para sobreviver a relog). */
    private final Set<UUID> fighters = new HashSet<>();
    private int phase = 1;
    private int meleePressure;
    private int rangedPressure;

    MonarchBrain(PathfinderMob mob, ElementType element) {
        super(mob, MobTier.BOSS, element);
        bar = new ServerBossEvent(mob.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        addMove(new Seismic());
        addMove(new MeteorRain());
        addMove(new Charge());
        addMove(new Vortex());
        addMove(new Breath());
        addMove(new SummonMinions());
        addRageMove(new QuakeMarch());
        addRageMove(new GiantMeteor());
        addRageMove(new EndlessCharge());
        addRageMove(new EruptionRing());
        if (element == ElementType.FIRE) {
            Move fireballs = new RoyalVolley();
            addMove(fireballs);
            addRageMove(fireballs);
        }
        List<Move> powers = switch (element) {
            case NONE -> List.of(); // O Golpe Sísmico já é o Punho do Colosso.
            case FIRE -> List.of(new FireGenki());
            case WATER -> List.of(new TidalWave(), new WaterBarrage());
            case EARTH -> List.of(new AncientRoots(), new Stomp(), new GreatLeap());
            case AIR -> List.of(new Tornadoes());
        };
        for (Move power : powers) {
            addMove(power);
            addRageMove(power);
        }
        if (element == ElementType.NONE) addRageMove(new Seismic());
    }

    @Override public int phase() { return phase; }
    @Override protected Component bossBarName() {
        return mob.getDisplayName().copy().append(Component.translatable("mob_title.solomod.phase", phase));
    }
    @Override protected int thinkGap() { return phase == 1 ? 30 : phase == 2 ? 20 : 12; }
    @Override protected int meleeInterval() { return 28 - 4 * phase; }
    @Override protected float meleeElementChance() { return 0.5F; }
    @Override protected float cooldownScale() { return phase == 1 ? 1.0F : phase == 2 ? 0.8F : 0.6F; }

    @Override
    public void tick() {
        if (isCombatPaused()) return;
        int wanted = phaseFor(mob.getHealth() / mob.getMaxHealth());
        if (wanted > phase && !isRunning(phaseShift)) {
            phaseShift.next = wanted;
            force(phaseShift);
        }
        super.tick();
    }

    /** A fase 3 começa junto com a Fúria. */
    @Override
    protected void onRageStart() {
        phase = 3;
        bar.setColor(BossEvent.BossBarColor.RED);
        bar.setDarkenScreen(true);
    }

    @Override
    protected void rageSummon() {
        summon(MobTier.NORMAL, 4);
    }

    @Override
    protected void engage() {
        chase(isRaging() ? 1.45 : phase == 3 ? 1.25 : 1.0);
        if (tryMelee(1.0F, SoundEvents.IRON_GOLEM_ATTACK, 0.6F)) {
            particles(ParticleTypes.CRIT, target.getEyePosition(), 8, 0.3, 0.2);
        }
    }

    @Override
    public void onHurt(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player player)) return;
        fighters.add(player.getUUID());
        boolean ranged = source.getDirectEntity() != player || player.distanceTo(mob) > 5 + mob.getBbWidth() / 2;
        if (ranged) rangedPressure += 2;
        else meleePressure += 1;
    }

    @Override
    public void passiveTick() {
        if (isCombatPaused()) {
            updateBossBar(bar, 48);
            return;
        }
        // A leitura do jogador "esquece" aos poucos: metade a cada 5 s.
        if (mob.tickCount % 100 == 0) {
            meleePressure /= 2;
            rangedPressure /= 2;
        }
        if (mob.tickCount % 4 == 0) {
            SkillVfx.ring(level(), mob.position().add(0, 0.1, 0), 0.7 * mob.getScale(), elementParticle(), 10 + 4 * phase);
        }
        if (phase == 3) {
            particles(ParticleTypes.LARGE_SMOKE, center(), 1, 0.5, 0.01);
            if (mob.tickCount % 20 == 0) {
                SkillVfx.ring(level(), mob.position().add(0, 0.2, 0), 4.5, Hazards.dust(element, 1.6F), 36);
                sound(SoundEvents.WARDEN_HEARTBEAT, 1.5F, 0.7F);
                for (LivingEntity e : enemiesWithin(4.5)) magicStrike(e, baseDamage() * 0.3F);
            }
        }
        updateBossBar(bar, 48);
    }

    @Override
    public void onRemoved() {
        bar.removeAllPlayers();
    }

    @Override
    public void onDeath() {
        // Cinemática para quem estava na luta: faixas pretas, câmera no chefe se desintegrando.
        int duration = TierMob.DISSOLVE_TICKS + 40;
        CinematicPayload cinematic = new CinematicPayload(mob.getId(), duration, CinematicPayload.Kind.DEFEAT,
                Component.translatable("cinematic.solomod.defeated"), mob.getDisplayName());
        for (ServerPlayer player : level().players()) {
            if (player.distanceToSqr(mob) < 64 * 64) ModNetwork.sendCinematic(player, cinematic);
        }
        // Conquista: só quem lutou (o golpe final passa por onHurt antes da morte).
        if (mob.getType() == ModEntities.MONARCH.get()) {
            for (UUID id : fighters) {
                ServerPlayer player = level().getServer().getPlayerList().getPlayer(id);
                if (player != null) ModAdvancements.awardBossDefeat(player, element);
            }
        }
        sound(SoundEvents.WITHER_DEATH, 2.0F, 0.8F);
        sound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
        particles(ParticleTypes.EXPLOSION_EMITTER, center(), 2, 0.5, 0);
        particles(elementParticle(), center(), 80, 1.5, 0.2);
        bar.removeAllPlayers();
    }

    private static int phaseFor(float healthRatio) {
        // A fase 3 não vem daqui: ela começa com a Fúria (TierBrain) a 1/4 da vida.
        return healthRatio <= 0.6F ? 2 : 1;
    }

    // ------------------------------------------------------------------ ataques

    /** Poder característico do fogo: prepara por 1 s e dispara 1/2/3 bolas lentas, espaçadas por 0,9 s. */
    private final class RoyalVolley extends Move {
        RoyalVolley() { super(100, 12); }

        @Override protected boolean usable(double dist) {
            return dist > TierIdentity.ROYAL_FIREBALL_MIN_RANGE && dist < 32 && mob.getSensing().hasLineOfSight(target);
        }

        @Override protected void begin() {
            anim(MobAnim.BREATH, 24 + (phase - 1) * 18);
            sound(SoundEvents.GHAST_WARN, 2F, 0.65F);
        }

        @Override protected boolean tick(int t) {
            face(flatDirTo(target.position()));
            particles(ParticleTypes.FLAME, mob.getEyePosition(), 8, 0.45, 0.04);
            if (t >= 20 && (t - 20) % 18 == 0 && (t - 20) / 18 < phase) {
                RoyalFireball.launch(level(), mob, target, baseDamage() * TierIdentity.ROYAL_FIREBALL_DAMAGE_MULTIPLIER);
            }
            return t < 24 + (phase - 1) * 18;
        }
    }

    // ------------------------------------------------------------------ poderes dos cinco reis

    /** Genki-dama do Rei Vulcânico: carga visível sobre as mãos e projétil rebatível com explosão. */
    private final class FireGenki extends Move {
        FireGenki() { super(MonarchPowers.FIRE_COOLDOWN, 7); }

        @Override protected boolean usable(double dist) {
            return dist > TierIdentity.ROYAL_FIREBALL_MIN_RANGE && dist < MonarchPowers.FIRE_RANGE
                    && mob.getSensing().hasLineOfSight(target);
        }

        @Override protected void begin() {
            anim(MobAnim.GENKI, MonarchPowers.FIRE_TOTAL);
            sound(SoundEvents.GHAST_WARN, 2F, 0.5F);
        }

        @Override protected boolean tick(int t) {
            Vec3 hands = mob.position().add(0, mob.getBbHeight() + 1.8, 0);
            if (t < MonarchPowers.FIRE_CHARGE) {
                face(flatDirTo(target.position()));
                double r = 0.4 + 1.4 * t / MonarchPowers.FIRE_CHARGE;
                Hazards.farParticles(level(), ParticleTypes.FLAME, hands.x, hands.y, hands.z, 10,
                        r * 0.5, r * 0.5, r * 0.5, 0.03);
                if (t % 2 == 0) {
                    SkillVfx.ring(level(), hands, r, Hazards.dust(element, 1.8F), 24);
                    particles(ParticleTypes.LAVA, hands, 2, r * 0.5, 0);
                }
                if (t % 12 == 0) sound(SoundEvents.FIRE_AMBIENT, 2F, 0.5F);
            } else if (t == MonarchPowers.FIRE_CHARGE) {
                Vec3 aim = target.position().add(target.getDeltaMovement().multiply(1, 0, 1).scale(12)).add(0, 0.6, 0);
                RoyalFireball.launchGenki(level(), mob, hands, aim, baseDamage() * MonarchPowers.FIRE_DAMAGE,
                        MonarchPowers.FIRE_SPEED, MonarchPowers.FIRE_BLAST);
            }
            return t < MonarchPowers.FIRE_TOTAL;
        }
    }

    /** Maremoto do Leviatã: faixa travada por 1 s, crista 3D e um único acerto por vítima. */
    private final class TidalWave extends Move {
        private final Set<Integer> hit = new HashSet<>();
        private Vec3 dir, start;

        TidalWave() { super(MonarchPowers.WATER_COOLDOWN, 6); }

        @Override protected boolean usable(double dist) { return dist < MonarchPowers.WATER_LENGTH; }

        @Override protected void begin() {
            hit.clear();
            dir = flatDirTo(target.position().add(target.getDeltaMovement().multiply(1, 0, 1).scale(10)));
            start = mob.position().add(dir.scale(mob.getBbWidth() / 2));
            face(dir);
            anim(MobAnim.WINDUP, MonarchPowers.WATER_AIM);
            sound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.5F, 0.6F);
        }

        @Override protected boolean tick(int t) {
            Vec3 side = new Vec3(-dir.z, 0, dir.x);
            if (t < MonarchPowers.WATER_AIM) {
                face(dir);
                if (t % 2 == 0) {
                    for (double d = 2; d < MonarchPowers.WATER_LENGTH; d += 1.5) {
                        Vec3 p = start.add(dir.scale(d)).add(0, 0.15, 0);
                        for (double s : new double[]{-MonarchPowers.WATER_HALF_WIDTH, MonarchPowers.WATER_HALF_WIDTH}) {
                            particles(Hazards.dust(element, 1.2F), p.add(side.scale(s)), 1, 0, 0);
                        }
                    }
                }
                return true;
            }
            if (t == MonarchPowers.WATER_AIM) {
                for (int k = -2; k <= 2; k++) {
                    WaterOrb.wave(level(), mob, start.add(side.scale(k * 1.7)).add(0, 1.4, 0),
                            dir.scale(MonarchPowers.WATER_SPEED), MonarchPowers.WATER_LIFE);
                }
                anim(MobAnim.SLAM, 10);
                sound(SoundEvents.GENERIC_SPLASH, 2.5F, 0.5F);
            }
            double front = (t - MonarchPowers.WATER_AIM) * MonarchPowers.WATER_SPEED;
            Vec3 crest = start.add(dir.scale(front));
            particles(ParticleTypes.SPLASH, crest.add(0, 0.5, 0), 12, 1.5, 0.1);
            for (LivingEntity e : enemiesWithin(MonarchPowers.WATER_LENGTH + mob.getBbWidth())) {
                Vec3 rel = e.position().subtract(start);
                double along = rel.dot(dir), across = Math.abs(rel.dot(side));
                // Não acerta jogadores muito acima/abaixo da crista.
                if (along > front - 1.5 && along < front + 0.8 && across <= MonarchPowers.WATER_HALF_WIDTH
                        && Math.abs(rel.y) < 3 && hit.add(e.getId())) {
                    magicStrike(e, baseDamage() * MonarchPowers.WATER_DAMAGE);
                    Hazards.push(e, dir.scale(MonarchPowers.WATER_PUSH).add(0, MonarchPowers.WATER_LIFT, 0));
                }
            }
            return front < MonarchPowers.WATER_LENGTH;
        }
    }

    /** Cinco esferas orbitam o Leviatã e depois perseguem jogadores; um golpe direto as estoura. */
    private final class WaterBarrage extends Move {
        WaterBarrage() { super(MonarchPowers.ORB_COOLDOWN, 6); }

        @Override protected boolean usable(double dist) {
            return dist > MonarchPowers.ORB_MIN_RANGE && dist < MonarchPowers.ORB_RANGE;
        }

        @Override protected void begin() {
            anim(MobAnim.CAST, MonarchPowers.ORB_TOTAL);
            sound(SoundEvents.GENERIC_SPLASH, 2.5F, 0.5F);
        }

        @Override protected boolean tick(int t) {
            face(flatDirTo(target.position()));
            if (t == 10) {
                List<LivingEntity> chosen = powerVictims(MonarchPowers.ORB_RANGE);
                if (chosen.isEmpty()) return false;
                for (int i = 0; i < MonarchPowers.ORB_COUNT; i++) {
                    WaterOrb.orbit(level(), mob, chosen.get(i % chosen.size()), Math.PI * 2 * i / MonarchPowers.ORB_COUNT,
                            MonarchPowers.ORB_ORBIT, baseDamage() * MonarchPowers.ORB_DAMAGE);
                }
                sound(SoundEvents.ELDER_GUARDIAN_CURSE, 2F, 0.6F);
            }
            return t < MonarchPowers.ORB_TOTAL;
        }
    }

    /** Raízes do Titã: duas levas, cada uma mirando a posição atual dos jogadores. */
    private final class AncientRoots extends Move {
        AncientRoots() { super(MonarchPowers.ROOT_COOLDOWN, 6); }

        @Override protected boolean usable(double dist) { return dist < MonarchPowers.ROOT_RANGE; }

        @Override protected void begin() {
            anim(MobAnim.WINDUP, MonarchPowers.ROOT_FIRST);
            sound(SoundEvents.ROOTED_DIRT_BREAK, 2F, 0.5F);
        }

        @Override protected boolean tick(int t) {
            if (t == MonarchPowers.ROOT_FIRST || t == MonarchPowers.ROOT_SECOND) {
                if (t == MonarchPowers.ROOT_FIRST) anim(MobAnim.SLAM, 10);
                for (LivingEntity v : powerVictims(MonarchPowers.ROOT_RANGE)) {
                    // Pólen âmbar da Gazela: a raiz nem chega a brotar sob o jogador.
                    if (PassiveEchoTriggers.amberPollen(v)) continue;
                    Hazards.add(new Hazards.Blast(level(), mob, element, v.position(), MonarchPowers.ROOT_RADIUS,
                            t == MonarchPowers.ROOT_FIRST ? MonarchPowers.ROOT_DELAY : MonarchPowers.ROOT_SECOND_DELAY,
                            baseDamage() * MonarchPowers.ROOT_DAMAGE, potency()));
                }
                sound(SoundEvents.EVOKER_FANGS_ATTACK, 2F, 0.6F);
                particles(ParticleTypes.COMPOSTER, mob.position(), 30, 2, 0.1);
            }
            return t < MonarchPowers.ROOT_TOTAL;
        }
    }

    /** Pisão do Titã: ergue a perna, arremessa quem está perto e levanta blocos do chão. */
    private final class Stomp extends Move {
        Stomp() { super(MonarchPowers.STOMP_COOLDOWN, 6); }

        @Override protected boolean usable(double dist) { return dist < MonarchPowers.STOMP_RANGE && mob.onGround(); }
        @Override protected int urgency(double dist) { return dist < MonarchPowers.STOMP_HIT_RANGE ? 2 : 0; }

        @Override protected void begin() {
            anim(MobAnim.STOMP, MonarchPowers.STOMP_TOTAL);
            sound(SoundEvents.RAVAGER_ROAR, 2F, 0.5F);
        }

        @Override protected boolean tick(int t) {
            if (t < MonarchPowers.STOMP_WINDUP) {
                face(flatDirTo(target.position()));
                if (t % 3 == 0) particles(ParticleTypes.POOF, mob.position(), 6, 1.5, 0.02);
            } else if (t == MonarchPowers.STOMP_WINDUP) {
                earthImpact(MonarchPowers.STOMP_RADIUS, MonarchPowers.STOMP_DAMAGE, MonarchPowers.STOMP_WAVE_DAMAGE, 0.7F);
            }
            return t < MonarchPowers.STOMP_TOTAL;
        }
    }

    /** Pulão do Titã: salta até o alvo e só causa o terremoto ao aterrissar. */
    private final class GreatLeap extends Move {
        private Vec3 dir;
        private double speed;
        private boolean airborne;

        GreatLeap() { super(MonarchPowers.LEAP_COOLDOWN, 5); }

        @Override protected boolean usable(double dist) {
            return mob.onGround() && dist > MonarchPowers.LEAP_MIN_RANGE && dist < MonarchPowers.LEAP_RANGE;
        }

        @Override protected void begin() {
            airborne = false;
            anim(MobAnim.JUMP, 50);
            sound(SoundEvents.RAVAGER_ROAR, 2F, 0.5F);
        }

        @Override protected boolean tick(int t) {
            if (t < MonarchPowers.LEAP_WINDUP) {
                face(flatDirTo(target.position()));
                if (t % 3 == 0) particles(ParticleTypes.POOF, mob.position(), 6, 1.5, 0.02);
                return true;
            }
            if (t == MonarchPowers.LEAP_WINDUP) {
                dir = flatDirTo(target.position());
                speed = Mth.clamp((mob.distanceTo(target) - 3) / 18, 0.2, MonarchPowers.LEAP_SPEED);
                mob.setDeltaMovement(dir.x * speed, MonarchPowers.LEAP_VERTICAL, dir.z * speed);
                mob.hasImpulse = true;
                sound(SoundEvents.WIND_CHARGE_BURST.value(), 2F, 0.5F);
                return true;
            }
            if (!mob.onGround()) {
                airborne = true;
                mob.setDeltaMovement(dir.x * speed, mob.getDeltaMovement().y, dir.z * speed);
                return t < MonarchPowers.LEAP_TIMEOUT;
            }
            if (airborne) earthImpact(MonarchPowers.LEAP_RADIUS, MonarchPowers.LEAP_DAMAGE, MonarchPowers.LEAP_WAVE_DAMAGE, 1F);
            return false;
        }

        @Override protected void end() {
            mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
        }
    }

    /** Quatro tornados do Imperador, telegrafados por círculos de vento durante 2 s. */
    private final class Tornadoes extends Move {
        Tornadoes() { super(MonarchPowers.TORNADO_COOLDOWN, 7); }

        @Override protected boolean usable(double dist) { return dist < MonarchPowers.TORNADO_RANGE; }

        @Override protected void begin() {
            anim(MobAnim.FLOAT, MonarchPowers.TORNADO_TOTAL);
            sound(SoundEvents.WIND_CHARGE_BURST.value(), 2.5F, 0.5F);
        }

        @Override protected boolean tick(int t) {
            face(flatDirTo(target.position()));
            if (t < MonarchPowers.TORNADO_RISE) {
                if (t % 2 == 0) SkillVfx.ring(level(), mob.position().add(0, 0.2, 0),
                        2 + t * 0.12, Hazards.dust(element, 1.8F), 28);
                particles(ParticleTypes.CLOUD, mob.position(), 8, 2, 0.05);
                if (t % 10 == 0) sound(SoundEvents.PHANTOM_FLAP, 2.5F, 0.5F);
            } else if (t == MonarchPowers.TORNADO_RISE) {
                List<LivingEntity> chosen = powerVictims(MonarchPowers.TORNADO_RANGE);
                if (chosen.isEmpty()) return false;
                for (int i = 0; i < MonarchPowers.TORNADO_COUNT; i++) {
                    double a = Math.PI * 2 * i / MonarchPowers.TORNADO_COUNT;
                    Vec3 start = mob.position().add(Math.cos(a) * MonarchPowers.TORNADO_SPAWN_RADIUS,
                            0, Math.sin(a) * MonarchPowers.TORNADO_SPAWN_RADIUS);
                    SovereignTornado.spawn(level(), mob, chosen.get(i % chosen.size()), start,
                            baseDamage() * MonarchPowers.TORNADO_DAMAGE);
                }
            }
            return t < MonarchPowers.TORNADO_TOTAL;
        }
    }

    /** Distribui poderes entre os inimigos presentes; mantém o alvo se ele está na borda da busca. */
    private List<LivingEntity> powerVictims(double range) {
        List<LivingEntity> nearby = enemiesWithin(range).stream().filter(e -> e instanceof Player).toList();
        return nearby.isEmpty() && isValidTarget(target) ? List.of(target) : nearby;
    }

    /** Impacto terrestre compartilhado pelo pisão e pelo salto; blocos respeitam mobGriefing. */
    private void earthImpact(double radius, float directDamage, float waveDamage, float shake) {
        PassiveEchoTriggers.quakeStarted(mob, mob.position());
        sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.5F, 0.4F);
        sound(SoundEvents.GENERIC_EXPLODE.value(), 2F, 0.5F);
        particles(ParticleTypes.EXPLOSION, mob.position(), 5, 2, 0);
        for (LivingEntity e : enemiesWithin(MonarchPowers.STOMP_HIT_RANGE)) strike(e, baseDamage() * directDamage, true);
        for (LivingEntity e : enemiesWithin(radius)) {
            double distance = mob.distanceTo(e);
            double push = MonarchPowers.EARTH_PUSH_MIN + MonarchPowers.EARTH_PUSH_BONUS * (1 - Math.min(1, distance / radius));
            Hazards.push(e, flatDirTo(e.position()).scale(push).add(0, MonarchPowers.EARTH_LIFT, 0));
        }
        Hazards.add(new Hazards.Shockwave(level(), mob, element, mob.position(), radius, 0.5,
                baseDamage() * waveDamage, potency()));
        Hazards.add(new Hazards.QuakeBlocks(level(), mob, mob.position(), radius, 0.5));
        ModNetwork.sendShake(level(), mob.position(), 48, shake, 30);
    }

    /** Troca de fase: rugido invulnerável, onda que arremessa, reforços ou fúria. */
    private final class PhaseShift extends Move {
        int next;

        PhaseShift() { super(0, 0); }

        @Override
        protected void begin() {
            phase = next;
            invulnerable = true;
            anim(MobAnim.PHASE, 40);
            sound(phase == 3 ? SoundEvents.WITHER_SPAWN : SoundEvents.ENDER_DRAGON_GROWL, 2.0F, 0.8F);
            bar.setName(bossBarName());
            if (phase == 3) {
                bar.setColor(BossEvent.BossBarColor.RED);
                bar.setDarkenScreen(true);
            }
        }

        @Override
        protected boolean tick(int t) {
            particles(ParticleTypes.LARGE_SMOKE, center(), 3, 0.8, 0.05);
            if (t == 20) {
                particles(ParticleTypes.EXPLOSION_EMITTER, center(), 1, 0, 0);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.6F);
                for (LivingEntity e : enemiesWithin(10)) {
                    Hazards.push(e, flatDirTo(e.position()).scale(1.6).add(0, 0.6, 0));
                }
                Hazards.add(new Hazards.Shockwave(level(), mob, element, mob.position(), 12, 0.6, baseDamage() * 0.6F, potency()));
            }
            if (t == 30 && phase == 2) {
                summon(MobTier.OP, 2);
                sound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.6F);
            }
            return t < 40;
        }

        @Override
        protected void end() {
            invulnerable = false;
        }
    }

    /** Golpe Sísmico: 20 ticks erguendo, esmaga e solta onda(s) de choque. */
    private final class Seismic extends Move {
        Seismic() { super(90, 5); }

        @Override
        protected boolean usable(double dist) {
            return dist < 6;
        }

        @Override
        protected int weight(double dist) {
            return weight + meleePressure;
        }

        @Override
        protected void begin() {
            anim(MobAnim.WINDUP, 20);
            sound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.7F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < 20) {
                face(flatDirTo(target.position()));
                particles(ParticleTypes.CRIT, center().add(0, mob.getBbHeight() * 0.4, 0), 2, 0.4, 0.1);
                return true;
            }
            if (t == 20 || (t == 32 && phase == 3)) {
                if (t == 20) {
                    PassiveEchoTriggers.quakeStarted(mob, mob.position());
                    anim(MobAnim.SLAM, 12);
                    for (LivingEntity e : enemiesWithin(2.5 + mob.getBbWidth() / 2)) {
                        strike(e, baseDamage() * 1.6F, true);
                    }
                    if (element == ElementType.NONE) {
                        for (LivingEntity e : enemiesWithin(MonarchPowers.FIST_PUSH_RANGE)) {
                            Hazards.push(e, flatDirTo(e.position()).scale(MonarchPowers.FIST_PUSH).add(0, MonarchPowers.FIST_LIFT, 0));
                        }
                        ModNetwork.sendShake(level(), mob.position(), 40, 0.5F, 14);
                    }
                }
                sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.0F, 0.6F);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 0.6F);
                particles(ParticleTypes.EXPLOSION, mob.position(), 3, 1.0, 0);
                Hazards.add(new Hazards.Shockwave(level(), mob, element, mob.position(), 10, 0.5, baseDamage(), potency()));
            }
            return t < (phase == 3 ? 34 : 24);
        }
    }

    /** Chuva Elemental: conjura e marca 4/6/8 círculos; o primeiro sempre onde o alvo vai estar. */
    private final class MeteorRain extends Move {
        MeteorRain() { super(150, 4); }

        @Override
        protected int weight(double dist) {
            return weight + rangedPressure / 2;
        }

        @Override
        protected void begin() {
            anim(MobAnim.CAST, 24);
            sound(SoundEvents.EVOKER_PREPARE_ATTACK, 2.0F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            particles(elementParticle(), mob.getEyePosition().add(0, 1, 0), 3, 0.4, 0.05);
            if (t == 12) {
                List<LivingEntity> victims = enemiesWithin(24);
                int count = 2 + 2 * phase;
                for (int i = 0; i < count; i++) {
                    LivingEntity v = victims.isEmpty() ? target : victims.get(i % victims.size());
                    Vec3 pos = v.position();
                    if (i == 0) pos = pos.add(v.getDeltaMovement().multiply(1, 0, 1).scale(20));
                    else pos = pos.add((random.nextDouble() - 0.5) * 10, 0, (random.nextDouble() - 0.5) * 10);
                    Hazards.add(new Hazards.Blast(level(), mob, element, pos, 2.5, 30 + i * 4, baseDamage() * 1.4F, potency()));
                }
                sound(Hazards.castSound(element), 2.0F, 0.6F);
            }
            return t < 24;
        }
    }

    /**
     * Investida Devastadora: por investida, 12 ticks mirando + 14 correndo + 6 de pausa; repete 1/2/3 vezes.
     * Bater numa parede atordoa por 2 s (janela de punição).
     */
    private final class Charge extends Move {
        private static final int AIM = 12, RUN = 14, REST = 6, CYCLE = AIM + RUN + REST;
        private final Set<Integer> hit = new HashSet<>();
        private Vec3 dir;

        Charge() { super(160, 4); }

        @Override
        protected boolean usable(double dist) {
            return dist > 5 && dist < 24;
        }

        @Override
        protected boolean tick(int t) {
            int index = t / CYCLE, local = t % CYCLE;
            if (index >= phase) return false;
            if (local == 0) {
                hit.clear();
                anim(MobAnim.WINDUP, AIM);
                sound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.9F + 0.1F * index);
            }
            if (local < AIM) {
                dir = flatDirTo(target.position());
                face(dir);
                if (local % 2 == 0) {
                    Vec3 from = mob.position().add(0, 0.2, 0);
                    SkillVfx.line(level(), from, from.add(dir.scale(16)), Hazards.dust(element, 1.6F), 20);
                }
                return true;
            }
            if (local < AIM + RUN) {
                if (local == AIM) {
                    anim(MobAnim.DASH, RUN);
                    sound(SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.5F, 0.6F);
                }
                face(dir);
                mob.setDeltaMovement(dir.x * 1.3, mob.getDeltaMovement().y, dir.z * 1.3);
                if (element == ElementType.FIRE) Hazards.magmaTrail(level(), mob);
                particles(ParticleTypes.CLOUD, mob.position().add(0, 0.2, 0), 3, 0.5, 0.02);
                for (LivingEntity e : Hazards.enemiesIn(level(), mob, mob.getBoundingBox().inflate(0.6))) {
                    if (hit.add(e.getId())) {
                        strike(e, baseDamage() * 1.8F, true);
                        Hazards.push(e, dir.scale(1.6).add(0, 0.5, 0));
                        sound(SoundEvents.RAVAGER_ATTACK, 1.5F, 0.8F);
                    }
                }
                if (mob.horizontalCollision) {
                    sound(SoundEvents.ANVIL_LAND, 1.0F, 0.5F);
                    particles(ParticleTypes.EXPLOSION, center(), 2, 0.5, 0);
                    if (stun(40)) return false;
                }
                return true;
            }
            mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.5, 1, 0.5));
            return true;
        }
    }

    /** Vórtice: 40 ticks puxando todos num raio de 16, depois detona em volta. */
    private final class Vortex extends Move {
        Vortex() { super(240, 4); }

        @Override
        protected boolean usable(double dist) {
            return phase >= 2 && dist < 16;
        }

        @Override
        protected int weight(double dist) {
            return weight + rangedPressure;
        }

        @Override
        protected void begin() {
            anim(MobAnim.CAST, 50);
            sound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 0.7F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < 40) {
                for (int arm = 0; arm < 2; arm++) {
                    double a = t * 0.4 + arm * Math.PI;
                    double r = 6 - (t % 10) * 0.5;
                    Vec3 p = mob.position().add(Math.cos(a) * r, 0.5 + (t % 10) * 0.1, Math.sin(a) * r);
                    particles(elementParticle(), p, 2, 0.05, 0);
                    particles(ParticleTypes.PORTAL, p, 2, 0.1, 0);
                }
                for (LivingEntity e : enemiesWithin(16)) {
                    Hazards.push(e, flatDirTo(e.position()).scale(-0.09));
                }
                return true;
            }
            if (t == 40) {
                anim(MobAnim.SLAM, 10);
                sound(SoundEvents.WARDEN_SONIC_BOOM, 2.0F, 0.8F);
                particles(ParticleTypes.SONIC_BOOM, center(), 1, 0, 0);
                particles(ParticleTypes.EXPLOSION, center(), 4, 1.5, 0);
                for (LivingEntity e : enemiesWithin(5.5)) {
                    magicStrike(e, baseDamage() * 2.0F);
                    Hazards.push(e, flatDirTo(e.position()).scale(1.2).add(0, 0.5, 0));
                }
            }
            return t < 46;
        }
    }

    /** Sopro Elemental: 10 ticks inspirando, 30 ticks varrendo um cone de 9 blocos de um lado ao outro. */
    private final class Breath extends Move {
        private static final double RANGE = 9;
        private Vec3 baseDir;

        Breath() { super(110, 4); }

        @Override
        protected boolean usable(double dist) {
            return dist < RANGE && mob.getSensing().hasLineOfSight(target);
        }

        @Override
        protected int weight(double dist) {
            return weight + meleePressure / 2;
        }

        @Override
        protected void begin() {
            anim(MobAnim.WINDUP, 10);
            sound(SoundEvents.GHAST_WARN, 1.5F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < 10) {
                baseDir = flatDirTo(target.position());
                face(baseDir);
                return true;
            }
            if (t == 10) anim(MobAnim.BREATH, 30);
            // Varre de -25° a +25° em volta da direção travada.
            double sweep = Math.toRadians(-25 + 50 * (t - 10) / 30.0);
            Vec3 dir = new Vec3(baseDir.x * Math.cos(sweep) - baseDir.z * Math.sin(sweep), 0,
                    baseDir.x * Math.sin(sweep) + baseDir.z * Math.cos(sweep));
            face(dir);
            Vec3 mouth = mob.getEyePosition();
            for (int i = 1; i <= 6; i++) {
                Vec3 p = mouth.add(dir.scale(i * 1.5)).add(0, -i * 0.15, 0);
                level().sendParticles(elementParticle(), p.x, p.y, p.z, 2, 0.15 * i, 0.15 * i, 0.15 * i, 0.02);
            }
            if (t % 6 == 0) sound(Hazards.castSound(element), 1.2F, 0.7F);
            if (t % 5 == 0) {
                for (LivingEntity e : enemiesWithin(RANGE)) {
                    if (dir.dot(flatDirTo(e.position())) > Math.cos(Math.toRadians(30))) magicStrike(e, baseDamage() * 0.45F);
                }
            }
            return t < 40;
        }
    }

    /** Convocar Lacaios: braços ao céu e 3 Diabretes do mesmo elemento (no máximo 6 vivos por perto). */
    private final class SummonMinions extends Move {
        SummonMinions() { super(360, 3); }

        @Override
        protected boolean usable(double dist) {
            return minionsAround(MobTier.NORMAL) < 6;
        }

        @Override
        protected int weight(double dist) {
            return minionsAround(MobTier.NORMAL) == 0 ? weight + 3 : weight;
        }

        @Override
        protected void begin() {
            anim(MobAnim.SUMMON, 30);
            sound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            for (int k = 0; k < 3; k++) {
                double a = t * 0.4 + k * 2.09;
                particles(elementParticle(), mob.position().add(Math.cos(a) * 3, t * 0.12, Math.sin(a) * 3), 2, 0.05, 0);
            }
            if (t == 30) {
                summon(MobTier.NORMAL, 3);
                sound(SoundEvents.EVOKER_CAST_SPELL, 2.0F, 0.6F);
            }
            return t < 30;
        }
    }

    // ------------------------------------------------------------------ ataques da Fúria

    /** Marcha Sísmica: avança sem parar e pisa a cada 16 ticks, soltando ondas de choque. */
    private final class QuakeMarch extends Move {
        QuakeMarch() { super(140, 4); }

        @Override
        protected boolean tick(int t) {
            chase(1.5);
            if (t % 16 == 8) {
                anim(MobAnim.SLAM, 10);
                sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.0F, 0.5F);
                particles(ParticleTypes.EXPLOSION, mob.position(), 2, 1.0, 0);
                Hazards.add(new Hazards.Shockwave(level(), mob, element, mob.position(), 8, 0.6, baseDamage() * 0.9F, potency()));
            }
            return t < 80;
        }
    }

    /** Meteoro Colossal: um círculo enorme no alvo e um anel de impactos menores em volta. */
    private final class GiantMeteor extends Move {
        GiantMeteor() { super(200, 3); }

        @Override
        protected void begin() {
            anim(MobAnim.CAST, 24);
            sound(SoundEvents.WITHER_SHOOT, 2.0F, 0.4F);
        }

        @Override
        protected boolean tick(int t) {
            particles(elementParticle(), mob.getEyePosition().add(0, 2, 0), 6, 0.6, 0.1);
            if (t == 10) {
                Vec3 at = target.position().add(target.getDeltaMovement().multiply(1, 0, 1).scale(25));
                Hazards.add(new Hazards.Blast(level(), mob, element, at, 6.0, 45, baseDamage() * 2.2F, potency()));
                for (int i = 0; i < 6; i++) {
                    double a = Math.PI * 2 * i / 6;
                    Vec3 p = at.add(Math.cos(a) * 8, 0, Math.sin(a) * 8);
                    Hazards.add(new Hazards.Blast(level(), mob, element, p, 2.5, 30 + i * 3, baseDamage(), potency()));
                }
                sound(Hazards.castSound(element), 2.0F, 0.4F);
            }
            return t < 24;
        }
    }

    /** Investida Sem Fim: 5 investidas curtas; batendo na parede ricocheteia direto para a próxima. */
    private final class EndlessCharge extends Move {
        private static final int AIM = 6, RUN = 14, CYCLE = AIM + RUN;
        private final Set<Integer> hit = new HashSet<>();
        private Vec3 dir;
        private int offset;

        EndlessCharge() { super(180, 4); }

        @Override
        protected boolean usable(double dist) {
            return dist > 3;
        }

        @Override
        protected void begin() {
            offset = 0;
            sound(SoundEvents.RAVAGER_ROAR, 2.0F, 1.1F);
        }

        @Override
        protected boolean tick(int t) {
            int lt = t + offset;
            int index = lt / CYCLE, local = lt % CYCLE;
            if (index >= 5) return false;
            if (local == 0) {
                hit.clear();
                anim(MobAnim.WINDUP, AIM);
            }
            if (local < AIM) {
                dir = flatDirTo(target.position());
                face(dir);
                Vec3 from = mob.position().add(0, 0.2, 0);
                SkillVfx.line(level(), from, from.add(dir.scale(14)), Hazards.dust(element, 1.6F), 16);
                return true;
            }
            if (local == AIM) {
                anim(MobAnim.DASH, RUN);
                sound(SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.5F, 0.7F);
            }
            face(dir);
            mob.setDeltaMovement(dir.x * 1.5, mob.getDeltaMovement().y, dir.z * 1.5);
            if (element == ElementType.FIRE) Hazards.magmaTrail(level(), mob);
            particles(ParticleTypes.FLAME, mob.position().add(0, 0.3, 0), 4, 0.6, 0.02);
            for (LivingEntity e : Hazards.enemiesIn(level(), mob, mob.getBoundingBox().inflate(0.6))) {
                if (hit.add(e.getId())) {
                    strike(e, baseDamage() * 1.5F, true);
                    Hazards.push(e, dir.scale(1.4).add(0, 0.5, 0));
                    sound(SoundEvents.RAVAGER_ATTACK, 1.5F, 0.8F);
                }
            }
            if (mob.horizontalCollision) {
                // Ricochete: pula o resto desta corrida e já mira a próxima.
                sound(SoundEvents.ANVIL_LAND, 1.2F, 0.6F);
                particles(ParticleTypes.EXPLOSION, center(), 1, 0.3, 0);
                offset += CYCLE - local - 1;
            }
            return true;
        }
    }

    /** Anel de Erupções: três anéis de explosões a 3, 6 e 9 blocos, um depois do outro — fique no vão. */
    private final class EruptionRing extends Move {
        EruptionRing() { super(160, 4); }

        @Override
        protected void begin() {
            anim(MobAnim.WINDUP, 14);
            sound(SoundEvents.GHAST_WARN, 1.5F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            if (t == 14) {
                anim(MobAnim.SLAM, 10);
                sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.0F, 0.6F);
                int[] counts = {6, 10, 14};
                for (int ring = 0; ring < 3; ring++) {
                    double radius = 3 + ring * 3;
                    double turn = random.nextDouble();
                    for (int i = 0; i < counts[ring]; i++) {
                        double a = Math.PI * 2 * (i + turn) / counts[ring];
                        Vec3 p = mob.position().add(Math.cos(a) * radius, 0, Math.sin(a) * radius);
                        Hazards.add(new Hazards.Blast(level(), mob, element, p, 1.6, 20 + ring * 10,
                                baseDamage() * 1.1F, potency()));
                    }
                }
            }
            return t < 20;
        }
    }
}
