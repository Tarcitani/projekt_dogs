package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.advancement.ModAdvancements;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.entity.RoyalFireball;
import com.tarcitani.solomod.network.CinematicPayload;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * O Soberano: chefe final da versão de testes. Há eras os cinco Monarcas se uniram para derrotá-lo; ele
 * voltou com o poder de todos eles.
 *
 * <p><b>Estágios</b>: a vida é dividida em 6 faixas iguais (barra com 6 marcas). Cada vez que o jogador
 * termina uma faixa, o Soberano <b>agoniza</b> por 4 s: fica invulnerável e parado, os jogadores ganham um
 * close cinemático no rosto dele e ele desperta o poder de mais um Monarca (um ataque novo). Nenhum golpe
 * pula estágio: a vida para no limite da faixa atual, mesmo num golpe fatal (só {@code /kill} ignora).
 *
 * <table>
 *   <tr><th>Estágio</th><th>Vida</th><th>Poder despertado</th><th>Ataque liberado</th></tr>
 *   <tr><td>1</td><td>100–83%</td><td>Colosso Rei</td><td>Punho do Colosso</td></tr>
 *   <tr><td>2</td><td>83–67%</td><td>Rei Vulcânico</td><td>Bolas de Fogo Reais rebatíveis</td></tr>
 *   <tr><td>3</td><td>67–50%</td><td>Leviatã Coroado</td><td>Maremoto</td></tr>
 *   <tr><td>4</td><td>50–33%</td><td>Titã Ancestral</td><td>Raízes Ancestrais</td></tr>
 *   <tr><td>5</td><td>33–17%</td><td>Imperador dos Céus</td><td>Tempestade</td></tr>
 *   <tr><td>6</td><td>17–0%</td><td>todos</td><td>Convergência + FÚRIA sem fim</td></tr>
 * </table>
 * Os ataques se acumulam, exceto as bolas de fogo (somente estágios 2 e 6). No 6 entra em Fúria até morrer (+35% de dano,
 * -35% de dano recebido, recargas e pausas menores) e ganha a Convergência.
 *
 * <p><b>Ataques</b> (todos telegrafados):
 * <ul>
 *   <li>Punho do Colosso: ergue o punho e esmaga; ondas de choque no chão (duas a partir do estágio 4) — pule.</li>
 *   <li>Bolas de Fogo Reais (somente estágios 2 e 6): rajada lenta rebatível; fora da Fúria,
 *       acertar o Soberano com ela o deixa agachado se recuperando por 5 s.
 *       A agonia é invulnerável e não pode ser interrompida; limites de estágio continuam valendo.</li>
 *   <li>Maremoto: mostra a faixa no chão e lança uma onda reta que arrasta quem estiver nela.</li>
 *   <li>Raízes Ancestrais: duas levas de raízes brotam sob cada jogador (a segunda onde ele estiver).</li>
 *   <li>Tempestade: puxa todos para perto e chama raios sobre cada jogador por 3 s.</li>
 *   <li>Convergência (só estágio 6): três rajadas de orbes dos cinco elementos e uma estrela de explosões.</li>
 * </ul>
 *
 * <p><b>Morte</b>: desintegra como os Monarcas. Quem estava perto ou lutou vê a cinemática final e, ao fim
 * dela, os créditos. Quem causou dano ganha a conquista do Soberano. A lista de quem lutou não é salva.
 */
public final class SovereignBrain extends TierBrain {
    public static final int STAGES = 6;
    /** Duração da agonia (e do close) a cada estágio. */
    public static final int AGONY_TICKS = 80;
    /** Poder despertado ao ENTRAR em cada estágio (índice = estágio - 1); o 6 desperta todos. */
    private static final ElementType[] AWAKENED = {
            ElementType.NONE, ElementType.FIRE, ElementType.WATER, ElementType.EARTH, ElementType.AIR, ElementType.NONE};
    private static final ElementType[] ALL = {
            ElementType.NONE, ElementType.FIRE, ElementType.WATER, ElementType.EARTH, ElementType.AIR};

