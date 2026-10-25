package com.tarcitani.solomod.entity.sanctuary;

import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.passive.PassiveEchoEntity;
import com.tarcitani.solomod.era.EraSevenState;
import com.tarcitani.solomod.mob.Hazards;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Caçador de Ecos: chefe da Catacumba Sussurrante (Santuário Neutro, subterrâneo). Caçador encapuzado que persegue
 * os ecos, e é o único do mod que ataca a Fauna Pacífica (corpo a corpo; mire os companheiros por perto). Golpes:
 *
 * <ul>
 *   <li><b>Lâminas</b> (sempre): atira 3 facas arcanas rápidas em leque.</li>
 *   <li><b>Sumir</b> (sempre): fica invisível por 2 s; pegadas de alma aparecem atrás do alvo meio segundo antes de
 *       ele reaparecer ali com um golpe forte. Saia de perto das pegadas.</li>
 *   <li><b>Rede de Ecos</b> (fase 2+): 5 marcas em cruz no alvo que explodem em 1,75 s.</li>
 *   <li><b>Uivo</b> (fase 3): chama 2 {@link WailingShade} (no máximo 4 por perto).</li>
 * </ul>
 */
public class EchoHunter extends SanctuaryBoss {
    private static final int BLADES = 1;
    private static final int VANISH = 2;
    private static final int NET = 3;
    private static final int HOWL = 4;
    /** Reverência da Era VII: ajoelhado diante do Desperto (ver {@link #customServerAiStep}). */
    private static final int REVERENCE = 5;
    private static final int VANISH_STRIKE = 40;
    private static final int TRACKS_AT = 28;
    private static final int REVERENCE_TICKS = 60;
    private static final String REVERENCED_TAG = "SoloReverenced";

    private Vec3 ambush = Vec3.ZERO;
    private int howlCooldown = 200;
    private boolean reverenced;

    public EchoHunter(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.YELLOW);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 240)
                .add(Attributes.ATTACK_DAMAGE, 11)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ARMOR, 6)
                .add(Attributes.FOLLOW_RANGE, 40);
    }

    @Override
    protected void registerGoals() {
        addGroundGoals(1.1);
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PassiveEchoEntity.class, true));
    }

    /**
     * Era VII: se o Soberano já caiu, na primeira vez que mira um jogador ele não ataca. Ajoelha por 3 s diante do
     * Desperto (o mestre agora habita o jogador por inteiro) e só então luta. Vale uma vez por Caçador (fica salvo).
     */
    @Override
    protected void customServerAiStep() {
        if (howlCooldown > 0) howlCooldown--;
        if (!reverenced && attackId() == 0 && getTarget() instanceof ServerPlayer player
                && EraSevenState.isSovereignDefeated(player.server)) {
            reverenced = true;
            setAttack(REVERENCE);
            player.displayClientMessage(Component.translatable("message.solomod.echo_hunter.reverence")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC), true);
            playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.6F);
        }
        super.customServerAiStep();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(REVERENCED_TAG, reverenced);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        reverenced = tag.getBoolean(REVERENCED_TAG);
    }

    @Override
    public float kneel(float partialTick) {
        if (attackId() != REVERENCE) return 0.0F;
        float t = attackTicks() + partialTick;
        return Mth.clamp(t / 8.0F, 0.0F, 1.0F) * Mth.clamp((REVERENCE_TICKS - t) / 8.0F, 0.0F, 1.0F);
    }

    @Override
    protected int chooseAttack(LivingEntity target, double dist, int phase) {
        if (phase >= 3 && howlCooldown <= 0 && countNearby(WailingShade.class, 30) < 4) return HOWL;
        return pick(dist < 20 ? BLADES : 0, VANISH, phase >= 2 ? NET : 0);
    }

    @Override
    protected boolean tickAttack(ServerLevel server, int id, int t, @Nullable LivingEntity target) {
        switch (id) {
            case BLADES -> {
                if (t == 10 && target != null) {
                    Vec3 from = position().add(0, 1.4, 0);
                    Vec3 aim = target.getEyePosition().subtract(from).normalize();
                    for (int i = -1; i <= 1; i++) {
                        Hazards.add(new Hazards.Orb(server, this, ElementType.NONE, from, aim.yRot(i * 0.15F).scale(1.2),
                                damage(0.7F), 1, 30));
                    }
                    Hazards.sound(server, from, SoundEvents.TRIDENT_THROW.value(), 1.2F, 1.4F);
                }
                return t < 24;
            }
            case VANISH -> {
                if (t == 1) {
                    // Sem alvo no meio do golpe, reaparece onde sumiu.
                    ambush = position();
                    setInvisible(true);
                    Hazards.sound(server, position(), SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.4F, 0.7F);
                    server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 25, 0.5, 0.8, 0.5, 0.02);
                }
                if (t == TRACKS_AT && target != null) {
                    ambush = target.position().subtract(target.getLookAngle().multiply(1, 0, 1).normalize().scale(1.6));
                }
                if (t >= TRACKS_AT && t < VANISH_STRIKE) {
                    server.sendParticles(ParticleTypes.SOUL, ambush.x, ambush.y + 0.1, ambush.z, 2, 0.3, 0, 0.3, 0.01);
                }
                if (t == VANISH_STRIKE) {
                    teleportTo(ambush.x, ambush.y, ambush.z);
                    setInvisible(false);
                    Hazards.sound(server, position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.5F, 0.7F);
                    server.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1, getZ(), 3, 0.8, 0.3, 0.8, 0);
                    for (LivingEntity e : Hazards.enemiesIn(server, this, getBoundingBox().inflate(1.8))) {
                        Hazards.hit(this, e, damage(1.5F), ElementType.NONE, 2, false);
                    }
                }
                return t < VANISH_STRIKE + 10;
            }
            case NET -> {
                if (t == 1 && target != null) {
                    Hazards.sound(server, position(), SoundEvents.SCULK_SHRIEKER_SHRIEK, 1.0F, 1.2F);
                    Vec3 c = target.position();
                    Hazards.add(new Hazards.Blast(server, this, ElementType.NONE, c, 2.0, 35, damage(0.8F), 2));
                    for (int i = 0; i < 4; i++) {
                        double a = Math.PI / 2 * i;
                        Hazards.add(new Hazards.Blast(server, this, ElementType.NONE,
                                c.add(Math.cos(a) * 3, 0, Math.sin(a) * 3), 2.0, 35, damage(0.8F), 2));
                    }
                }
                return t < 50;
            }
            case HOWL -> {
                if (t == 12) {
                    Hazards.sound(server, position(), SoundEvents.WOLF_HOWL, 2.0F, 0.5F);
                    summon(server, ModEntities.WAILING_SHADE.get(), 2, 2.5, 2.0);
                    howlCooldown = 400;
                }
                return t < 40;
            }
            case REVERENCE -> {
                if (t % 4 == 0) {
                    server.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.4, getZ(), 3, 0.5, 0.2, 0.5, 0.02);
                }
                return t < REVERENCE_TICKS;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public float swing(float partialTick) {
        float t = attackTicks() + partialTick;
        return switch (attackId()) {
            case BLADES -> Mth.clamp(t / 10.0F, 0.0F, 1.0F) * (t > 12 ? 0.0F : 1.0F);
            case VANISH -> t >= VANISH_STRIKE - 4 && t < VANISH_STRIKE ? 1.0F : 0.0F;
            case HOWL, NET -> Mth.clamp(t / 10.0F, 0.0F, 1.0F);
            default -> 0.0F;
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_NEARBY_CLOSE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }
}
