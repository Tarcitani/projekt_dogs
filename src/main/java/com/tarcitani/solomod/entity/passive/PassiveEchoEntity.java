package com.tarcitani.solomod.entity.passive;

import com.tarcitani.solomod.advancement.ModAdvancements;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.attachment.ActiveEchoAttachment;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.TierMob;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Base dos 15 mobs pacíficos (Fauna Elemental). Cada um tem de 1 a 3 cargas de suporte; usar a habilidade
 * gasta uma carga. Ao gastar a última, entra em Exaustão Elemental por {@link #cooldownTicks()} e recarrega tudo
 * ao fim. Quando sofre dano ambiental letal, não morre: entra na Forma Astral (invisível e inativo) por
 * {@link #DORMANT_TICKS} e reaparece ao lado do dono. Dano de chefe (Guardião, Monarca, Soberano) é ignorado.
 *
 * <p>Estado sincronizado com o cliente (cargas, exaustão e dormência) para o renderer apagar a camada
 * emissiva e parar as partículas. A Ressonância de Alma Única (um companheiro ativo por jogador) fica fora
 * desta classe: aqui só existe {@link #setOwner(UUID)}, usado pela Forma Astral para o reaparecimento.
 *
 * <p>Entidades com a tag {@link #ISOLATION_TAG} nunca recebem perfil hostil: use {@link #isPassiveEcho(Entity)}
 * na lista de alvos da IA dos monstros.
 */
public abstract class PassiveEchoEntity extends PathfinderMob {
    /** Tag de isolamento: mobs com ela ficam de fora do spawn hostil e da lista de alvos dos monstros. */
    public static final TagKey<EntityType<?>> ISOLATION_TAG =
            TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "passive_echo"));

    public static final int MIN_CHARGES = 1;
    public static final int MAX_CHARGES = 3;
    /** 60 segundos de Estado Dormente. */
    public static final int DORMANT_TICKS = 1200;
    /** Vida restaurada ao sair da Forma Astral: 1 ponto, nunca zero. */
    private static final float ASTRAL_HEALTH = 1.0F;
    /** Distância do reaparecimento ao lado do dono, em blocos. */
    private static final double RESPAWN_OFFSET = 1.5;
    /** Acima de 24 blocos do dono, o mob voador não persegue: reaparece direto no alvo. */
    private static final double TELEPORT_DISTANCE_SQR = 24.0 * 24.0;
    /** Valor sentinela das cargas antes da primeira batida de servidor. */
    private static final int UNSET = -1;

    private static final EntityDataAccessor<Integer> CHARGES =
            SynchedEntityData.defineId(PassiveEchoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXHAUSTION =
            SynchedEntityData.defineId(PassiveEchoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DORMANT =
            SynchedEntityData.defineId(PassiveEchoEntity.class, EntityDataSerializers.BOOLEAN);

    private int dormantTicks;
    /** Verdadeiro enquanto segue o dono (então não vagueia). Só o servidor calcula. */
    private boolean following;
    @Nullable
    private UUID ownerId;

    protected PassiveEchoEntity(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
    }

    /** Altura média de voo acima do chão ao vaguear; só os voadores usam. Cada forma tem a sua. */
    protected double hoverHeight() {
        return 2.0;
    }

    /** Cargas máximas do mob; o valor é limitado a 1..3 por {@link #getMaxCharges()}. */
    protected abstract int maxCharges();

    /** Duração da Exaustão Elemental, em ticks. */
    protected abstract int cooldownTicks();

    /** Efeito da habilidade no servidor, já com a carga gasta. */
    protected abstract void onAbility(ServerPlayer owner);

    /** Partículas do elemento usadas ao entrar e sair da Forma Astral. */
    protected abstract ParticleOptions astralParticle();

    /** Movimento enquanto vinculado: chamado a cada tick do servidor, com o dono presente. */
    protected abstract void followOwner(ServerPlayer owner);

    /** Item (e condição extra, como agachar) que vincula o mob ao jogador que o oferece. */
    protected abstract boolean isBondOffer(Player player, ItemStack held);

    /** Sem dono ativo, o mob vive sozinho: terrestres vagueiam, voadores pairam e passeiam, todos olham em volta. */
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return !isNoGravity() && !following && !isDormant() && super.canUse();
            }
        });
        goalSelector.addGoal(5, new HoverWanderGoal());
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** Passeio dos voadores: escolhe um ponto próximo, numa altura em torno de {@link #hoverHeight()}, e plana até lá. */
    private final class HoverWanderGoal extends Goal {
        private static final int MAX_TICKS = 100;
        private Vec3 target;
        private int ticks;

        HoverWanderGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!isNoGravity() || following || isDormant() || getRandom().nextInt(40) != 0) return false;
            double x = getX() + (getRandom().nextDouble() - 0.5) * 12.0;
            double z = getZ() + (getRandom().nextDouble() - 0.5) * 12.0;
            double ground = level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                    net.minecraft.core.BlockPos.containing(x, getY(), z)).getY();
            double y = Math.max(ground, getY() - 6.0) + hoverHeight() * (0.6 + getRandom().nextDouble() * 0.8);
            target = new Vec3(x, y, z);
            return true;
        }

        @Override
        public void start() {
            ticks = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !following && !isDormant() && ticks < MAX_TICKS && position().distanceToSqr(target) > 0.6;
        }

        @Override
        public void tick() {
            ticks++;
            Vec3 delta = target.subtract(position());
            Vec3 dir = delta.normalize().scale(0.07);
            setDeltaMovement(getDeltaMovement().scale(0.85).add(dir));
            float yaw = (float) (Math.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0F;
            setYRot(yaw);
            yBodyRot = yaw;
            yHeadRot = yaw;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGES, UNSET);
        builder.define(EXHAUSTION, 0);
        builder.define(DORMANT, false);
    }

    /** Cargas máximas já limitadas a 1..3. */
    public final int getMaxCharges() {
        return Math.clamp(maxCharges(), MIN_CHARGES, MAX_CHARGES);
    }

    public final int getCharges() {
        return Math.max(0, entityData.get(CHARGES));
    }

    /** Ticks restantes de Exaustão Elemental; zero quando não está exausto. */
    public final int getExhaustionTicks() {
        return entityData.get(EXHAUSTION);
    }

    public final boolean isExhausted() {
        return getExhaustionTicks() > 0;
    }

    public final boolean isDormant() {
        return entityData.get(DORMANT);
    }

    /**
     * Gasta uma carga e dispara a habilidade. Falha sem efeito se o mob estiver dormente, exausto ou sem cargas.
     * Ao gastar a última carga, inicia a Exaustão Elemental.
     */
    public final boolean useAbility(ServerPlayer owner) {
        if (level().isClientSide || isDormant() || isExhausted() || getCharges() <= 0) return false;
        int remaining = getCharges() - 1;
        entityData.set(CHARGES, remaining);
        if (remaining == 0) {
            entityData.set(EXHAUSTION, cooldownTicks());
            ModAdvancements.onEchoExhausted(owner);
        }
        onAbility(owner);
        return true;
    }

    public final void setOwner(UUID id) {
        this.ownerId = id;
    }

    /** Mob com a tag de isolamento: não é alvo de monstros hostis e não recebe perfil de Tier/Elemento. */
    public static boolean isPassiveEcho(Entity entity) {
        return entity.getType().is(ISOLATION_TAG);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (entityData.get(CHARGES) == UNSET) {
            entityData.set(CHARGES, getMaxCharges());
        }
        if (isDormant()) {
            if (--dormantTicks <= 0) wake();
            return;
        }
        if (isExhausted()) {
            int left = getExhaustionTicks() - 1;
            if (left <= 0) {
                entityData.set(EXHAUSTION, 0);
                entityData.set(CHARGES, getMaxCharges());
            } else {
                entityData.set(EXHAUSTION, left);
            }
        }
        ServerPlayer owner = findOwner();
        following = owner != null && isActiveFor(owner);
        if (following) followOwner(owner);
    }

    /**
     * Primeiro vínculo: o item consome-se e o mob passa a ser do jogador. Depois, o dono reativa o companheiro
     * segurando o mesmo item (sem consumir). Só um companheiro fica ativo por jogador; o anterior vai para espera.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level().isClientSide || !isBondOffer(player, held)) return super.mobInteract(player, hand);
        if (ownerId == null) {
            setOwner(player.getUUID());
            if (!player.getAbilities().instabuild) held.shrink(1);
        } else if (!ownerId.equals(player.getUUID())) {
            return super.mobInteract(player, hand);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setData(ModAttachments.ACTIVE_ECHO, new ActiveEchoAttachment(getUUID()));
            ModAdvancements.onEchoBond(serverPlayer, this);
        }
        return InteractionResult.SUCCESS;
    }

    /** Verdadeiro se este é o companheiro ativo do dono (os outros vinculados ficam parados em espera). */
    private boolean isActiveFor(ServerPlayer owner) {
        return getUUID().equals(owner.getData(ModAttachments.ACTIVE_ECHO).echo());
    }

    /** Partícula de poeira na cor RGB dada, usada pelas Formas Astrais. */
    protected static ParticleOptions dust(int rgb) {
        Vector3f color = new Vector3f(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
        return new DustParticleOptions(color, 1.0F);
    }

    /** Vira o vetor de movimento para o alvo, sem gravidade. Longe demais, reaparece direto no alvo. */
    protected final void flyTowards(Vec3 target, double speed) {
        setNoGravity(true);
        if (position().distanceToSqr(target) > TELEPORT_DISTANCE_SQR) {
            moveTo(target.x, target.y, target.z, getYRot(), getXRot());
            return;
        }
        Vec3 delta = target.subtract(position());
        setDeltaMovement(getDeltaMovement().scale(0.6).add(delta.scale(speed)));
        hasImpulse = true;
    }

    /** Dono atual, se estiver online e no mesmo nível. */
    @Nullable
    protected final ServerPlayer findOwner() {
        if (ownerId == null || !(level() instanceof ServerLevel server)) return null;
        return server.getEntity(ownerId) instanceof ServerPlayer player ? player : null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isDormant() || isBossSource(source)) return false;
        // Dano ambiental (queda, lava, fogo, afogamento) sem atacante: o evento de morte é cancelado.
        if (source.getEntity() == null && amount >= getHealth() && level() instanceof ServerLevel server) {
            enterAstralForm(server);
            return false;
        }
        return super.hurt(source, amount);
    }

    /**
     * Dano vindo de Guardião, Monarca ou Soberano (todos são {@link TierMob} de tier SUB_BOSS ou BOSS).
     * As fontes de dano dos chefes não separam área de corpo a corpo, então todo o dano deles é bloqueado.
     */
    private static boolean isBossSource(DamageSource source) {
        return source.getEntity() instanceof TierMob boss
                && (boss.tier() == MobTier.SUB_BOSS || boss.tier() == MobTier.BOSS);
    }

    private void enterAstralForm(ServerLevel server) {
        entityData.set(DORMANT, true);
        dormantTicks = DORMANT_TICKS;
        setHealth(ASTRAL_HEALTH);
        setInvisible(true);
        setNoAi(true);
        ServerPlayer owner = findOwner();
        if (owner != null) ModAdvancements.onEchoAstral(owner);
        server.sendParticles(astralParticle(), getX(), getY() + getBbHeight() / 2, getZ(),
                40, getBbWidth() / 2, getBbHeight() / 2, getBbWidth() / 2, 0.05);
    }

    private void wake() {
        entityData.set(DORMANT, false);
        setInvisible(false);
        setNoAi(false);
        if (level() instanceof ServerLevel server) {
            ServerPlayer owner = findOwner();
            if (owner != null) {
                double yaw = Math.toRadians(owner.getYRot() + 90);
                moveTo(owner.getX() - Math.sin(yaw) * RESPAWN_OFFSET, owner.getY(),
                        owner.getZ() + Math.cos(yaw) * RESPAWN_OFFSET, getYRot(), getXRot());
            }
            server.sendParticles(astralParticle(), getX(), getY() + getBbHeight() / 2, getZ(),
                    40, getBbWidth() / 2, getBbHeight() / 2, getBbWidth() / 2, 0.05);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Charges", entityData.get(CHARGES));
        tag.putInt("Exhaustion", entityData.get(EXHAUSTION));
        tag.putBoolean("Dormant", isDormant());
        tag.putInt("DormantTicks", dormantTicks);
        if (ownerId != null) tag.putUUID("Owner", ownerId);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(CHARGES, tag.contains("Charges") ? tag.getInt("Charges") : UNSET);
        entityData.set(EXHAUSTION, tag.getInt("Exhaustion"));
        entityData.set(DORMANT, tag.getBoolean("Dormant"));
        dormantTicks = tag.getInt("DormantTicks");
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
