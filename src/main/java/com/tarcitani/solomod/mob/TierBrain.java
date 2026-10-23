package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.attachment.MobProfile;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.Sovereign;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

/**
 * Cérebro de combate de um mob do mod. Um {@link Goal} de prioridade máxima que, enquanto há alvo,
 * substitui a IA de combate vanilla (movimento, olhar, golpe) pela do tier:
 *
 * <ul>
 *   <li>{@link MinionBrain} (NORMAL): covarde e em bando — chama aliados, foge com pouca vida, dá botes.</li>
 *   <li>{@link EliteBrain} (OP): duelista — circula o alvo, apara golpes e contra-ataca, investidas e combos.</li>
 *   <li>{@link GuardianBrain} (SUB_BOSS): mago tático — mantém distância, orbes, repulsão, salto e teleporte.</li>
 *   <li>{@link MonarchBrain} (BOSS): três fases, lê o estilo do jogador e escolhe ataques contra ele.</li>
 *   <li>{@link SovereignBrain} (BOSS, só a entidade {@link Sovereign}): chefe final com 6 estágios.</li>
 * </ul>
 *
 * <p>Ataques especiais são {@link Move}s: cada um tem recarga, peso e condição de uso; quando nenhum está
 * pronto, o cérebro executa {@link #engage()} (posicionamento + golpe básico próprio do tier).
 *
 * <p><b>Fúria</b> (Guardião e Monarca): ao chegar a 1/4 da vida o chefe ruge (invulnerável), arremessa
 * quem está perto, chama Lacaios e entra em Fúria por {@link #RAGE_TICKS} ticks: aura vermelha, um conjunto
 * de ataques TOTALMENTE diferente ({@link #addRageMove}), +35% de dano e -35% de dano recebido.
 * Quando acaba, fica exausto por 3 s (recebe +25% de dano) e volta aos ataques normais. Subclasses podem
 * mudar o gatilho ({@link #rageThreshold}), começar a Fúria por conta própria ({@link #beginRage}) e torná-la
 * sem fim ({@link #endlessRage}), como o Soberano.
 * Stun deixa o chefe agachado se recuperando (5 s para bolas devolvidas), mas nunca entra durante
 * a Fúria ou transições invulneráveis. Toda entrada em Fúria emite um grito, repetido a cada 5 s.
 * Monarcas, Soberano e Guardiões que matam um jogador pausam para rir por 4 s; a comemoração
 * aguarda o fim de uma transição invulnerável e é simulada mesmo sem alvo vivo.
 *
 * <p>Fora de combate os goals vanilla (vagar, olhar ao redor) continuam funcionando.
 */
public abstract class TierBrain extends Goal {
    private static final Map<PathfinderMob, TierBrain> BRAINS = new WeakHashMap<>();

    // ------------------------------------------------------------------ instalação

    /** Instala (ou reinstala) o cérebro do tier do perfil do mob. */
    public static void install(PathfinderMob mob) {
        uninstall(mob);
        if (!mob.hasData(ModAttachments.MOB_PROFILE)) return;
        MobProfile profile = mob.getData(ModAttachments.MOB_PROFILE);
        TierBrain brain = switch (profile.tier()) {
            case NORMAL -> new MinionBrain(mob, profile.element());
            case OP -> new EliteBrain(mob, profile.element());
            case SUB_BOSS -> new GuardianBrain(mob, profile.element());
            case BOSS -> mob instanceof Sovereign ? new SovereignBrain(mob) : new MonarchBrain(mob, profile.element());
        };
        mob.goalSelector.addGoal(0, brain);
        // Todo mob do mod é hostil ao jogador, mesmo que o mob vanilla seja passivo.
        mob.targetSelector.addGoal(0, new RetaliateGoal(mob));
        mob.targetSelector.addGoal(1, new HuntPlayerGoal(mob));
        BRAINS.put(mob, brain);
    }

    public static void uninstall(PathfinderMob mob) {
        TierBrain old = BRAINS.remove(mob);
        if (old != null) old.onRemoved();
        removeGoals(mob.goalSelector, g -> g instanceof TierBrain);
        removeGoals(mob.targetSelector, g -> g instanceof RetaliateGoal || g instanceof HuntPlayerGoal);
    }

