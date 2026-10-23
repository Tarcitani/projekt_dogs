package com.tarcitani.solomod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Malhas dos efeitos 3D do Soberano, no estilo do Minecraft: só CUBOS e barras retas, nada redondo. Cada face
 * mostra a textura 16x16 inteira (cada texel vira um pixel grande), em luz máxima.
 */
final class EffectMesh {
    private EffectMesh() {}

    /** Cubo centrado na origem, com {@code half} de meia-aresta. */
    static void cube(PoseStack pose, VertexConsumer buffer, float half, int r, int g, int b, int alpha) {
        box(pose, buffer, -half, -half, -half, half, half, half, r, g, b, alpha);
    }

    /** Caixa de (x0, y0, z0) a (x1, y1, z1), com as 6 faces texturizadas. */
    static void box(PoseStack pose, VertexConsumer buffer, float x0, float y0, float z0, float x1, float y1, float z1,
                    int r, int g, int b, int alpha) {
        quad(pose, buffer, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0, 0, -1, r, g, b, alpha); // norte
        quad(pose, buffer, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, 0, 0, 1, r, g, b, alpha);  // sul
        quad(pose, buffer, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, -1, 0, 0, r, g, b, alpha); // oeste
        quad(pose, buffer, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, 1, 0, 0, r, g, b, alpha);  // leste
        quad(pose, buffer, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0, 1, 0, r, g, b, alpha);  // topo
        quad(pose, buffer, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, 0, -1, 0, r, g, b, alpha); // fundo
    }

    /**
     * Anel QUADRADO oco de {@code y0} a {@code y1}: quatro barras de espessura {@code thick} formando um quadrado
     * de meia-aresta externa {@code outer}. É a "camada" do funil do tornado.
     */
    static void squareRing(PoseStack pose, VertexConsumer buffer, float outer, float thick, float y0, float y1,
                           int r, int g, int b, int alpha) {
        float inner = outer - thick;
        box(pose, buffer, -outer, y0, -outer, outer, y1, -inner, r, g, b, alpha);      // norte
        box(pose, buffer, -outer, y0, inner, outer, y1, outer, r, g, b, alpha);        // sul
        box(pose, buffer, -outer, y0, -inner, -inner, y1, inner, r, g, b, alpha);      // oeste
        box(pose, buffer, inner, y0, -inner, outer, y1, inner, r, g, b, alpha);        // leste
    }

    private static void quad(PoseStack pose, VertexConsumer buffer,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float nx, float ny, float nz, int r, int g, int b, int alpha) {
        vertex(pose, buffer, ax, ay, az, 0, 1, r, g, b, alpha, nx, ny, nz);
        vertex(pose, buffer, bx, by, bz, 1, 1, r, g, b, alpha, nx, ny, nz);
        vertex(pose, buffer, cx, cy, cz, 1, 0, r, g, b, alpha, nx, ny, nz);
        vertex(pose, buffer, dx, dy, dz, 0, 0, r, g, b, alpha, nx, ny, nz);
    }

    private static void vertex(PoseStack pose, VertexConsumer buffer, float x, float y, float z, float u, float v,
                               int r, int g, int b, int alpha, float nx, float ny, float nz) {
        buffer.addVertex(pose.last().pose(), x, y, z).setColor(r, g, b, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose.last(), nx, ny, nz);
    }
}
