package com.tarcitani.solomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.network.PlayerAnimPayload;
import com.tarcitani.solomod.network.RollAnimationPayload;
import com.tarcitani.solomod.event.StatsLogic.Dodge;
import com.tarcitani.solomod.event.StatsLogic.WeaponAbilities;
import com.tarcitani.solomod.skill.PlayerAnim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

/**
 * Animações das habilidades das armas no jogador:
 * <ul>
 *   <li>Espada: preparação, meia-lua horizontal, corte ascendente ou dois cortes cruzados e recuperação;
 *       braços e mão de apoio acompanham os rastros 3D de WeaponEffects via mixin após setupAnim.</li>
 *   <li>Rapieira: carga/dash, rajada de três estocadas e estocada seguida de recuo, com poses de esgrima.</li>
 *   <li>Martelo: salto/mergulho, giro de duas mãos e preparação vertical da onda sísmica.</li>
 *   <li>Adaga: arremesso, puxada de retorno e reaparição no teleporte.</li>
 *   <li>Rolamento: recolhe braços/pernas e dá uma cambalhota na direção validada do deslocamento.</li>
 * </ul>
 * O clique esquerdo continua vanilla. As habilidades vêm do servidor; só a pose de carga da rapieira
 * acompanha o estado de uso nativo do jogador. Nenhuma pose de câmera causa dano ou movimento.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class PlayerAnimations {
    private PlayerAnimations() {}

    private record Playing(PlayerAnim anim, long start, int duration, Vec3 direction) {}

    private static final Map<Integer, Playing> PLAYING = new HashMap<>();
    private static boolean pushed;

    public static void onPayload(PlayerAnimPayload p) {
        start(p.entityId(), p.anim(), p.duration());
    }

    private static void start(int entityId, PlayerAnim anim, int duration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) PLAYING.put(entityId, new Playing(anim, mc.level.getGameTime(), Math.max(1, duration), Vec3.ZERO));
    }

    public static void onRoll(RollAnimationPayload p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) PLAYING.put(p.entityId(), new Playing(PlayerAnim.ROLL, mc.level.getGameTime(), Dodge.TOTAL_TICKS,
                new Vec3(p.dirX(), 0, p.dirZ()).normalize()));
    }

    /** Progresso 0..1 da animação do jogador, ou -1 se não houver. */
    private static float progress(Player player, PlayerAnim anim, float partial) {
        Playing p = PLAYING.get(player.getId());
        if (p == null || p.anim() != anim) return -1;
        if (!player.isAlive() || !matchesWeapon(player, anim)
                || (anim == PlayerAnim.HAMMER_DIVE && player.onGround() && player.level().getGameTime() - p.start() > 2)) {
            PLAYING.remove(player.getId());
            return -1;
        }
        float t = (player.level().getGameTime() - p.start() + partial) / p.duration();
        if (t >= 1) {
            PLAYING.remove(player.getId());
            return -1;
        }
        return Math.max(0, t);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PLAYING.clear();
    }

    private static float rapierCharge(Player player, float partial) {
        ItemStack stack = player.getUseItem();
        if (!player.isUsingItem() || player.getUsedItemHand() != InteractionHand.MAIN_HAND
                || !(stack.getItem() instanceof ModularWeaponItem item) || item.weaponClass() != WeaponClass.RAPIER) return -1;
        return Mth.clamp((player.getTicksUsingItem() + partial) / WeaponAbilities.RAPIER_CHARGE_TICKS, 0, 1);
    }

    // ------------------------------------------------------------------ corpo (terceira pessoa / outros jogadores)

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        Playing playing = PLAYING.get(player.getId());
        float partial = event.getPartialTick();
        float charge = rapierCharge(player, partial);
        if (charge >= 0) {
            event.getPoseStack().pushPose();
            pushed = true;
            float yaw = player.yBodyRot * Mth.DEG_TO_RAD;
            event.getPoseStack().mulPose(new Quaternionf().rotateAxis(-12F * charge * Mth.DEG_TO_RAD,
                    Mth.cos(yaw), 0, Mth.sin(yaw)));
            return;
        }
        if (playing == null) return;
        float t = progress(player, playing.anim(), partial);
        if (t < 0) return;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pushed = true;
        float yaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        float arc = Mth.sin(t * Mth.PI);
        switch (playing.anim()) {
            case SPIN -> {
                float ease = 1 - (1 - t) * (1 - t);
                pose.mulPose(Axis.YP.rotationDegrees(-360F * ease));
            }
            case THRUST -> {
                pose.mulPose(new Quaternionf().rotateAxis(35F * arc * Mth.DEG_TO_RAD, Mth.cos(yaw), 0, Mth.sin(yaw)));
                pose.scale(1F, 1F - 0.08F * arc, 1F);
            }
            case RAPIER_FLURRY -> {
                float jab = flurryPulse(t * playing.duration());
                pose.mulPose(new Quaternionf().rotateAxis(12F * jab * Mth.DEG_TO_RAD, Mth.cos(yaw), 0, Mth.sin(yaw)));
                pose.mulPose(Axis.YP.rotationDegrees(-8F * jab));
            }
            case RAPIER_RETREAT -> {
                float age = t * playing.duration(), jab = jabPulse(age, WeaponAbilities.RETREAT_HIT);
                float back = Mth.sin(Mth.clamp((age - WeaponAbilities.RETREAT_HIT) / 10F, 0, 1) * Mth.PI);
                pose.mulPose(new Quaternionf().rotateAxis((15F * jab - 12F * back) * Mth.DEG_TO_RAD, Mth.cos(yaw), 0, Mth.sin(yaw)));
            }
            case HAMMER_SWEEP -> {
                float turn = smooth((t * playing.duration() - WeaponAbilities.HAMMER_SWEEP_HIT) / 8F);
                pose.mulPose(Axis.YP.rotationDegrees(-360F * turn));
                pose.scale(1, 1 - arc * 0.09F, 1);
            }
            case HAMMER_QUAKE -> {
                float age = t * playing.duration();
                float slam = smooth((age - WeaponAbilities.HAMMER_QUAKE_HIT) / 3F);
                float weight = 1 - recovery(age, playing.duration());
                pose.mulPose(new Quaternionf().rotateAxis((-10F + 32F * slam) * weight * Mth.DEG_TO_RAD, Mth.cos(yaw), 0, Mth.sin(yaw)));
            }
            case LEAP_FLIP -> {
                pose.translate(0, 0.9, 0);
                pose.mulPose(new Quaternionf().rotateAxis(360F * t * Mth.DEG_TO_RAD, Mth.cos(yaw), 0, Mth.sin(yaw)));
                pose.translate(0, -0.9, 0);
            }
            case SLAM -> {
                float k = 1 - t;
                pose.scale(1F + 0.2F * k, 1F - 0.3F * k, 1F + 0.2F * k);
            }
            case CRESCENT, RISING_CUT, CROSS_CUT -> {
                float age = t * playing.duration();
                float sweep = stroke(age);
                float recover = recovery(age, playing.duration());
                float turn = 38F - 103F * sweep;
                if (playing.anim() == PlayerAnim.CROSS_CUT) turn += 103F * stroke(age - 10);
                if (playing.anim() == PlayerAnim.RISING_CUT) {
                    turn *= 0.3F;
                    pose.mulPose(new Quaternionf().rotateAxis((16F - 28F * sweep) * (1F - recover) * Mth.DEG_TO_RAD,
                            Mth.cos(yaw), 0, Mth.sin(yaw)));
                }
                pose.mulPose(Axis.YP.rotationDegrees(turn * (1F - recover)));
                pose.scale(1F, 1F - 0.09F * arc, 1F);
            }
            case HAMMER_DIVE -> pose.mulPose(new Quaternionf().rotateAxis(28F * Mth.DEG_TO_RAD,
                    Mth.cos(yaw), 0, Mth.sin(yaw)));
            case DAGGER_THROW -> pose.mulPose(Axis.YP.rotationDegrees(-20F * arc));
            case DAGGER_RECALL -> pose.mulPose(Axis.YP.rotationDegrees(14F * arc));
            case DAGGER_BLINK -> {
                float fade = 0.85F + 0.15F * t;
                pose.scale(fade, 1.12F - 0.12F * t, fade);
            }
            case ROLL -> {
                Vec3 dir = playing.direction().lengthSqr() > 0.01 ? playing.direction() : player.getLookAngle().multiply(1, 0, 1).normalize();
                float p = Mth.clamp(t / 0.75F, 0, 1);
                float rotation = 360F * (p * p * (3F - 2F * p));
                pose.translate(0, 0.65, 0);
                pose.mulPose(new Quaternionf().rotateAxis(rotation * Mth.DEG_TO_RAD, (float) dir.z, 0, (float) -dir.x));
                pose.scale(1F, 1F - 0.22F * arc, 1F);
                pose.translate(0, -0.65, 0);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPost(RenderPlayerEvent.Post event) {
        if (pushed) {
            pushed = false;
            event.getPoseStack().popPose();
        }
    }

    // ------------------------------------------------------------------ primeira pessoa

    /** Poses da arma em primeira pessoa, incluindo a mão principal de jogadores canhotos. */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || event.getHand() != InteractionHand.MAIN_HAND) return;
        PoseStack pose = event.getPoseStack();
        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1F : -1F;
        float charge = rapierCharge(player, event.getPartialTick());
        if (charge >= 0) {
            pose.translate(-0.12F * side * charge, -0.12F * charge, 0.35F * charge);
            pose.mulPose(Axis.YP.rotationDegrees(-18F * side * charge));
            pose.mulPose(Axis.XP.rotationDegrees(-20F * charge));
            pose.mulPose(Axis.ZP.rotationDegrees(side * Mth.sin((player.tickCount + event.getPartialTick()) * 0.9F) * charge));
            return;
        }
        Playing playing = PLAYING.get(player.getId());
        if (playing == null) return;
        float t = progress(player, playing.anim(), event.getPartialTick());
        if (t < 0) return;
        float arc = Mth.sin(t * Mth.PI);
        switch (playing.anim()) {
            case SPIN -> {
                pose.translate(0, -0.1, -0.4);
                pose.mulPose(Axis.YP.rotationDegrees(side * (70F - 220F * (1 - (1 - t) * (1 - t)))));
            }
            case CRESCENT -> {
                float sweep = stroke(t * playing.duration());
                float wind = Mth.clamp(t * WeaponAbilities.CRESCENT_TICKS / WeaponAbilities.CRESCENT_WINDUP, 0, 1);
                float recover = Mth.clamp((t - 0.7F) / 0.3F, 0, 1);
                pose.translate(side * (0.32F * wind - 0.95F * sweep) * (1F - recover),
                        (0.2F * wind - 0.28F * arc) * (1F - recover), (-0.6F * arc + 0.2F * wind) * (1F - recover));
                pose.mulPose(Axis.YP.rotationDegrees(side * (70F * wind - 185F * sweep) * (1F - recover)));
                pose.mulPose(Axis.ZP.rotationDegrees(side * (-35F * wind - 65F * Mth.sin(sweep * Mth.PI)) * (1F - recover)));
                pose.mulPose(Axis.XP.rotationDegrees((-65F * wind + 90F * sweep) * (1F - recover)));
            }
            case RISING_CUT -> {
                float age = t * playing.duration(), sweep = stroke(age);
                float weight = Mth.clamp(age / WeaponAbilities.CRESCENT_WINDUP, 0, 1) * (1F - recovery(age, playing.duration()));
                pose.translate(side * (0.2F - 0.4F * sweep) * weight, (-0.48F + 1.05F * sweep) * weight, -0.55F * weight);
                pose.mulPose(Axis.XP.rotationDegrees((55F - 170F * sweep) * weight));
                pose.mulPose(Axis.ZP.rotationDegrees(side * (-25F + 50F * sweep) * weight));
            }
            case CROSS_CUT -> {
                float age = t * playing.duration(), first = stroke(age), second = stroke(age - 10);
                float weight = Mth.clamp(age / WeaponAbilities.CRESCENT_WINDUP, 0, 1) * (1F - recovery(age, playing.duration()));
                float sweep = first - second;
                pose.translate(side * (0.35F - 0.9F * sweep) * weight, (0.3F - 0.65F * first + 0.6F * second) * weight, -0.6F * weight);
                pose.mulPose(Axis.YP.rotationDegrees(side * (65F - 145F * sweep) * weight));
                pose.mulPose(Axis.ZP.rotationDegrees(side * (-45F + 100F * first - 110F * second) * weight));
                pose.mulPose(Axis.XP.rotationDegrees((-65F + 95F * first - 85F * second) * weight));
            }
            case THRUST -> {
                float extension = Mth.clamp(t / 0.12F, 0, 1) * (1F - Mth.clamp((t - 0.65F) / 0.35F, 0, 1));
                pose.translate(-0.18F * side * extension, -0.12F * extension, -0.95F * extension);
                pose.mulPose(Axis.XP.rotationDegrees(-28F * extension));
                pose.mulPose(Axis.YP.rotationDegrees(-15F * side * extension));
            }
            case RAPIER_FLURRY -> {
                float age = t * playing.duration(), jab = flurryPulse(age);
                pose.translate(-0.12F * side * arc, -0.08F * arc, -0.9F * jab + 0.15F * arc);
                pose.mulPose(Axis.XP.rotationDegrees(-22F * arc - 8F * jab));
                pose.mulPose(Axis.YP.rotationDegrees(-14F * side * arc));
                pose.mulPose(Axis.ZP.rotationDegrees(side * Mth.sin(age * 1.5F) * 2F * jab));
            }
            case RAPIER_RETREAT -> {
                float age = t * playing.duration(), jab = jabPulse(age, WeaponAbilities.RETREAT_HIT);
                float back = Mth.sin(Mth.clamp((age - WeaponAbilities.RETREAT_HIT) / 10F, 0, 1) * Mth.PI);
                pose.translate(-0.18F * side * jab, -0.15F * back, -0.85F * jab + 0.4F * back);
                pose.mulPose(Axis.XP.rotationDegrees(-26F * jab + 15F * back));
                pose.mulPose(Axis.ZP.rotationDegrees(18F * side * back));
            }
            case HAMMER_SWEEP -> {
                float age = t * playing.duration(), wind = smooth(age / WeaponAbilities.HAMMER_SWEEP_HIT);
                float turn = smooth((age - WeaponAbilities.HAMMER_SWEEP_HIT) / 8F), weight = 1 - recovery(age, playing.duration());
                pose.translate(side * (0.3F * wind - 0.9F * turn) * weight, -0.2F * arc, -0.5F * arc);
                pose.mulPose(Axis.YP.rotationDegrees(side * (65F * wind - 250F * turn) * weight));
                pose.mulPose(Axis.ZP.rotationDegrees(side * (-35F * wind - 25F * turn) * weight));
            }
            case HAMMER_QUAKE -> {
                float age = t * playing.duration(), wind = smooth(age / WeaponAbilities.HAMMER_QUAKE_HIT);
                float slam = smooth((age - WeaponAbilities.HAMMER_QUAKE_HIT) / 3F), weight = 1 - recovery(age, playing.duration());
                pose.translate(-0.16F * side * weight, (0.4F * wind - 0.9F * slam) * weight, (0.1F * wind - 0.65F * slam) * weight);
                pose.mulPose(Axis.XP.rotationDegrees((-110F * wind + 175F * slam) * weight));
                pose.mulPose(Axis.ZP.rotationDegrees(-20F * side * weight));
            }
            case LEAP_FLIP -> {
                pose.translate(0.12F * side * arc, 0.35F * arc, 0.15F * arc);
                pose.mulPose(Axis.XP.rotationDegrees(-95F * arc));
            }
            case HAMMER_DIVE -> {
                pose.translate(-0.12F * side, 0.25F * (1F - t), -0.25F * t);
                pose.mulPose(Axis.XP.rotationDegrees(-95F + 130F * t));
                pose.mulPose(Axis.ZP.rotationDegrees(-18F * side));
            }
            case SLAM -> {
                float impact = 1F - t;
                pose.translate(0, -0.45F * impact, -0.35F * impact);
                pose.mulPose(Axis.XP.rotationDegrees(65F * impact));
            }
            case DAGGER_THROW -> {
                float snap = Mth.sin(Mth.clamp(t / 0.65F, 0, 1) * Mth.PI);
                pose.translate(-0.1F * side * snap, 0.18F * snap, -0.8F * snap);
                pose.mulPose(Axis.XP.rotationDegrees(-80F * snap));
                pose.mulPose(Axis.ZP.rotationDegrees(22F * side * snap));
            }
            case DAGGER_RECALL -> {
                pose.translate(0.08F * side * arc, -0.08F * arc, 0.35F * arc);
                pose.mulPose(Axis.ZP.rotationDegrees(30F * side * arc));
            }
            case DAGGER_BLINK -> {
                float emerge = (1F - t) * (1F - t);
                pose.translate(-0.22F * side * emerge, -0.4F * emerge, -0.35F * arc);
                pose.mulPose(Axis.ZP.rotationDegrees(75F * side * emerge));
                pose.mulPose(Axis.YP.rotationDegrees(-25F * side * emerge));
            }
            case ROLL -> {
                pose.translate(0.08F * side * arc, -0.65F * arc, 0.2F * arc);
                pose.mulPose(Axis.XP.rotationDegrees(45F * arc));
                pose.mulPose(Axis.ZP.rotationDegrees(-18F * side * arc));
            }
        }
    }

    private static boolean swordAnimation(PlayerAnim anim) {
        return anim == PlayerAnim.CRESCENT || anim == PlayerAnim.RISING_CUT || anim == PlayerAnim.CROSS_CUT;
    }

    private static boolean matchesWeapon(Player player, PlayerAnim anim) {
        if (anim == PlayerAnim.ROLL) return true;
        if (!(player.getMainHandItem().getItem() instanceof ModularWeaponItem item)) return false;
        return switch (anim) {
            case CRESCENT, RISING_CUT, CROSS_CUT, SPIN -> item.weaponClass() == WeaponClass.LONGSWORD;
            case THRUST, RAPIER_FLURRY, RAPIER_RETREAT -> item.weaponClass() == WeaponClass.RAPIER;
            case HAMMER_SWEEP, HAMMER_QUAKE -> item.weaponClass() == WeaponClass.HAMMER;
            case DAGGER_THROW, DAGGER_RECALL, DAGGER_BLINK -> item.weaponClass() == WeaponClass.DAGGER;
            case LEAP_FLIP, HAMMER_DIVE, SLAM -> item.weaponClass() == WeaponClass.HAMMER || item.weaponClass() == WeaponClass.AXE;
            case ROLL -> true;
        };
    }

    private static float stroke(float age) {
        float p = Mth.clamp((age - WeaponAbilities.CRESCENT_WINDUP) / 5F, 0, 1);
        return p * p * (3F - 2F * p);
    }

    private static float recovery(float age, int duration) { return Mth.clamp((age - duration + 7) / 7F, 0, 1); }

    private static float smooth(float value) {
        float p = Mth.clamp(value, 0, 1);
        return p * p * (3 - 2 * p);
    }

    private static float jabPulse(float age, int hit) {
        return Mth.sin(Mth.clamp((age - hit + 2) / 5F, 0, 1) * Mth.PI);
    }

    private static float flurryPulse(float age) {
        float result = 0;
        for (int i = 0; i < WeaponAbilities.FLURRY_HITS; i++) result = Math.max(result,
                jabPulse(age, WeaponAbilities.FLURRY_FIRST_HIT + i * WeaponAbilities.FLURRY_HIT_GAP));
        return result;
    }

    /** Chamado pelo mixin após setupAnim: arma e braços têm poses reais, copiadas também para mangas/armadura. */
    public static void poseModel(HumanoidModel<?> model, LivingEntity entity, float age) {
        if (!(entity instanceof Player player)) return;
        boolean right = player.getMainArm() == HumanoidArm.RIGHT;
        var weaponArm = right ? model.rightArm : model.leftArm;
        var supportArm = right ? model.leftArm : model.rightArm;
        float side = right ? 1F : -1F;
        float partial = age - player.tickCount;
        float charge = rapierCharge(player, partial);
        if (charge >= 0) {
            weaponArm.xRot = -1.25F + 0.3F * charge;
            weaponArm.yRot = side * (-0.15F - 0.45F * charge);
            weaponArm.zRot = -0.12F * side;
            supportArm.xRot = -0.55F * charge;
            return;
        }
        Playing playing = PLAYING.get(player.getId());
        if (playing == null) return;
        float t = progress(player, playing.anim(), partial);
        if (t < 0) return;
        float k = Mth.sin(t * Mth.PI);
        if (swordAnimation(playing.anim())) {
            float elapsed = t * playing.duration(), stroke = stroke(elapsed);
            float wind = Mth.clamp(elapsed / WeaponAbilities.CRESCENT_WINDUP, 0, 1);
            float weight = wind * (1F - recovery(elapsed, playing.duration()));
            float second = playing.anim() == PlayerAnim.CROSS_CUT ? stroke(elapsed - 10) : 0;
            float swing = stroke - second;
            boolean rising = playing.anim() == PlayerAnim.RISING_CUT;
            weaponArm.xRot = Mth.lerp(weight, weaponArm.xRot, rising ? -0.4F - 2.2F * stroke : -1.6F + 0.75F * swing);
            weaponArm.yRot = side * (0.85F - 2.1F * swing) * weight;
            weaponArm.zRot = side * (-0.55F + 0.35F * swing) * weight;
            supportArm.xRot = Mth.lerp(weight, supportArm.xRot, rising ? -0.3F - 1.8F * stroke : -1.25F + 0.4F * swing);
            supportArm.yRot = side * (-0.7F - 0.7F * swing) * weight;
            model.body.xRot += 0.12F * k;
            model.head.xRot -= 0.12F * k;
        } else if (playing.anim() == PlayerAnim.RAPIER_FLURRY || playing.anim() == PlayerAnim.RAPIER_RETREAT) {
            float elapsed = t * playing.duration();
            float jab = playing.anim() == PlayerAnim.RAPIER_FLURRY ? flurryPulse(elapsed) : jabPulse(elapsed, WeaponAbilities.RETREAT_HIT);
            weaponArm.xRot = Mth.lerp(k, weaponArm.xRot, -1.2F - 0.5F * jab);
            weaponArm.yRot = -0.18F * side * k;
            weaponArm.zRot = -0.12F * side * k;
            supportArm.xRot = 0.45F * k;
            model.rightLeg.xRot -= 0.12F * k;
            model.leftLeg.xRot += 0.12F * k;
        } else if (playing.anim() == PlayerAnim.HAMMER_SWEEP || playing.anim() == PlayerAnim.HAMMER_QUAKE) {
            float elapsed = t * playing.duration(), weight = smooth(elapsed / WeaponAbilities.HAMMER_SWEEP_HIT) * (1 - recovery(elapsed, playing.duration()));
            if (playing.anim() == PlayerAnim.HAMMER_QUAKE) {
                float slam = smooth((elapsed - WeaponAbilities.HAMMER_QUAKE_HIT) / 3F);
                weaponArm.xRot = Mth.lerp(weight, weaponArm.xRot, -2.9F + 2.2F * slam);
                supportArm.xRot = Mth.lerp(weight, supportArm.xRot, -2.6F + 2F * slam);
                weaponArm.yRot = -0.25F * side * weight;
                supportArm.yRot = 0.4F * side * weight;
            } else {
                float turn = smooth((elapsed - WeaponAbilities.HAMMER_SWEEP_HIT) / 8F);
                weaponArm.xRot = Mth.lerp(weight, weaponArm.xRot, -1.3F + 0.5F * turn);
                supportArm.xRot = Mth.lerp(weight, supportArm.xRot, -1.1F + 0.4F * turn);
                weaponArm.yRot = side * (0.7F - 1.7F * turn) * weight;
                supportArm.yRot = side * (-0.5F - 0.6F * turn) * weight;
            }
        } else if (playing.anim() == PlayerAnim.THRUST) {
            weaponArm.xRot = -1.65F;
            weaponArm.yRot = -0.12F * side;
            weaponArm.zRot = -0.1F * side;
            supportArm.xRot = 0.55F * k;
        } else if (playing.anim() == PlayerAnim.DAGGER_THROW || playing.anim() == PlayerAnim.DAGGER_RECALL || playing.anim() == PlayerAnim.DAGGER_BLINK) {
            float reach = playing.anim() == PlayerAnim.DAGGER_THROW ? -1.8F : playing.anim() == PlayerAnim.DAGGER_RECALL ? -1.15F : -0.85F;
            weaponArm.xRot = Mth.lerp(k, weaponArm.xRot, reach);
            weaponArm.yRot = -0.35F * side * k;
            weaponArm.zRot = -0.3F * side * k;
            supportArm.xRot = -0.25F * k;
        } else if (playing.anim() == PlayerAnim.HAMMER_DIVE || playing.anim() == PlayerAnim.LEAP_FLIP) {
            weaponArm.xRot = -2.8F;
            supportArm.xRot = -2.5F;
            weaponArm.yRot = -0.25F * side;
            supportArm.yRot = 0.4F * side;
        } else if (playing.anim() == PlayerAnim.SLAM) {
            weaponArm.xRot = supportArm.xRot = -0.7F * (1F - t);
        } else if (playing.anim() == PlayerAnim.ROLL) {
            model.body.xRot = 0.55F * k;
            model.head.xRot = 0.5F * k;
            model.rightArm.xRot = model.leftArm.xRot = -1.4F * k;
            model.rightArm.yRot = -0.35F * k;
            model.leftArm.yRot = 0.35F * k;
            model.rightLeg.xRot = model.leftLeg.xRot = -1.05F * k;
        }
    }

    /** Câmera: acompanha o corte, dá impulso na estocada e treme no impacto do martelo. */
    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        float partial = (float) event.getPartialTick();
        float spin = progress(player, PlayerAnim.SPIN, partial);
        if (spin >= 0) event.setRoll(event.getRoll() + Mth.sin(spin * Mth.PI) * 6F);
        float crescent = progress(player, PlayerAnim.CRESCENT, partial);
        if (crescent >= 0) {
            event.setRoll(event.getRoll() + Mth.sin(crescent * Mth.PI) * 6F);
            event.setPitch(event.getPitch() - Mth.sin(crescent * Mth.PI) * 2.5F);
        }
        float rising = progress(player, PlayerAnim.RISING_CUT, partial);
        if (rising >= 0) event.setPitch(event.getPitch() - Mth.sin(rising * Mth.PI) * 3F);
        float cross = progress(player, PlayerAnim.CROSS_CUT, partial);
        if (cross >= 0) event.setRoll(event.getRoll() + Mth.sin(cross * Mth.PI * 2) * 4F);
        float roll = progress(player, PlayerAnim.ROLL, partial);
        if (roll >= 0) {
            float k = Mth.sin(roll * Mth.PI);
            event.setPitch(event.getPitch() + k * 8F);
            event.setRoll(event.getRoll() + k * 4F);
        }
        float thrust = progress(player, PlayerAnim.THRUST, partial);
        if (thrust >= 0) event.setPitch(event.getPitch() - Mth.sin(thrust * Mth.PI) * 2F);
        float flurry = progress(player, PlayerAnim.RAPIER_FLURRY, partial);
        if (flurry >= 0) event.setPitch(event.getPitch() - flurryPulse(flurry * WeaponAbilities.FLURRY_TICKS) * 1.2F);
        float retreat = progress(player, PlayerAnim.RAPIER_RETREAT, partial);
        if (retreat >= 0) event.setRoll(event.getRoll() + Mth.sin(retreat * Mth.PI) * 2F);
        float sweep = progress(player, PlayerAnim.HAMMER_SWEEP, partial);
        if (sweep >= 0) event.setRoll(event.getRoll() + Mth.sin(sweep * Mth.PI) * 4F);
        float slam = progress(player, PlayerAnim.SLAM, partial);
        if (slam >= 0) {
            float amp = (1 - slam) * 3.5F;
            float time = player.tickCount + partial;
            event.setPitch(event.getPitch() + Mth.sin(time * 3.7F) * amp);
            event.setYaw(event.getYaw() + Mth.cos(time * 3.1F) * amp);
            event.setRoll(event.getRoll() + Mth.sin(time * 4.3F) * amp);
        }
    }
}
