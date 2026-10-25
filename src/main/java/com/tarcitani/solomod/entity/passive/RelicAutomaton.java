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
 * Autômato Relíquia (Neutro): construto de pedra terrestre e pesado. Nasce sem vínculo (parado) e segue o jogador
 * depois de ser ativado com Bloco de Lápis-Lazúli ou Olho do Fim.
 * Habilidade: Égide Cinética (1 carga, exaustão de 50 s). O escudo contra a Rajada Preditiva ainda não está ligado
 * e os 18 slots de baú ainda não existem.
 */
public class RelicAutomaton extends PassiveEchoEntity {
    /** Distância ao quadrado, a partir da qual o autômato anda até o dono (4 blocos). */
    private static final double FOLLOW_DISTANCE_SQR = 4.0 * 4.0;
    private static final double FOLLOW_SPEED = 1.0;

    public RelicAutomaton(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected int maxCharges() {
        return 1;
    }

    @Override
    protected int cooldownTicks() {
        return 50 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 0.6, getZ(), 30, 0.7, 0.4, 0.7, 0.1);
        }
        playSound(SoundEvents.IRON_GOLEM_REPAIR, 0.9F, 0.8F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xD4AF37);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (distanceToSqr(owner) > FOLLOW_DISTANCE_SQR) {
            getNavigation().moveTo(owner, FOLLOW_SPEED);
        }
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.LAPIS_BLOCK) || held.is(Items.ENDER_EYE);
    }
}
