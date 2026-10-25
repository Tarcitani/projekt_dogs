package com.tarcitani.solomod.entity.passive;

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
import net.minecraft.world.phys.Vec3;

/**
 * Escaravelho de Sílica Dourada (Fogo): besouro que flutua na altura do peito do jogador. Vínculo: Pepitas de Ouro
 * ao meio-dia (entre 5000 e 7000 ticks do dia). Habilidade: Espelho Solar (2 cargas, exaustão de 40 s), acionada
 * pelo {@link PassiveEchoTriggers#solarMirror} quando um projétil ígneo chega a 3 blocos do jogador.
 */
public class GoldenSilicaScarab extends PassiveEchoEntity {
    /** Altura do vôo em relação ao jogador (altura do peito). */
    private static final double CHEST_HEIGHT = 1.2;
    /** Distância lateral de vôo, em blocos. */
    private static final double SIDE_OFFSET = 1.2;
    private static final long NOON_START = 5000L;
    private static final long NOON_END = 7000L;

    public GoldenSilicaScarab(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected double hoverHeight() {
        return 1.6;
    }

    @Override
    protected int maxCharges() {
        return 2;
    }

    @Override
    protected int cooldownTicks() {
        return 40 * 20;
    }

    /** O rebate em si é feito pelo projétil; aqui só o flash dourado de confirmação. */
    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLASH, getX(), getY() + 0.3, getZ(), 1, 0, 0, 0, 0);
        }
        playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.4F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xFFD54F);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        Vec3 side = owner.getLookAngle().yRot((float) Math.toRadians(90)).multiply(1, 0, 1).normalize().scale(SIDE_OFFSET);
        flyTowards(owner.position().add(side).add(0, CHEST_HEIGHT, 0), 0.2);
    }

    /** Pepitas de Ouro só vinculam ao meio-dia. */
    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        long time = player.level().getDayTime() % 24000L;
        return held.is(Items.GOLD_NUGGET) && time >= NOON_START && time <= NOON_END;
    }
}
