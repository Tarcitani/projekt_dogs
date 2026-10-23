package com.tarcitani.solomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;

/** Geometria de efeitos em barras e cubos retos, em luz própria, sem texturas lisas. */
final class FxGeometry {
    private FxGeometry() {}
    static void bar(PoseStack pose, VertexConsumer out, Vec3 a, Vec3 b, double width, int color, int alpha) {
        Vec3 dir = b.subtract(a);
        if (dir.lengthSqr() < 0.00001 || alpha <= 0) return;
        Vec3 side = dir.cross(Math.abs(dir.normalize().y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize().scale(width);
        Vec3 up = dir.normalize().cross(side).normalize().scale(width);
        Vec3[] p = {a.add(side).add(up), a.subtract(side).add(up), a.subtract(side).subtract(up), a.add(side).subtract(up),
                b.add(side).add(up), b.subtract(side).add(up), b.subtract(side).subtract(up), b.add(side).subtract(up)};
        for (int[] f : new int[][]{{0, 1, 2, 3}, {7, 6, 5, 4}, {0, 4, 5, 1}, {2, 6, 7, 3}, {0, 3, 7, 4}, {1, 5, 6, 2}}) {
            for (int index : f) {
                Vec3 v = p[index];
                out.addVertex(pose.last().pose(), (float) v.x, (float) v.y, (float) v.z)
                        .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, Math.min(255, alpha));
            }
        }
    }
    static void spark(PoseStack pose, VertexConsumer out, Vec3 p, double size, int color, int alpha) {
        bar(pose, out, p.add(0, -size, 0), p.add(0, size, 0), size, color, alpha);
    }
}
