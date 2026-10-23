package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.mob.TierIdentity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Criatura própria de cada tier, com modelo e textura exclusivos (ver client/model):
 * <ul>
 *   <li>NORMAL — Diabrete: gárgula pequena de cabeça enorme, asas e rabo de lança.</li>
 *   <li>OP — Duelista Espectral: cavaleiro negro e dourado com penacho, capa, espada rúnica e escudo.</li>
 *   <li>SUB_BOSS — Oráculo: manto flutuante com olho no peito, auréola, coroa e mãos soltas.</li>
 *   <li>BOSS — Colosso Rei: gigante de pedra com coroa de espinhos, chifres e cristais nas costas.</li>
 * </ul>
 * Cada criatura ainda tem uma forma completamente diferente por elemento (ver tools/mob_variants.py).
 * O combate vem do {@link com.tarcitani.solomod.mob.TierBrain} do tier; aqui ficam só corpo, sons e morte.
 *
 * <p>O Colosso não morre como um mob comum: por {@link #DISSOLVE_TICKS} ticks ele se desintegra
 * (o modelo se desfaz em pedaços e vira cinza) enquanto os jogadores veem a cinemática.
 */
public class TierMob extends Monster {
    public static final int DISSOLVE_TICKS = 100;
    /** Chave usada pelos ovos de cada elemento (dados da entidade no item). */
    public static final String EGG_ELEMENT_TAG = "SoloEggElement";

    /** Elemento sincronizado com o cliente desde o nascimento: decide qual modelo/textura desenhar. */
    private static final EntityDataAccessor<Byte> ELEMENT = SynchedEntityData.defineId(TierMob.class, EntityDataSerializers.BYTE);

    private final MobTier tier;
    private int dissolveTicks;

    public TierMob(EntityType<? extends TierMob> type, Level level, MobTier tier) {
        super(type, level);
        this.tier = tier;
        this.xpReward = 5 + 10 * tier.ordinal();
    }

    public MobTier tier() {
        return tier;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ELEMENT, (byte) 0);
    }

    public ElementType element() {
        ElementType[] all = ElementType.values();
        return all[Math.floorMod(entityData.get(ELEMENT), all.length)];
    }

    public void setElement(ElementType element) {
        entityData.set(ELEMENT, (byte) element.ordinal());
    }

    /** Ovo de um elemento específico: aplica o perfil com esse elemento logo depois do finalizeSpawn. */
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(EGG_ELEMENT_TAG) && level() instanceof ServerLevel) {
            ElementType element = ElementType.byName(tag.getString(EGG_ELEMENT_TAG));
            if (element != null) TierIdentity.apply(this, tier, element);
        }
    }

    /** Atributos BASE; o multiplicador do tier é somado por TierIdentity (como em qualquer mob marcado). */
    public static AttributeSupplier.Builder attributes(MobTier tier) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.MOVEMENT_SPEED, tier == MobTier.NORMAL ? 0.3 : 0.26)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.STEP_HEIGHT, tier.ordinal() >= MobTier.SUB_BOSS.ordinal() ? 1.5 : 0.6);
    }

    @Override
    protected void registerGoals() {
        // Só a vida fora de combate; o combate é instalado pelo TierBrain.
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /** Ovo, /summon ou spawn do mod: sem perfil ainda, recebe o do próprio tier com um elemento aleatório. */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, data);
        if (!hasData(ModAttachments.MOB_PROFILE)) TierIdentity.apply(this, tier, randomElement());
        return result;
    }

    public ElementType randomElement() {
        ElementType[] all = ElementType.values();
        return all[1 + random.nextInt(all.length - 1)];
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ------------------------------------------------------------------ sons de cada criatura

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (tier) {
            case NORMAL -> SoundEvents.VEX_AMBIENT;
            case OP -> SoundEvents.WITHER_SKELETON_AMBIENT;
            case SUB_BOSS -> SoundEvents.EVOKER_AMBIENT;
            case BOSS -> SoundEvents.RAVAGER_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (tier) {
            case NORMAL -> SoundEvents.VEX_HURT;
            case OP -> SoundEvents.WITHER_SKELETON_HURT;
            case SUB_BOSS -> SoundEvents.EVOKER_HURT;
            case BOSS -> SoundEvents.IRON_GOLEM_DAMAGE;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (tier) {
            case NORMAL -> SoundEvents.VEX_DEATH;
            case OP -> SoundEvents.WITHER_SKELETON_DEATH;
            case SUB_BOSS -> SoundEvents.EVOKER_DEATH;
            case BOSS -> SoundEvents.RAVAGER_DEATH;
        };
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        switch (tier) {
            case NORMAL -> playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.5F);
            case OP -> playSound(SoundEvents.CHAIN_STEP, 0.4F, 0.9F);
            case SUB_BOSS -> {} // flutua: sem passos
            case BOSS -> playSound(SoundEvents.RAVAGER_STEP, 1.2F, 0.6F);
        }
    }

    @Override
    public float getVoicePitch() {
        float base = switch (tier) {
            case NORMAL -> 1.5F;
            case OP -> 0.9F;
            case SUB_BOSS -> 0.7F;
            case BOSS -> 0.55F;
        };
        return base + (random.nextFloat() - 0.5F) * 0.1F;
    }

    @Override
    protected float getSoundVolume() {
        return tier == MobTier.BOSS ? 2.5F : 1.0F;
    }

    // ------------------------------------------------------------------ morte

    /** 0..1 durante a desintegração do Colosso (0 enquanto vivo). */
    public float dissolveProgress(float partialTick) {
        if (dissolveTicks <= 0) return 0F;
        return Mth.clamp((dissolveTicks + partialTick) / DISSOLVE_TICKS, 0F, 1F);
    }

    /**
     * Colosso: em vez de tombar e sumir em 1 s, se desfaz por 5 s. O deathTime vanilla não sobe, então
     * o corpo não tomba nem fica vermelho — o modelo cuida da animação. Roda nos dois lados.
     */
    @Override
    protected void tickDeath() {
        if (tier != MobTier.BOSS) {
            super.tickDeath();
            return;
        }
        dissolveTicks++;
        if (level() instanceof ServerLevel server) {
            float p = dissolveTicks / (float) DISSOLVE_TICKS;
            // A desintegração "desce" da coroa até os pés: a faixa de cinzas acompanha.
            double y = getY() + getBbHeight() * (1.0 - p);
            double w = getBbWidth() * 0.6;
            server.sendParticles(ParticleTypes.WHITE_ASH, getX(), y, getZ(), 30, w, 0.4, w, 0.05);
            server.sendParticles(ParticleTypes.ASH, getX(), y, getZ(), 20, w, 0.4, w, 0.02);
            server.sendParticles(ParticleTypes.SOUL, getX(), y, getZ(), 2, w, 0.3, w, 0.04);
            server.sendParticles(ParticleTypes.END_ROD, getX(), y, getZ(), 3, w, 0.3, w, 0.08);
            if (dissolveTicks % 12 == 0) {
                server.playSound(null, getX(), y, getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 2.0F, 0.6F);
            }
            if (dissolveTicks % 20 == 0) {
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);
            }
            if (dissolveTicks >= DISSOLVE_TICKS && !isRemoved()) {
                server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1, getZ(), 80, 1.2, 1.5, 1.2, 0.15);
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 3.0F, 0.5F);
                level().broadcastEntityEvent(this, (byte) 60);
                remove(Entity.RemovalReason.KILLED);
            }
        }
    }
}
