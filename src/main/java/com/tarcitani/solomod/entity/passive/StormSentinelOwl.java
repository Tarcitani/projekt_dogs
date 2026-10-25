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
 * Coruja Tempestuosa de Sentinela (Ar): voa ao lado direito do ombro do jogador. Vínculo: Carne de Coelho ou Pena.
 * Evasão de Rajada (3 cargas, exaustão de 25 s): quando um Elite telegrafa a Investida, dá um impulso lateral de
 * 3 blocos no próprio jogador ({@code PassiveEchoTriggers.dashTelegraphed}). O vínculo por contato visual (3 s
 * parado) ainda não existe: basta oferecer o item.
 */
public class StormSentinelOwl extends PassiveEchoEntity {
    /** Altura do ombro, em blocos. */
    private static final double SHOULDER_HEIGHT = 1.4;
    /** Distância lateral do ombro, em blocos. */
    private static final double SHOULDER_SIDE = 0.6;

    public StormSentinelOwl(EntityType<? extends PassiveEchoEntity> type, Level level) {
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
        return 2.0;
    }

    @Override
    protected int maxCharges() {
        return 3;
    }

    @Override
    protected int cooldownTicks() {
        return 25 * 20;
    }

    /** O impulso vem do gatilho; aqui, só as faíscas elétricas do ombro. */
    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 14, 0.4, 0.3, 0.4, 0.1);
        }
        playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.25F, 1.8F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0x7C4DFF);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        Vec3 shoulder = owner.getLookAngle().yRot((float) Math.toRadians(90)).multiply(1, 0, 1).normalize()
                .scale(SHOULDER_SIDE);
        flyTowards(owner.position().add(shoulder).add(0, SHOULDER_HEIGHT, 0), 0.25);
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.RABBIT) || held.is(Items.COOKED_RABBIT) || held.is(Items.FEATHER);
    }
}
