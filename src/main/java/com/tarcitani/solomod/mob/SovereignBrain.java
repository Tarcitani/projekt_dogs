package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.advancement.ModAdvancements;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.RoyalFireball;
import com.tarcitani.solomod.entity.SovereignTornado;
import com.tarcitani.solomod.entity.WaterOrb;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.network.CinematicPayload;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * O Soberano: chefe final da versão de testes. Há eras os cinco Monarcas se uniram para derrotá-lo; ele
 * voltou com o poder de todos eles.
 *
 * <p><b>Estágios</b>: a vida é dividida em 6 faixas iguais (barra com 6 marcas). Cada vez que o jogador
 * termina uma faixa, o Soberano <b>agoniza</b> por 4 s: fica invulnerável e parado, os jogadores ganham um
 * close cinemático no rosto dele e ele desperta o poder de mais um Monarca. Nenhum golpe pula estágio: a vida
 * para no limite da faixa atual, mesmo num golpe fatal (só {@code /kill} ignora).
 *
 * <p>O estágio <b>não é salvo</b>: sai da vida atual sempre que o cérebro é instalado (ver
 * {@link #stageFromHealth}). Sem isso, recarregar o chefe (o chunk descarrega quando o jogador morre e
 * reaparece longe) o faria começar do estágio 1 e repetir TODAS as agonias até alcançar a vida dele.
 *
 * <table>
 *   <tr><th>Estágio</th><th>Vida</th><th>Poder</th><th>Ataques (só estes, neste estágio)</th></tr>
 *   <tr><td>1</td><td>100–83%</td><td>Colosso Rei</td><td>Punho do Colosso</td></tr>
 *   <tr><td>2</td><td>83–67%</td><td>Rei Vulcânico</td><td>Genki-dama de fogo</td></tr>
 *   <tr><td>3</td><td>67–50%</td><td>Leviatã Coroado</td><td>Maremoto e Esferas de Água</td></tr>
 *   <tr><td>4</td><td>50–33%</td><td>Titã Ancestral</td><td>Raízes Ancestrais, Pisão e Pulão</td></tr>
 *   <tr><td>5</td><td>33–17%</td><td>Imperador dos Céus</td><td>Tornados</td></tr>
 *   <tr><td>6</td><td>17–0%</td><td>todos + só dele</td><td>todos os acima, um por vez, + Raio do Peito + FÚRIA sem fim</td></tr>
 * </table>
 * <b>Regra dos poderes</b>: cada estágio de 1 a 5 representa um elemento e só usa os poderes DELE (nada se
 * acumula: no estágio 3 não há punho nem genki-dama; no 4, nem maremoto). Só o <b>estágio 6</b> libera todos
 * os poderes, um por vez (nunca combinados), com PRIORIDADE para o raio do peito, que é só dele: sempre que o
 * raio está pronto, vem antes. Nele ele entra em Fúria até morrer (+35% de dano, -35% de dano recebido) e
 * ganha o recuo (voo curto) para abrir espaço para o raio e a corrida frenética. O voo curto de AVANÇO
 * (alvo kitando ou ele preso no terreno) é movimento, não poder, e vale em qualquer estágio.
 *
 * <p><b>Ataques</b> (todos telegrafados):
 * <ul>
 *   <li>Punho do Colosso: ergue o punho e esmaga; ondas de choque no chão (duas a partir do estágio 4) — pule.</li>
 *   <li>Genki-dama: une os braços acima da cabeça, carrega uma esfera de fogo por 3,5 s e a joga para baixo
 *       sobre o jogador focado; explode em área. Rebatível: devolvida, só o atordoa (ele é imune a fogo).</li>
 *   <li>Maremoto: mostra a faixa no chão e lança uma onda reta que arrasta quem estiver nela.</li>
 *   <li>Raízes Ancestrais: duas levas de raízes brotam sob cada jogador (a segunda onde ele estiver).</li>
 *   <li>Esferas de Água: 5 esferas 3D giram em volta dele e depois perseguem os jogadores; dá para desviar
 *       ou estourá-las com um golpe. O Maremoto agora é uma crista de esferas de água rolando.</li>
 *   <li>Pisão (de perto): ergue a perna e esmaga o chão; terremoto forte que arremessa e sacode blocos.</li>
 *   <li>Pulão: salta (~3 blocos) até perto do jogador e cai num terremoto ainda maior (22 blocos).</li>
 *   <li>Tornados: flutua de braços abertos e chama 4 tornados 3D que perseguem os jogadores e os jogam para o
 *       alto (o dano vem da queda).</li>
 *   <li>Corrida Frenética (só Fúria): se o jogador focado está correndo, persegue-o numa corrida exagerada e
 *       desfere um golpe pesado ao alcançá-lo.</li>
 *   <li>Raio do Peito (só estágio 6): 1,5 s de carga e 6 s de raio que tenta seguir o jogador focado (um pouco
 *       mais devagar que quem corre); depois ajoelha cansado por 4 s, vulnerável (+40% de dano).</li>
 * </ul>
 *
 * <p><b>Inteligência</b> (a IA escolhe por prioridade, não só por sorteio; ver {@code TierBrain.Move#urgency}):
 * <ol>
 *   <li>Jogador colado nos pés (&lt; 9 blocos): pisão (6, joga para longe) &gt; punho (4, também empurra).</li>
 *   <li>Voo curto (5): recuo na Fúria quando o jogador está perto e o raio vai ficar pronto (~22 s de recarga,
 *       60% de chance quando as condições valem) e avanço quando o alvo kita (&gt; 22 blocos por 5 s) ou ele
 *       está preso no terreno (2 s sem sair do lugar).</li>
 *   <li>Raio do peito (3): sempre que pronto.</li>
 *   <li>O resto sai por sorteio ponderado: quem luta de perto atrai ataques em área, quem luta de longe atrai
 *       ataques de longe (leitura com decaimento, como a do Monarca); logo depois do pisão os ataques de longe
 *       dobram de peso (o jogador foi lançado para longe).</li>
 *   <li>Mira à frente: a genki-dama e o maremoto apontam para onde o jogador vai estar, não onde está.</li>
 * </ol>
 *
 * <p><b>Vários jogadores</b>: (1) <i>foco rotativo</i> — cada ataque escolhe o jogador que está há mais tempo
 * sem ser alvo, o marca com chamas e um aviso; (2) <i>aglomerados</i> (todos a menos de 8 blocos) atraem
 * ataques em área (Punho, Pisão); (3) <i>dispersos</i> (alguém a mais de 24 blocos) atraem ataques de longe
 * (genki-dama, Maremoto) e o foco vai para o mais afastado; (4) o dano recebido cai 20% por jogador extra
 * (até 4 extras); (5) o raio troca de jogador a cada 2 s.
 *
 * <p><b>Movimento</b>: anda em linha reta até o alvo, sem o pathfinding vanilla (com a hitbox de 8 x 24 o
 * cálculo de rota ficaria caríssimo e quase sempre sem caminho). Serve para arena aberta.
 *
 * <p><b>Morte</b>: desintegra como os Monarcas. Quem estava perto ou lutou vê a cinemática final e, ao fim
 * dela, os créditos. Quem causou dano ganha a conquista do Soberano. A lista de quem lutou não é salva.
 * Se ele mata um jogador, ri por 4 s (com uma cinemática curta no rosto dele para quem está por perto).
 */
public final class SovereignBrain extends TierBrain {
    public static final int STAGES = 6;
    /** Estágio do poder do Rei Vulcânico: o único em que existe a genki-dama. */
    public static final int FIRE_STAGE = 2;
    /** Estágios do poder da água e do ar (esferas de água e tornados somem se ele sai deles, exceto no 6). */
    public static final int WATER_STAGE = 3, AIR_STAGE = 5;
    /** Duração da agonia (e do close) a cada estágio. */
    public static final int AGONY_TICKS = 80;
    /** Tempo ajoelhado depois do raio. */
    public static final int KNEEL_TICKS = 80;
    /** Poder despertado ao ENTRAR em cada estágio (índice = estágio - 1); o 6 desperta todos. */
    private static final ElementType[] AWAKENED = {
            ElementType.NONE, ElementType.FIRE, ElementType.WATER, ElementType.EARTH, ElementType.AIR, ElementType.NONE};
    private static final ElementType[] ALL = {
            ElementType.NONE, ElementType.FIRE, ElementType.WATER, ElementType.EARTH, ElementType.AIR};

    /** Alturas e recuos do modelo na escala 7 (blocos acima/à frente dos pés). */
    private static final double HAND_HEIGHT = 34.0, CHEST_HEIGHT = 15.3, CHEST_FORWARD = 3.9;
    /** Distância (centro a centro) abaixo da qual o jogador está "nos pés" dele: pisão ou punho têm prioridade. */
    private static final double CLOSE_RANGE = 9;
    /** Velocidade em que o ponto de mira do raio persegue o alvo (bloco/tick): corredor escapa, andando não. */
    private static final double BEAM_AIM_SPEED = 0.2;
    private static final DustParticleOptions BEAM_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.92F, 0.55F), 2.6F);

    private final ServerBossEvent bar;
    /** Jogadores que causaram dano nesta luta (UUID, para sobreviver a relog). */
    private final Set<UUID> fighters = new HashSet<>();
    /** Quando cada jogador foi o foco pela última vez (tick de jogo): quem está há mais tempo sem ser alvo é o próximo. */
    private final Map<UUID, Long> lastFocused = new HashMap<>();
    private LivingEntity focusTarget;
    private int stage;
    /** Ticks restantes da agonia (0 = lutando). */
    private int agonyTicks;
    /** Jogadores no alcance, e se estão juntos ou espalhados (recalculado a cada 0,5 s). */
    private int crowd = 1;
    private boolean clustered, spread;
    /** Como os jogadores lutam: golpes de perto (melee) e de longe (ranged), com decaimento. */
    private int meleePressure, rangedPressure;
    /** Depois do pisão, ataques de longe têm prioridade até este tick (o jogador foi jogado para longe). */
    private long comboUntil;
    /** Ticks seguidos com o alvo a mais de 22 blocos, e ticks seguidos sem sair do lugar tentando alcançá-lo. */
    private int farTicks, stuckTicks;
    private Vec3 lastChasePos;
    /** Ticks seguidos em que o jogador focado corre (sprint), e onde ele estava no tick anterior. */
    private int runTicks;
    private Vec3 lastTargetPos;
    /** Quem estava correndo no último tick (o alvo da Corrida Frenética). */
    private LivingEntity runnerRef;
    private ChestBeam beam;

    SovereignBrain(PathfinderMob mob) {
        super(mob, MobTier.BOSS, ElementType.NONE);
        // Chefes salvos antes desta versão ainda têm o contorno brilhante: apaga.
        mob.setGlowingTag(false);
        // noGravity é salvo no NBT: se o chunk descarregou no meio de um voo curto, ele não pode voltar flutuando.
        mob.setNoGravity(false);
        bar = new ServerBossEvent(mob.getDisplayName(), barColor(1), BossEvent.BossBarOverlay.NOTCHED_6);
        // REGRA: cada estágio (1 a 5) só usa os poderes do próprio elemento; no 6 todos valem. Ver StageMove.
        // Um Move roda por vez: nunca dois ao mesmo tempo.
        for (Move move : List.of(new ColossusFist(), new FireGenki(), new TidalWave(), new WaterBarrage(),
                new AncientRoots(), new Stomp(), new GreatLeap(), new Tornadoes(), new Hop(false))) {
            addMove(move);
            addRageMove(move);
        }
        // Só na Fúria: o recuo (voo curto, antes do raio) e o raio do peito, que tem prioridade sobre o resto.
        addRageMove(new Hop(true));
        addRageMove(new Frenzy());
        beam = new ChestBeam();
        addRageMove(beam);
        stage = stageFromHealth();
        bar.setColor(barColor(stage));
        if (stage == STAGES) resumeRage();
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
    /** No estágio 6 só há o raio: as recargas têm de ser lidas como estão (o ciclo raio, cansaço e caminhada). */
    @Override protected float cooldownScale() { return stage == STAGES ? 1.0F : 1.0F - 0.06F * (stage - 1); }
    /** A Fúria não começa pela vida: ela vem no fim da última agonia. */
    @Override protected float rageThreshold() { return 0F; }
    /** A bola de fogo devolvida o ajoelha mesmo na Fúria (senão rebatê-la não valeria nada, ele é imune a fogo). */
    @Override protected boolean allowsStunInRage() { return true; }
    @Override protected boolean endlessRage() { return true; }

    /** Vida mínima do estágio (o limite de baixo da faixa). */
    private float stageFloor(int s) {
        return mob.getMaxHealth() * (STAGES - s) / STAGES;
    }

    /** O primeiro estágio cuja faixa ainda contém a vida atual (o 6 se ela já passou de todos os limites). */
    private int stageFromHealth() {
        for (int s = 1; s < STAGES; s++) {
            if (mob.getHealth() > stageFloor(s) + 0.01F) return s;
        }
        return STAGES;
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

    /** Ajoelhado fica mais vulnerável; cada jogador extra na luta corta 20% do dano (até 4 extras). */
    @Override
    public float damageTakenMultiplier() {
        float group = 1F / (1F + 0.2F * Math.min(crowd - 1, 4));
        return (isStunned() ? 1.4F : super.damageTakenMultiplier()) * group;
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
        if (mob.tickCount % 10 == 0) scanCrowd();
        if (mob.tickCount % 100 == 0) {
            // A leitura do jogador "esquece" aos poucos: metade a cada 5 s.
            meleePressure /= 2;
            rangedPressure /= 2;
        }
        LivingEntity watched = mob.getTarget();
        farTicks = watched != null && watched.isAlive() && mob.distanceTo(watched) > 22 ? farTicks + 1 : 0;
        // O alvo está CORRENDO? Anda mais de 5 blocos/s (um sprint é ~5,6; andar, ~4,3), medido pela posição.
        LivingEntity runner = focusTarget != null && focusTarget.isAlive() ? focusTarget : watched;
        runnerRef = runner;
        if (runner != null && runner.isAlive()) {
            Vec3 now = runner.position();
            if (lastTargetPos != null && runner.level() == mob.level()) {
                runTicks = Math.hypot(now.x - lastTargetPos.x, now.z - lastTargetPos.z) > 0.25 ? runTicks + 1 : Math.max(0, runTicks - 2);
            }
            lastTargetPos = now;
        } else {
            runTicks = 0;
            lastTargetPos = null;
        }
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
            SkillVfx.ring(level(), mob.position().add(0, 0.1, 0), 6 + 0.5 * stage, Hazards.dust(e, 1.6F), 24 + 4 * stage);
        }
        updateBossBar(bar, 64);
    }

    @Override
    public void onHurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            fighters.add(player.getUUID());
            // Lê como lutam: golpe de longe (flecha, magia, bola) ou de perto.
            boolean ranged = source.getDirectEntity() != player || player.distanceTo(mob) > 8 + mob.getBbWidth() / 2;
            if (ranged) rangedPressure += 2;
            else meleePressure += 1;
        }
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
            for (LivingEntity e : enemiesWithin(14)) {
                Hazards.push(e, flatDirTo(e.position()).scale(1.4).add(0, 0.5, 0));
            }
            Hazards.add(new Hazards.Shockwave(level(), mob, power, mob.position(), 16, 0.6, baseDamage() * 0.6F, potency()));
        }
        if (agonyTicks == 0) endAgony();
    }

    private void endAgony() {
        invulnerable = false;
        if (stage == STAGES && !isRaging()) awaken();
        else anim(MobAnim.NONE, 0);
    }

    /** Fim da última agonia: liberta o poder dele e entra em Fúria até morrer. */
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

    // ------------------------------------------------------------------ vários jogadores

    /** Jogadores vivos e atacáveis a até 48 blocos. */
    private List<ServerPlayer> arena() {
        List<ServerPlayer> list = new ArrayList<>();
        for (ServerPlayer player : level().players()) {
            if (Hazards.isEnemy(mob, player) && player.distanceToSqr(mob) < 48 * 48) list.add(player);
        }
        return list;
    }

    private void scanCrowd() {
        List<ServerPlayer> players = arena();
        crowd = Math.max(1, players.size());
        clustered = spread = false;
        if (players.size() < 2) return;
        double widest = 0;
        boolean straggler = false;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).distanceToSqr(mob) > 24 * 24) straggler = true;
            for (int j = i + 1; j < players.size(); j++) widest = Math.max(widest, players.get(i).distanceTo(players.get(j)));
        }
        clustered = widest <= 8;
        spread = widest >= 24 || straggler;
    }

    /**
     * Escolhe o jogador que o próximo ataque vai focar. Sozinho: o único. Em grupo: quem está há mais tempo
     * sem ser alvo (foco rotativo); para ataque de longe com alguém afastado, o mais longe (nenhum canto é seguro).
     */
    private LivingEntity pickFocus(boolean ranged) {
        List<ServerPlayer> players = arena();
        if (players.isEmpty()) return target;
        if (players.size() == 1) return players.get(0);
        if (ranged) {
            ServerPlayer farthest = players.get(0);
            for (ServerPlayer p : players) if (p.distanceToSqr(mob) > farthest.distanceToSqr(mob)) farthest = p;
            if (farthest.distanceToSqr(mob) > 24 * 24) return farthest;
        }
        ServerPlayer next = players.get(0);
        for (ServerPlayer p : players) {
            if (lastFocused.getOrDefault(p.getUUID(), 0L) < lastFocused.getOrDefault(next.getUUID(), 0L)) next = p;
        }
        return next;
    }

    /** Avisa o jogador (e só quando há mais de um): chamas sobem da cabeça dele e aparece um aviso. */
    private void markFocus(LivingEntity chosen) {
        focusTarget = chosen;
        lastFocused.put(chosen.getUUID(), now());
        if (crowd < 2 || !(chosen instanceof ServerPlayer player)) return;
        player.displayClientMessage(Component.translatable("message.solomod.sovereign_focus")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), true);
        Hazards.farParticles(level(), ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 2.4, player.getZ(),
                14, 0.25, 0.8, 0.25, 0.02);
    }

    /** Foco do ataque em curso (cai para o alvo normal se ele morreu ou sumiu). */
    private LivingEntity focus() {
        return focusTarget != null && focusTarget.isAlive() && isValidTarget(focusTarget) ? focusTarget : target;
    }

    /** Todos os jogadores da arena (até 4, o foco primeiro), ou só o alvo se não houver nenhum. */
    private List<LivingEntity> victims() {
        List<LivingEntity> list = new ArrayList<>();
        LivingEntity f = focus();
        if (f != null) list.add(f);
        for (ServerPlayer p : arena()) {
            if (list.size() >= 4) break;
            if (p != f) list.add(p);
        }
        return list;
    }

    @Override
    protected void force(Move move) {
        if (move instanceof StageMove staged) markFocus(pickFocus(staged.style == Style.RANGED));
        super.force(move);
    }

    // ------------------------------------------------------------------ combate básico

    /** Anda em linha reta (sem pathfinding): para encostado no alvo em vez de empurrá-lo. */
    @Override
    protected void chase(double speed) {
        if (mob.isWithinMeleeAttackRange(target)) {
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0);
            stuckTicks = 0;
            return;
        }
        // Preso: andando para o alvo, mas quase não sai do lugar (parede, morro). Depois de 2 s, voa por cima.
        Vec3 pos = mob.position();
        stuckTicks = lastChasePos != null && pos.distanceToSqr(lastChasePos) < 0.0036 ? stuckTicks + 1 : 0;
        lastChasePos = pos;
        mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), speed);
    }

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

    /** Risada de vitória: close curto no rosto dele para quem está por perto (e para o jogador que caiu). */
    @Override
    protected void onVictoryLaugh() {
        CinematicPayload laugh = new CinematicPayload(mob.getId(), TierIdentity.BOSS_LAUGH_TICKS, CinematicPayload.Kind.LAUGH,
                Component.translatable("cinematic.solomod.laugh"), Component.translatable("cinematic.solomod.laugh.line"));
        for (ServerPlayer player : audience()) ModNetwork.sendCinematic(player, laugh);
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

    /** Como o ataque reage aos padrões de grupo (ver a Javadoc da classe). */
    private enum Style { AREA, RANGED, NEUTRAL }

    /**
     * Poder de um estágio. REGRA: cada estágio representa um elemento e só usa os poderes DELE
     * ({@code ownStage}); nada se acumula. Só o último estágio (6) libera todos de uma vez (um por vez).
     * O foco do jogador é escolhido em {@link #force}.
     */
    private abstract class StageMove extends Move {
        private final int ownStage;
        final Style style;

        StageMove(int ownStage, int cooldown, int weight, Style style) {
            super(cooldown, weight);
            this.ownStage = ownStage;
            this.style = style;
        }

        @Override
        protected final boolean usable(double dist) {
            return (stage == ownStage || stage == STAGES) && inRange(dist);
        }

        protected boolean inRange(double dist) { return true; }

        /**
         * Peso do sorteio: quem luta de perto atrai ataques em área, quem luta de longe atrai ataques de longe;
         * grupo junto dobra a área, grupo espalhado (ou logo depois do pisão) dobra o de longe.
         */
        @Override
        protected int weight(double dist) {
            int w = weight;
            if (style == Style.AREA) w += meleePressure;
            if (style == Style.RANGED) w += rangedPressure / 2;
            if (style == Style.AREA && clustered) w *= 2;
            if (style == Style.RANGED && (spread || now() < comboUntil)) w *= 2;
            return w;
        }
    }

    /** Ponto acima das mãos erguidas (na escala 7 os braços esticados chegam a ~34 blocos). */
    private Vec3 hands() {
        return mob.position().add(0, HAND_HEIGHT, 0);
    }

    /** Centro do peito (de onde sai o raio), um pouco à frente do corpo. */
    private Vec3 chestOrigin() {
        return mob.position().add(Vec3.directionFromRotation(0F, mob.yBodyRot).scale(CHEST_FORWARD)).add(0, CHEST_HEIGHT, 0);
    }

    /** Punho do Colosso: 20 ticks erguendo o punho, esmaga e solta onda(s) de choque. */
    private final class ColossusFist extends StageMove {
        ColossusFist() { super(1, 80, 5, Style.AREA); }

        @Override
        protected boolean inRange(double dist) {
            return dist < 12;
        }

        /** Colado nos pés dele: o punho é a resposta (no último estágio, o pisão vem antes). */
        @Override
        protected int urgency(double dist) {
            return dist < CLOSE_RANGE ? 4 : 0;
        }

        @Override
        protected void begin() {
            anim(MobAnim.WINDUP, 20);
            sound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < 20) {
                face(flatDirTo(focus().position()));
                particles(ParticleTypes.CRIT, center().add(0, mob.getBbHeight() * 0.4, 0), 3, 0.5, 0.1);
                return true;
            }
            if (t == 20) {
                anim(MobAnim.SLAM, 12);
                for (LivingEntity e : enemiesWithin(3 + mob.getBbWidth() / 2)) strike(e, baseDamage() * 1.5F, false);
                // O soco joga para longe quem está colado.
                for (LivingEntity e : enemiesWithin(CLOSE_RANGE)) {
                    Hazards.push(e, flatDirTo(e.position()).scale(1.1).add(0, 0.4, 0));
                }
                ModNetwork.sendShake(level(), mob.position(), 40, 0.5F, 14);
            }
            if (t == 20 || (t == 32 && stage >= 4)) {
                sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.5F, 0.5F);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.6F);
                particles(ParticleTypes.EXPLOSION, mob.position(), 4, 1.5, 0);
                Hazards.add(new Hazards.Shockwave(level(), mob, ElementType.NONE, mob.position(), 14, 0.55, baseDamage() * 0.9F, 0));
            }
            return t < 36;
        }
    }

    /**
     * Genki-dama (Rei Vulcânico, só no estágio 2): une os braços acima da cabeça e carrega uma esfera de fogo
     * por 3,5 s; no fim a joga para baixo, sobre o jogador focado. Telegrafada pelo tamanho da esfera.
     */
    private final class FireGenki extends StageMove {
        private static final int CHARGE = 70, TOTAL = 100;

        FireGenki() { super(FIRE_STAGE, 150, 7, Style.RANGED); }

        @Override
        protected boolean inRange(double dist) {
            return dist > TierIdentity.ROYAL_FIREBALL_MIN_RANGE && dist < 48;
        }

        @Override
        protected void begin() {
            anim(MobAnim.GENKI, TOTAL);
            sound(SoundEvents.GHAST_WARN, 3.0F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            // O ponto de lançamento é fixo acima das mãos; o fogo e os anéis têm de ser vistos de longe.
            if (stage != FIRE_STAGE && stage != STAGES) return false;
            LivingEntity f = focus();
            Vec3 hands = hands();
            if (t < CHARGE) {
                face(flatDirTo(f.position()));
                double r = 0.6 + 3.2 * t / CHARGE;
                Hazards.farParticles(level(), ParticleTypes.FLAME, hands.x, hands.y, hands.z, 10, r * 0.5, r * 0.5, r * 0.5, 0.03);
                if (t % 2 == 0) {
                    // Esfera: três anéis (equador e dois paralelos) do raio atual.
                    for (double dy : new double[]{-r * 0.6, 0, r * 0.6}) {
                        double rr = Math.sqrt(Math.max(0, r * r - dy * dy));
                        for (int k = 0; k < 18; k++) {
                            double a = Math.PI * 2 * k / 18 + t * 0.2;
                            Hazards.farParticles(level(), Hazards.dust(ElementType.FIRE, 2.2F),
                                    hands.x + Math.cos(a) * rr, hands.y + dy, hands.z + Math.sin(a) * rr, 1, 0, 0, 0, 0);
                        }
                    }
                }
                if (t % 4 == 0) Hazards.farParticles(level(), ParticleTypes.LAVA, hands.x, hands.y, hands.z, 2, r * 0.5, r * 0.5, r * 0.5, 0);
                if (t % 12 == 0) sound(SoundEvents.FIRE_AMBIENT, 3.0F, 0.5F);
                return true;
            }
            if (t == CHARGE) {
                // Mira onde o jogador vai estar em ~0,6 s (a bola cai a 0,7 bloco/tick).
                Vec3 aim = f.position().add(f.getDeltaMovement().multiply(1, 0, 1).scale(12)).add(0, 0.6, 0);
                RoyalFireball.launchGenki(level(), mob, hands, aim, baseDamage() * TierIdentity.GENKI_DAMAGE_MULTIPLIER,
                        TierIdentity.GENKI_SPEED, TierIdentity.GENKI_BLAST_RADIUS);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.7F);
            }
            return t < TOTAL;
        }
    }

    /** Maremoto (Leviatã Coroado): 20 ticks mostrando a faixa, depois uma onda reta de 7 de largura. */
    private final class TidalWave extends StageMove {
        private static final int AIM = 20, LENGTH = 24;
        private static final double HALF_WIDTH = 3.5;
        private final Set<Integer> hit = new HashSet<>();
        private Vec3 dir;

        TidalWave() { super(3, 160, 4, Style.RANGED); }

        @Override
        protected boolean inRange(double dist) {
            return dist < LENGTH + 4;
        }

        @Override
        protected void begin() {
            hit.clear();
            // Mira onde o jogador estará quando a faixa fechar (~0,5 s), não onde ele estava.
            LivingEntity f = focus();
            dir = flatDirTo(f.position().add(f.getDeltaMovement().multiply(1, 0, 1).scale(10)));
            face(dir);
            anim(MobAnim.WINDUP, AIM);
            sound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.5F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            Vec3 side = new Vec3(-dir.z, 0, dir.x);
            // A faixa e a onda saem da borda da caixa (4 blocos), não dos pés dele.
            Vec3 start = mob.position().add(dir.scale(mob.getBbWidth() / 2));
            if (t < AIM) {
                face(dir);
                if (t % 2 == 0) {
                    for (double d = 2; d < LENGTH; d += 1.5) {
                        Vec3 p = start.add(dir.scale(d)).add(0, 0.15, 0);
                        for (double s : new double[]{-HALF_WIDTH, HALF_WIDTH}) {
                            Vec3 q = p.add(side.scale(s));
                            level().sendParticles(Hazards.dust(ElementType.WATER, 1.2F), q.x, q.y, q.z, 1, 0, 0, 0, 0);
                        }
                    }
                }
                return true;
            }
            if (t == AIM) {
                // A crista vira cinco esferas de água rolando juntas (só visual; o dano é calculado aqui).
                for (int k = -2; k <= 2; k++) {
                    WaterOrb.wave(level(), mob, start.add(side.scale(k * 1.7)).add(0, 1.4, 0), dir.scale(1.1), 24);
                }
                anim(MobAnim.SLAM, 10);
                sound(SoundEvents.GENERIC_SPLASH, 3.0F, 0.5F);
                sound(SoundEvents.ELDER_GUARDIAN_AMBIENT, 2.0F, 0.6F);
            }
            double front = (t - AIM) * 1.1;
            Vec3 crest = start.add(dir.scale(front));
            for (double s = -HALF_WIDTH; s <= HALF_WIDTH; s += 0.7) {
                Vec3 q = crest.add(side.scale(s));
                level().sendParticles(ParticleTypes.SPLASH, q.x, q.y + 0.5, q.z, 4, 0.2, 0.6, 0.2, 0.1);
                level().sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, q.x, q.y + 0.2, q.z, 2, 0.2, 0.4, 0.2, 0.1);
            }
            for (LivingEntity e : enemiesWithin(LENGTH + 6)) {
                Vec3 rel = e.position().subtract(start);
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
        AncientRoots() { super(4, 150, 4, Style.NEUTRAL); }

        @Override
        protected void begin() {
            anim(MobAnim.WINDUP, 14);
            sound(SoundEvents.ROOTED_DIRT_BREAK, 2.5F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            if (t == 14 || t == 34) {
                if (t == 14) anim(MobAnim.SLAM, 10);
                for (LivingEntity v : victims()) blast(ElementType.EARTH, v.position(), 2.0, t == 14 ? 22 : 18, 1.3F);
                sound(SoundEvents.EVOKER_FANGS_ATTACK, 2.0F, 0.6F);
                particles(ParticleTypes.COMPOSTER, mob.position(), 30, 2.0, 0.1);
            }
            return t < 40;
        }
    }

    /**
     * Pisão (modo terra): ergue a perna e esmaga o chão. Tem prioridade quando o jogador está colado nos pés
     * dele. Um terremoto forte: arremessa para longe (mais forte quanto mais perto), tremor de tela, anel de
     * choque de 14 blocos (pule) e os blocos do chão por onde o anel passa dão um salto e voltam.
     */
    private final class Stomp extends StageMove {
        private static final int WINDUP = 24, TOTAL = 40;
        private static final double QUAKE_RADIUS = 14;

        Stomp() { super(4, 110, 6, Style.AREA); }

        @Override
        protected boolean inRange(double dist) {
            return dist < 18;
        }

        @Override
        protected int urgency(double dist) {
            return dist < CLOSE_RANGE ? 6 : 0;
        }

        @Override
        protected void begin() {
            anim(MobAnim.STOMP, TOTAL);
            sound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            if (t < WINDUP) {
                face(flatDirTo(focus().position()));
                if (t % 3 == 0) particles(ParticleTypes.POOF, mob.position(), 6, 2.5, 0.02);
                return true;
            }
            if (t == WINDUP) {
                sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 3.0F, 0.4F);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 2.5F, 0.4F);
                particles(ParticleTypes.EXPLOSION, mob.position(), 8, 4.0, 0);
                particles(ParticleTypes.POOF, mob.position(), 80, 5.0, 0.12);
                for (LivingEntity e : enemiesWithin(7)) strike(e, baseDamage() * 1.1F, false);
                // Arremesso: de 0,7 (na borda do terremoto) a 2,0 (nos pés) de velocidade horizontal.
                for (LivingEntity e : enemiesWithin(QUAKE_RADIUS)) {
                    double d = Math.sqrt(e.position().distanceToSqr(mob.position()));
                    double push = 0.7 + 1.3 * (1 - Math.min(1, d / QUAKE_RADIUS));
                    Hazards.push(e, flatDirTo(e.position()).scale(push).add(0, 0.55, 0));
                }
                Hazards.add(new Hazards.Shockwave(level(), mob, ElementType.EARTH, mob.position(), QUAKE_RADIUS, 0.5,
                        baseDamage() * 0.9F, potency()));
                Hazards.add(new Hazards.QuakeBlocks(level(), mob, mob.position(), QUAKE_RADIUS, 0.5));
                ModNetwork.sendShake(level(), mob.position(), 48, 1.0F, 30);
            }
            return t < TOTAL;
        }

        /** Quem foi jogado para longe está à distância de um ataque de longe: ele ganha prioridade um instante. */
        @Override
        protected void end() {
            comboUntil = now() + 80;
        }
    }

    /**
     * Tornados (Imperador dos Céus): flutua de braços abertos e chama 4 tornados, distribuídos entre os
     * jogadores, que os perseguem e os jogam para o alto (dano de queda).
     */
    private final class Tornadoes extends StageMove {
        private static final int RISE = 40, TOTAL = 100;

        Tornadoes() { super(5, 220, 6, Style.NEUTRAL); }

        @Override
        protected void begin() {
            anim(MobAnim.FLOAT, TOTAL);
            sound(SoundEvents.WIND_CHARGE_BURST.value(), 3.0F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            face(flatDirTo(focus().position()));
            if (t < RISE) {
                if (t % 2 == 0) SkillVfx.ring(level(), mob.position().add(0, 0.2, 0), 4 + t * 0.2, Hazards.dust(ElementType.AIR, 1.8F), 28);
                particles(ParticleTypes.CLOUD, mob.position(), 8, 3.0, 0.05);
                if (t % 10 == 0) sound(SoundEvents.PHANTOM_FLAP, 3.0F, 0.5F);
                return true;
            }
            if (t == RISE) {
                List<LivingEntity> chosen = victims();
                for (int i = 0; i < 4; i++) {
                    LivingEntity v = chosen.get(i % chosen.size());
                    double a = Math.PI / 2 * i + random.nextDouble() * 0.6;
                    Vec3 start = mob.position().add(Math.cos(a) * 10, 0, Math.sin(a) * 10);
                    SovereignTornado.spawn(level(), mob, v, start, baseDamage() * 0.3F);
                }
                sound(SoundEvents.ELDER_GUARDIAN_CURSE, 2.0F, 0.5F);
            }
            return t < TOTAL;
        }
    }

    /**
     * Raio do Peito (estágio 6, só dele): 1,5 s de carga com energia convergindo no peito e 6 s de raio que
     * desce até o jogador focado. O ponto de mira persegue o alvo a 0,2 bloco/tick (quem corre escapa) e o
     * foco troca de jogador a cada 2 s. Depois ele ajoelha, cansado, por 4 s.
     */
    private final class ChestBeam extends Move {
        private static final int CHARGE = 30, FIRE = 120, SWITCH = 40;
        private Vec3 aim;
        private boolean fired;

        ChestBeam() { super(480, 10); }

        @Override
        protected boolean usable(double dist) {
            return dist < 60;
        }

        /** O golpe característico dele: sempre que está pronto, vem antes dos poderes dos Monarcas. */
        @Override
        protected int urgency(double dist) {
            return 3;
        }

        @Override
        protected void begin() {
            fired = false;
            markFocus(pickFocus(true));
            aim = focus().position();
            anim(MobAnim.BEAM, CHARGE + FIRE);
            sound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 0.5F);
            sound(SoundEvents.BEACON_ACTIVATE, 3.0F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            Vec3 origin = chestOrigin();
            if (t >= CHARGE + SWITCH && (t - CHARGE) % SWITCH == 0 && crowd > 1) markFocus(pickFocus(false));
            LivingEntity f = focus();
            if (t < CHARGE) {
                aim = f.position();
            } else {
                Vec3 to = f.position().subtract(aim);
                aim = aim.add(to.lengthSqr() > BEAM_AIM_SPEED * BEAM_AIM_SPEED ? to.normalize().scale(BEAM_AIM_SPEED) : to);
            }
            face(flatDirTo(aim));

            if (t < CHARGE) {
                double r = 0.5 + 5 * (1 - t / (double) CHARGE);
                Hazards.farParticles(level(), ParticleTypes.END_ROD, origin.x, origin.y, origin.z, 8, r, r, r, 0.15);
                Hazards.farParticles(level(), BEAM_DUST, origin.x, origin.y, origin.z, 4, r * 0.5, r * 0.5, r * 0.5, 0);
                return true;
            }
            if (t == CHARGE) {
                fired = true;
                sound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 0.7F);
            }
            Vec3 dir = aim.add(0, 0.9, 0).subtract(origin).normalize();
            BlockHitResult wall = level().clip(new ClipContext(origin, origin.add(dir.scale(64)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
            Vec3 end = wall.getLocation();
            double length = end.distanceTo(origin);
            // Um ponto a cada 1,8 bloco (com várias partículas cada) mantém o raio denso sem inundar a rede.
            for (double d = 0; d < length; d += 1.8) {
                Vec3 p = origin.add(dir.scale(d));
                Hazards.farParticles(level(), ParticleTypes.END_ROD, p.x, p.y, p.z, 3, 0.2, 0.2, 0.2, 0);
                Hazards.farParticles(level(), BEAM_DUST, p.x, p.y, p.z, 2, 0.35, 0.35, 0.35, 0);
            }
            Hazards.farParticles(level(), ParticleTypes.END_ROD, end.x, end.y + 0.3, end.z, 10, 0.8, 0.4, 0.8, 0.2);
            if (t % 2 == 0) Hazards.farParticles(level(), ParticleTypes.LARGE_SMOKE, end.x, end.y + 0.3, end.z, 3, 0.6, 0.2, 0.6, 0.02);
            if (t % 5 == 0) Hazards.farParticles(level(), ParticleTypes.EXPLOSION, end.x, end.y + 0.5, end.z, 1, 0.6, 0.1, 0.6, 0);
            if (t % 10 == 0) {
                sound(SoundEvents.BEACON_AMBIENT, 3.0F, 1.2F);
                Hazards.sound(level(), end, SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 1.4F);
            }
            // Dano a cada 4 ticks em quem estiver a até ~1 bloco do raio.
            if ((t - CHARGE) % 4 == 0) {
                for (LivingEntity e : enemiesWithin(70)) {
                    if (e.getBoundingBox().inflate(1.0).clip(origin, end).isPresent()) {
                        Hazards.hit(mob, e, baseDamage() * 0.22F, ElementType.NONE, 0, true);
                    }
                }
            }
            return t < CHARGE + FIRE;
        }

        /** Só ajoelha se o raio chegou a disparar (um raio cancelado na carga não cansa). */
        @Override
        protected void end() {
            if (fired) kneel(KNEEL_TICKS);
        }
    }

    /**
     * Voo curto (algumas vezes por luta, não sempre): ele sobe, atravessa o ar de costas ou de frente e pousa.
     * <ul>
     *   <li>Recuo (só na Fúria): se o jogador está perto e o raio do peito vai ficar pronto, afasta-se uns 18
     *       blocos para ter espaço para o raio. Recarga de 22 s.</li>
     *   <li>Avanço: se o alvo ficou a mais de 22 blocos por 5 s (kitando) ou ele está preso no terreno por 2 s,
     *       voa até perto dele. Recarga de 12 s.</li>
     * </ul>
     * Tem prioridade sobre o sorteio quando as condições valem (mas só o pisão a supera). O pouso treme a
     * tela e solta um anel de choque pequeno.
     */
    private final class Hop extends Move {
        private static final int RISE = 12, CRUISE = 24;
        private final boolean back;
        private Vec3 dir = Vec3.ZERO;
        private double speed;
        private boolean flew;

        Hop(boolean back) {
            super(back ? 450 : 240, 1);
            this.back = back;
        }

        @Override
        protected boolean usable(double dist) {
            if (back) {
                return isRaging() && dist < 13 && beam != null && beam.readyAt - now() <= 80 && random.nextFloat() < 0.6F;
            }
            return (farTicks >= 100 || stuckTicks >= 40) && dist > 10 && dist < 60;
        }

        @Override
        protected int urgency(double dist) {
            return 5;
        }

        @Override
        protected void begin() {
            Vec3 toTarget = flatDirTo(target.position());
            double goal = back ? 18 : Mth.clamp(mob.distanceTo(target) - 8, 6, 24);
            dir = back ? toTarget.scale(-1) : toTarget;
            speed = Math.min(0.9, goal / (CRUISE + 6.0));
            flew = false;
            farTicks = 0;
            stuckTicks = 0;
            mob.setNoGravity(true);
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0);
            anim(MobAnim.HOP, RISE + CRUISE + 8);
            sound(SoundEvents.WIND_CHARGE_BURST.value(), 3.0F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            mob.getNavigation().stop();
            mob.setZza(0);
            mob.setXxa(0);
            mob.setSpeed(0);
            face(flatDirTo(target.position()));
            // Sobe (4,8 blocos, passa por morros), cruza sem cair e desce reto até pousar.
            double vy = t < RISE ? 0.4 : t < RISE + CRUISE ? 0.0 : -0.5;
            double vh = t >= 4 && t < RISE + CRUISE ? speed : 0;
            mob.setDeltaMovement(dir.x * vh, vy, dir.z * vh);
            flew |= t > RISE;
            if (t % 3 == 0) particles(ParticleTypes.CLOUD, mob.position(), 6, 3.0, 0.02);
            return t < 80 && !(t > RISE + CRUISE && mob.onGround());
        }

        /** Devolve a gravidade sempre (inclusive se interrompido) e, se pousou, treme o chão. */
        @Override
        protected void end() {
            mob.setNoGravity(false);
            if (!flew || !mob.onGround()) return;
            sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.5F, 0.6F);
            particles(ParticleTypes.POOF, mob.position(), 40, 3.5, 0.08);
            Hazards.add(new Hazards.Shockwave(level(), mob, ElementType.EARTH, mob.position(), 9, 0.5, baseDamage() * 0.5F, 0));
            ModNetwork.sendShake(level(), mob.position(), 40, 0.5F, 14);
        }
    }

    /**
     * Esferas de Água (Leviatã Coroado): ergue os braços e 5 esferas de água (3D) surgem girando em volta dele a
     * ~12 blocos de altura; depois de 2,5 s cada uma persegue um jogador (distribuídas entre todos) e explode em
     * respingo. Dá para desviar de lado ou estourar a esfera com um golpe direto.
     */
    private final class WaterBarrage extends StageMove {
        private static final int ORBIT = 50, TOTAL = 90;

        WaterBarrage() { super(3, 170, 5, Style.RANGED); }

        @Override
        protected boolean inRange(double dist) {
            return dist > 6 && dist < 48;
        }

        @Override
        protected void begin() {
            anim(MobAnim.CAST, TOTAL);
            sound(SoundEvents.GENERIC_SPLASH, 3.0F, 0.5F);
        }

        @Override
        protected boolean tick(int t) {
            face(flatDirTo(focus().position()));
            if (t == 10) {
                List<LivingEntity> chosen = victims();
                int count = 5;
                for (int i = 0; i < count; i++) {
                    WaterOrb.orbit(level(), mob, chosen.get(i % chosen.size()), Math.PI * 2 * i / count, ORBIT, baseDamage() * 1.2F);
                }
                sound(SoundEvents.ELDER_GUARDIAN_CURSE, 2.0F, 0.6F);
            }
            return t < TOTAL;
        }
    }

    /**
     * Pulão: agacha, salta (só o bastante para sair do chão, ~3 blocos) em direção ao jogador focado e cai
     * esmagando o chão. É o pisão mais forte: terremoto de 22 blocos que arremessa todos para longe, tremor
     * de tela maior e até 260 blocos do chão saltando. Só a certa distância (colado, ele pisa).
     */
    private final class GreatLeap extends StageMove {
        private static final int WINDUP = 14;
        /** Velocidade vertical do salto: sobe ~3 blocos. */
        private static final double V0 = 0.7;
        private static final double QUAKE_RADIUS = 22;
        private Vec3 dir = Vec3.ZERO;
        private double hspeed;

        GreatLeap() { super(4, 200, 5, Style.AREA); }

        @Override
        protected boolean inRange(double dist) {
            return dist > CLOSE_RANGE && dist < 32;
        }

        @Override
        protected void begin() {
            anim(MobAnim.JUMP, 50);
            sound(SoundEvents.RAVAGER_ROAR, 2.5F, 0.4F);
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0);
        }

        @Override
        protected boolean tick(int t) {
            mob.getNavigation().stop();
            mob.setZza(0);
            mob.setXxa(0);
            mob.setSpeed(0);
            LivingEntity f = focus();
            if (t < WINDUP) {
                face(flatDirTo(f.position()));
                if (t % 3 == 0) particles(ParticleTypes.POOF, mob.position(), 8, 3.0, 0.03);
                return true;
            }
            if (t == WINDUP) {
                dir = flatDirTo(f.position());
                // ~18 ticks no ar: aterrissa perto do jogador (a 4 blocos dele, na borda da caixa).
                hspeed = Mth.clamp((mob.distanceTo(f) - 4) / 18.0, 0.2, 0.85);
                mob.setDeltaMovement(dir.x * hspeed, V0, dir.z * hspeed);
                mob.hasImpulse = true;
                sound(SoundEvents.WIND_CHARGE_BURST.value(), 3.0F, 0.5F);
                particles(ParticleTypes.EXPLOSION, mob.position(), 4, 2.5, 0);
                return true;
            }
            // O atrito do ar comeria o avanço horizontal: reaplica a cada tick até pousar.
            if (!mob.onGround()) {
                Vec3 v = mob.getDeltaMovement();
                mob.setDeltaMovement(dir.x * hspeed, v.y, dir.z * hspeed);
                return t < 80;
            }
            if (t <= WINDUP + 3) return true;
            impact();
            return false;
        }

        private void impact() {
            sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 3.0F, 0.3F);
            sound(SoundEvents.GENERIC_EXPLODE.value(), 3.0F, 0.35F);
            particles(ParticleTypes.EXPLOSION_EMITTER, mob.position(), 2, 1.5, 0);
            particles(ParticleTypes.POOF, mob.position(), 120, 7.0, 0.15);
            for (LivingEntity e : enemiesWithin(9)) strike(e, baseDamage() * 1.4F, false);
            // Arremesso de 0,8 (na borda) a 2,4 (nos pés) de velocidade horizontal.
            for (LivingEntity e : enemiesWithin(QUAKE_RADIUS)) {
                double d = Math.sqrt(e.position().distanceToSqr(mob.position()));
                double push = 0.8 + 1.6 * (1 - Math.min(1, d / QUAKE_RADIUS));
                Hazards.push(e, flatDirTo(e.position()).scale(push).add(0, 0.65, 0));
            }
            Hazards.add(new Hazards.Shockwave(level(), mob, ElementType.EARTH, mob.position(), QUAKE_RADIUS, 0.6,
                    baseDamage() * 1.1F, potency()));
            Hazards.add(new Hazards.QuakeBlocks(level(), mob, mob.position(), QUAKE_RADIUS, 0.6, 260));
            ModNetwork.sendShake(level(), mob.position(), 64, 1.7F, 44);
            comboUntil = now() + 80;
        }
    }

    /**
     * Corrida Frenética (só na Fúria): se o jogador focado vem CORRENDO há 1,5 s, o Soberano o persegue numa corrida
     * absurda, a ~10 blocos/s (mais rápida que um sprint), com o corpo todo inclinado e braços e pernas num
     * exagero, cada pegada sacudindo a tela. Ao alcançá-lo, desfere um golpe pesado que o joga longe; se não
     * alcança em 4,5 s ou empaca no terreno, desiste. Recarga de 18 s.
     */
    private final class Frenzy extends Move {
        private static final int TOTAL = 90;
        private Vec3 checkpoint = Vec3.ZERO;

        Frenzy() { super(360, 1); }

        @Override
        protected boolean usable(double dist) {
            return isRaging() && runTicks >= 30 && dist > 12 && dist < 55;
        }

        @Override
        protected int urgency(double dist) {
            return 4;
        }

        @Override
        protected void begin() {
            // Persegue quem foi visto correndo (não o último foco de outro ataque).
            if (runnerRef != null && runnerRef.isAlive()) focusTarget = runnerRef;
            runTicks = 0;
            checkpoint = mob.position();
            anim(MobAnim.SPRINT, TOTAL);
            sound(SoundEvents.RAVAGER_ROAR, 3.0F, 0.4F);
            sound(SoundEvents.WARDEN_ROAR, 3.0F, 0.6F);
        }

        @Override
        protected boolean tick(int t) {
            LivingEntity f = focus();
            if (f == null || !isValidTarget(f)) return false;
            face(flatDirTo(f.position()));
            mob.getMoveControl().setWantedPosition(f.getX(), f.getY(), f.getZ(), 2.1);
            if (t % 5 == 0) {
                sound(SoundEvents.RAVAGER_STEP, 3.0F, 0.5F);
                particles(ParticleTypes.POOF, mob.position(), 10, 3.0, 0.05);
                ModNetwork.sendShake(level(), mob.position(), 40, 0.3F, 8);
            }
            if (mob.isWithinMeleeAttackRange(f)) {
                anim(MobAnim.SLAM, 12);
                strike(f, baseDamage() * 1.8F, false);
                Hazards.push(f, flatDirTo(f.position()).scale(1.9).add(0, 0.6, 0));
                sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 3.0F, 0.5F);
                ModNetwork.sendShake(level(), mob.position(), 40, 0.8F, 18);
                return false;
            }
            // Empacou (parede, morro): a cada 1 s precisa ter andado pelo menos 4 blocos.
            if (t > 0 && t % 20 == 0) {
                if (mob.position().distanceToSqr(checkpoint) < 16) return false;
                checkpoint = mob.position();
            }
            return t < TOTAL;
        }

        /** Para de correr quando acaba (senão o MoveControl continuaria empurrando para o último destino). */
        @Override
        protected void end() {
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0);
        }
    }

    private void blast(ElementType e, Vec3 at, double radius, int delay, float damageMult) {
        Hazards.add(new Hazards.Blast(level(), mob, e, at, radius, delay, baseDamage() * damageMult, potency()));
    }
}
