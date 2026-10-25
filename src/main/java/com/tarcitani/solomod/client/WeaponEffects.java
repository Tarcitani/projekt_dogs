package com.tarcitani.solomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.skill.WeaponTechnique;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.event.StatsLogic.WeaponAbilities;
import com.tarcitani.solomod.network.CrescentSlashPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Rastros 3D das técnicas da espada longa: lâmina de luz em segmentos retos com volume, halo elemental e fio branco.
 * Nasce de pacote S→C; só desenha, respeita profundidade e não causa dano. Cada corte revela o arco
 * progressivamente e o deixa se dissipar na recuperação. Sem textura externa ou arquivos gerados.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT)
public final class WeaponEffects {
    private WeaponEffects() {}
    private record Slash(ClientLevel level, int playerId, Vec3 direction, ElementType element, WeaponTechnique technique, long start) {}
    private static final List<Slash> SLASHES = new ArrayList<>();

    public static void start(CrescentSlashPayload p) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        SLASHES.removeIf(s -> s.level() != level || s.playerId() == p.entityId());
        if (SLASHES.size() >= 32) SLASHES.removeFirst();
        SLASHES.add(new Slash(level, p.entityId(), new Vec3(p.dirX(), 0, p.dirZ()).normalize(), p.element(), p.technique(), level.getGameTime()));
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SLASHES.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        SLASHES.removeIf(s -> s.level() != mc.level || mc.level.getGameTime() - s.start() >= s.technique().duration()
                || !(mc.level.getEntity(s.playerId()) instanceof Player player) || !player.isAlive()
                || !(player.getMainHandItem().getItem() instanceof ModularWeaponItem item) || item.weaponClass() != WeaponClass.LONGSWORD);
        var buffers = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(RenderType.lightning());
        Vec3 camera = event.getCamera().getPosition();
        for (Slash slash : SLASHES) {
            float age = mc.level.getGameTime() - slash.start() + partial - WeaponAbilities.CRESCENT_WINDUP;
            if (age < 0) continue;
            Player player = (Player) mc.level.getEntity(slash.playerId());
            Vec3 center = player.getPosition(partial).add(0, 1.05, 0);
            PoseStack pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(center.x - camera.x, center.y - camera.y, center.z - camera.z);
            Vec3 side = new Vec3(-slash.direction().z, 0, slash.direction().x);
            if (slash.technique() == WeaponTechnique.RISING) {
                plane(pose, buffer, slash, new Vec3(0, 1, 0), age, -0.2, Math.PI / 2 + 0.2);
            } else if (slash.technique() == WeaponTechnique.CROSS) {
                plane(pose, buffer, slash, side.scale(0.85).add(0, 0.52, 0), age, -Math.PI / 2, Math.PI / 2);
                plane(pose, buffer, slash, side.scale(0.85).add(0, -0.52, 0), age - (WeaponAbilities.CROSS_SECOND_HIT - WeaponAbilities.CRESCENT_WINDUP), -Math.PI / 2, Math.PI / 2);
            } else plane(pose, buffer, slash, side, age, -Math.PI / 2, Math.PI / 2);
            pose.popPose();
        }
        buffers.endBatch(RenderType.lightning());
    }

    private static void plane(PoseStack pose, VertexConsumer buffer, Slash slash, Vec3 side, float age, double from, double to) {
        if (age < 0 || age > 12) return;
        float sweep = Mth.clamp(age / 5F, 0, 1);
        float fade = 1F - Mth.clamp((age - 5F) / 7F, 0, 1);
        int color = slash.element() == ElementType.NONE ? 0x8EDCFF : ClientSetup.tintFor(slash.element());
        double lead = from + sweep * (to - from);
        for (int i = 0; i < 24; i++) {
            double a = from + i * (to - from) / 24, b = Math.min(lead, a + (to - from) / 24);
            if (b <= a) continue;
            float tail = (float) (0.3 + 0.7 * (1 - (lead - a) / (to - from)));
            double width = 0.18 + 0.55 * Math.sin((i + 0.5) * Math.PI / 24);
            double radius = slash.technique().range() * 0.87;
            blade(pose, buffer, slash.direction(), side, a, b, radius + width * 0.45, 0.06, 0.025, 0xFFFFFF, (int) (240 * fade * tail));
            blade(pose, buffer, slash.direction(), side, a, b, radius, width, 0.06, color, (int) (165 * fade * tail));
            blade(pose, buffer, slash.direction(), side, a, b, radius, width + 0.3, 0.12, color, (int) (45 * fade * tail));
        }
    }

    private static void blade(PoseStack pose, VertexConsumer buffer, Vec3 direction, Vec3 side, double a, double b,
                              double radius, double width, double height, int color, int alpha) {
        Vec3 normal = direction.cross(side).normalize();
        Vec3[] p = new Vec3[8];
        for (int i = 0; i < 4; i++) {
            double angle = i < 2 ? a : b;
            double r = i == 0 || i == 3 ? radius - width : radius + width * 0.4;
            Vec3 v = direction.scale(Math.cos(angle) * r).add(side.scale(Math.sin(angle) * r));
            p[i] = v.add(normal.scale(height));
            p[i + 4] = v.subtract(normal.scale(height));
        }
        for (int[] face : new int[][]{{0, 1, 2, 3}, {7, 6, 5, 4}, {1, 5, 6, 2}, {0, 3, 7, 4}, {0, 4, 5, 1}, {3, 2, 6, 7}}) {
            for (int index : face) {
                Vec3 v = p[index];
                buffer.addVertex(pose.last().pose(), (float) v.x, (float) v.y, (float) v.z)
                        .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, alpha);
            }
        }
    }

    @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { SLASHES.clear(); }
}
