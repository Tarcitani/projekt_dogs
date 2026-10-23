package com.tarcitani.solomod.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Créditos do fim da versão de testes, abertos por {@link BossCinematic} quando a cinemática da morte do
 * Soberano acaba. O texto sobe sozinho com a música de créditos do Minecraft; ESPAÇO acelera e ESC fecha
 * (o jogador volta ao mundo onde estava).
 *
 * <p>Textos em {@code credits.solomod.*} no lang. Os nomes dos autores não são traduzidos.
 */
public class CreditsScreen extends Screen {
    private static final float SPEED = 0.6F, FAST = 4.0F;
    private static final int LINE = 12;
    private static final String[] AUTHORS = {"Gustavo Tarcitani", "Vitor Oliveira Gomes"};

    private record Line(FormattedCharSequence text, int color, float scale) {}

    private final List<Line> lines = new ArrayList<>();
    private float scroll;
    private boolean fast;

    public CreditsScreen() {
        super(Component.translatable("credits.solomod.title"));
    }

    @Override
    protected void init() {
        lines.clear();
        heading("credits.solomod.title", 0xE8C547, 3.0F);
        text(Component.translatable("credits.solomod.subtitle"), 0xA8A8A8);
        gap(4);
        for (int i = 1; i <= 4; i++) text(Component.translatable("credits.solomod.epilogue." + i), 0xFFFFFF);
        gap(4);
        heading("credits.solomod.created_by", 0xE8C547, 1.5F);
        for (String author : AUTHORS) text(Component.literal(author), 0xFFFFFF);
        gap(2);
        heading("credits.solomod.made_with", 0xE8C547, 1.5F);
        text(Component.literal("Minecraft · NeoForge"), 0xFFFFFF);
        gap(4);
        heading("credits.solomod.thanks", 0xFFFFFF, 2.0F);
        gap(6);
        text(Component.translatable("credits.solomod.to_be_continued").withStyle(ChatFormatting.ITALIC), 0xD04545);
    }

    private void heading(String key, int color, float scale) {
        add(Component.translatable(key).withStyle(ChatFormatting.BOLD), color, scale);
        gap(1);
    }

    private void text(Component text, int color) {
        add(text, color, 1.0F);
    }

    /** Quebra o texto na largura da tela (já considerando a escala). */
    private void add(Component text, int color, float scale) {
        for (FormattedCharSequence part : font.split(text, (int) (Math.min(width - 40, 320) / scale))) {
            lines.add(new Line(part, color, scale));
        }
    }

    private void gap(int count) {
        for (int i = 0; i < count; i++) lines.add(new Line(FormattedCharSequence.EMPTY, 0, 1.0F));
    }

    private int totalHeight() {
        int h = 0;
        for (Line line : lines) h += (int) (LINE * line.scale());
        return h;
    }

    @Override
    public void tick() {
        // Com o jogo pausado, música e sons precisam ser tocados à mão (como na tela de créditos vanilla).
        minecraft.getMusicManager().tick();
        minecraft.getSoundManager().tick(false);
        if (scroll > totalHeight() + height) onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        scroll += (fast ? FAST : SPEED) * minecraft.getTimer().getRealtimeDeltaTicks();
        g.fill(0, 0, width, height, 0xFF000000);
        float y = height - scroll;
        for (Line line : lines) {
            int lineHeight = (int) (LINE * line.scale());
            if (y > -lineHeight && y < height) {
                g.pose().pushPose();
                g.pose().translate(width / 2F, y, 0);
                g.pose().scale(line.scale(), line.scale(), 1);
                g.drawString(font, line.text(), -font.width(line.text()) / 2, 0, 0xFF000000 | line.color(), true);
                g.pose().popPose();
            }
            y += lineHeight;
        }
        Component hint = Component.translatable("credits.solomod.skip");
        g.drawString(font, hint, width - font.width(hint) - 6, height - 12, 0xFF555555, false);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // Fundo preto desenhado no render.
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_SPACE) fast = true;
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_SPACE) fast = false;
        return super.keyReleased(key, scanCode, modifiers);
    }

    @Override
    public void removed() {
        minecraft.getMusicManager().stopPlaying(Musics.CREDITS);
    }

    @Override
    public Music getBackgroundMusic() {
        return Musics.CREDITS;
    }
}