    /** removeGoal (e não removeAllGoals) para que um goal em execução seja parado e libere suas flags. */
    private static void removeGoals(GoalSelector selector, Predicate<Goal> filter) {
        selector.getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(filter).toList()
                .forEach(selector::removeGoal);
    }

    public static TierBrain of(Entity entity) {
        return entity instanceof PathfinderMob mob ? BRAINS.get(mob) : null;
    }

    private static final class RetaliateGoal extends HurtByTargetGoal {
        RetaliateGoal(PathfinderMob mob) { super(mob); }
    }

    private static final class HuntPlayerGoal extends NearestAttackableTargetGoal<Player> {
        HuntPlayerGoal(PathfinderMob mob) { super(mob, Player.class, true); }
    }

    // ------------------------------------------------------------------ estado

    protected final PathfinderMob mob;
    protected final MobTier tier;
    protected final ElementType element;
    protected final RandomSource random;
    protected LivingEntity target;
    protected int meleeCooldown;

    public static final int RAGE_TICKS = 400;
    private static final int EXHAUST_TICKS = 60;

    private final List<Move> moves = new ArrayList<>();
    private final List<Move> rageMoves = new ArrayList<>();
    private int rageTicks;
    private int exhaustedTicks;
    private boolean rageUsed;
    /** Rugidos de fase/fúria: nenhum dano entra. */
    protected boolean invulnerable;
    private Move current;
    private Move last;
    private int moveTick;
    private int thinkDelay;
    private int pathDelay;
    private long stunnedUntil;
    private long laughingUntil;
    private Vec3 defeatedAt;
    private boolean victoryPending;

    public final boolean isStunned() { return now() < stunnedUntil; }

    /** Sincroniza a duração restante para quem começa a enxergar o chefe durante a recuperação. */
    public final int remainingStunTicks() { return (int) Math.max(0, stunnedUntil - now()); }

    public final int remainingLaughTicks() { return (int) Math.max(0, laughingUntil - now()); }

    protected final boolean isCombatPaused() { return isStunned() || remainingLaughTicks() > 0; }

    /** Janela de punição da bola devolvida, com a mesma imunidade a stun de todos os chefes. */
    public final boolean stunFromFireball(int ticks) {
        return stun(ticks);
    }

    /** Stun compartilhado (inclusive colisão de investida): Fúria e transições nunca são interrompidas. */
    protected final boolean stun(int ticks) {
        if ((isRaging() && !allowsStunInRage()) || invulnerable || !mob.isAlive() || remainingLaughTicks() > 0) return false;
        kneel(ticks);
        return true;
    }

    /**
     * Ajoelha o chefe por {@code ticks}, parado e sem agir. Ao contrário de {@link #stun}, vale também na
     * Fúria: é o cansaço planejado do Soberano depois do raio (quem chama decide quando).
     */
    protected final void kneel(int ticks) {
        if (!mob.isAlive() || remainingLaughTicks() > 0) return;
        interrupt();
        stunnedUntil = now() + ticks;
        mob.getNavigation().stop();
        mob.setDeltaMovement(0, Math.min(0, mob.getDeltaMovement().y), 0);
        anim(MobAnim.KNOCKED_DOWN, ticks);
        sound(SoundEvents.RAVAGER_STUNNED, 2F, 0.6F);
    }

    /** Se a Fúria deste chefe pode ser atordoada (só o Soberano: a bola devolvida o ajoelha mesmo na Fúria). */
    protected boolean allowsStunInRage() { return false; }

    /** Começo da risada de vitória (depois de o chefe matar um jogador): o Soberano acrescenta uma cinemática. */
    protected void onVictoryLaugh() {}

    /** Só agenda: a morte pode acontecer dentro de Move.tick, que não pode ser interrompido por reentrância. */
    public final void onPlayerDefeated(Player player) {
        if ((tier != MobTier.BOSS && tier != MobTier.SUB_BOSS) || !mob.isAlive()) return;
        defeatedAt = player.position();
        victoryPending = true;
    }

