package com.tarcitani.solomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.network.WeaponMotionPayload;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.StatsLogic.WeaponAbilities;
import com.tarcitani.solomod.item.ModularWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Efeitos 3D em pixels: teleporte amarelo com raios quebrados e selo, adaga com rastro elétrico,
 * linhas/agulhas/recuo da rapieira, giro, fissura e impacto do martelo. Só desenha: não causa dano, fogo,
 * raio real nem teleporte. Rastros seguem posições reais das entidades, sem prever travessia de paredes.
 * CombatVfx complementa esta malha com flipbooks, selos e lâminas de luz; técnicas com escudo são desenhadas lá.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT)
public final class MotionEffects {
    private MotionEffects() {}
    private static final class Effect {
        final ClientLevel level;
        final WeaponMotionPayload data;
        final long start;
        final List<Vec3> trail = new ArrayList<>();
        long sampled = -1;
        int ended = -1;
        Effect(ClientLevel level, WeaponMotionPayload data) { this.level = level; this.data = data; start = level.getGameTime(); }
        int life() { return switch (data.kind()) {
            case RAPIER -> Math.max(8, (int) data.power() + 6);
            case DAGGER_THROW -> 40;
            case DAGGER_RECALL -> 70;
            case DAGGER_BLINK -> 12;
            case HAMMER_IMPACT -> 20;
            case RAPIER_FLURRY -> WeaponAbilities.FLURRY_TICKS;
            case RAPIER_RETREAT -> WeaponAbilities.RETREAT_TICKS;
            case HAMMER_SWEEP -> WeaponAbilities.HAMMER_SWEEP_TICKS;
            case HAMMER_QUAKE -> 20;
            case SHIELD_SWORD, SHIELD_RAPIER, SHIELD_DAGGER, SHIELD_HAMMER -> Math.max(12, (int) data.power());
        }; }
    }
    private static final List<Effect> ACTIVE = new ArrayList<>();

