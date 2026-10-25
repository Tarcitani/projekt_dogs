package com.tarcitani.solomod.entity.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Poldro de Cinzas Vulcânicas (Fogo): pequeno wyvern que fica empoleirado no ombro do jogador. Vínculo: Fungo
 * Carmesim. Habilidade: Passo de Cinzas (2 cargas, exaustão de 45 s). Ao pisar em magma ou lava rasa, o jogador
 * ganha Resistência ao Fogo por 6 s, que anula o dano do piso e deixa andar sobre a lava (sem trocar blocos).
 */
public class VolcanicAshColt extends PassiveEchoEntity {
    /** Duração do efeito do Passo de Cinzas: 6 s. */
    private static final int ASH_STEP_TICKS = 6 * 20;
    /** Lado e altura do ombro, em blocos. */
    private static final double SHOULDER_SIDE = 0.4;
    private static final double SHOULDER_HEIGHT = 1.4;
    /** Impulso vertical enquanto anda sobre a lava, para ficar na superfície. */
    private static final double LAVA_FLOAT = 0.12;

    public VolcanicAshColt(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected double hoverHeight() {
        return 2.6;
    }

    @Override
    protected int maxCharges() {
        return 2;
    }

    @Override
    protected int cooldownTicks() {
        return 45 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.5, getZ(), 12, 0.3, 0.2, 0.3, 0.02);
        }
        playSound(SoundEvents.FIRECHARGE_USE, 0.7F, 0.8F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xFF3D00);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (onHotFloor(owner) && owner.getEffect(MobEffects.FIRE_RESISTANCE) == null && !isExhausted()) {
            useAbility(owner);
            owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ASH_STEP_TICKS));
        }
        if (owner.isInLava() && owner.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            Vec3 motion = owner.getDeltaMovement();
            owner.setDeltaMovement(motion.x, Math.max(motion.y, LAVA_FLOAT), motion.z);
            owner.resetFallDistance();
        }
        Vec3 shoulder = owner.getLookAngle().yRot((float) Math.toRadians(90)).multiply(1, 0, 1).normalize()
                .scale(SHOULDER_SIDE);
        flyTowards(owner.position().add(shoulder).add(0, SHOULDER_HEIGHT, 0), 0.25);
    }

    /** Magma sob os pés ou lava rasa na altura do corpo. */
    private static boolean onHotFloor(ServerPlayer owner) {
        BlockPos feet = owner.blockPosition();
        return owner.level().getBlockState(owner.getOnPos()).is(Blocks.MAGMA_BLOCK) || owner.isInLava()
                || owner.level().getBlockState(feet).is(Blocks.LAVA);
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.CRIMSON_FUNGUS);
    }
}