    protected TierBrain(PathfinderMob mob, MobTier tier, ElementType element) {
        this.mob = mob;
        this.tier = tier;
        this.element = element;
        this.random = mob.getRandom();
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    /**
     * Um ataque especial. {@link #tick} recebe o tick local (0, 1, 2...) e devolve false quando terminou.
     * A recarga começa a contar quando o ataque inicia.
     */
    protected abstract class Move {
        final int cooldown;
        final int weight;
        long readyAt;

        protected Move(int cooldown, int weight) {
            this.cooldown = cooldown;
            this.weight = weight;
        }

        protected boolean usable(double dist) { return true; }

        protected int weight(double dist) { return weight; }

        /**
         * Urgência: entre os ataques prontos, só os de MAIOR urgência entram no sorteio (0 = comum). Serve para
         * respostas que não podem esperar a sorte: afastar quem colou nos pés, usar o golpe característico.
         */
        protected int urgency(double dist) { return 0; }

        protected void begin() {}

        protected abstract boolean tick(int t);

        protected void end() {}
    }

    protected void addMove(Move move) {
        // Atraso inicial aleatório: mobs iguais não abrem a luta com o mesmo ataque no mesmo tick.
        move.readyAt = now() + 20 + random.nextInt(40);
        moves.add(move);
    }

    /** Ataque exclusivo da Fúria. Ter ao menos um faz o mob entrar em Fúria a 1/4 da vida. */
    protected void addRageMove(Move move) {
        rageMoves.add(move);
    }

    public boolean isRaging() {
        return rageTicks > 0;
    }

    /** Multiplicador do dano que o mob RECEBE: resistente em Fúria, frágil exausto. */
    public float damageTakenMultiplier() {
        return isRaging() ? 0.65F : exhaustedTicks > 0 ? 1.25F : 1.0F;
    }

    /** Cancela o golpe durante rugidos (com som de bloqueio). */
    public boolean blocksDamage() {
        if (invulnerable) sound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.5F);
        return invulnerable;
    }

    // ------------------------------------------------------------------ ganchos dos tiers

    /** Posicionamento e golpe básico quando nenhum ataque especial foi escolhido. */
    protected abstract void engage();

    /** Pausa entre ataques especiais (ticks). */
    protected abstract int thinkGap();

    /** Cadência do golpe básico (ticks). */
    protected abstract int meleeInterval();

    /** Chance de o golpe básico aplicar a passiva do elemento. */
    protected abstract float meleeElementChance();

    /** Multiplicador das recargas (o Monarca acelera por fase). */
    protected float cooldownScale() { return 1.0F; }

    /** Primeiro tick com um alvo. */
    protected void onEngage() {}

    /** Todo tick do mob, com ou sem alvo: aura, barra de chefe, fases. */
    public void passiveTick() {}

    /** Início da Fúria (no começo do rugido): barra vermelha, fase, etc. */
    protected void onRageStart() {}

    protected void onRageEnd() {}

    /** Reforços chamados no rugido da Fúria. */
    protected void rageSummon() {
        summon(MobTier.NORMAL, 2);
    }

    /** Fração da vida em que a Fúria começa sozinha (se houver ataques de Fúria). 0 = nunca sozinha. */
    protected float rageThreshold() { return 0.25F; }

    /** Fúria sem fim: não conta o tempo e nunca entra em exaustão. */
    protected boolean endlessRage() { return false; }

    /** Chamado pelo MobEvents a cada tick do mob vivo: contagem e visual da Fúria + {@link #passiveTick}. */
    public final void baseTick() {
        if (victoryPending && !invulnerable) {
            victoryPending = false;
            interrupt();
            stunnedUntil = 0;
            laughingUntil = now() + TierIdentity.BOSS_LAUGH_TICKS;
            anim(MobAnim.LAUGH, TierIdentity.BOSS_LAUGH_TICKS);
            laughSound();
            onVictoryLaugh();
        }
        if (remainingLaughTicks() > 0 && remainingLaughTicks() < TierIdentity.BOSS_LAUGH_TICKS && mob.tickCount % 18 == 0) {
            laughSound();
        }
        if (isCombatPaused()) {
            mob.getNavigation().stop();
            mob.setSpeed(0);
            mob.setZza(0);
            mob.setXxa(0);
            mob.setJumping(false);
            mob.setDeltaMovement(0, Math.min(0, mob.getDeltaMovement().y), 0);
            if (remainingLaughTicks() > 0 && defeatedAt != null) face(flatDirTo(defeatedAt));
            if (isStunned() && mob.tickCount % 10 == 0) particles(ParticleTypes.CRIT, mob.getEyePosition(), 8, 0.5, 0.05);
            // A risada não é stun: a Fúria continua contando e mantém seus efeitos/gritos.
            if (!isStunned() && rageTicks > 0) tickRage();
            passiveTick();
            return;
        }
        if (exhaustedTicks > 0) exhaustedTicks--;
        if (rageTicks > 0) tickRage();
        passiveTick();
    }