    public static void start(WeaponMotionPayload payload) {
        CombatVfx.start(payload);
        if (CombatVfx.isShield(payload.kind())) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        ACTIVE.removeIf(e -> e.level != level || (e.data.entityId() == payload.entityId()
                && (e.data.kind() == payload.kind() || payload.kind() == WeaponMotionPayload.Kind.DAGGER_RECALL)));
        if (ACTIVE.size() >= 48) ACTIVE.removeFirst();
        ACTIVE.add(new Effect(level, payload));
    }

    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        ACTIVE.removeIf(e -> e.level != mc.level || mc.level.getGameTime() - e.start >= e.life()
                || (e.ended >= 0 && mc.level.getGameTime() - e.start >= e.ended + 6) || invalidTechnique(e));
        var buffers = mc.renderBuffers().bufferSource();
        var out = buffers.getBuffer(RenderType.lightning());
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        pose.pushPose(); pose.translate(-camera.x, -camera.y, -camera.z);
        for (Effect e : ACTIVE) {
            float age = mc.level.getGameTime() - e.start + partial;
            float fade = 1 - Mth.clamp(age / e.life(), 0, 1);
            switch (e.data.kind()) {
                case DAGGER_BLINK -> {
                    blink(pose, out, e.data.from(), age, fade);
                    blink(pose, out, e.data.to(), age, fade);
                    lightning(pose, out, e.data.from().add(0, 1, 0), e.data.to().add(0, 1, 0), fade * 0.5F, 7);
                }
                case HAMMER_IMPACT -> impact(pose, out, e.data.from(), age, fade, e.data.power());
                case RAPIER_FLURRY, RAPIER_RETREAT, HAMMER_SWEEP -> technique(pose, out, e, age, partial);
                case HAMMER_QUAKE -> quake(pose, out, e, age, fade);
                default -> trail(pose, out, e, age, fade, partial);
            }
        }
        pose.popPose(); buffers.endBatch(RenderType.lightning());
    }

    private static void trail(PoseStack pose, VertexConsumer out, Effect e, float age, float fade, float partial) {
        var entity = e.level.getEntity(e.data.entityId());
        boolean rapier = e.data.kind() == WeaponMotionPayload.Kind.RAPIER;
        Vec3 direction = e.data.to().subtract(e.data.from()).normalize();
        Vec3 lateral = new Vec3(-direction.z, 0, direction.x);
        boolean sampling = !rapier || age <= e.data.power();
        if (entity != null && sampling && e.sampled != e.level.getGameTime()) {
            e.sampled = e.level.getGameTime(); e.trail.add(entity.position().add(0, rapier ? 0.85 : 0.1, 0));
            if (e.trail.size() > 9) e.trail.removeFirst();
        } else if (((entity == null && age > 4) || !sampling) && e.ended < 0) e.ended = (int) age;
        Vec3 head = entity != null && sampling ? entity.getPosition(partial).add(0, rapier ? 0.85 : 0.1, 0)
                : e.trail.isEmpty() ? e.data.from() : e.trail.getLast();
        List<Vec3> points = new ArrayList<>(e.trail); points.add(head);
        if (e.ended >= 0) fade *= 1 - Mth.clamp((age - e.ended) / 6F, 0, 1);
        for (int i = 1; i < points.size(); i++) {
            float weight = fade * i / points.size();
            Vec3 a = points.get(i - 1), b = points.get(i);
            if (rapier) {
                for (int lane = -1; lane <= 1; lane++) {
                    Vec3 offset = lateral.scale(lane * 0.22).add(0, lane == 0 ? 0 : 0.4, 0);
                    FxGeometry.bar(pose, out, a.add(offset), b.add(offset), 0.018, 0xE9FFFF, (int) (210 * weight));
                    FxGeometry.bar(pose, out, a.add(offset), b.add(offset), 0.055, 0x60DFFF, (int) (65 * weight));
                }
            } else lightning(pose, out, a, b, weight, i);
        }
        if (rapier && entity != null && sampling) {
            Vec3 dir = direction;
            Vec3 side = lateral;
            for (int i = 0; i < 6; i++) {
                Vec3 offset = side.scale((i % 2 == 0 ? -1 : 1) * (0.45 + (i % 3) * 0.18)).add(0, (i % 3 - 1) * 0.36, 0);
                Vec3 a = head.add(offset).subtract(dir.scale(0.4 + (i % 3) * 0.2));
                FxGeometry.bar(pose, out, a, a.subtract(dir.scale(0.8)), 0.014, 0xBBF6FF, (int) (190 * fade));
            }
        }
    }

    private static boolean invalidTechnique(Effect effect) {
        WeaponClass expected = switch (effect.data.kind()) {
            case RAPIER_FLURRY, RAPIER_RETREAT -> WeaponClass.RAPIER;
            case HAMMER_SWEEP, HAMMER_QUAKE -> WeaponClass.HAMMER;
            default -> null;
        };
        if (expected == null) return false;
        var entity = effect.level.getEntity(effect.data.entityId());
        return entity == null || !entity.isAlive() || !(entity instanceof net.minecraft.world.entity.player.Player player)
                || !(player.getMainHandItem().getItem() instanceof ModularWeaponItem item) || item.weaponClass() != expected;
    }

    private static void technique(PoseStack pose, VertexConsumer out, Effect effect, float age, float partial) {
        var entity = effect.level.getEntity(effect.data.entityId());
        if (entity == null) return;
        Vec3 origin = entity.getPosition(partial).add(0, 1.0, 0);
        Vec3 dir = effect.data.to().subtract(effect.data.from()).normalize();
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        if (effect.data.kind() == WeaponMotionPayload.Kind.HAMMER_SWEEP) {
            float p = Mth.clamp((age - WeaponAbilities.HAMMER_SWEEP_HIT) / 8F, 0, 1);
            float fade = 1 - Mth.clamp((age - WeaponAbilities.HAMMER_SWEEP_HIT - 8) / 8F, 0, 1);
            if (age < WeaponAbilities.HAMMER_SWEEP_HIT) return;
            for (int i = 0; i < 36; i++) {
                double a = i * Math.PI / 18, b = Math.min(p * Math.PI * 2, (i + 1) * Math.PI / 18);
                if (b <= a) continue;
                Vec3 from = origin.add(dir.scale(Math.cos(a) * WeaponAbilities.HAMMER_SWEEP_RANGE)).add(side.scale(Math.sin(a) * WeaponAbilities.HAMMER_SWEEP_RANGE));
                Vec3 to = origin.add(dir.scale(Math.cos(b) * WeaponAbilities.HAMMER_SWEEP_RANGE)).add(side.scale(Math.sin(b) * WeaponAbilities.HAMMER_SWEEP_RANGE));
                FxGeometry.bar(pose, out, from, to, 0.075, 0xFFD180, (int) (160 * fade));
                FxGeometry.bar(pose, out, from.add(0, 0.09, 0), to.add(0, 0.09, 0), 0.018, 0xFFF9D4, (int) (230 * fade));
                if (i % 3 == 0) FxGeometry.spark(pose, out, from.add(0, Math.sin(age + i) * 0.2, 0), 0.055, 0xE5A557, (int) (180 * fade));
            }
        } else {
            int count = effect.data.kind() == WeaponMotionPayload.Kind.RAPIER_FLURRY ? WeaponAbilities.FLURRY_HITS : 1;
            for (int i = 0; i < count; i++) {
                float elapsed = age - WeaponAbilities.FLURRY_FIRST_HIT - i * WeaponAbilities.FLURRY_HIT_GAP;
                if (elapsed < 0 || elapsed > 6) continue;
                float extension = Mth.clamp(elapsed / 2F, 0, 1), fade = 1 - elapsed / 6F;
                double reach = effect.data.kind() == WeaponMotionPayload.Kind.RAPIER_FLURRY ? WeaponAbilities.FLURRY_RANGE : WeaponAbilities.RETREAT_RANGE;
                Vec3 from = origin.add(side.scale((i - (count - 1) / 2.0) * 0.16));
                Vec3 tip = from.add(dir.scale(0.5 + (reach - 0.5) * extension));
                FxGeometry.bar(pose, out, from.add(dir.scale(0.5)), tip, 0.035, 0xE9FFFF, (int) (240 * fade));
                FxGeometry.bar(pose, out, from.add(dir.scale(0.5)), tip, 0.08, 0x55CFEF, (int) (75 * fade));
                for (int lane = -1; lane <= 1; lane += 2) {
                    FxGeometry.bar(pose, out, tip, tip.subtract(dir.scale(0.45)).add(side.scale(lane * 0.16)), 0.025, 0xC0FAFF, (int) (230 * fade));
                    FxGeometry.spark(pose, out, tip.add(side.scale(lane * 0.24)), 0.035, 0x8CEBFF, (int) (180 * fade));
                }
            }
            if (effect.data.kind() == WeaponMotionPayload.Kind.RAPIER_RETREAT && age > WeaponAbilities.RETREAT_HIT && age < 14) {
                if (effect.sampled != effect.level.getGameTime()) {
                    effect.sampled = effect.level.getGameTime(); effect.trail.add(origin);
                    if (effect.trail.size() > 6) effect.trail.removeFirst();
                }
                for (int i = 1; i < effect.trail.size(); i++) for (int lane = -1; lane <= 1; lane++) {
                    Vec3 offset = side.scale(lane * 0.3).add(0, lane * 0.25, 0);
                    FxGeometry.bar(pose, out, effect.trail.get(i - 1).add(offset), effect.trail.get(i).add(offset), 0.025,
                            0xA9F5FF, (int) (180 * i / (float) effect.trail.size() * (1 - age / 18F)));
                }
            }
        }
    }

    private static void quake(PoseStack pose, VertexConsumer out, Effect effect, float age, float fade) {
        Vec3 dir = effect.data.to().subtract(effect.data.from()).normalize();
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        double reach = effect.data.from().distanceTo(effect.data.to());
        for (double distance = 0.5; distance <= Math.min(reach, (age + 1) * WeaponAbilities.HAMMER_QUAKE_STEP); distance += 0.5) {
            float localAge = age - (float) (distance / WeaponAbilities.HAMMER_QUAKE_STEP) + 1;
            if (localAge < 0 || localAge > 10) continue;
            float localFade = (1 - localAge / 10F) * fade;
            Vec3 center = effect.data.from().add(dir.scale(distance)).add(0, 0.08, 0);
            double lift = Math.sin(Math.min(1, localAge / 10.0) * Math.PI) * 0.8;
            FxGeometry.bar(pose, out, center.subtract(dir.scale(0.3)), center.add(dir.scale(0.3)), 0.04, 0xFFE298, (int) (200 * localFade));
            for (int lane = -1; lane <= 1; lane += 2) {
                Vec3 shard = center.add(side.scale(lane * (0.35 + localAge * 0.045))).add(0, lift, 0);
                FxGeometry.spark(pose, out, shard, 0.1 + (distance % 1) * 0.1, 0x8C7862, (int) (220 * localFade));
                FxGeometry.bar(pose, out, center, shard, 0.018, 0xF4C271, (int) (160 * localFade));
            }
        }
    }

    private static void lightning(PoseStack pose, VertexConsumer out, Vec3 a, Vec3 b, float fade, int seed) {
        double length = a.distanceTo(b);
        if (length < 0.01) return;
        int steps = Math.min(40, Math.max(3, (int) (length * 3)));
        Vec3 dir = b.subtract(a).normalize();
        Vec3 side = dir.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 prev = a;
        for (int i = 1; i <= steps; i++) {
            Vec3 next = a.lerp(b, i / (double) steps);
            if (i < steps) next = next.add(side.scale(Math.sin(i * 5.3 + seed) * 0.15)).add(0, Math.cos(i * 4.1 + seed) * 0.12, 0);
            FxGeometry.bar(pose, out, prev, next, 0.014, 0xFFFFE6, (int) (245 * fade));
            FxGeometry.bar(pose, out, prev, next, 0.055, 0xFFD443, (int) (115 * fade));
            prev = next;
        }
    }

    private static void blink(PoseStack pose, VertexConsumer out, Vec3 origin, float age, float fade) {
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            double r = 0.3 + age * 0.055;
            Vec3 foot = origin.add(Math.cos(a) * r, 0.05, Math.sin(a) * r);
            lightning(pose, out, foot, foot.add(Math.cos(a) * 0.35, 1.8 + Math.sin(i * 2) * 0.5, Math.sin(a) * 0.35), fade, i);
            Vec3 next = origin.add(Math.cos(a + Math.PI / 4) * r, 0.05, Math.sin(a + Math.PI / 4) * r);
            FxGeometry.bar(pose, out, foot, next, 0.025, 0xFFE38B, (int) (200 * fade));
            FxGeometry.spark(pose, out, foot.add(0, 0.5 + age * 0.03, 0), 0.035, 0xFFF6C0, (int) (210 * fade));
        }
    }

    private static void impact(PoseStack pose, VertexConsumer out, Vec3 center, float age, float fade, float power) {
        for (int ring = 0; ring < 2; ring++) {
            double radius = Math.max(0.1, (age - ring * 2) * (0.3 + power * 0.14));
            if (age < ring * 2) continue;
            for (int i = 0; i < 20; i++) {
                double a = i * Math.PI / 10, b = (i + 1) * Math.PI / 10;
                Vec3 from = center.add(Math.cos(a) * radius, 0.12, Math.sin(a) * radius);
                Vec3 to = center.add(Math.cos(b) * radius, 0.12, Math.sin(b) * radius);
                FxGeometry.bar(pose, out, from, to, 0.035, 0xE1B46F, (int) (165 * fade));
                if (i % 3 == 0) FxGeometry.spark(pose, out, from.add(0, Math.sin(age * 0.25) * (0.4 + power), 0), 0.06, 0xFFE2AA, (int) (190 * fade));
            }
        }
    }

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ACTIVE.clear(); }
}
