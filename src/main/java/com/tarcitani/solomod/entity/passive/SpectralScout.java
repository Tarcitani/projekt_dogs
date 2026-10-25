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
 * Batedor Espectral (Neutro): ave de névoa que voa 3 blocos atrás do jogador. Vínculo: Fragmento de Ametista.
 * Habilidade: Pulso de Afastamento (2 cargas, exaustão de 35 s). O efeito contra o Lacaio ainda não está ligado.
 */
public class SpectralScout extends PassiveEchoEntity {
    public SpectralScout(EntityType<? extends PassiveEchoEntity> type, Level level) {
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
        return 1.2;
    }

    @Override
    protected int maxCharges() {
        return 2;
    }

    @Override
    protected int cooldownTicks() {
        return 35 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.7, getZ(), 24, 1.2, 0.4, 1.2, 0.05);
        }
        playSound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 0.8F, 1.6F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0x00E5FF);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        Vec3 behind = owner.getLookAngle().scale(-3.0).add(0, 1.5, 0);
        flyTowards(owner.position().add(behind), 0.2);
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.AMETHYST_SHARD);
    }
}
