package com.tarcitani.solomod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/** Arte da HUD: escudo facetado com XP no perímetro, coroa, filigranas e barras em pixels. */
final class HudArt {
    private HudArt() {}
    private static final java.util.List<int[]> XP_CONTOUR = shieldContour();
    static void crest(GuiGraphics g, int x, int y, int accent, float progress) {
        // Silhueta simétrica de um escudo de metal, construída por linhas retas de pixels.
        for (int row = 0; row < 36; row++) {
            int inset = row < 4 ? 4 - row : row > 26 ? (row - 26) : 0;
            int left = x + inset, right = x + 30 - inset;
            g.fill(left + 1, y + row + 2, right + 2, y + row + 3, 0x65000000);
            g.fill(left, y + row, right, y + row + 1, row < 16 ? 0xFF1F2C3F : 0xFF0C1423);
            g.fill(left, y + row, left + 1, y + row + 1, 0xFF524A3F);
            g.fill(right - 1, y + row, right, y + row + 1, 0xFF79613E);
        }
        g.fill(x + 4, y, x + 26, y + 1, accent);
        g.fill(x + 4, y + 5, x + 26, y + 6, 0xFF39465B);
        // Coroa sobre o nível, losango central e ornamentos em degraus.
        for (int i = 0; i < 3; i++) g.fill(x + 8 + i * 6, y - 3 - (i == 1 ? 2 : 0), x + 10 + i * 6, y + 1, accent);
        g.fill(x + 14, y + 27, x + 16, y + 30, accent);
        g.fill(x + 13, y + 28, x + 17, y + 29, accent);
        int filled = Math.round(Mth.clamp(progress, 0, 1) * XP_CONTOUR.size());
        for (int i = 0; i < XP_CONTOUR.size(); i++) {
            int[] p = XP_CONTOUR.get(i);
            g.fill(x + p[0], y + p[1], x + p[0] + 1, y + p[1] + 1, i < filled ? accent : 0xFF514839);
        }
        if (filled > 0) {
            int[] tip = XP_CONTOUR.get(filled - 1);
            g.fill(x + tip[0], y + tip[1], x + tip[0] + 1, y + tip[1] + 1, 0xFFFFF2C4);
        }
    }

    /** Percurso horário fechado a partir do topo, sem duplicar os pixels dos vértices. */
    private static java.util.List<int[]> shieldContour() {
        int[][] corners = {{15, 0}, {25, 0}, {29, 4}, {29, 26}, {20, 35}, {9, 35}, {0, 26}, {0, 4}, {4, 0}, {15, 0}};
        var pixels = new java.util.ArrayList<int[]>();
        for (int i = 1; i < corners.length; i++) {
            int[] a = corners[i - 1], b = corners[i];
            int steps = Math.max(Math.abs(b[0] - a[0]), Math.abs(b[1] - a[1]));
            for (int step = 0; step < steps; step++) pixels.add(new int[]{
                    a[0] + (b[0] - a[0]) * step / steps, a[1] + (b[1] - a[1]) * step / steps});
        }
        return java.util.List.copyOf(pixels);
    }

    static void rail(GuiGraphics g, int x, int y, int w, int h, float value, int color, float time) {
        g.fill(x - 2, y - 2, x + w + 1, y + h + 2, 0xA0080D17);
        g.fill(x - 1, y - 1, x + w, y + h + 1, 0xFF5A4A36);
        g.fill(x, y, x + w - 1, y + h, 0xFF161D2B);
        int fill = Math.round((w - 2) * Mth.clamp(value, 0, 1));
        if (fill > 0) {
            g.fillGradient(x, y, x + fill, y + h, color, shade(color));
            g.fill(x, y, x + fill, y + 1, 0x90FFFFFF);
            int shine = (int) (time * 0.7F) % Math.max(1, w);
            if (shine < fill) g.fill(x + shine, y + 1, x + Math.min(fill, shine + 2), y + h, 0x40FFFFFF);
        }
        g.fill(x + w, y + 2, x + w + 2, y + Math.max(3, h - 2), RpgUi.GOLD);
        if (h >= 6) for (int i = 1; i < 4; i++) g.fill(x + w * i / 4, y + h - 2, x + w * i / 4 + 1, y + h, 0x70081019);
    }

    private static int shade(int color) { return 0xFF000000 | ((color >> 16 & 255) * 3 / 5 << 16) | ((color >> 8 & 255) * 3 / 5 << 8) | ((color & 255) * 3 / 5); }
}