    private final ServerBossEvent bar;
    /** Jogadores que causaram dano nesta luta (UUID, para sobreviver a relog). */
    private final Set<UUID> fighters = new HashSet<>();
    private int stage = 1;
    /** Ticks restantes da agonia (0 = lutando). */
    private int agonyTicks;

    SovereignBrain(PathfinderMob mob) {
        super(mob, MobTier.BOSS, ElementType.NONE);
        bar = new ServerBossEvent(mob.getDisplayName(), barColor(1), BossEvent.BossBarOverlay.NOTCHED_6);
        // Os mesmos ataques valem na Fúria (já liberados), mais a Convergência.
        for (Move move : List.of(new ColossusFist(), new RoyalVolley(), new TidalWave(), new AncientRoots(), new SkyStorm())) {
            addMove(move);
            addRageMove(move);
        }
        addRageMove(new Convergence());
    }

    /** Teste ({@code /modtest sovereign <estágio>}): pula direto para o estágio, sem as cinemáticas do caminho. */
    public static boolean skipToStage(Entity entity, int target) {
        if (!(TierBrain.of(entity) instanceof SovereignBrain brain)) return false;
        brain.jumpTo(Mth.clamp(target, 1, STAGES));
        return true;
    }

    // ------------------------------------------------------------------ estágios

    @Override public int phase() { return stage; }
    @Override protected Component bossBarName() {
        return mob.getDisplayName().copy().append(Component.translatable("mob_title.solomod.stage", stage, STAGES));
    }
    @Override protected int thinkGap() { return 40 - 4 * stage; }
    @Override protected int meleeInterval() { return 30 - 2 * stage; }
    @Override protected float meleeElementChance() { return 0F; }
    @Override protected float cooldownScale() { return 1.0F - 0.06F * (stage - 1); }
    /** A Fúria não começa pela vida: ela vem no fim da última agonia. */
    @Override protected float rageThreshold() { return 0F; }
    @Override protected boolean endlessRage() { return true; }

    /** Vida mínima do estágio (o limite de baixo da faixa). */
    private float stageFloor(int s) {
        return mob.getMaxHealth() * (STAGES - s) / STAGES;
    }

    private static BossEvent.BossBarColor barColor(int s) {
        return switch (s) {
            case 1 -> BossEvent.BossBarColor.YELLOW;
            case 2 -> BossEvent.BossBarColor.RED;
            case 3 -> BossEvent.BossBarColor.BLUE;
            case 4 -> BossEvent.BossBarColor.GREEN;
            case 5 -> BossEvent.BossBarColor.WHITE;
            default -> BossEvent.BossBarColor.PURPLE;
        };
    }

    @Override
    public void tick() {
        if (isCombatPaused()) return;
        // Agonizando: não ataca nem anda (a agonia em si roda no passiveTick, com ou sem alvo).
        if (agonyTicks > 0) {
            mob.getNavigation().stop();
            return;
        }
        super.tick();
    }

    @Override
    public void passiveTick() {
        if (isCombatPaused()) {
            updateBossBar(bar, 64);
            return;
        }
        if (agonyTicks > 0) {
            tickAgony();
        } else if (stage < STAGES && mob.getHealth() <= stageFloor(stage) + 0.01F) {
            beginAgony();
        }
        // Aura: um anel por segundo de cada poder já despertado, alternando.
        if (mob.tickCount % 4 == 0) {
            ElementType e = ALL[(mob.tickCount / 20) % Math.min(stage, ALL.length)];
            SkillVfx.ring(level(), mob.position().add(0, 0.1, 0), 2.2 + 0.2 * stage, Hazards.dust(e, 1.6F), 12 + 3 * stage);
        }
        updateBossBar(bar, 64);
    }

