package com.tarcitani.solomod.entity.passive;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.TierMob;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Salamandra de Núcleo Ígneo (Fogo): réptil rasteiro, imune a fogo e lava. Vínculo: Creme de Magma.
 * Habilidade: Dreno Térmico (3 cargas, exaustão de 25 s). Se o jogador estiver em chamas com um Diabrete Ígneo
 * ou Executor Infernal por perto, apaga o fogo e ganha 2 corações de Absorção.
 */
public class IgneousSalamander extends PassiveEchoEntity {
    /** Raio, em blocos, em que um inimigo de fogo ativa o dreno. */
    private static final double FIRE_FOE_RANGE = 16.0;
    /** Absorção concedida: 2 corações = 4 pontos. */
    private static final float ABSORPTION = 4.0F;
    private static final double FOLLOW_DISTANCE_SQR = 4.0 * 4.0;

    public IgneousSalamander(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected int maxCharges() {
        return 3;
    }

    @Override
    protected int cooldownTicks() {
        return 25 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        owner.clearFire();
        owner.setAbsorptionAmount(owner.getAbsorptionAmount() + ABSORPTION);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, owner.getX(), owner.getY() + 1.0, owner.getZ(), 20, 0.4, 0.5, 0.4, 0.02);
        }
        playSound(SoundEvents.FIRE_EXTINGUISH, 0.9F, 1.1F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xFF5722);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (owner.isOnFire() && !isExhausted() && hasFireFoeNear(owner)) {
            useAbility(owner);
        }
        if (distanceToSqr(owner) > FOLLOW_DISTANCE_SQR) {
            getNavigation().moveTo(owner, 1.0);
        }
    }

    /** Diabrete Ígneo (NORMAL) ou Executor Infernal (OP) com elemento de fogo a até {@link #FIRE_FOE_RANGE} blocos. */
    private boolean hasFireFoeNear(ServerPlayer owner) {
        AABB box = owner.getBoundingBox().inflate(FIRE_FOE_RANGE);
        List<TierMob> foes = owner.level().getEntitiesOfClass(TierMob.class, box,
                mob -> (mob.tier() == MobTier.NORMAL || mob.tier() == MobTier.OP) && mob.element() == ElementType.FIRE);
        return !foes.isEmpty();
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.MAGMA_CREAM);
    }
}
