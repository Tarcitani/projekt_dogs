package com.tarcitani.solomod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/** Arte procedural exclusiva da HUD: brasão facetado, coroa, runas, filigranas e barras com pontas em pixels. */
final class HudArt {
    private HudArt() {}
    static void crest(GuiGraphics g, int x, int y, int accent, float time) {
        // Silhueta simétrica de um escudo de metal, construída por linhas retas de pixels.
        for (int row = 0; row < 36; row++) {
            int inset = row < 4 ? 4 - row : row > 26 ? (row - 26) : 0;
            int left = x + inset, right = x + 30 - inset;
            g.fill(left + 1, y + row + 2, right + 2, y + row + 3, 0x65000000);
            g.fill(left, y + row, right, y + row + 1, row < 16 ? 0xFF1F2C3F : 0xFF0C1423);
            g.fill(left, y + row, left + 1, y + row + 1, accent);
            g.fill(right - 1, y + row, right, y + row + 1, 0xFF79613E);
        }
        g.fill(x + 4, y, x + 26, y + 1, accent);
        g.fill(x + 4, y + 5, x + 26, y + 6, 0xFF39465B);
        // Coroa sobre o nível, losango central e ornamentos em degraus.
        for (int i = 0; i < 3; i++) g.fill(x + 8 + i * 6, y - 3 - (i == 1 ? 2 : 0), x + 10 + i * 6, y + 1, accent);
        g.fill(x + 14, y + 27, x + 16, y + 30, accent);
        g.fill(x + 13, y + 28, x + 17, y + 29, accent);
        for (int i = 0; i < 4; i++) {
            float angle = time * 0.035F + i * Mth.HALF_PI;
            int px = x + 14 + Math.round(Mth.cos(angle) * 19), py = y + 15 + Math.round(Mth.sin(angle) * 17);
            g.fill(px, py, px + 1, py + 2, i % 2 == 0 ? accent : RpgUi.CYAN);
        }
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
