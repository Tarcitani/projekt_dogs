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
import net.minecraft.world.phys.Vec3;

/**
 * Axolote de Mana Líquida (Água): flutua em uma bolha ao lado do jogador. Vínculo: Peixe Tropical.
 * Purificação Abissal (3 cargas, exaustão de 30 s): enquanto tem cargas, dá respiração aquática ao jogador
 * debaixo d'água. Se o jogador recebe Lentidão, Cegueira ou Náusea (o atordoamento do Tritão), a bolha estoura:
 * remove esses efeitos e dá Graça do Golfinho por 5 s.
 */
public class LiquidManaAxolotl extends PassiveEchoEntity {
    private static final int BREATH_TICKS = 40;
    private static final int DOLPHIN_TICKS = 5 * 20;
    /** Lado da bolha, em blocos. */
    private static final double SIDE_OFFSET = 1.0;
    private static final double BUBBLE_HEIGHT = 0.6;

    public LiquidManaAxolotl(EntityType<? extends PassiveEchoEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected double hoverHeight() {
        return 1.0;
    }

    @Override
    protected int maxCharges() {
        return 3;
    }

    @Override
    protected int cooldownTicks() {
        return 30 * 20;
    }

    @Override
    protected void onAbility(ServerPlayer owner) {
        owner.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        owner.removeEffect(MobEffects.BLINDNESS);
        owner.removeEffect(MobEffects.CONFUSION);
        owner.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, DOLPHIN_TICKS));
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.BUBBLE_POP, owner.getX(), owner.getY() + 1.0, owner.getZ(), 24, 0.6, 0.6, 0.6, 0.05);
        }
        playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 0.9F, 1.2F);
    }

    @Override
    protected ParticleOptions astralParticle() {
        return dust(0xE0F7FA);
    }

    @Override
    protected void followOwner(ServerPlayer owner) {
        if (owner.isUnderWater() && getCharges() > 0 && !isExhausted()) {
            owner.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, BREATH_TICKS, 0, true, false));
        }
        if (!isExhausted() && (owner.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || owner.hasEffect(MobEffects.BLINDNESS)
                || owner.hasEffect(MobEffects.CONFUSION))) {
            useAbility(owner);
        }
        Vec3 side = owner.getLookAngle().yRot((float) Math.toRadians(-90)).multiply(1, 0, 1).normalize()
                .scale(SIDE_OFFSET);
        flyTowards(owner.position().add(side).add(0, BUBBLE_HEIGHT, 0), 0.2);
    }

    @Override
    protected boolean isBondOffer(Player player, ItemStack held) {
        return held.is(Items.TROPICAL_FISH);
    }
}
