package com.tarcitani.solomod.client;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.core.StatType;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.network.SpendPointsPayload;
import com.tarcitani.solomod.network.TestActionPayload;
import com.tarcitani.solomod.network.TestActionPayload.Action;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela de Maestria (tecla K). Duas abas:
 * <ul>
 *   <li><b>Maestria</b>: nível, XP, pontos e botões +1/+5 para distribuir em FOR/AGI/INT/VIT.</li>
 *   <li><b>Teste</b>: atalhos dos comandos /modtest (gerar arma, invocar/marcar mob, XP, stats, mana, reset).
 *       Precisa de cheats/OP; o servidor recusa caso contrário.</li>
 * </ul>
 * Não pausa o jogo, então os valores atualizam ao vivo conforme o servidor sincroniza.
 */
public class StatsScreen extends Screen {
    private static final int PANEL_W = 280, PANEL_H = 214;
    private static final int BG = 0xE0101018, BORDER = 0xFF6A5ACD, HEADER = 0xFF1E1A3A;

    // Seleções da aba Teste ficam guardadas entre aberturas da tela.
    private static boolean testTab = false;
    private static int selClass = 0, selTier = 2, selElement = 3;

    private int left, top;
    private final List<Button> spendButtons = new ArrayList<>();

    public StatsScreen() {
        super(Component.translatable("screen.solomod.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        spendButtons.clear();

        // Abas
        addRenderableWidget(Button.builder(Component.translatable("screen.solomod.tab_mastery"), b -> switchTab(false))
                .bounds(left + 6, top + 6, 80, 16).build()).active = testTab;
        addRenderableWidget(Button.builder(Component.translatable("screen.solomod.tab_test"), b -> switchTab(true))
                .bounds(left + 90, top + 6, 80, 16).build()).active = !testTab;

        if (testTab) initTestTab(); else initMasteryTab();
    }

    private void switchTab(boolean test) {
        testTab = test;
        rebuildWidgets();
    }

    // ------------------------------------------------------------------ aba Maestria

    private int statRowY(int i) {
        return top + 62 + i * 26;
    }

    private void initMasteryTab() {
        StatType[] stats = StatType.values();
        for (int i = 0; i < stats.length; i++) {
            StatType stat = stats[i];
            int y = statRowY(i);
            spendButtons.add(addRenderableWidget(Button.builder(Component.literal("+1"),
                    b -> PacketDistributor.sendToServer(new SpendPointsPayload(stat, 1))).bounds(left + PANEL_W - 62, y, 26, 18).build()));
            spendButtons.add(addRenderableWidget(Button.builder(Component.literal("+5"),
                    b -> PacketDistributor.sendToServer(new SpendPointsPayload(stat, 5))).bounds(left + PANEL_W - 32, y, 26, 18).build()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (minecraft == null || minecraft.player == null) return;
        boolean hasPoints = stats().getPontosDisponiveis() > 0;
        for (Button b : spendButtons) b.active = hasPoints;
    }

    // ------------------------------------------------------------------ aba Teste

    private void initTestTab() {
        int x = left + 10, y = top + 34, w = 126, h = 18, gap = 22;
        int x2 = left + PANEL_W - 10 - w;

        // Seletores (clique para alternar)
        addRenderableWidget(Button.builder(classLabel(), b -> {
            selClass = (selClass + 1) % WeaponClass.values().length;
            b.setMessage(classLabel());
        }).bounds(x, y, w, h).build());
        addRenderableWidget(Button.builder(tierLabel(), b -> {
            selTier = (selTier + 1) % MobTier.values().length;
            b.setMessage(tierLabel());
        }).bounds(x, y + gap, w, h).build());
        addRenderableWidget(Button.builder(elementLabel(), b -> {
            selElement = (selElement + 1) % ElementType.values().length;
            b.setMessage(elementLabel());
        }).bounds(x, y + gap * 2, w, h).build());

        // Ações que usam a seleção
        addRenderableWidget(Button.builder(Component.translatable("screen.solomod.give_weapon"),
                b -> send(Action.GIVE_WEAPON, selClass, selTier, selElement)).bounds(x2, y, w, h).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.solomod.spawn_mob"),
                b -> send(Action.SPAWN_MOB, selTier, selElement, 0)).bounds(x2, y + gap, w, h).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.solomod.mark_nearest"),
                b -> send(Action.MARK_NEAREST, selTier, selElement, 0)).bounds(x2, y + gap * 2, w, h).build());

        // Atalhos de stats
        int y3 = y + gap * 3 + 12;
        StatType[] stats = StatType.values();
        int sw = (PANEL_W - 20 - 3 * 4) / 4;
        for (int i = 0; i < stats.length; i++) {
            StatType stat = stats[i];
            addRenderableWidget(Button.builder(Component.literal("+10 ").append(stat.shortName()),
                    b -> send(Action.ADD_STAT, stat.ordinal(), 10, 0)).bounds(left + 10 + i * (sw + 4), y3, sw, h).build());
            addRenderableWidget(Button.builder(Component.literal("-10 ").append(stat.shortName()),
                    b -> send(Action.ADD_STAT, stat.ordinal(), -10, 0)).bounds(left + 10 + i * (sw + 4), y3 + gap, sw, h).build());
        }

        int y4 = y3 + gap * 2 + 4;
        int bw = (PANEL_W - 20 - 2 * 4) / 3;
        addRenderableWidget(Button.builder(Component.translatable("screen.solomod.add_xp"), b -> send(Action.ADD_XP, 100, 0, 0))
                .bounds(left + 10, y4, bw, h).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.solomod.fill_mana"), b -> send(Action.FILL_MANA, 0, 0, 0))
                .bounds(left + 10 + bw + 4, y4, bw, h).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.solomod.reset"), b -> send(Action.RESET, 0, 0, 0))
                .bounds(left + 10 + (bw + 4) * 2, y4, bw, h).build());
    }

    private static void send(Action action, int a, int b, int c) {
        PacketDistributor.sendToServer(new TestActionPayload(action, a, b, c));
    }

    private static Component classLabel() {
        return Component.translatable("screen.solomod.sel_class", WeaponClass.values()[selClass].displayName());
    }

    private static Component tierLabel() {
        return Component.translatable("screen.solomod.sel_tier", MobTier.values()[selTier].displayName());
    }

    private static Component elementLabel() {
        return Component.translatable("screen.solomod.sel_element", ElementType.values()[selElement].displayName());
    }

    // ------------------------------------------------------------------ desenho

    private PlayerStatsAttachment stats() {
        return minecraft.player.getData(ModAttachments.PLAYER_STATS);
    }

    /** Painel atrás dos botões (desenhado no fundo para não cobrir os widgets). */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        g.fill(left - 1, top - 1, left + PANEL_W + 1, top + PANEL_H + 1, BORDER);
        g.fill(left, top, left + PANEL_W, top + PANEL_H, BG);
        g.fill(left, top, left + PANEL_W, top + 28, HEADER);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (minecraft == null || minecraft.player == null) return;
        PlayerStatsAttachment s = stats();

        g.drawString(font, title, left + PANEL_W - 8 - font.width(title), top + 10, 0xFFD54F, true);

        if (testTab) {
            renderTestTab(g, s);
        } else {
            renderMasteryTab(g, s);
        }
    }

    private void renderMasteryTab(GuiGraphics g, PlayerStatsAttachment s) {
        int x = left + 10, y = top + 34;
        g.drawString(font, Component.translatable("screen.solomod.summary", s.getLevel(), s.getXpGlobal()), x, y, 0xFFFFFF, true);
        Component points = Component.translatable("screen.solomod.points", s.getPontosDisponiveis());
        g.drawString(font, points, left + PANEL_W - 10 - font.width(points), y,
                s.getPontosDisponiveis() > 0 ? 0x7CFC00 : 0x888888, true);

        // Barra de XP até o próximo ponto
        float xp = (s.getXpGlobal() % PlayerStatsAttachment.XP_PER_POINT) / (float) PlayerStatsAttachment.XP_PER_POINT;
        bar(g, x, y + 12, PANEL_W - 20, 4, xp, 0xFFFFB300, 0xFF3A2E00);

        // Linhas de stats: nome, valor e o que ele dá agora
        StatType[] stats = StatType.values();
        for (int i = 0; i < stats.length; i++) {
            StatType stat = stats[i];
            int ry = statRowY(i);
            int value = s.get(stat);
            g.drawString(font, stat.shortName(), x, ry + 1, 0xFFFFFF, true);
            g.drawString(font, String.valueOf(value), x + 34, ry + 1, 0xFFFFFF, true);
            g.drawString(font, effectText(stat, s), x, ry + 11, 0x9E9E9E, false);
            // mini-barra proporcional (referência: 100 pontos)
            bar(g, x + 60, ry + 3, 110, 4, value / 100F, statColor(stat), 0xFF222222);
        }

        // Mana e arma na mão
        int by = statRowY(4) + 2;
        g.drawString(font, "MP " + s.getManaAtual() + "/" + s.getMaxMana(), x, by, 0x7FC8FF, true);
        bar(g, x + 80, by + 2, PANEL_W - 100, 5, s.getManaAtual() / (float) Math.max(1, s.getMaxMana()), 0xFF2F80ED, 0xFF0B2545);

        ItemStack held = minecraft.player.getMainHandItem();
        if (held.getItem() instanceof ModularWeaponItem) {
            WeaponData data = ModularWeaponItem.getData(held);
            boolean ok = s.meets(data.requiredStatType(), data.requiredStatValue());
            g.renderItem(held, x, by + 12);
            g.drawString(font, held.getHoverName(), x + 20, by + 12, 0xFFFFFF, true);
            g.drawString(font, Component.translatable("tooltip.solomod.requires", data.requiredStatValue(),
                    data.requiredStatType().shortName().getString()), x + 20, by + 22, ok ? 0x55FF55 : 0xFF5555, false);
        }
    }

    private void renderTestTab(GuiGraphics g, PlayerStatsAttachment s) {
        int y = top + 34 + 22 * 3;
        g.drawString(font, Component.translatable("screen.solomod.test_hint"), left + 10, y, 0x9E9E9E, false);
        String line = String.format("FOR %d  AGI %d  INT %d  VIT %d  |  XP %d  Pts %d",
                s.get(StatType.FOR), s.get(StatType.AGI), s.get(StatType.INT), s.get(StatType.VIT),
                s.getXpGlobal(), s.getPontosDisponiveis());
        g.drawString(font, line, left + 10, top + PANEL_H - 14, 0xFFFFFF, true);
    }

    private static Component effectText(StatType stat, PlayerStatsAttachment s) {
        int v = s.get(stat);
        return switch (stat) {
            case FOR -> Component.translatable("screen.solomod.effect_for", String.format("%.2f", v * 0.25));
            case AGI -> Component.translatable("screen.solomod.effect_agi", v, String.format("%.1f", Math.min(50, v * 0.5)));
            case INT -> Component.translatable("screen.solomod.effect_int", s.getMaxMana(), 1 + v / 10);
            case VIT -> Component.translatable("screen.solomod.effect_vit", v, Math.min(50, v));
        };
    }

    private static int statColor(StatType stat) {
        return switch (stat) {
            case FOR -> 0xFFE53935;
            case AGI -> 0xFF43A047;
            case INT -> 0xFF1E88E5;
            case VIT -> 0xFFFFB300;
        };
    }

    private static void bar(GuiGraphics g, int x, int y, int w, int h, float progress, int fg, int bg) {
        g.fill(x, y, x + w, y + h, bg);
        int filled = (int) (w * Mth.clamp(progress, 0F, 1F));
        if (filled > 0) g.fill(x, y, x + filled, y + h, fg);
    }
}
