package com.tarcitani.solomod.entity.passive;

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

/**
 * Arraia das Nuvens (Ar): planeia 4 blocos acima do jogador. Vínculo: Membrana de Fantasma.
 * Corrente Ascendente (2 cargas, exaustão de 40 s): se o jogador cai mais de 8 blocos, a arraia mergulha sob ele:
 * zera o dano de queda e dá Queda Lenta por 4 s. Não é ligada aos Tornados em específico: vale para qualquer queda longa.
 */
public class CloudMantaRay extends PassiveEchoEntity {
    /** Altura de planeio sobre o jogador, em blocos. */
    private static final double GLIDE_HEIGHT = 4.0;
    /** Queda, em blocos, a partir da qual a corrente age. */
    private static final double UPDRAFT_FALL = 8.0;
    private static final int SLOW_FALL_TICKS = 4 * 20;

    public CloudMantaRay(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected double hoverHeight() {
        return 4.5;
    }

    @Override
    protected int maxCharges() {
        return 2;
    }

    @Override
    protected int cooldownTicks() {
        return 40 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        owner.resetFallDistance();
        owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SLOW_FALL_TICKS));
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, owner.getX(), owner.getY(), owner.getZ(), 24, 0.8, 0.2, 0.8, 0.05);
        }
        playSound(SoundEvents.WIND_CHARGE_BURST.value(), 1.0F, 1.2F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xB0BEC5);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (owner.fallDistance > UPDRAFT_FALL && !isExhausted()) {
            useAbility(owner);
        }
        flyTowards(owner.position().add(0, GLIDE_HEIGHT, 0), 0.2);
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.PHANTOM_MEMBRANE);
    }
}
