package com.tarcitani.solomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.StatsLogic;
import com.tarcitani.solomod.event.StatsLogic.WeaponAbilities;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.network.WeaponMotionPayload;
import com.tarcitani.solomod.network.WeaponMotionPayload.Kind;
import com.tarcitani.solomod.skill.ShieldAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Camada extravagante das armas: agulhas múltiplas, selos giratórios, rastro de lâminas e impactos
 * fragmentados. Flipbooks pixel art originais (oito quadros) são amostrados por UV, sem depender de CIT.
 * Só apresenta ações confirmadas pelo servidor, exceto a carga observável pelo uso vanilla da rapieira.
 * Rastros usam posições reais; estocadas visuais param em paredes. Tudo respeita profundidade.
 * Sem dano/colisão/teleporte. Orçamento: 32 ações, alcance visual 80 blocos e menos segmentos à distância.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT)
public final class CombatVfx {
    private CombatVfx() {}
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final ResourceLocation RUNE = texture("rune"), STREAK = texture("streak"), BURST = texture("burst");
    private static final List<Action> ACTIVE = new ArrayList<>();
    private static final List<Sprite> SPRITES = new ArrayList<>();
    private record Sprite(ResourceLocation texture, Vec3 center, Vec3 right, Vec3 up, double width,
                          double height, int frame, int color, int alpha) {}
    private static final class Action {
        final ClientLevel level;
        final WeaponMotionPayload data;
        final long start;
        final List<Vec3> trail = new ArrayList<>();
        long sampled = -1;
        Action(ClientLevel level, WeaponMotionPayload data) { this.level = level; this.data = data; start = level.getGameTime(); }
        int life() {
            return switch (data.kind()) {
                case RAPIER -> Math.max(12, (int) data.power() + 10);
                case RAPIER_FLURRY -> WeaponAbilities.FLURRY_TICKS + 6;
                case RAPIER_RETREAT -> WeaponAbilities.RETREAT_TICKS + 6;
                case DAGGER_THROW, DAGGER_RECALL -> 40;
                case DAGGER_BLINK -> 16;
                case HAMMER_IMPACT, HAMMER_QUAKE -> 22;
                case HAMMER_SWEEP -> WeaponAbilities.HAMMER_SWEEP_TICKS + 5;
                case SHIELD_SWORD, SHIELD_RAPIER, SHIELD_DAGGER, SHIELD_HAMMER -> (int) data.power() + 7;
            };
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "textures/effect/combat_" + name + ".png");
    }
    static boolean isShield(Kind kind) {
        return kind == Kind.SHIELD_SWORD || kind == Kind.SHIELD_RAPIER || kind == Kind.SHIELD_DAGGER || kind == Kind.SHIELD_HAMMER;
    }
    public static void start(WeaponMotionPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        ACTIVE.removeIf(e -> e.level != level || e.data.entityId() == payload.entityId()
                && (e.data.kind() == payload.kind() || payload.kind() == Kind.DAGGER_RECALL));
        if (ACTIVE.size() >= 32) ACTIVE.removeFirst();
        ACTIVE.add(new Action(level, payload));
    }

