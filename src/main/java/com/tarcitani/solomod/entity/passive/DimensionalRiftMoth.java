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
 * Mariposa da Fenda Dimensional (Ar): flutua atrás das costas do jogador. Vínculo: Fruta do Coro Assada.
 * Passo de Fenda (1 carga, exaustão de 60 s): anula o dano de teleporte das Pérolas do End
 * ({@code PassiveEchoEvents}) e, no estágio 6 do Soberano, quando o Raio do Peito dispara no jogador, teleporta-o
 * 6 blocos atrás do Soberano ({@code PassiveEchoTriggers.chestBeamFired}).
 */
public class DimensionalRiftMoth extends PassiveEchoEntity {
    /** Distância atrás das costas do jogador, em blocos. */
    private static final double BACK_DISTANCE = 1.5;
    private static final double BACK_HEIGHT = 1.5;

    public DimensionalRiftMoth(EntityType<? extends PassiveEchoEntity> type, Level level) {
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
        return 3.5;
    }

    @Override
    protected int maxCharges() {
        return 1;
    }

    @Override
    protected int cooldownTicks() {
        return 60 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, owner.getX(), owner.getY() + 1.0, owner.getZ(), 30, 0.5, 0.8, 0.5, 0.2);
        }
        playSound(SoundEvents.ENDERMAN_TELEPORT, 0.6F, 1.6F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xD500F9);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        Vec3 back = owner.getLookAngle().scale(-BACK_DISTANCE).add(0, BACK_HEIGHT, 0);
        flyTowards(owner.position().add(back), 0.2);
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.POPPED_CHORUS_FRUIT);
    }
}
