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
 * Golem Pedregulho Rúnico (Terra): esfera de pedra entalhada que rola atrás do jogador. Vínculo: Quartzo do Nether
 * ou Ferro Bruto. Gravidade de Chumbo (2 cargas, exaustão de 35 s): quando um Guardião dispara a Repulsão, ou um
 * Elite de Terra acerta um golpe pesado, o jogador fica imune a empurrões por 3 s ({@code PassiveEchoTriggers}).
 * A rolagem visual ainda não está ligada: anda a pé como os outros mobs de solo.
 */
public class RunicBoulderGolem extends PassiveEchoEntity {
    /** Distância ao quadrado a partir da qual anda até o dono (4 blocos). */
    private static final double FOLLOW_DISTANCE_SQR = 4.0 * 4.0;
    private static final double FOLLOW_SPEED = 1.0;

    public RunicBoulderGolem(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
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
            server.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.4, getZ(), 16, 0.4, 0.3, 0.4, 0.05);
        }
        playSound(SoundEvents.STONE_BREAK, 1.0F, 0.6F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xFFB300);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (distanceToSqr(owner) > FOLLOW_DISTANCE_SQR) {
            getNavigation().moveTo(owner, FOLLOW_SPEED);
        }
    }

    /** Quartzo do Nether ou Ferro Bruto vinculam o golem. */
    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.QUARTZ) || held.is(Items.RAW_IRON);
    }
}
