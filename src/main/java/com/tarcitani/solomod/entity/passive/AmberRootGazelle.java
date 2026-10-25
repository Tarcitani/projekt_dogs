package com.tarcitani.solomod.entity.passive;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Gazela das Raízes de Âmbar (Terra): cervo esbelto de trote ágil que segue o jogador. Vínculo: aproximar-se
 * agachado com uma muda de árvore. Seiva Libertadora (2 cargas, exaustão de 30 s): quando Raízes Ancestrais brotam
 * sob o jogador, o pólen destrói a raiz, limpa Fadiga de Mineração e dá Velocidade II por 3 s
 * ({@code PassiveEchoTriggers.amberPollen}).
 */
public class AmberRootGazelle extends PassiveEchoEntity {
    /** Distância ao quadrado a partir da qual trota até o dono (4 blocos). */
    private static final double FOLLOW_DISTANCE_SQR = 4.0 * 4.0;
    private static final double FOLLOW_SPEED = 1.3;

    public AmberRootGazelle(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    @Override
    protected int maxCharges() {
        return 2;
    }

    @Override
    protected int cooldownTicks() {
        return 30 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, owner.getX(), owner.getY() + 1.0, owner.getZ(), 16, 0.5, 0.6, 0.5, 0.05);
        }
        playSound(SoundEvents.SWEET_BERRY_BUSH_PLACE, 1.0F, 1.4F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xFFA000);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (distanceToSqr(owner) > FOLLOW_DISTANCE_SQR) {
            getNavigation().moveTo(owner, FOLLOW_SPEED);
        }
    }

    /** Só vincula com o jogador agachado e segurando uma muda. */
    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return player.isShiftKeyDown() && held.is(ItemTags.SAPLINGS);
    }
}