    @Override
    public void onHurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) fighters.add(player.getUUID());
        // Não pula estágio: a vida para no limite da faixa (até num golpe fatal) e a agonia acontece.
        if (stage < STAGES && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            float floor = stageFloor(stage);
            if (mob.getHealth() < floor) mob.setHealth(floor);
        }
    }

    private void beginAgony() {
        interrupt();
        stage++;
        agonyTicks = AGONY_TICKS;
        invulnerable = true;
        anim(MobAnim.AGONY, AGONY_TICKS);
        bar.setColor(barColor(stage));
        bar.setName(bossBarName());
        sound(SoundEvents.WITHER_HURT, 3.0F, 0.4F);
        sound(SoundEvents.ENDER_DRAGON_GROWL, 2.5F, 0.5F);
        String power = stage == STAGES ? "all" : AWAKENED[stage - 1].getSerializedName();
        CinematicPayload closeUp = new CinematicPayload(mob.getId(), AGONY_TICKS, CinematicPayload.Kind.CLOSE_UP,
                Component.translatable("cinematic.solomod.agony"), Component.translatable("cinematic.solomod.awaken." + power));
        for (ServerPlayer player : audience()) ModNetwork.sendCinematic(player, closeUp);
    }

    /** Agonia: o poder despertado converge para o corpo e explode no fim. */
    private void tickAgony() {
        int t = AGONY_TICKS - agonyTicks--;
        mob.getNavigation().stop();
        mob.setDeltaMovement(0, Math.min(0, mob.getDeltaMovement().y), 0);
        ElementType power = stage == STAGES ? ALL[(t / 6) % ALL.length] : AWAKENED[stage - 1];

        double r = Math.max(0.8, 9.0 * (1 - t / 60.0));
        if (t < 60 && t % 2 == 0) {
            SkillVfx.ring(level(), center(), r, Hazards.dust(power, 2.0F), 24);
            particles(SkillVfx.particleFor(power), center(), 4, 1.2, 0.05);
        }
        if (t % 10 == 0) sound(SoundEvents.WARDEN_HEARTBEAT, 2.5F, 0.5F);
        if (t == 20 || t == 45) sound(SoundEvents.RAVAGER_HURT, 2.5F, 0.4F);
        if (t == 60) {
            particles(ParticleTypes.EXPLOSION_EMITTER, center(), 2, 0.6, 0);
            particles(SkillVfx.particleFor(power), center(), 80, 2.0, 0.25);
            sound(SoundEvents.GENERIC_EXPLODE.value(), 2.5F, 0.5F);
            sound(Hazards.impactSound(power), 2.5F, 0.6F);
            for (LivingEntity e : enemiesWithin(12)) {
                Hazards.push(e, flatDirTo(e.position()).scale(1.4).add(0, 0.5, 0));
            }
            Hazards.add(new Hazards.Shockwave(level(), mob, power, mob.position(), 14, 0.6, baseDamage() * 0.6F, potency()));
        }
        if (agonyTicks == 0) endAgony();
    }

    private void endAgony() {
        invulnerable = false;
        if (stage == STAGES && !isRaging()) awaken();
        else anim(MobAnim.NONE, 0);
    }

    /** Fim da última agonia: liberta o poder dos cinco Monarcas e entra em Fúria até morrer. */
    private void awaken() {
        beginRage();
        anim(MobAnim.ROAR, 30);
        sound(SoundEvents.WITHER_SPAWN, 2.0F, 0.7F);
        Component warning = Component.translatable("message.solomod.rage", mob.getDisplayName())
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        for (ServerPlayer player : audience()) player.displayClientMessage(warning, true);
    }

    @Override
    protected void onRageStart() {
        bar.setColor(BossEvent.BossBarColor.PURPLE);
        bar.setDarkenScreen(true);
        bar.setCreateWorldFog(true);
    }

    private void jumpTo(int target) {
        interrupt();
        agonyTicks = 0;
        invulnerable = false;
        stage = target;
        mob.setHealth(mob.getMaxHealth() * (STAGES - target + 1) / STAGES);
        bar.setColor(barColor(stage));
        if (stage == STAGES && !isRaging()) awaken();
        else anim(MobAnim.NONE, 0);
    }

    /** Quem vê as cinemáticas: os jogadores a até 64 blocos e quem lutou (se ainda estiver online). */
    private Set<ServerPlayer> audience() {
        Set<ServerPlayer> players = new LinkedHashSet<>();
        for (ServerPlayer player : level().players()) {
            if (player.distanceToSqr(mob) < 64 * 64) players.add(player);
        }
        for (UUID id : fighters) {
            ServerPlayer player = level().getServer().getPlayerList().getPlayer(id);
            if (player != null) players.add(player);
        }
        return players;
    }

    // ------------------------------------------------------------------ combate básico

    @Override
    protected void engage() {
        chase(isRaging() ? 1.35 : 1.0);
        if (tryMelee(1.2F, SoundEvents.IRON_GOLEM_ATTACK, 0.5F)) {
            particles(ParticleTypes.CRIT, target.getEyePosition(), 10, 0.4, 0.2);
        }
    }

    @Override
    public void onRemoved() {
        bar.removeAllPlayers();
    }

    @Override
    public void onDeath() {
        int duration = TierMob.DISSOLVE_TICKS + 40;
        CinematicPayload finale = new CinematicPayload(mob.getId(), duration, CinematicPayload.Kind.FINALE,
                Component.translatable("cinematic.solomod.sovereign_defeated"), mob.getDisplayName());
        for (ServerPlayer player : audience()) ModNetwork.sendCinematic(player, finale);
        // Conquista: só quem lutou (o golpe final passa por onHurt antes da morte).
        for (UUID id : fighters) {
            ServerPlayer player = level().getServer().getPlayerList().getPlayer(id);
            if (player != null) ModAdvancements.awardSovereignDefeat(player);
        }
        sound(SoundEvents.WITHER_DEATH, 3.0F, 0.6F);
        sound(SoundEvents.ENDER_DRAGON_DEATH, 2.0F, 0.8F);
        sound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
        particles(ParticleTypes.EXPLOSION_EMITTER, center(), 3, 0.8, 0);
        for (ElementType e : ALL) particles(SkillVfx.particleFor(e), center(), 40, 2.0, 0.25);
        bar.removeAllPlayers();
    }

    // ------------------------------------------------------------------ ataques

    /** Ataque liberado a partir de um estágio. */
    private abstract class StageMove extends Move {
        private final int unlock;

        StageMove(int unlock, int cooldown, int weight) {
            super(cooldown, weight);
            this.unlock = unlock;
        }

        @Override
        protected final boolean usable(double dist) {
            return stage >= unlock && inRange(dist);
        }

        protected boolean inRange(double dist) { return true; }
    }

    /** Punho do Colosso: 20 ticks erguendo o punho, esmaga e solta onda(s) de choque. */
    private final class ColossusFist extends StageMove {
        ColossusFist() { super(1, 80, 5); }

        @Override
        protected boolean inRange(double dist) {
            return dist < 7;
        }

        @Override
        protected void begin() {
            anim(MobAnim.WINDUP, 20);
            sound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < 20) {
                face(flatDirTo(target.position()));
                particles(ParticleTypes.CRIT, center().add(0, mob.getBbHeight() * 0.4, 0), 3, 0.5, 0.1);
                return true;
            }
            if (t == 20) {
                anim(MobAnim.SLAM, 12);
                for (LivingEntity e : enemiesWithin(3 + mob.getBbWidth() / 2)) strike(e, baseDamage() * 1.5F, false);
            }
            if (t == 20 || (t == 32 && stage >= 4)) {
                sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.5F, 0.5F);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.6F);
                particles(ParticleTypes.EXPLOSION, mob.position(), 4, 1.5, 0);
                Hazards.add(new Hazards.Shockwave(level(), mob, ElementType.NONE, mob.position(), 12, 0.55, baseDamage() * 0.9F, 0));
            }
            return t < 36;
        }
    }

    /** Poder do Rei Vulcânico: três bolas lentas, rebatíveis, com 1 s de preparação e 0,9 s entre tiros. */
    private final class RoyalVolley extends StageMove {
        RoyalVolley() { super(2, 140, 6); }

        @Override protected boolean inRange(double dist) {
            return (stage == 2 || stage == STAGES) && dist > TierIdentity.ROYAL_FIREBALL_MIN_RANGE
                    && dist < 32 && mob.getSensing().hasLineOfSight(target);
        }

        @Override
        protected void begin() {
            anim(MobAnim.BREATH, 60);
            sound(SoundEvents.GHAST_WARN, 2.0F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            // Um projétil já lançado também é removido se a luta sair do estágio de fogo.
            if (stage != 2 && stage != STAGES) return false;
            face(flatDirTo(target.position()));
            particles(ParticleTypes.FLAME, mob.getEyePosition(), 10, 0.6, 0.05);
            if (t == 20 || t == 38 || t == 56) {
                RoyalFireball.launch(level(), mob, target, baseDamage() * TierIdentity.ROYAL_FIREBALL_DAMAGE_MULTIPLIER);
            }
            return t < 60;
        }
    }

    /** Maremoto (Leviatã Coroado): 20 ticks mostrando a faixa, depois uma onda reta de 7 de largura. */
    private final class TidalWave extends StageMove {
        private static final int AIM = 20, LENGTH = 24;
        private static final double HALF_WIDTH = 3.5;
        private final Set<Integer> hit = new HashSet<>();
        private Vec3 dir;

        TidalWave() { super(3, 160, 4); }

        @Override
        protected boolean inRange(double dist) {
            return dist < LENGTH;
        }

        @Override
        protected void begin() {
            hit.clear();
            dir = flatDirTo(target.position());
            face(dir);
            anim(MobAnim.WINDUP, AIM);
            sound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.5F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            Vec3 side = new Vec3(-dir.z, 0, dir.x);
            if (t < AIM) {
                face(dir);
                if (t % 2 == 0) {
                    for (double d = 2; d < LENGTH; d += 1.5) {
                        Vec3 p = mob.position().add(dir.scale(d)).add(0, 0.15, 0);
                        for (double s : new double[]{-HALF_WIDTH, HALF_WIDTH}) {
                            Vec3 q = p.add(side.scale(s));
                            level().sendParticles(Hazards.dust(ElementType.WATER, 1.2F), q.x, q.y, q.z, 1, 0, 0, 0, 0);
                        }
                    }
                }
                return true;
            }
            if (t == AIM) {
                anim(MobAnim.SLAM, 10);
                sound(SoundEvents.GENERIC_SPLASH, 3.0F, 0.5F);
                sound(SoundEvents.ELDER_GUARDIAN_AMBIENT, 2.0F, 0.6F);
            }
            double front = (t - AIM) * 1.1;
            Vec3 crest = mob.position().add(dir.scale(front));
            for (double s = -HALF_WIDTH; s <= HALF_WIDTH; s += 0.7) {
                Vec3 q = crest.add(side.scale(s));
                level().sendParticles(ParticleTypes.SPLASH, q.x, q.y + 0.5, q.z, 4, 0.2, 0.6, 0.2, 0.1);
                level().sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, q.x, q.y + 0.2, q.z, 2, 0.2, 0.4, 0.2, 0.1);
            }
            for (LivingEntity e : enemiesWithin(LENGTH + 2)) {
                Vec3 rel = e.position().subtract(mob.position());
                double along = rel.dot(dir), across = Math.abs(rel.dot(side));
                if (along > front - 1.5 && along < front + 0.8 && across <= HALF_WIDTH && hit.add(e.getId())) {
                    Hazards.hit(mob, e, baseDamage() * 1.2F, ElementType.WATER, potency(), true);
                    Hazards.push(e, dir.scale(1.6).add(0, 0.4, 0));
                }
            }
            return front < LENGTH;
        }
    }

    /** Raízes Ancestrais (Titã Ancestral): duas levas de raízes sob cada jogador. */
    private final class AncientRoots extends StageMove {
        AncientRoots() { super(4, 150, 4); }

        @Override
        protected void begin() {
            anim(MobAnim.WINDUP, 14);
            sound(SoundEvents.ROOTED_DIRT_BREAK, 2.5F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            if (t == 14 || t == 34) {
                if (t == 14) anim(MobAnim.SLAM, 10);
                for (LivingEntity v : targets(24)) blast(ElementType.EARTH, v.position(), 2.0, t == 14 ? 22 : 18, 1.3F);
                sound(SoundEvents.EVOKER_FANGS_ATTACK, 2.0F, 0.6F);
                particles(ParticleTypes.COMPOSTER, mob.position(), 30, 2.0, 0.1);
            }
            return t < 40;
        }
    }

    /** Tempestade (Imperador dos Céus): puxa todos e chama raios sobre cada jogador a cada 0,5 s. */
    private final class SkyStorm extends StageMove {
        private static final int CHANNEL = 50, STRIKE_DELAY = 14;
        private final List<Strike> pending = new ArrayList<>();

        private record Strike(Vec3 pos, int at) {}

        SkyStorm() { super(5, 170, 4); }

        @Override
        protected void begin() {
            pending.clear();
            anim(MobAnim.CAST, CHANNEL);
            sound(Hazards.castSound(ElementType.AIR), 2.5F, 0.5F);
            sound(SoundEvents.TRIDENT_THUNDER.value(), 1.5F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < CHANNEL) {
                for (LivingEntity e : enemiesWithin(16)) Hazards.push(e, flatDirTo(e.position()).scale(-0.06));
                SkillVfx.ring(level(), center(), 4 + Mth.sin(t * 0.3F), Hazards.dust(ElementType.AIR, 1.6F), 16);
                if (t % 10 == 5) {
                    for (LivingEntity v : targets(24)) {
                        blast(ElementType.AIR, v.position(), 1.8, STRIKE_DELAY, 1.0F);
                        pending.add(new Strike(v.position(), t + STRIKE_DELAY));
                    }
                }
            }
            // O raio (só visual) cai junto com a explosão da área marcada.
            pending.removeIf(s -> {
                if (s.at() != t) return false;
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
                if (bolt != null) {
                    bolt.moveTo(s.pos());
                    bolt.setVisualOnly(true);
                    level().addFreshEntity(bolt);
                }
                return true;
            });
            return t < CHANNEL + STRIKE_DELAY + 1;
        }
    }

    /** Convergência (só no estágio 6): três rajadas de orbes dos cinco elementos e uma estrela de explosões. */
    private final class Convergence extends StageMove {
        Convergence() { super(6, 200, 6); }

        @Override
        protected void begin() {
            anim(MobAnim.CAST, 60);
            sound(SoundEvents.BEACON_POWER_SELECT, 3.0F, 0.5F);
            sound(SoundEvents.WARDEN_SONIC_CHARGE, 2.5F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            for (int k = 0; k < ALL.length; k++) {
                double a = t * 0.15 + Math.PI * 2 * k / ALL.length;
                Vec3 p = center().add(Math.cos(a) * 3, Math.min(t, 30) * 0.06, Math.sin(a) * 3);
                level().sendParticles(Hazards.dust(ALL[k], 2.0F), p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0);
            }
            if (t == 30 || t == 40 || t == 50) {
                sound(SoundEvents.BLAZE_SHOOT, 2.5F, 0.6F);
                for (LivingEntity v : targets(32)) {
                    for (int k = 0; k < ALL.length; k++) {
                        double a = Math.PI * 2 * k / ALL.length;
                        Vec3 from = center().add(Math.cos(a) * 3, 1, Math.sin(a) * 3);
                        Vec3 vel = v.getEyePosition().subtract(from).normalize().scale(0.9);
                        Hazards.add(new Hazards.Orb(level(), mob, ALL[k], from, vel, baseDamage() * 0.7F, potency(), 40));
                    }
                }
            }
            if (t == 60) {
                for (int k = 0; k < ALL.length; k++) {
                    double a = Math.PI * 2 * k / ALL.length;
                    blast(ALL[k], target.position().add(Math.cos(a) * 4, 0, Math.sin(a) * 4), 2.5, 20, 1.4F);
                }
                blast(ElementType.NONE, target.position(), 2.5, 26, 1.4F);
            }
            return t < 64;
        }
    }

    /** Até 4 inimigos por perto (o alvo primeiro), para os ataques que miram em cada jogador. */
    private List<LivingEntity> targets(double radius) {
        List<LivingEntity> list = new ArrayList<>();
        list.add(target);
        for (LivingEntity e : enemiesWithin(radius)) {
            if (list.size() >= 4) break;
            if (e != target) list.add(e);
        }
        return list;
    }

    private void blast(ElementType e, Vec3 at, double radius, int delay, float damageMult) {
        Hazards.add(new Hazards.Blast(level(), mob, e, at, radius, delay, baseDamage() * damageMult, potency()));
    }
}
