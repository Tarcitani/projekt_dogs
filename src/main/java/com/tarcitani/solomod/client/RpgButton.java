package com.tarcitani.solomod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Botão de aço e ouro, com foco visível e a mesma navegação/narração do Button vanilla. */
public final class RpgButton extends Button {
    public static RpgBuilder builder(Component text, OnPress action) { return new RpgBuilder(text, action); }
    public static final class RpgBuilder extends Button.Builder {
        private final Component text;
        private final OnPress action;
        private int x, y, w = 150, h = 20;
        public RpgBuilder(Component text, OnPress action) { super(text, action); this.text = text; this.action = action; }
        @Override public RpgBuilder bounds(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; return this; }
        @Override public RpgButton build() { return new RpgButton(text, x, y, w, h, action); }
    }
    public RpgButton(Component text, int x, int y, int w, int h, OnPress action) {
        super(x, y, w, h, text, action, DEFAULT_NARRATION);
    }
    @Override protected void renderWidget(GuiGraphics g, int mx, int my, float partial) {
        int border = active ? (isHoveredOrFocused() ? RpgUi.GOLD : RpgUi.LINE) : 0xFF303A49;
        g.fill(getX(), getY(), getX() + width, getY() + height, border);
        g.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1,
                active && isHoveredOrFocused() ? 0xFF30394A : 0xFF172130);
        var font = Minecraft.getInstance().font;
        RpgUi.fit(g, font, getMessage(), getX() + 5, getY() + (height - 7) / 2, width - 10,
                active ? RpgUi.TEXT : RpgUi.MUTED, 0.85F);
    }
}