    private void tickRage() {
        if (!endlessRage()) rageTicks--;
        rageVisuals();
        if (mob.tickCount % TierIdentity.RAGE_CRY_INTERVAL == 0) rageCry();
        if (rageTicks == 0) endRage();
    }

    private void laughSound() {
        sound(SoundEvents.WITCH_CELEBRATE, 2.2F, mob instanceof Sovereign ? 0.45F : 0.65F);
    }

    /** Antes do dano entrar. Devolve true para cancelar o golpe (aparar, invulnerabilidade). */
    public boolean onIncomingDamage(DamageSource source, float amount) { return false; }

    /** Depois que o dano entrou e antes da checagem de morte (mudar a vida aqui pode evitar a morte). */
    public void onHurt(DamageSource source, float amount) {}

    public void onDeath() {}

    /** Mob saiu do mundo ou perdeu o perfil: limpar barras de chefe etc. */
    public void onRemoved() {}

    public int phase() { return 1; }

    protected Component bossBarName() { return mob.getDisplayName(); }

    // ------------------------------------------------------------------ Goal

    @Override
    public boolean canUse() {
        return isCombatPaused() || isValidTarget(mob.getTarget());
    }

    @Override
    public boolean canContinueToUse() {
        return isCombatPaused() || current != null || isValidTarget(mob.getTarget());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        target = mob.getTarget();
        mob.setAggressive(true);
        thinkDelay = 10;
        onEngage();
    }

    @Override
    public void stop() {
        if (current != null) finishMove();
        target = null;
        mob.setAggressive(false);
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (isCombatPaused()) return;
        LivingEntity t = mob.getTarget();
        if (isValidTarget(t)) target = t;
        if (meleeCooldown > 0) meleeCooldown--;

        if (!isValidTarget(target)) {
            if (current != null) finishMove();
            return;
        }
        if (!rageMoves.isEmpty() && !rageUsed && mob.getHealth() <= mob.getMaxHealth() * rageThreshold()) {
            force(new RageStart());
            return;
        }
        if (current != null) {
            Move running = current;
            if (!running.tick(moveTick++) && current == running) finishMove();
            return;
        }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (thinkDelay > 0) {
            thinkDelay--;
        } else {
            Move next = pickMove();
            if (next != null) {
                force(next);
                return;
            }
        }
        engage();
    }

    /** Inicia um ataque imediatamente, interrompendo o atual (usado por troca de fase). */
    protected void force(Move move) {
        if (current != null) finishMove();
        current = move;
        last = move;
        moveTick = 0;
        move.readyAt = now() + (long) (move.cooldown * cooldownScale() * (isRaging() ? 0.75F : 1.0F));
        mob.getNavigation().stop();
        move.begin();
    }

    protected boolean isBusy() {
        return current != null;
    }

    /** Para o ataque em andamento (as áreas já lançadas no mundo continuam). */
    protected void interrupt() {
        if (current != null) finishMove();
    }

    protected boolean isRunning(Move move) {
        return current == move;
    }

    private void finishMove() {
        Move m = current;
        current = null;
        m.end();
        thinkDelay = isRaging() ? thinkGap() / 2 : thinkGap();
    }

