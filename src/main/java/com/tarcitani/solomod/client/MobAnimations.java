package com.tarcitani.solomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.mob.MobAnim;
import com.tarcitani.solomod.network.MobAnimPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Animações dos mobs do mod, aplicadas ao modelo vanilla inteiro (posição, giro, inclinação, escala).
 *
 * <p><b>Ociosas</b> (sempre, por tier):
 * <ul>
 *   <li>Lacaio: corre curvado para a frente, com trote saltitante.</li>
 *   <li>Elite: balança os ombros de um lado para o outro (postura de duelo).</li>
 *   <li>Guardião: flutua acima do chão, subindo e descendo.</li>
 *   <li>Monarca e Soberano: respiram pesado (o corpo incha e desincha); mais rápido na fase 2, tremendo da
 *       fase 3 em diante (o Soberano tem 6 estágios).</li>
 * </ul>
 *
 * <p><b>De ação</b>: as {@link MobAnim} enviadas pelo servidor a cada ataque, por cima da ociosa.
 * {@code KNOCKED_DOWN} mantém o chefe agachado se recuperando; {@code LAUGH} o faz rir após vencer.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class MobAnimations {
    private MobAnimations() {}

    private static final class State {
        MobTier tier = MobTier.NORMAL;
        ElementType element = ElementType.NONE;
        int phase = 1;
        boolean rage;
        MobAnim anim = MobAnim.NONE;
        long start;
        int duration;
    }

    /** Animação de ação em andamento e seu progresso 0..1 (os modelos próprios posam os membros com isso). */
    public record Action(MobAnim anim, float t) {
        public static final Action NONE = new Action(MobAnim.NONE, 0F);

        public boolean is(MobAnim a) {
            return anim == a;
        }

        /** 0 -> 1 -> 0 ao longo da animação. */
        public float arc() {
            return Mth.sin(t * Mth.PI);
        }
    }

    private static final Map<Integer, State> STATES = new HashMap<>();
    /** Entidades cuja pose foi empilhada no Pre e precisa ser desempilhada no Post. */
    private static final Set<LivingEntity> PUSHED = Collections.newSetFromMap(new WeakHashMap<>());

    public static Action action(LivingEntity entity, float partial) {
        State s = STATES.get(entity.getId());
        if (s == null || s.anim == MobAnim.NONE) return Action.NONE;
        float elapsed = entity.level().getGameTime() - s.start + partial;
        return elapsed >= s.duration ? Action.NONE : new Action(s.anim, Mth.clamp(elapsed / s.duration, 0F, 1F));
    }

    public static ElementType element(LivingEntity entity) {
        State s = STATES.get(entity.getId());
        return s == null ? ElementType.NONE : s.element;
    }

    /** Chefe em Fúria: aura vermelha, tremor e decorações aceleradas. */
    public static boolean isRaging(LivingEntity entity) {
        State s = STATES.get(entity.getId());
        return s != null && s.rage;
    }

    public static int phase(LivingEntity entity) {
        State s = STATES.get(entity.getId());
        return s == null ? 1 : s.phase;
    }

    public static void onPayload(MobAnimPayload p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        State s = STATES.computeIfAbsent(p.entityId(), id -> new State());
        s.tier = p.tier();
        s.element = p.element();
        s.phase = p.phase();
        s.rage = p.rage();
        if (p.anim() != MobAnim.NONE) {
            s.anim = p.anim();
            s.start = mc.level.getGameTime();
            s.duration = Math.max(1, p.duration());
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) STATES.remove(event.getEntity().getId());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        STATES.clear();
    }

    /** Prioridade mínima: roda depois de qualquer outro mod que possa cancelar a renderização. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        State s = STATES.get(entity.getId());
        if (s == null) return;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        PUSHED.add(entity);

        float partial = event.getPartialTick();
        float age = entity.tickCount + partial;
        float size = entity.getScale();
        float yaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);

        idle(pose, entity, s, age, size, partial, yaw);
        if (s.rage) {
            // Fúria: incha um pouco e treme sem parar.
            float swell = 1.06F + Mth.sin(age * 0.5F) * 0.02F;
            pose.scale(swell, swell, swell);
            shake(pose, age, 0.03F * size);
        }

        if (s.anim != MobAnim.NONE) {
            float elapsed = entity.level().getGameTime() - s.start + partial;
            if (elapsed >= s.duration) {
                s.anim = MobAnim.NONE;
            } else {
                action(pose, s.anim, Mth.clamp(elapsed / s.duration, 0F, 1F), age, size, yaw);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
        if (PUSHED.remove(event.getEntity())) event.getPoseStack().popPose();
    }

    // ------------------------------------------------------------------ ociosas

    private static void idle(PoseStack pose, LivingEntity e, State s, float age, float size, float partial, float yaw) {
        switch (s.tier) {
            case NORMAL -> {
                float walk = e.walkAnimation.speed(partial);
                float pos = e.walkAnimation.position(partial);
                pose.translate(0, Math.abs(Mth.sin(pos * 0.66F)) * 0.08F * walk * size, 0);
                lean(pose, yaw, 14F * Math.min(1F, walk * 1.5F));
            }
            case OP -> roll(pose, yaw, Mth.sin(age * 0.1F) * 3.5F);
            case SUB_BOSS -> pose.translate(0, (0.25F + Mth.sin(age * 0.08F) * 0.1F) * size, 0);
            case BOSS -> {
                float speed = s.phase == 1 ? 0.08F : s.phase == 2 ? 0.14F : 0.22F;
                float breath = 1F + Mth.sin(age * speed) * (0.02F + 0.01F * s.phase);
                pose.scale(breath, 1F + (breath - 1F) * 0.5F, breath);
                if (s.phase >= 3) shake(pose, age, 0.025F * size);
            }
        }
    }

    // ------------------------------------------------------------------ de ação

    /** {@code t} = progresso 0..1 da animação. */
    private static void action(PoseStack pose, MobAnim anim, float t, float age, float size, float yaw) {
        float arc = Mth.sin(t * Mth.PI); // 0 -> 1 -> 0
        switch (anim) {
            case WINDUP -> {
                // Agacha cada vez mais e treme: "vai vir algo".
                pose.scale(1F + 0.12F * t, 1F - 0.18F * t, 1F + 0.12F * t);
                shake(pose, age, 0.04F * t * size);
            }
            case LUNGE -> {
                lean(pose, yaw, 40F * arc);
                pose.scale(1F - 0.1F * arc, 1F + 0.15F * arc, 1F - 0.1F * arc);
            }
            case DASH -> {
                float ease = t > 0.8F ? (1F - t) / 0.2F : 1F;
                lean(pose, yaw, 28F * ease);
                stretchForward(pose, yaw, 1F + 0.2F * ease);
            }
            case COMBO -> spin(pose, Mth.sin(t * Mth.PI * 3F) * 40F);
            case DODGE -> roll(pose, yaw, 30F * arc);
            case CAST -> {
                pose.translate(0, 0.45F * arc * size, 0);
                spin(pose, 180F * t);
            }
            case LEAP -> {
                spin(pose, 360F * t);
                pose.scale(1F - 0.1F * arc, 1F + 0.2F * arc, 1F - 0.1F * arc);
            }
            case SLAM -> {
                float squash = 1F - t;
                pose.scale(1F + 0.3F * squash, 1F - 0.35F * squash, 1F + 0.3F * squash);
            }
            case ROAR -> {
                float s = 1F + 0.12F * arc;
                pose.scale(s, s, s);
                lean(pose, yaw, -15F * arc);
                shake(pose, age, 0.05F * arc * size);
            }
            case HOWL -> lean(pose, yaw, -30F * arc);
            case BLINK -> {
                float s = Math.max(0.05F, Math.abs(1F - 2F * t));
                pose.scale(s, s, s);
            }
            case SUMMON -> {
                pose.translate(0, 0.6F * arc * size, 0);
                spin(pose, 720F * t);
            }
            case PHASE -> {
                float s = 1F + 0.25F * arc;
                pose.scale(s, s, s);
                spin(pose, 360F * t);
                shake(pose, age, 0.06F * arc * size);
            }
            case BREATH -> {
                lean(pose, yaw, 18F);
                shake(pose, age, 0.015F * size);
            }
            case EXHAUSTED, KNOCKED_DOWN -> {
                float k = Math.min(1F, t * 10F) * Math.min(1F, (1F - t) * 10F);
                // O corpo permanece em pé, comprimido sobre as pernas dobradas.
                pose.scale(1F, 1F - 0.12F * k, 1F);
                lean(pose, yaw, 8F * k);
            }
            case AGONY -> {
                // Afunda (ajoelha), inclina para a frente e treme forte; entra e sai em 1/5 da animação.
                float k = Mth.clamp(t * 5F, 0F, 1F) * Mth.clamp((1F - t) * 5F, 0F, 1F);
                pose.translate(0, -0.18F * k * size, 0);
                lean(pose, yaw, 10F * k);
                shake(pose, age, 0.05F * k * size);
            }
            case LAUGH -> {
                float k = Math.min(1F, t * 8F) * Math.min(1F, (1F - t) * 8F);
                lean(pose, yaw, (-6F + Mth.sin(age * 0.9F) * 2F) * k);
                pose.translate(0, Math.abs(Mth.sin(age * 0.9F)) * 0.035F * size * k, 0);
            }
            case NONE -> {}
        }
    }

    // ------------------------------------------------------------------ transformações

    /**
     * Inclina o corpo para a frente (graus positivos) ou para trás, girando em volta do eixo "direita"
     * do mob — que depende do giro do corpo, já que a pose aqui ainda está no espaço do mundo.
     */
    private static void lean(PoseStack pose, float bodyYaw, float degrees) {
        float r = bodyYaw * Mth.DEG_TO_RAD;
        pose.mulPose(new Quaternionf().rotateAxis(degrees * Mth.DEG_TO_RAD, Mth.cos(r), 0, Mth.sin(r)));
    }

    /** Tomba de lado (giro em volta do eixo "frente" do mob). */
    private static void roll(PoseStack pose, float bodyYaw, float degrees) {
        float r = bodyYaw * Mth.DEG_TO_RAD;
        pose.mulPose(new Quaternionf().rotateAxis(degrees * Mth.DEG_TO_RAD, -Mth.sin(r), 0, Mth.cos(r)));
    }

    private static void spin(PoseStack pose, float degrees) {
        pose.mulPose(Axis.YP.rotationDegrees(degrees));
    }

    /** Estica o corpo na direção em que corre (e afina nas outras). */
    private static void stretchForward(PoseStack pose, float bodyYaw, float factor) {
        spin(pose, -bodyYaw);
        pose.scale(1F / factor, 1F, factor);
        spin(pose, bodyYaw);
    }

    private static void shake(PoseStack pose, float age, float amplitude) {
        pose.translate(Mth.sin(age * 3.1F) * amplitude, 0, Mth.cos(age * 2.7F) * amplitude);
    }
}
