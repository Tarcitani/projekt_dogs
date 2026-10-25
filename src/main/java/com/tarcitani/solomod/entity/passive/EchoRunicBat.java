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
 * Morcego Eco-Rúnico (Neutro): voa em órbita 2,5 blocos acima do jogador. Vínculo: Glow Berries com o jogador agachado.
 * Habilidade: Sonar Arcano (3 cargas, exaustão de 30 s). O contorno azul em inimigos ainda não está ligado.
 */
public class EchoRunicBat extends PassiveEchoEntity {
    /** Raio da órbita ao redor do jogador, em blocos. */
    private static final double ORBIT_RADIUS = 2.0;
    /** Altura da órbita acima do jogador, em blocos. */
    private static final double ORBIT_HEIGHT = 2.5;
    /** Velocidade angular da órbita, em radianos por tick. */
    private static final double ORBIT_SPEED = 0.05;

    public EchoRunicBat(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected double hoverHeight() {
        return 3.0;
    }

    @Override
    protected int maxCharges() {
        return 3;
    }

    @Override
    protected int cooldownTicks() {
        return 30 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 0.4, getZ(), 16, 0.6, 0.3, 0.6, 0.02);
        }
        playSound(SoundEvents.SCULK_CLICKING, 0.9F, 1.3F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0x1E88E5);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        double angle = tickCount * ORBIT_SPEED;
        Vec3 orbit = new Vec3(Math.cos(angle) * ORBIT_RADIUS, ORBIT_HEIGHT, Math.sin(angle) * ORBIT_RADIUS);
        flyTowards(owner.position().add(orbit), 0.15);
    }

    /** Glow Berries só vinculam com o jogador agachado, para não vincular por acidente. */
    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return player.isShiftKeyDown() && held.is(Items.GLOW_BERRIES);
    }
}