    /** Sorteio ponderado entre os ataques prontos e utilizáveis, evitando repetir o último. */
    private Move pickMove() {
        double dist = mob.distanceTo(target);
        long now = now();
        List<Move> ready = new ArrayList<>();
        for (Move m : isRaging() ? rageMoves : moves) {
            if (now >= m.readyAt && m.weight(dist) > 0 && m.usable(dist)) ready.add(m);
        }
        int top = 0;
        for (Move m : ready) top = Math.max(top, m.urgency(dist));
        if (top > 0) {
            final int level = top;
            ready.removeIf(m -> m.urgency(dist) < level);
        }
        if (ready.size() > 1) ready.remove(last);
        int total = 0;
        for (Move m : ready) total += m.weight(dist);
        if (total <= 0) return null;
        int roll = random.nextInt(total);
        for (Move m : ready) {
            roll -= m.weight(dist);
            if (roll < 0) return m;
        }
        return null;
    }

    protected boolean isValidTarget(LivingEntity t) {
        if (t == null || !t.isAlive() || t.level() != mob.level()) return false;
        if (t instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        return mob.distanceToSqr(t) < 64 * 64;
    }

    // ------------------------------------------------------------------ movimento

    protected void chase(double speed) {
        if (--pathDelay <= 0) {
            pathDelay = 4 + random.nextInt(5);
            mob.getNavigation().moveTo(target, speed);
        }
    }

    /** Recua para longe do alvo. */
    protected void retreat(double speed) {
        if (--pathDelay <= 0 || mob.getNavigation().isDone()) {
            pathDelay = 10;
            Vec3 away = DefaultRandomPos.getPosAway(mob, 8, 4, target.position());
            if (away != null) mob.getNavigation().moveTo(away.x, away.y, away.z, speed);
        }
    }

    /** Anda de lado em volta do alvo (como o esqueleto), sem pathfinding. */
    protected void strafe(float forward, float sideways) {
        mob.getNavigation().stop();
        mob.getMoveControl().strafe(forward, sideways);
        mob.lookAt(target, 30.0F, 30.0F);
    }

    /** Vira o corpo inteiro na direção (horizontal) dada. */
    protected void face(Vec3 dir) {
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0F;
        mob.setYRot(yaw);
        mob.yBodyRot = yaw;
        mob.yHeadRot = yaw;
    }

    protected Vec3 flatDirTo(Vec3 to) {
        Vec3 d = to.subtract(mob.position()).multiply(1, 0, 1);
        return d.lengthSqr() < 1.0E-4 ? mob.getLookAngle().multiply(1, 0, 1).normalize() : d.normalize();
    }

    // ------------------------------------------------------------------ dano

    /** Dano base do mob (já inclui o multiplicador do tier). Mobs sem ATTACK_DAMAGE usam 2 x multiplicador. */
    protected float baseDamage() {
        AttributeInstance attr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        float base = attr != null ? (float) attr.getValue() : (float) (2.0 * TierIdentity.stats(tier).damage());
        return isRaging() ? base * 1.35F : base;
    }

    /** Potência das passivas elementais deste mob (o Lacaio usa a leve). */
    protected int potency() {
        return Math.max(1, tier.elementPotency());
    }

    /** Golpe básico do tier, se o alvo estiver ao alcance. Devolve true se golpeou. */
    protected boolean tryMelee(float damageMult, SoundEvent sound, float pitch) {
        if (meleeCooldown > 0 || !mob.isWithinMeleeAttackRange(target) || !mob.getSensing().hasLineOfSight(target)) {
            return false;
        }
        meleeCooldown = meleeInterval();
        mob.swing(InteractionHand.MAIN_HAND);
        strike(target, baseDamage() * damageMult, random.nextFloat() < meleeElementChance());
        sound(sound, 1.0F, pitch);
        return true;
    }

    protected void strike(LivingEntity victim, float damage, boolean elemental) {
        Hazards.hit(mob, victim, damage, element, elemental ? potency() : 0, false);
    }

    protected void magicStrike(LivingEntity victim, float damage) {
        Hazards.hit(mob, victim, damage, element, potency(), true);
    }

    protected List<LivingEntity> enemiesWithin(double radius) {
        return enemiesWithin(mob.position(), radius);
    }

    protected List<LivingEntity> enemiesWithin(Vec3 center, double radius) {
        return Hazards.enemiesIn(level(), mob, new AABB(center, center).inflate(radius, radius * 0.6 + 1, radius))
                .stream().filter(e -> e.distanceToSqr(center) <= radius * radius + 4).toList();
    }

    /** Mobs do mod próximos (para o comportamento de bando e alertas). */
    protected List<PathfinderMob> allies(double radius) {
        return level().getEntitiesOfClass(PathfinderMob.class, mob.getBoundingBox().inflate(radius),
                e -> e != mob && e.isAlive() && e.hasData(ModAttachments.MOB_PROFILE));
    }

    /** Reforços do tier dado que ainda estão vivos por perto (para não lotar a arena). */
    protected int minionsAround(MobTier minionTier) {
        int n = 0;
        for (PathfinderMob ally : allies(32)) {
            if (ally.getData(ModAttachments.MOB_PROFILE).tier() == minionTier) n++;
        }
        return n;
    }

    /**
     * Invoca reforços do tier dado com o mesmo elemento. Criaturas do mod chamam a criatura daquele tier
     * (o Colosso chama Diabretes); mobs vanilla marcados chamam cópias de si mesmos.
     */
    protected void summon(MobTier minionTier, int count) {
        ServerLevel level = level();
        for (int i = 0; i < count; i++) {
            Entity created = mob instanceof TierMob ? ModEntities.forTier(minionTier).create(level) : mob.getType().create(level);
            if (!(created instanceof PathfinderMob minion)) return;
            double angle = 2 * Math.PI * i / count + random.nextDouble();
            Vec3 pos = mob.position().add(Math.cos(angle) * 2.5, 0, Math.sin(angle) * 2.5);
            minion.moveTo(pos.x, pos.y, pos.z, random.nextFloat() * 360F, 0);
            minion.finalizeSpawn(level, level.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            TierIdentity.apply(minion, minionTier, element);
            minion.setTarget(target);
            level.addFreshEntity(minion);
            level.sendParticles(ParticleTypes.POOF, pos.x, pos.y + 0.5, pos.z, 12, 0.3, 0.5, 0.3, 0.02);
            SkillVfx.line(level, mob.position().add(0, mob.getBbHeight() * 0.6, 0), pos.add(0, 0.5, 0), elementParticle(), 10);
        }
    }

    // ------------------------------------------------------------------ Fúria

    private static final ParticleOptions RAGE_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.08F, 0.05F), 2.2F);

