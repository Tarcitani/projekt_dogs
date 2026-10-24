package com.tarcitani.solomod.entity;

import com.tarcitani.solomod.item.ModItems;
import com.tarcitani.solomod.skill.ClassAbility;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Adaga Relâmpago (protótipo, tier chefe, tema raio). É a adaga arremessada de sempre, com duas diferenças:
 * <ul>
 *   <li>enquanto está no ar ou cravada, o dono usa de novo (clique direito) e se teletransporta até ela;
 *       ela vai para o inventário na hora ({@link #teleportOwner});</li>
 *   <li>ao tocar o chão (ou um mob) fica 1 s parada e volta sozinha, com a animação de volta das outras adagas.</li>
 * </ul>
 * O dano é fixo (protótipo): não passa pela maestria/elemento, porque o raio ainda não é um elemento do mod.
 */
public class LightningDagger extends ThrownDagger {
    /** Tempo cravada/parada antes de voltar sozinha (1 s). */
    public static final int REST_TICKS = 20;
    /** Se não acertar nada, volta depois de 5 s de voo. */
    private static final int FLIGHT_LIMIT = 100;
    public static final float DAMAGE = 14.0F;

    private int restTicks;
    private Vec3 flight = Vec3.ZERO;

    public LightningDagger(EntityType<? extends LightningDagger> type, Level level) {
        super(type, level);
    }

    public LightningDagger(Level level, LivingEntity owner, ItemStack weapon) {
        super(ModEntities.LIGHTNING_DAGGER.get(), level, owner, weapon);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.LIGHTNING_DAGGER.get());
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && !isReturning()) {
            if (!isStuck() && getDeltaMovement().lengthSqr() > 0.01) flight = getDeltaMovement();
            boolean resting = isStuck() || dealtDamage;
            if ((resting && ++restTicks >= REST_TICKS) || tickCount > FLIGHT_LIMIT) recall();
            if (level() instanceof ServerLevel server && tickCount % 2 == 0) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 2, 0.15, 0.15, 0.15, 0.1);
            }
        }
        super.tick();
    }

    @Override
    protected void strike(Player player, LivingEntity target, float mult) {
        target.hurt(damageSources().thrown(this, player), DAMAGE * mult);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getBbHeight() * 0.5,
                    target.getZ(), 16, 0.3, 0.4, 0.3, 0.3);
            server.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.5F, 1.8F);
        }
    }

    /**
     * Teletransporta o dono para a adaga e a devolve ao inventário. Se não houver espaço livre para ele
     * (adaga cravada dentro de um vão apertado), só chama a adaga de volta. Devolve se o teleporte aconteceu.
     */
    public boolean teleportOwner(ServerPlayer player) {
        Vec3 spot = findSpot(player);
        if (spot == null) {
            recall();
            return false;
        }
        ServerLevel level = player.serverLevel();
        Vec3 from = player.position().add(0, 1.0, 0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, from.x, from.y, from.z, 30, 0.3, 0.6, 0.3, 0.4);
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.7F, 1.9F);
        player.teleportTo(spot.x, spot.y, spot.z);
        player.fallDistance = 0;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spot.x, spot.y + 1.0, spot.z, 40, 0.3, 0.6, 0.3, 0.5);
        level.sendParticles(ParticleTypes.FLASH, spot.x, spot.y + 1.0, spot.z, 1, 0, 0, 0, 0);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.6F);
        ClassAbility.daggerCaught(player, this);
        discard();
        return true;
    }

    /** Primeiro ponto livre para o jogador perto da adaga, recuando no sentido contrário ao do voo. */
    private Vec3 findSpot(ServerPlayer player) {
        Vec3 back = flight.lengthSqr() > 1.0E-4 ? flight.normalize().scale(-1) : player.getLookAngle().scale(-1);
        for (int step = 0; step <= 8; step++) {
            Vec3 point = position().add(back.scale(step * 0.25));
            // Cravada no teto o jogador fica pendurado abaixo dela: testa também os pés mais baixos.
            for (double dy : new double[]{0.05, -0.9, -1.85}) {
                AABB box = player.getBoundingBox().move(point.x - player.getX(), point.y + dy - player.getY(), point.z - player.getZ());
                if (level().noCollision(player, box)) return new Vec3(point.x, point.y + dy, point.z);
            }
        }
        return null;
    }
}