    private static boolean invalid(Action action, ClientLevel level) {
        if (action.level != level || level.getGameTime() - action.start >= action.life()) return true;
        var entity = level.getEntity(action.data.entityId());
        if (entity instanceof Player p && !p.isAlive()) return true;
        if (isShield(action.data.kind())) {
            if (!(entity instanceof Player p)) return true;
            var gear = ShieldAbility.equipment(p);
            WeaponClass expected = switch (action.data.kind()) {
                case SHIELD_SWORD -> WeaponClass.LONGSWORD;
                case SHIELD_RAPIER -> WeaponClass.RAPIER;
                case SHIELD_DAGGER -> WeaponClass.DAGGER;
                default -> WeaponClass.HAMMER;
            };
            return gear == null || gear.weaponClass() != expected;
        }
        return false;
    }

    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) { ACTIVE.clear(); return; }
        ACTIVE.removeIf(e -> invalid(e, mc.level));
        if (ACTIVE.isEmpty() && mc.level.players().stream().noneMatch(p -> p.isUsingItem()
                && p.getUseItem().getItem() instanceof ModularWeaponItem item && item.weaponClass() == WeaponClass.RAPIER)) return;
        SPRITES.clear();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        var buffers = mc.renderBuffers().bufferSource();
        var out = buffers.getBuffer(RenderType.lightning());
        PoseStack pose = event.getPoseStack();
        pose.pushPose(); pose.translate(-camera.x, -camera.y, -camera.z);
        for (Action action : ACTIVE) {
            var entity = mc.level.getEntity(action.data.entityId());
            Vec3 origin = entity instanceof Player ? entity.getPosition(partial).add(0, 0.95, 0) : action.data.from().add(0, 0.9, 0);
            if (origin.distanceToSqr(camera) > 80 * 80) continue;
            float age = mc.level.getGameTime() - action.start + partial;
            int detail = origin.distanceToSqr(camera) > 32 * 32 ? 8 : 16;
            Vec3 dir = action.data.to().subtract(action.data.from()).multiply(1, 0, 1).normalize();
            if (dir.lengthSqr() < 0.01) dir = new Vec3(0, 0, 1);
            Vec3 side = new Vec3(-dir.z, 0, dir.x);
            float fade = 1 - age / action.life();
            switch (action.data.kind()) {
                case RAPIER -> dash(pose, out, action, origin, dir, age, partial, detail);
                case RAPIER_FLURRY, RAPIER_RETREAT, SHIELD_RAPIER -> {
                    int count = action.data.kind() == Kind.RAPIER_FLURRY ? WeaponAbilities.FLURRY_HITS : 1;
                    int first = action.data.kind() == Kind.SHIELD_RAPIER ? StatsLogic.ShieldSkills.RAPIER.hitTick()
                            : action.data.kind() == Kind.RAPIER_RETREAT ? WeaponAbilities.RETREAT_HIT : WeaponAbilities.FLURRY_FIRST_HIT;
                    double reach = action.data.kind() == Kind.SHIELD_RAPIER ? StatsLogic.ShieldSkills.RAPIER.range()
                            : action.data.kind() == Kind.RAPIER_RETREAT ? WeaponAbilities.RETREAT_RANGE : WeaponAbilities.FLURRY_RANGE;
                    prepare(pose, out, origin.add(dir.scale(0.7)), dir, age, first, detail);
                    for (int i = 0; i < count; i++) thrust(pose, out, action, origin, dir, age - first - i * WeaponAbilities.FLURRY_HIT_GAP, reach, i, detail);
                    if (action.data.kind() == Kind.RAPIER_RETREAT) trail(pose, out, action, origin, dir, age, partial, 0x83F5FF);
                }
                case SHIELD_SWORD, SHIELD_DAGGER -> {
                    boolean sword = action.data.kind() == Kind.SHIELD_SWORD;
                    int hit = sword ? StatsLogic.ShieldSkills.SWORD.hitTick() : StatsLogic.ShieldSkills.DAGGER.hitTick();
                    prepare(pose, out, origin.add(dir.scale(0.6)), dir, age, hit, detail);
                    float elapsed = age - hit;
                    if (elapsed >= 0 && elapsed < 12) {
                        float f = 1 - elapsed / 12F;
                        for (int i = 0; i < detail; i++) {
                            double a = -1.1 + i * 2.2 / detail, b = a + 2.2 / detail;
                            double radius = sword ? 2.6 : 1.65;
                            Vec3 from = origin.add(dir.scale(Math.cos(a) * radius)).add(side.scale(Math.sin(a) * radius));
                            Vec3 to = origin.add(dir.scale(Math.cos(b) * radius)).add(side.scale(Math.sin(b) * radius));
                            FxGeometry.bar(pose, out, from, to, 0.07, sword ? 0x72E9FF : 0xFFD47F, (int) (190 * f));
                            FxGeometry.bar(pose, out, from.add(0, 0.07, 0), to.add(0, 0.07, 0), 0.015, 0xF0FFFF, (int) (245 * f));
                            if (i % 3 == 0) FxGeometry.spark(pose, out, to.add(0, elapsed * 0.03, 0), 0.05, 0xEDCE83, (int) (220 * f));
                        }
                        sprite(BURST, origin.add(dir.scale(sword ? 2.4 : 1.6)), side, UP, 1.4, 1.4, elapsed, 0xFFFFFF, (int) (210 * f));
                    }
                }
                case SHIELD_HAMMER -> {
                    int hit = StatsLogic.ShieldSkills.HAMMER.hitTick();
                    prepare(pose, out, origin.add(dir.scale(0.6)), dir, age, hit, detail);
                    if (age >= hit) shock(pose, out, origin.add(dir.scale(1.6)).add(0, -0.85, 0), age - hit, 1.7, detail);
                }
                case HAMMER_IMPACT -> shock(pose, out, action.data.from().add(0, 0.1, 0), age, 2.6 + action.data.power(), detail);
                case HAMMER_SWEEP -> {
                    float t = age - WeaponAbilities.HAMMER_SWEEP_HIT;
                    if (t >= 0 && t < 14) {
                        wheel(pose, out, origin, UP, 3.5, age * 0.12, 0xFFD18A, (int) (190 * (1-t/14)), detail);
                        sprite(RUNE, origin.add(0, -0.8, 0), dir, side, 3, 3, age, 0xFFFFFF, (int) (140 * fade));
                    }
                }
                case HAMMER_QUAKE -> {
                    double reach = action.data.from().distanceTo(action.data.to());
                    double distance = Math.min(reach, age * WeaponAbilities.HAMMER_QUAKE_STEP);
                    if (distance > 0.5) shock(pose, out, action.data.from().add(dir.scale(distance)).add(0, 0.1, 0), age % 8, 0.8, 8);
                }
                case DAGGER_BLINK -> {
                    for (Vec3 p : List.of(action.data.from(), action.data.to())) {
                        sprite(RUNE, p.add(0, 0.08, 0), dir, side, 2 + age * 0.08, 2 + age * 0.08, age, 0xFFFFFF, (int) (210 * fade));
                        sprite(BURST, p.add(0, 1, 0), side, UP, 2, 2, age, 0xFFE3AA, (int) (230 * fade));
                        wheel(pose, out, p.add(0, 1, 0), dir, 0.3 + age * 0.04, age * 0.2, 0xFFE0A0, (int) (180 * fade), detail);
                    }
                }
                case DAGGER_THROW, DAGGER_RECALL -> {
                    if (entity != null) {
                        Vec3 p = entity.getPosition(partial);
                        sprite(BURST, p, side, UP, 0.75, 0.75, age, 0xFFE9B0, (int) (200 * fade));
                        wheel(pose, out, p, dir, 0.24, age * 0.5, 0xFFE6A1, (int) (180 * fade), 8);
                    }
                }
            }
        }
        // Carga: estado de uso sincronizado, sem novo pacote por tick.
        for (Player player : mc.level.players()) {
            if (player.position().distanceToSqr(camera) > 48 * 48 || !player.isUsingItem()
                    || !(player.getUseItem().getItem() instanceof ModularWeaponItem item) || item.weaponClass() != WeaponClass.RAPIER) continue;
            float age = player.getTicksUsingItem() + partial, charge = Mth.clamp(age / WeaponAbilities.RAPIER_CHARGE_TICKS, 0, 1);
            Vec3 dir = Vec3.directionFromRotation(0, player.getYRot()), side = new Vec3(-dir.z, 0, dir.x);
            Vec3 p = player.getPosition(partial).add(0, 1.0, 0).add(dir.scale(0.8));
            wheel(pose, out, p, dir, 0.18 + charge * 0.24, age * 0.08, 0x7DEAFF, (int) (110 + charge * 110), 12);
            sprite(RUNE, p, side, UP, 0.7 + charge * 0.4, 0.7 + charge * 0.4, age, 0xFFFFFF, (int) (120 + charge * 90));
        }
        buffers.endBatch(RenderType.lightning());
        for (ResourceLocation texture : List.of(RUNE, STREAK, BURST)) {
            RenderType type = RenderType.entityTranslucentEmissive(texture);
            VertexConsumer buffer = buffers.getBuffer(type);
            for (Sprite sprite : SPRITES) if (sprite.texture().equals(texture)) quad(pose, buffer, sprite);
            buffers.endBatch(type);
        }
        pose.popPose();
    }

    private static void prepare(PoseStack pose, VertexConsumer out, Vec3 origin, Vec3 dir, float age, int hit, int detail) {
        if (age >= hit) return;
        float p = age / hit;
        wheel(pose, out, origin, dir, 0.35 - p * 0.12, -age * 0.2, 0x8AE8FF, (int) (90 + p * 110), detail);
    }

    private static void thrust(PoseStack pose, VertexConsumer out, Action action, Vec3 origin, Vec3 dir,
                               float elapsed, double reach, int index, int detail) {
        if (elapsed < 0 || elapsed > 10) return;
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        Vec3 end = action.level.clip(new ClipContext(origin, origin.add(dir.scale(reach)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, action.level.getEntity(action.data.entityId()))).getLocation();
        reach = Math.min(reach, end.distanceTo(origin));
        double extension = Math.min(1, elapsed / 1.5);
        float fade = 1 - elapsed / 10F;
        Vec3 tip = origin.add(dir.scale(reach * extension));
        for (int lane = -2; lane <= 2; lane++) {
            Vec3 offset = side.scale(lane * 0.16).add(0, Math.sin(lane * 1.9 + index) * 0.15, 0);
            Vec3 from = origin.add(dir.scale(0.45)).add(offset), to = tip.add(offset.scale(0.3));
            FxGeometry.bar(pose, out, from, to, lane == 0 ? 0.035 : 0.015, 0xDFFFFF, (int) (220 * fade));
            FxGeometry.bar(pose, out, from, to, lane == 0 ? 0.11 : 0.045, 0x4DD8FA, (int) (75 * fade));
        }
        sprite(STREAK, origin.lerp(tip, 0.55), dir, side, Math.max(0.1, reach * extension), 0.38, elapsed, 0xFFFFFF, (int) (230 * fade));
        wheel(pose, out, tip, dir, 0.25 + elapsed * 0.055, elapsed * 0.15, 0x9EEDFF, (int) (185 * fade), detail);
        sprite(BURST, tip, side, UP, 1 + elapsed * 0.06, 1 + elapsed * 0.06, elapsed, 0xFFFFFF, (int) (230 * fade));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4 + index;
            Vec3 p = tip.add(side.scale(Math.cos(angle) * elapsed * 0.065)).add(0, Math.sin(angle) * elapsed * 0.065, 0);
            FxGeometry.spark(pose, out, p, 0.035, i % 2 == 0 ? 0xF5DC9F : 0x8AEEFF, (int) (210 * fade));
        }
    }

    private static void dash(PoseStack pose, VertexConsumer out, Action action, Vec3 origin, Vec3 dir, float age, float partial, int detail) {
        trail(pose, out, action, origin, dir, age, partial, 0x65E4FF);
        if (age > action.data.power() + 2) return;
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        for (int i = 0; i < 2; i++) {
            Vec3 p = origin.subtract(dir.scale(0.4 + i * 0.65));
            wheel(pose, out, p, dir, 0.45 + i * 0.13, age * (i == 0 ? 0.18 : -0.18), 0x76E7FF, 160 - i * 50, detail);
        }
        sprite(RUNE, origin.add(dir.scale(0.5)), side, UP, 1.3, 1.3, age, 0xFFFFFF, 130);
    }

    private static void trail(PoseStack pose, VertexConsumer out, Action action, Vec3 origin, Vec3 dir, float age, float partial, int color) {
        boolean sampling = action.data.kind() == Kind.RAPIER ? age <= action.data.power()
                : age >= WeaponAbilities.RETREAT_HIT && age <= WeaponAbilities.RETREAT_HIT + WeaponAbilities.RETREAT_MOVE_TICKS;
        if (sampling && action.sampled != action.level.getGameTime()) {
            action.sampled = action.level.getGameTime();
            action.trail.add(origin);
            if (action.trail.size() > 12) action.trail.removeFirst();
        }
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        float fade = 1 - age / action.life();
        for (int i = 1; i < action.trail.size(); i++) {
            Vec3 a = action.trail.get(i-1), b = action.trail.get(i);
            float f = i / (float) action.trail.size() * fade;
            for (int lane = -1; lane <= 1; lane++) {
                Vec3 offset = side.scale(lane * 0.3).add(0, lane == 0 ? -0.25 : 0.3, 0);
                FxGeometry.bar(pose, out, a.add(offset), b.add(offset), 0.025, color, (int) (175 * f));
                if (i % 3 == 0) sprite(STREAK, b.add(offset).subtract(dir.scale(0.4)), dir, side, 0.9, 0.17, age+i, 0xFFFFFF, (int) (155 * f));
            }
        }
    }

    private static void shock(PoseStack pose, VertexConsumer out, Vec3 center, float age, double radius, int detail) {
        if (age < 0 || age > 18) return;
        float fade = 1 - age / 18F;
        double r = Math.min(radius, 0.3 + age * 0.22);
        wheel(pose, out, center, UP, r, 0, 0xF6D18A, (int) (220 * fade), detail);
        wheel(pose, out, center.add(0, 0.16, 0), UP, r * 0.7, age * 0.05, 0x9CECF4, (int) (150 * fade), detail);
        sprite(RUNE, center.add(0, 0.04, 0), new Vec3(1,0,0), new Vec3(0,0,1), r * 2, r * 2, age, 0xFFFFFF, (int) (200 * fade));
        for (int i = 0; i < detail; i += 2) {
            double a = i * Math.PI * 2 / detail;
            Vec3 p = center.add(Math.cos(a) * r, Math.sin(age / 18 * Math.PI) * 0.65, Math.sin(a) * r);
            FxGeometry.spark(pose, out, p, 0.08 + i % 3 * 0.025, i % 4 == 0 ? 0xDFC18A : 0x8A999F, (int) (220 * fade));
            FxGeometry.bar(pose, out, center, p.add(0, -0.1, 0), 0.012, 0xF6D18A, (int) (100 * fade));
        }
    }

    private static void wheel(PoseStack pose, VertexConsumer out, Vec3 center, Vec3 axis, double radius, double rotation,
                              int color, int alpha, int segments) {
        Vec3 right = Math.abs(axis.y) > 0.9 ? new Vec3(1,0,0) : axis.cross(UP).normalize();
        Vec3 up = axis.cross(right).normalize();
        for (int i = 0; i < segments; i++) {
            double a = i * Math.PI * 2 / segments + rotation, b = (i+1) * Math.PI * 2 / segments + rotation;
            Vec3 from = center.add(right.scale(Math.cos(a) * radius)).add(up.scale(Math.sin(a) * radius));
            Vec3 to = center.add(right.scale(Math.cos(b) * radius)).add(up.scale(Math.sin(b) * radius));
            FxGeometry.bar(pose, out, from, to, 0.012, color, alpha);
            if (i % 2 == 0) FxGeometry.spark(pose, out, from, 0.025, 0xFFF1C6, alpha);
        }
    }

    private static void sprite(ResourceLocation texture, Vec3 center, Vec3 right, Vec3 up, double width, double height,
                               float age, int color, int alpha) {
        if (SPRITES.size() >= 256 || alpha <= 0) return;
        SPRITES.add(new Sprite(texture, center, right, up, width, height, Math.floorMod((int) age, 8), color, Mth.clamp(alpha, 0, 255)));
    }
    private static void quad(PoseStack pose, VertexConsumer out, Sprite sprite) {
        Vec3 right = sprite.right().scale(sprite.width()/2), up = sprite.up().scale(sprite.height()/2);
        Vec3[] points = {sprite.center().subtract(right).subtract(up), sprite.center().add(right).subtract(up),
                sprite.center().add(right).add(up), sprite.center().subtract(right).add(up)};
        float v0 = sprite.frame()/8F, v1 = (sprite.frame()+1)/8F;
        float[][] uv = {{0,v1},{1,v1},{1,v0},{0,v0}};
        Vec3 normal = sprite.right().cross(sprite.up()).normalize();
        for (int i = 0; i < 4; i++) out.addVertex(pose.last().pose(), (float) points[i].x, (float) points[i].y, (float) points[i].z)
                .setColor((sprite.color() >> 16)&255, (sprite.color() >> 8)&255, sprite.color()&255, sprite.alpha())
                .setUv(uv[i][0], uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880)
                .setNormal(pose.last(), (float) normal.x, (float) normal.y, (float) normal.z);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ACTIVE.clear(); SPRITES.clear(); }
}
