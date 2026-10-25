package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.advancement.ModAdvancements;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Base dos mini-chefes dos Santuários de Eco: barra de vida, 3 fases por vida (acima de 66%, acima de 33%, resto)
 * e um ciclo de golpes. Sem golpe em andamento, espera o {@code cooldown} e pede um golpe a {@link #chooseAttack};
 * durante o golpe, {@link #tickAttack} roda a cada tick até devolver false. Na fase 3 o intervalo cai para 1,5 s.
 * Todos os golpes devem ser telegrafados (anel, linha, partículas ou som) antes do dano.
 */
public abstract class SanctuaryBoss extends SanctuaryMob {
    private final ServerBossEvent bossBar;
    /** Jogadores que feriram o chefe nesta luta: recebem a conquista dele. */
    private final Set<UUID> fighters = new HashSet<>();
    private int cooldown = 60;

    protected SanctuaryBoss(EntityType<? extends Monster> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.bossBar = new ServerBossEvent(getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
    }

    /** 1 (acima de 66% de vida), 2 (acima de 33%) ou 3. */
    public final int phase() {
        float ratio = getHealth() / getMaxHealth();
        return ratio > 0.66F ? 1 : ratio > 0.33F ? 2 : 3;
    }

    /** Golpe a iniciar contra o alvo (0 = nenhum agora). */
    protected abstract int chooseAttack(LivingEntity target, double distance, int phase);

    /** Um tick do golpe {@code id}, no tick {@code t} (começa em 1). Devolve false quando o golpe acabou. */
    protected abstract boolean tickAttack(ServerLevel server, int id, int t, @Nullable LivingEntity target);

    /** Chamado uma vez ao iniciar um golpe (guardar direção, alvo etc.). */
    protected void onAttackStart(int id, LivingEntity target) {}

    /** Se o chefe para de andar durante os golpes (voadores e investidas cuidam do próprio movimento). */
    protected boolean holdsStillWhileAttacking() {
        return true;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.setProgress(getHealth() / getMaxHealth());
        LivingEntity target = getTarget();
        if (attackId() == 0) {
            if (cooldown > 0) cooldown--;
            if (cooldown <= 0 && target != null) {
                int id = chooseAttack(target, distanceTo(target), phase());
                if (id == 0) {
                    cooldown = 15;
                } else {
                    setAttack(id);
                    onAttackStart(id, target);
                }
            }
            return;
        }
        int t = advanceAttack();
        if (holdsStillWhileAttacking()) getNavigation().stop();
        if (target != null) getLookControl().setLookAt(target, 40, 40);
        if (level() instanceof ServerLevel server && !tickAttack(server, attackId(), t, target)) finishAttack();
    }

    protected final void finishAttack() {
        setAttack(0);
        cooldown = phase() == 3 ? 30 : 50 + random.nextInt(20);
    }

    /** Sorteia uma das opções diferentes de 0; devolve 0 se não houver nenhuma. */
    protected final int pick(int... options) {
        int n = 0;
        for (int o : options) if (o != 0) n++;
        if (n == 0) return 0;
        int k = random.nextInt(n);
        for (int o : options) {
            if (o != 0 && k-- == 0) return o;
        }
        return 0;
    }

    protected final int countNearby(Class<? extends Entity> type, double radius) {
        return level().getEntitiesOfClass(type, getBoundingBox().inflate(radius)).size();
    }

    /** Chama {@code count} capangas em volta, {@code up} blocos acima, todos já mirando o alvo atual. */
    protected final void summon(ServerLevel server, EntityType<? extends Mob> type, int count, double spread, double up) {
        for (int i = 0; i < count; i++) {
            Mob mob = type.create(server);
            if (mob == null) continue;
            double angle = Math.PI * 2 * i / count + random.nextDouble() * 0.5;
            mob.moveTo(getX() + Math.cos(angle) * spread, getY() + up, getZ() + Math.sin(angle) * spread,
                    random.nextFloat() * 360F, 0);
            mob.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            mob.setPersistenceRequired();
            mob.setTarget(getTarget());
            server.addFreshEntity(mob);
            server.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + 0.5, mob.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
        }
    }

    // ------------------------------------------------------------------ barra de vida

    /** Cópias e outras variantes sem barra própria devolvem false. */
    protected boolean showsBossBar() {
        return true;
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (showsBossBar()) bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof ServerPlayer player) fighters.add(player.getUUID());
        return hurt;
    }

    /** Morte (cópias do Arquivista não chegam aqui): a conquista do chefe para quem lutou. */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel server)) return;
        if (source.getEntity() instanceof ServerPlayer killer) fighters.add(killer.getUUID());
        for (UUID id : fighters) {
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(id);
            if (player != null) ModAdvancements.onSanctuaryBossDefeat(player, getType());
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }
}