    private void rageVisuals() {
        ServerLevel level = level();
        double w = mob.getBbWidth() * 0.6, h = mob.getBbHeight();
        level.sendParticles(RAGE_DUST, mob.getX(), mob.getY() + h * 0.5, mob.getZ(), 4, w, h * 0.4, w, 0);
        if (mob.tickCount % 3 == 0) SkillVfx.ring(level, mob.position().add(0, 0.1, 0), mob.getBbWidth() * 0.9 + 0.5, RAGE_DUST, 14);
        if (mob.tickCount % 8 == 0) level.sendParticles(ParticleTypes.LAVA, mob.getX(), mob.getY() + h, mob.getZ(), 2, w, 0.2, w, 0);
        if (mob.tickCount % 20 == 0) sound(SoundEvents.WARDEN_HEARTBEAT, 2.0F, 0.6F);
    }

    /**
     * Entra em Fúria agora, com grito mas sem a pausa do rugido padrão (quem chama faz a própria entrada). Os ataques ficam
     * prontos em até 1 s.
     */
    protected void beginRage() {
        stunnedUntil = 0;
        rageUsed = true;
        rageTicks = RAGE_TICKS;
        onRageStart();
        rageCry();
        for (Move m : rageMoves) m.readyAt = now() + random.nextInt(20);
    }

    /**
     * Retoma a Fúria depois de recarregar o chefe (o cérebro não é salvo, só a vida): sem grito nem rugido,
     * para não repetir a entrada. Chamar só depois de registrar os ataques de Fúria.
     */
    protected void resumeRage() {
        rageUsed = true;
        rageTicks = RAGE_TICKS;
        onRageStart();
        for (Move m : rageMoves) m.readyAt = now() + 40 + random.nextInt(40);
    }

    private void endRage() {
        exhaustedTicks = EXHAUST_TICKS;
        onRageEnd();
        sound(SoundEvents.RAVAGER_STUNNED, 1.5F, 0.6F);
        particles(ParticleTypes.LARGE_SMOKE, center(), 20, 0.6, 0.02);
        if (mob.isAlive() && remainingLaughTicks() == 0) force(new Exhausted());
    }

