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
 * Rêmora de Fossa Abissal (Água): presa às costas do jogador. Vínculo: Fragmento de Prismarinho.
 * Intercepção Marinha (3 cargas, exaustão de 25 s): as esferas perseguidoras do Leviatã que chegam a 2,5 blocos
 * do jogador são estouradas pelo jato, uma por carga ({@code PassiveEchoTriggers.interceptChaser}).
 */
public class AbyssalTrenchRemora extends PassiveEchoEntity {
    /** Altura de vôo em relação ao jogador (costas), em blocos. */
    private static final double BACK_HEIGHT = 1.0;
    /** Distância atrás do jogador, em blocos. */
    private static final double BACK_DISTANCE = 0.5;

    public AbyssalTrenchRemora(EntityType<? extends PassiveEchoEntity> type, Level level) {
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
        return 2.2;
    }

    @Override
    protected int maxCharges() {
        return 3;
    }

    @Override
    protected int cooldownTicks() {
        return 25 * 20;
    }

    /** O estouro da esfera acontece no próprio Leviatã; aqui, só o jato do Remora. */
    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY(), getZ(), 16, 0.2, 0.2, 0.2, 0.1);
        }
        playSound(SoundEvents.DOLPHIN_SWIM, 0.8F, 1.3F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0x00B8D4);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        Vec3 back = owner.getLookAngle().scale(-BACK_DISTANCE).add(0, BACK_HEIGHT, 0);
        flyTowards(owner.position().add(back), 0.25);
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.PRISMARINE_SHARD);
    }
}
