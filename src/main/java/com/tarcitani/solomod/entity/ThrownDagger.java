package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.CombatEvents;
import com.tarcitani.solomod.item.ModItems;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.skill.ClassAbility;
import com.tarcitani.solomod.skill.SkillVfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Adaga arremessada. Voa girando; ao acertar um mob causa o dano da arma e cai no chão; ao acertar um bloco
 * fica cravada. Não pode ser pega por outros: só volta quando o dono a chama (clique direito com a mão
 * "fantasma") ou passa por cima dela.
 *
 * <p>Na volta ela atravessa o caminho até o dono como um bumerangue, ferindo quem estiver no meio.
 */
public class ThrownDagger extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> STACK =
            SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> RETURNING =
            SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.BOOLEAN);

    protected boolean dealtDamage;
    private final Set<Integer> returnHits = new HashSet<>();

    public ThrownDagger(EntityType<? extends ThrownDagger> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
    }

    public ThrownDagger(Level level, LivingEntity owner, ItemStack weapon) {
        super(ModEntities.THROWN_DAGGER.get(), owner, level, weapon.copyWithCount(1), null);
        pickup = Pickup.DISALLOWED;
        entityData.set(STACK, weapon.copyWithCount(1));
    }

    /** Para variantes (ex.: a Adaga Relâmpago): mesmo comportamento, outro tipo de entidade. */
    protected ThrownDagger(EntityType<? extends ThrownDagger> type, Level level, LivingEntity owner, ItemStack weapon) {
        super(type, owner, level, weapon.copyWithCount(1), null);
        pickup = Pickup.DISALLOWED;
        entityData.set(STACK, weapon.copyWithCount(1));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STACK, ItemStack.EMPTY);
        builder.define(RETURNING, false);
    }

    /** Pilha usada para desenhar (e para o elemento/tier do dano). */
    public ItemStack stack() {
        return entityData.get(STACK);
    }

    public boolean isReturning() {
        return entityData.get(RETURNING);
    }

    public boolean isStuck() {
        return inGround;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.get(WeaponClass.DAGGER));
    }

    /** Chamada pelo dono: solta do chão e começa a voltar. */
    public void recall() {
        if (isReturning()) return;
        entityData.set(RETURNING, true);
        dealtDamage = true;
        setNoPhysics(true);
        inGround = false;
        returnHits.clear();
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.6F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.2);
        }
    }

    @Override
    public void tick() {
        if (isReturning()) tickReturn();
        if (!isRemoved()) super.tick();
    }

    private void tickReturn() {
        Entity owner = getOwner();
        if (!(owner instanceof Player player) || !player.isAlive() || player.level() != level()) {
            if (!level().isClientSide()) discard();
            return;
        }
        setNoPhysics(true);
        Vec3 to = player.getEyePosition().add(0, -0.4, 0).subtract(position());
        setPosRaw(getX(), getY() + to.y * 0.03, getZ());
        // Acelera em direção ao dono: começa devagar (saindo do chão) e chega rápido.
        setDeltaMovement(getDeltaMovement().scale(0.88).add(to.normalize().scale(0.16)));

        if (level() instanceof ServerLevel server) {
            WeaponData data = ModularWeaponItem.getData(stack());
            Vec3 back = position().subtract(getDeltaMovement());
            SkillVfx.line(server, back, position(), SkillVfx.particleFor(data.elementType()), 4);
            if (tickCount % 4 == 0) {
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.4F, 2.0F);
            }
            for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.6),
                    e -> e != player && e.isAlive() && !(e instanceof Player))) {
                if (returnHits.add(e.getId())) strike(player, e, 0.6F);
            }
            if (distanceTo(player) < 1.5) {
                ClassAbility.daggerCaught(player, this);
                discard();
            }
        }
    }

    /** Dono passando por cima da adaga cravada também a recolhe. */
    @Override
    public void playerTouch(Player player) {
        if (!level().isClientSide() && inGround && player == getOwner()) {
            ClassAbility.daggerCaught(player, this);
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (dealtDamage || !(result.getEntity() instanceof LivingEntity target) || !(getOwner() instanceof Player player)) {
            return;
        }
        dealtDamage = true;
        strike(player, target, 1.5F);
        // Ricocheteia e cai: depois fica cravada no chão, esperando ser chamada.
        setDeltaMovement(getDeltaMovement().multiply(-0.05, -0.1, -0.05));
        playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.5F);
    }

    /** Dano da adaga com as mesmas regras do corpo a corpo (tier, maestria, elemento). */
    protected void strike(Player player, LivingEntity target, float mult) {
        WeaponData data = ModularWeaponItem.getData(stack());
        float raw = (data.weaponClass().baseDamage() + 1.0F) * mult;
        float damage = CombatEvents.weaponDamage(player, data, target, raw, true);
        target.hurt(damageSources().thrown(this, player), damage);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    /** Cravada no chão ela não some sozinha enquanto o dono existir. */
    @Override
    protected void tickDespawn() {
        if (getOwner() == null) super.tickDespawn();
    }

    @Override
    protected float getWaterInertia() {
        return 0.9F;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(STACK, getPickupItemStackOrigin().copy());
        dealtDamage = tag.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("DealtDamage", dealtDamage);
    }
}
