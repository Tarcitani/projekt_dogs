package com.tarcitani.solomod.entity.passive;

import com.tarcitani.solomod.advancement.ModAdvancements;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.mob.Hazards;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gatilhos da Fauna Pacífica do Neutro. Os cérebros dos mobs hostis chamam estes métodos nos momentos de
 * telegrafia; só o companheiro ativo do jogador (ver {@link PassiveEchoEntity}) pode reagir.
 *
 * <ul>
 *   <li>Batedor Espectral: Lacaio iniciando o Bote a até 10 blocos do jogador → derruba o Lacaio por 8 s.</li>
 *   <li>Morcego Eco-Rúnico: Elite preparando contra-ataque ou Guardião prestes a Translocar → silhueta azul por 6 s
 *       (sem Glowing vanilla; ignora Monarca e Soberano, pois só estes métodos chamam).</li>
 *   <li>Autômato Relíquia: Guardião disparando Rajada Elemental → escudo que intercepta até 3 orbes por 3 s.</li>
 * </ul>
 *
 * <p>Estado só no servidor (marcas e escudos), avançado por {@link #tick()} e zerado por {@link #clear()}.
 */
public final class PassiveEchoTriggers {
    private PassiveEchoTriggers() {}

    private static final double PULSE_RANGE = 10.0;
    private static final double SONAR_COUNTER_RANGE = 16.0;
    private static final double SONAR_BLINK_RANGE = 22.0;
    private static final double AEGIS_RANGE = 22.0;
    private static final int SILHOUETTE_TICKS = 6 * 20;
    private static final double SOLAR_MIRROR_RANGE = 3.0;
    private static final double INTERCEPT_RANGE = 2.5;
    private static final double WAVE_RANGE = 24.0;
    private static final int BUBBLE_TICKS = 4 * 20;
    private static final double BUBBLE_KNOCKBACK_REDUCTION = 0.8;
    private static final Map<UUID, Integer> BUBBLES = new HashMap<>();
    private static final double QUAKE_RANGE = 20.0;
    private static final double OWL_RANGE = 16.0;
    private static final double OWL_DASH_SPEED = 0.9;
    private static final double MOTH_BLINK_DISTANCE = 6.0;
    private static final double GUARDIAN_RANGE = 16.0;
    private static final double DOME_RADIUS = 5.0;
    private static final double DOME_VERTICAL_REDUCTION = 0.85;
    private static final int DOME_TICKS = 4 * 20;
    private static final int GRAVITY_TICKS = 3 * 20;
    private static final int POLLEN_SPEED_TICKS = 3 * 20;
    private static final Map<UUID, Integer> GRAVITY = new HashMap<>();
    private static final List<Dome> DOMES = new ArrayList<>();

    private static final class Dome {
        final Vec3 center;
        int ticks;

        Dome(Vec3 center, int ticks) {
            this.center = center;
            this.ticks = ticks;
        }
    }
    private static final int SHIELD_TICKS = 3 * 20;
    private static final int SHIELD_CHARGES = 3;
    /** Intervalo entre desenhos da silhueta e do escudo, em ticks. */
    private static final int DRAW_EVERY = 3;

    private static final List<Mark> MARKS = new ArrayList<>();
    private static final Map<UUID, Shield> SHIELDS = new HashMap<>();

    private static final class Mark {
        final LivingEntity target;
        int ticks = SILHOUETTE_TICKS;

        Mark(LivingEntity target) {
            this.target = target;
        }
    }

    private static final class Shield {
        final ServerPlayer player;
        int charges = SHIELD_CHARGES;
        int ticks = SHIELD_TICKS;

        Shield(ServerPlayer player) {
            this.player = player;
        }
    }

    /** Companheiro ativo do jogador, se estiver vivo no mesmo nível. */
    @Nullable
    public static PassiveEchoEntity activeOf(ServerPlayer player) {
        UUID id = player.getData(ModAttachments.ACTIVE_ECHO).echo();
        if (id == null) return null;
        return player.serverLevel().getEntity(id) instanceof PassiveEchoEntity echo && echo.isAlive() ? echo : null;
    }

    /** Lacaio iniciando o Bote. Devolve true se o Pulso de Afastamento derrubou o Lacaio. */
    public static boolean lungeStarted(LivingEntity lacaio, LivingEntity target) {
        if (!(target instanceof ServerPlayer player) || !(activeOf(player) instanceof SpectralScout scout)) return false;
        if (lacaio.distanceTo(player) > PULSE_RANGE || !scout.useAbility(player)) return false;
        if (scout.level() instanceof ServerLevel server) {
            ring(server, lacaio.position().add(0, 0.2, 0), 1.6, PassiveEchoEntity.dust(0x00E5FF));
        }
        lacaio.playSound(SoundEvents.WARDEN_SONIC_BOOM, 0.6F, 1.8F);
        return true;
    }

    /** Elite preparando contra-ataque após aparar o golpe de {@code attacker}. */
    public static void counterPrepared(LivingEntity elite, LivingEntity attacker) {
        sonar(elite, attacker, SONAR_COUNTER_RANGE);
    }

    /** Guardião prestes a Translocar para as costas de {@code target}. */
    public static void blinkPrepared(LivingEntity guardian, LivingEntity target) {
        sonar(guardian, target, SONAR_BLINK_RANGE);
    }

    private static void sonar(LivingEntity enemy, LivingEntity target, double range) {
        if (!(target instanceof ServerPlayer player) || !(activeOf(player) instanceof EchoRunicBat bat)) return;
        if (enemy.distanceTo(player) > range || !bat.useAbility(player)) return;
        MARKS.add(new Mark(enemy));
    }

    /** Guardião disparando a Rajada Elemental: o Autômato erge o escudo sobre o alvo, se não houver outro. */
    public static void volleyAimed(LivingEntity guardian, LivingEntity target) {
        if (!(target instanceof ServerPlayer player) || !(activeOf(player) instanceof RelicAutomaton automaton)) return;
        if (guardian.distanceTo(player) > AEGIS_RANGE || SHIELDS.containsKey(player.getUUID())) return;
        if (!automaton.useAbility(player)) return;
        SHIELDS.put(player.getUUID(), new Shield(player));
    }

    /**
     * Orbes da Rajada atingindo jogadores: cada escudo ativo absorve um orbe. Devolve true se o orbe deve parar.
     */
    public static boolean absorb(List<LivingEntity> hits) {
        boolean absorbed = false;
        for (LivingEntity hit : hits) {
            Shield shield = hit instanceof ServerPlayer player ? SHIELDS.get(player.getUUID()) : null;
            if (shield != null && shield.charges > 0) {
                shield.charges--;
                absorbed = true;
            }
        }
        return absorbed;
    }

    /**
     * Projétil ígneo a até 3 blocos do jogador com o Escaravelho de Sílica ativo: as asas abrem e o projétil volta.
     * Devolve o jogador que rebateu (e gasta a carga), ou null.
     */
    @Nullable
    public static ServerPlayer solarMirror(ServerLevel level, Vec3 projectile) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(projectile) > SOLAR_MIRROR_RANGE) continue;
            if (activeOf(player) instanceof GoldenSilicaScarab scarab && scarab.useAbility(player)) {
                level.sendParticles(PassiveEchoEntity.dust(0xFFD54F), player.getX(), player.getY() + 1.0, player.getZ(),
                        20, 0.6, 0.6, 0.6, 0.05);
                return player;
            }
        }
        return null;
    }

    /**
     * Esfera perseguidora do Leviatã a até 2,5 blocos do jogador com a Remora ativa: um jato estoura a esfera.
     * Devolve true se a Remora gastou carga e a esfera deve sumir.
     */
    public static boolean interceptChaser(ServerLevel level, Vec3 orb) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(orb) > INTERCEPT_RANGE) continue;
            if (activeOf(player) instanceof AbyssalTrenchRemora remora && remora.useAbility(player)) {
                level.sendParticles(ParticleTypes.BUBBLE_POP, orb.x, orb.y, orb.z, 12, 0.3, 0.3, 0.3, 0.05);
                return true;
            }
        }
        return false;
    }

    /** Maremoto criado em {@code pos}: a Bolha de Ancoragem da Tartaruga envolve os jogadores próximos. */
    public static void waveLaunched(ServerLevel level, Vec3 pos) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(pos) > WAVE_RANGE) continue;
            if (activeOf(player) instanceof LotusTurtleCrab crab) crab.useAbility(player);
        }
    }

    /** Envolve o jogador na bolha: empurrões de ondas perdem força por {@link #BUBBLE_TICKS}. */
    public static void raiseBubble(ServerPlayer player) {
        BUBBLES.put(player.getUUID(), BUBBLE_TICKS);
    }

    /**
     * Impacto de pisão ou golpe sísmico de um chefe de terra em {@code impact}: o Mini-Tank Silvestre do jogador
     * próximo ergue uma cúpula âmbar de 5 blocos que reduz o lançamento vertical.
     */
    public static void quakeStarted(LivingEntity boss, Vec3 impact) {
        if (!(boss.level() instanceof ServerLevel server)) return;
        for (ServerPlayer player : server.players()) {
            if (player.position().distanceTo(impact) > QUAKE_RANGE) continue;
            if (activeOf(player) instanceof SylvanMiniTank tank && tank.useAbility(player)) {
                DOMES.add(new Dome(tank.position(), DOME_TICKS));
                return;
            }
        }
    }

    /** Repulsão de um Guardião: o Golem Pedregulho dos jogadores próximos fica imune a empurrões. */
    public static void guardianRepulse(LivingEntity guardian) {
        for (ServerPlayer player : playersNear(guardian, GUARDIAN_RANGE)) {
            if (activeOf(player) instanceof RunicBoulderGolem golem && golem.useAbility(player)) {
                GRAVITY.put(player.getUUID(), GRAVITY_TICKS);
            }
        }
    }

    /** Golpe pesado de um Elite de Terra: mesma imunidade do Golem, se o alvo for um jogador com ele ativo. */
    public static void heavyBlow(LivingEntity victim) {
        if (!(victim instanceof ServerPlayer player) || !(activeOf(player) instanceof RunicBoulderGolem golem)) return;
        if (golem.useAbility(player)) GRAVITY.put(player.getUUID(), GRAVITY_TICKS);
    }

    /**
     * Raiz Ancestral prestes a brotar sob {@code victim}: se a Gazela ativa for do jogador, o pólen destrói a raiz,
     * limpa Fadiga de Mineração e dá Velocidade II por 3 s. Devolve true se a raiz deve ser cancelada.
     */
    public static boolean amberPollen(LivingEntity victim) {
        if (!(victim instanceof ServerPlayer player) || !(activeOf(player) instanceof AmberRootGazelle gazelle)) return false;
        if (!gazelle.useAbility(player)) return false;
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, POLLEN_SPEED_TICKS, 1));
        return true;
    }

    /** Vetor do empurrão do jogador após os efeitos ativos: imunidade (zero), bolha (-80%) e cúpula (vertical -85%). */
    public static Vec3 dampenPush(Entity entity, Vec3 velocity) {
        if (!(entity instanceof ServerPlayer player)) return velocity;
        UUID id = player.getUUID();
        if (GRAVITY.containsKey(id)) return Vec3.ZERO;
        if (BUBBLES.containsKey(id)) velocity = velocity.scale(1.0 - BUBBLE_KNOCKBACK_REDUCTION);
        for (Dome dome : DOMES) {
            if (player.position().distanceTo(dome.center) <= DOME_RADIUS) {
                velocity = new Vec3(velocity.x, velocity.y * (1.0 - DOME_VERTICAL_REDUCTION), velocity.z);
            }
        }
        return velocity;
    }

    /** Elite telegrafando a Investida contra {@code target}: a Coruja empurra o jogador 3 blocos para o lado. */
    public static void dashTelegraphed(LivingEntity elite, LivingEntity target) {
        if (!(target instanceof ServerPlayer player) || !(activeOf(player) instanceof StormSentinelOwl owl)) return;
        if (elite.distanceTo(player) > OWL_RANGE || !owl.useAbility(player)) return;
        Vec3 away = player.position().subtract(elite.position()).multiply(1, 0, 1).normalize();
        Vec3 side = new Vec3(-away.z, 0, away.x).scale(player.getRandom().nextBoolean() ? 1 : -1);
        Hazards.push(player, side.scale(OWL_DASH_SPEED));
    }

    /** Raio do Peito do Soberano disparando no estágio 6: a Mariposa teleporta o jogador para trás do Soberano. */
    public static void chestBeamFired(LivingEntity sovereign, LivingEntity focus) {
        if (!(focus instanceof ServerPlayer player) || !(activeOf(player) instanceof DimensionalRiftMoth moth)) return;
        if (!moth.useAbility(player)) return;
        Vec3 back = sovereign.getLookAngle().multiply(1, 0, 1).normalize().scale(-MOTH_BLINK_DISTANCE);
        Vec3 dest = sovereign.position().add(back);
        player.connection.teleport(dest.x, dest.y, dest.z, player.getYRot(), player.getXRot());
        ModAdvancements.onFrameJump(player);
    }

    /** Pérola do End do jogador com a Mariposa ativa: o dano do teleporte é anulado. Devolve true se anulou. */
    public static boolean negatePearlDamage(ServerPlayer player) {
        return activeOf(player) instanceof DimensionalRiftMoth moth && moth.useAbility(player);
    }

    private static List<ServerPlayer> playersNear(LivingEntity origin, double range) {
        List<ServerPlayer> near = new ArrayList<>();
        if (!(origin.level() instanceof ServerLevel server)) return near;
        for (ServerPlayer player : server.players()) {
            if (player.position().distanceTo(origin.position()) <= range) near.add(player);
        }
        return near;
    }

    /** Avança marcas, escudos e bolhas um tick e desenha os efeitos. Chamado pelo tick do servidor (MobEvents). */
    public static void tick() {
        for (Iterator<Map.Entry<UUID, Integer>> it = BUBBLES.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Integer> entry = it.next();
            if (entry.getValue() <= 1) {
                it.remove();
                continue;
            }
            entry.setValue(entry.getValue() - 1);
        }
        GRAVITY.replaceAll((id, ticks) -> ticks - 1);
        GRAVITY.values().removeIf(ticks -> ticks <= 0);
        for (Iterator<Dome> it = DOMES.iterator(); it.hasNext(); ) {
            if (--it.next().ticks <= 0) it.remove();
        }
        for (Iterator<Mark> it = MARKS.iterator(); it.hasNext(); ) {
            Mark mark = it.next();
            if (--mark.ticks <= 0 || !mark.target.isAlive() || !(mark.target.level() instanceof ServerLevel)) {
                it.remove();
            } else if (mark.ticks % DRAW_EVERY == 0) {
                silhouette((ServerLevel) mark.target.level(), mark.target);
            }
        }
        for (Iterator<Shield> it = SHIELDS.values().iterator(); it.hasNext(); ) {
            Shield shield = it.next();
            if (--shield.ticks <= 0 || shield.charges <= 0 || !shield.player.isAlive()) {
                it.remove();
            } else if (shield.ticks % DRAW_EVERY == 0) {
                arc(shield.player);
            }
        }
    }

    public static void clear() {
        MARKS.clear();
        SHIELDS.clear();
        BUBBLES.clear();
        GRAVITY.clear();
        DOMES.clear();
    }

    /** Contorno azul do inimigo: pontos em três alturas ao redor da caixa, feitos com partícula de poeira. */
    private static void silhouette(ServerLevel level, LivingEntity enemy) {
        double radius = enemy.getBbWidth() / 2 + 0.25;
        double[] heights = {0.2, enemy.getBbHeight() / 2, enemy.getBbHeight() - 0.2};
        for (double h : heights) {
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                level.sendParticles(PassiveEchoEntity.dust(0x1E88E5),
                        enemy.getX() + Math.cos(angle) * radius, enemy.getY() + h, enemy.getZ() + Math.sin(angle) * radius,
                        1, 0, 0, 0, 0);
            }
        }
        level.sendParticles(ParticleTypes.SCULK_SOUL, enemy.getX(), enemy.getY() + enemy.getBbHeight(), enemy.getZ(),
                1, 0.2, 0.1, 0.2, 0.01);
    }

    /** Arco dourado de 120 graus à frente do jogador: o escudo curvo da Égide. */
    private static void arc(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 forward = player.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 center = player.position().add(0, 1.0, 0);
        for (int deg = -60; deg <= 60; deg += 30) {
            double rad = Math.toRadians(deg);
            Vec3 dir = new Vec3(forward.x * Math.cos(rad) - forward.z * Math.sin(rad), 0,
                    forward.x * Math.sin(rad) + forward.z * Math.cos(rad));
            Vec3 p = center.add(dir.scale(1.6));
            level.sendParticles(PassiveEchoEntity.dust(0xD4AF37), p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS,
                0.15F, 1.5F);
    }

    private static void ring(ServerLevel level, Vec3 center, double radius, net.minecraft.core.particles.ParticleOptions particle) {
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
        }
    }
}