    /** Compartilhado com o despertar final do Soberano, que não usa RageStart. */
    private void rageCry() {
        sound(SoundEvents.WARDEN_ROAR, 3F, mob instanceof Sovereign ? 0.5F : 0.7F);
        sound(SoundEvents.RAVAGER_ROAR, 2F, 0.55F);
    }

    /** Rugido da Fúria: 30 ticks invulnerável, onda que arremessa e reforços. */
    private final class RageStart extends Move {
        RageStart() { super(0, 0); }

        @Override
        protected void begin() {
            rageUsed = true;
            stunnedUntil = 0;
            invulnerable = true;
            rageTicks = RAGE_TICKS + 30;
            onRageStart();
            anim(MobAnim.ROAR, 30);
            rageCry();
            Component warning = Component.translatable("message.solomod.rage", mob.getDisplayName())
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
            for (ServerPlayer player : level().players()) {
                if (player.distanceToSqr(mob) < 48 * 48) player.displayClientMessage(warning, true);
            }
        }

        @Override
        protected boolean tick(int t) {
            double r = 0.5 + t * 0.3;
            SkillVfx.ring(level(), mob.position().add(0, 0.2 + t * 0.08, 0), r, RAGE_DUST, 16);
            if (t == 15) {
                particles(ParticleTypes.EXPLOSION_EMITTER, center(), 1, 0, 0);
                sound(SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.5F);
                for (LivingEntity e : enemiesWithin(8)) {
                    Hazards.push(e, flatDirTo(e.position()).scale(1.5).add(0, 0.6, 0));
                }
            }
            if (t == 20) rageSummon();
            return t < 30;
        }

        @Override
        protected void end() {
            invulnerable = false;
            for (Move m : rageMoves) m.readyAt = now() + random.nextInt(20);
        }
    }

    /** Fim da Fúria: parado, ofegante e vulnerável. */
    private final class Exhausted extends Move {
        Exhausted() { super(0, 0); }

        @Override
        protected void begin() {
            anim(MobAnim.EXHAUSTED, EXHAUST_TICKS);
        }

        @Override
        protected boolean tick(int t) {
            mob.getNavigation().stop();
            if (t % 10 == 0) particles(ParticleTypes.SMOKE, mob.getEyePosition(), 6, 0.3, 0.01);
            return t < EXHAUST_TICKS;
        }
    }

    // ------------------------------------------------------------------ apresentação

    /** Atualiza a barra de chefe: vida, nome e quem a vê (jogadores vivos a até {@code range} blocos). */
    protected void updateBossBar(ServerBossEvent bar, double range) {
        bar.setProgress(mob.getHealth() / mob.getMaxHealth());
        if (mob.tickCount % 10 != 0) return;
        Component name = bossBarName();
        if (isRaging()) {
            Component suffix = endlessRage() ? Component.translatable("message.solomod.rage_bar_endless")
                    : Component.translatable("message.solomod.rage_bar", (rageTicks + 19) / 20);
            name = name.copy().append(suffix.copy().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
        bar.setName(name);
        for (ServerPlayer player : level().players()) {
            boolean near = player.isAlive() && player.distanceToSqr(mob) < range * range;
            if (near && !bar.getPlayers().contains(player)) bar.addPlayer(player);
            else if (!near && bar.getPlayers().contains(player)) bar.removePlayer(player);
        }
    }

    protected void anim(MobAnim anim, int duration) {
        ModNetwork.sendMobAnim(mob, TierIdentity.identity(mob, anim, duration));
    }

    protected void sound(SoundEvent sound, float volume, float pitch) {
        Hazards.sound(level(), mob.position(), sound, volume, pitch);
    }

    protected void particles(ParticleOptions particle, Vec3 at, int count, double spread, double speed) {
        level().sendParticles(particle, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    protected ParticleOptions elementParticle() {
        return SkillVfx.particleFor(element);
    }

    protected Vec3 center() {
        return mob.position().add(0, mob.getBbHeight() * 0.5, 0);
    }

    protected ServerLevel level() {
        return (ServerLevel) mob.level();
    }

    protected long now() {
        return mob.level().getGameTime();
    }
}
