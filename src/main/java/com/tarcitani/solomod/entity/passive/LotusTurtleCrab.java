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

/**
 * Caranguejo-Tartaruga de Lótus (Água): terrestre, anda ao lado do jogador. Vínculo: Bloco de Musgo ou Erva Marinha.
 * Bolha de Ancoragem (2 cargas, exaustão de 40 s): ao surgir um Maremoto do Leviatã ou do Soberano a até 24 blocos,
 * envolve o jogador por 4 s e reduz em 80% o empurrão das ondas (ver {@code PassiveEchoTriggers.dampenPush}).
 */
public class LotusTurtleCrab extends PassiveEchoEntity {
    /** Distância ao quadrado a partir da qual anda até o dono (4 blocos). */
    private static final double FOLLOW_DISTANCE_SQR = 4.0 * 4.0;
    private static final double FOLLOW_SPEED = 1.0;

    public LotusTurtleCrab(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
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
        PassiveEchoTriggers.raiseBubble(owner);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, owner.getX(), owner.getY(), owner.getZ(), 30, 0.8, 0.8, 0.8, 0.05);
        }
        playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 1.0F, 0.9F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xE040FB);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (distanceToSqr(owner) > FOLLOW_DISTANCE_SQR) {
            getNavigation().moveTo(owner, FOLLOW_SPEED);
        }
    }

    /** Musgo ou Erva Marinha (seagrass) vinculam a tartaruga. */
    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.MOSS_BLOCK) || held.is(Items.SEAGRASS);
    }
}
