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
 * Mini-Tank Silvestre (Terra): urso robusto de placas de granito que anda ao lado do jogador como guarda-costas.
 * Vínculo: Favo de Mel ou Maçã Dourada. Amortecedor Sísmico (2 cargas, exaustão de 40 s): quando um chefe de terra
 * dá o Golpe Sísmico ou o Pisão a até 20 blocos do jogador, ergue uma cúpula âmbar de 5 blocos que corta o
 * lançamento vertical em 85% ({@code PassiveEchoTriggers.quakeStarted}). O corte do tremor de câmera ainda não está.
 */
public class SylvanMiniTank extends PassiveEchoEntity {
    /** Distância ao quadrado a partir da qual anda até o dono (4 blocos). */
    private static final double FOLLOW_DISTANCE_SQR = 4.0 * 4.0;
    private static final double FOLLOW_SPEED = 1.0;

    public SylvanMiniTank(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
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

    /** A cúpula é erguida no próprio {@code PassiveEchoTriggers}; aqui, só a poeira do impacto. */
    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 20, 1.5, 0.3, 1.5, 0.02);
        }
        playSound(SoundEvents.ROOTED_DIRT_PLACE, 1.0F, 0.8F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0x689F38);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (distanceToSqr(owner) > FOLLOW_DISTANCE_SQR) {
            getNavigation().moveTo(owner, FOLLOW_SPEED);
        }
    }

    /** Favo de Mel ou Maçã Dourada vinculam o urso. */
    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.HONEYCOMB) || held.is(Items.GOLDEN_APPLE);
    }
}
