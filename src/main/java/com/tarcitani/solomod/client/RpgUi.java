package com.tarcitani.solomod.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Linguagem visual compartilhada: aço escuro, filetes dourados, runas ciano e recortes de pixels. */
public final class RpgUi {
    private RpgUi() {}
    public static final int GOLD = 0xFFD7B875, CYAN = 0xFF72DDEB, TEXT = 0xFFE9E5DA, MUTED = 0xFF9BA8B8;
    public static final int DARK = 0xED0B111B, CARD = 0xE916202E, LINE = 0xFF475369;

    public static void panel(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x65000000);
        g.fill(x + 2, y, x + w - 2, y + h, DARK);
        g.fill(x, y + 2, x + w, y + h - 2, DARK);
        g.fill(x + 3, y, x + w - 3, y + 1, accent);
        g.fill(x + 3, y + h - 1, x + w - 3, y + h, LINE);
        g.fill(x, y + 3, x + 1, y + h - 3, LINE);
        g.fill(x + w - 1, y + 3, x + w, y + h - 3, LINE);
        for (int cx : new int[]{x + 2, x + w - 4}) for (int cy : new int[]{y + 2, y + h - 4}) g.fill(cx, cy, cx + 2, cy + 2, accent);
    }

    public static void card(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x, y, x + w, y + h, CARD);
        g.fill(x, y, x + 2, y + h, accent);
        g.fill(x + 3, y + h - 1, x + w, y + h, 0x503C4C62);
    }

    public static void bar(GuiGraphics g, int x, int y, int w, int h, float progress, int color, float time) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF080D15);
        g.fill(x, y, x + w, y + h, 0xFF233044);
        int fill = (int) (w * Mth.clamp(progress, 0, 1));
        if (fill > 0) {
            g.fill(x, y, x + fill, y + h, color);
            g.fill(x, y, x + fill, y + 1, 0x70FFFFFF);
            int shine = (int) ((time * 0.65F) % Math.max(1, w));
            if (shine < fill) g.fill(x + shine, y, x + Math.min(fill, shine + 2), y + h, 0x45FFFFFF);
        }
        for (int i = 1; i < 10; i++) g.fill(x + w * i / 10, y, x + w * i / 10 + 1, y + h, 0x4009121D);
    }

    public static void fit(GuiGraphics g, Font font, Component text, int x, int y, int maxWidth, int color, float maxScale) {
        float scale = Math.min(maxScale, maxWidth / (float) Math.max(1, font.width(text)));
        g.pose().pushPose(); g.pose().translate(x, y, 0); g.pose().scale(scale, scale, 1);
        g.drawString(font, text, 0, 0, color, false); g.pose().popPose();
    }
}
