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
 * O painel usa cartões de aço escuro e ouro, em duas colunas. O seletor de armas usa o catálogo atual; os pacotes sempre enviam o ordinal original da classe.
 */
public class StatsScreen extends Screen {
    private static final int PANEL_W = 308, PANEL_H = 226;

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
        addRenderableWidget(RpgButton.builder(Component.translatable("screen.solomod.tab_mastery"), b -> switchTab(false))
                .bounds(left + 10, top + 34, 86, 16).build()).active = testTab;
        addRenderableWidget(RpgButton.builder(Component.translatable("screen.solomod.tab_test"), b -> switchTab(true))
                .bounds(left + 102, top + 34, 86, 16).build()).active = !testTab;

        if (testTab) initTestTab(); else initMasteryTab();
    }

    private void switchTab(boolean test) {
        testTab = test;
        rebuildWidgets();
    }

    // ------------------------------------------------------------------ aba Maestria

    private int statRowY(int i) {
        return top + 90 + (i / 2) * 46;
    }

    private int statRowX(int i) { return left + 10 + (i % 2) * 148; }

    private void initMasteryTab() {
        StatType[] stats = StatType.values();
        for (int i = 0; i < stats.length; i++) {
            StatType stat = stats[i];
            int y = statRowY(i);
            int x = statRowX(i);
            spendButtons.add(addRenderableWidget(RpgButton.builder(Component.literal("+1"),
                    b -> PacketDistributor.sendToServer(new SpendPointsPayload(stat, 1))).bounds(x + 88, y + 4, 22, 16).build()));
            spendButtons.add(addRenderableWidget(RpgButton.builder(Component.literal("+5"),
                    b -> PacketDistributor.sendToServer(new SpendPointsPayload(stat, 5))).bounds(x + 114, y + 4, 22, 16).build()));
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
        int x = left + 10, y = top + 58, w = 138, h = 18, gap = 22;
        int x2 = left + PANEL_W - 10 - w;

        // Seletores (clique para alternar)
        addRenderableWidget(RpgButton.builder(classLabel(), b -> {
            selClass = (selClass + 1) % WeaponClass.AVAILABLE.size();
            b.setMessage(classLabel());
        }).bounds(x, y, w, h).build());
        addRenderableWidget(RpgButton.builder(tierLabel(), b -> {
            selTier = (selTier + 1) % MobTier.values().length;
            b.setMessage(tierLabel());
        }).bounds(x, y + gap, w, h).build());
        addRenderableWidget(RpgButton.builder(elementLabel(), b -> {
            selElement = (selElement + 1) % ElementType.values().length;
            b.setMessage(elementLabel());
        }).bounds(x, y + gap * 2, w, h).build());

        // Ações que usam a seleção
        addRenderableWidget(RpgButton.builder(Component.translatable("screen.solomod.give_weapon"),
                b -> send(Action.GIVE_WEAPON, WeaponClass.AVAILABLE.get(selClass).ordinal(), selTier, selElement)).bounds(x2, y, w, h).build());
        addRenderableWidget(RpgButton.builder(Component.translatable("screen.solomod.spawn_mob"),
                b -> send(Action.SPAWN_MOB, selTier, selElement, 0)).bounds(x2, y + gap, w, h).build());
        addRenderableWidget(RpgButton.builder(Component.translatable("screen.solomod.mark_nearest"),
                b -> send(Action.MARK_NEAREST, selTier, selElement, 0)).bounds(x2, y + gap * 2, w, h).build());

        // Atalhos de stats
        int y3 = y + gap * 3 + 12;
        StatType[] stats = StatType.values();
        int sw = (PANEL_W - 20 - 3 * 4) / 4;
        for (int i = 0; i < stats.length; i++) {
            StatType stat = stats[i];
            addRenderableWidget(RpgButton.builder(Component.literal("+10 ").append(stat.shortName()),
                    b -> send(Action.ADD_STAT, stat.ordinal(), 10, 0)).bounds(left + 10 + i * (sw + 4), y3, sw, h).build());
            addRenderableWidget(RpgButton.builder(Component.literal("-10 ").append(stat.shortName()),
                    b -> send(Action.ADD_STAT, stat.ordinal(), -10, 0)).bounds(left + 10 + i * (sw + 4), y3 + gap, sw, h).build());
        }

        int y4 = y3 + gap * 2 + 4;
        int bw = (PANEL_W - 20 - 2 * 4) / 3;
        addRenderableWidget(RpgButton.builder(Component.translatable("screen.solomod.add_xp"), b -> send(Action.ADD_XP, 100, 0, 0))
                .bounds(left + 10, y4, bw, h).build());
        addRenderableWidget(RpgButton.builder(Component.translatable("screen.solomod.fill_mana"), b -> send(Action.FILL_MANA, 0, 0, 0))
                .bounds(left + 10 + bw + 4, y4, bw, h).build());
        addRenderableWidget(RpgButton.builder(Component.translatable("screen.solomod.reset"), b -> send(Action.RESET, 0, 0, 0))
                .bounds(left + 10 + (bw + 4) * 2, y4, bw, h).build());
    }

    private static void send(Action action, int a, int b, int c) {
        PacketDistributor.sendToServer(new TestActionPayload(action, a, b, c));
    }

    private static Component classLabel() {
        return Component.translatable("screen.solomod.sel_class", WeaponClass.AVAILABLE.get(selClass).displayName());
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
        g.fill(0, 0, width, height, 0xA0060C15);
        RpgUi.panel(g, left, top, PANEL_W, PANEL_H, RpgUi.GOLD);
        RpgUi.card(g, left + 7, top + 6, PANEL_W - 14, 24, RpgUi.GOLD);
        g.drawString(font, title, left + 17, top + 14, RpgUi.GOLD, false);
        if (!testTab && minecraft != null && minecraft.player != null) drawMasteryBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (minecraft == null || minecraft.player == null) return;
        int tabX = left + (testTab ? 102 : 10);
        g.fill(tabX, top + 51, tabX + 86, top + 52, RpgUi.GOLD);
        PlayerStatsAttachment s = stats();

        Component level = Component.translatable("hud.solomod.level", s.getLevel());
        g.drawString(font, level, left + PANEL_W - 18 - font.width(level), top + 14, RpgUi.TEXT, false);

        if (testTab) {
            renderTestTab(g, s);
        } else {
            renderMasteryTab(g, s);
        }
    }

    /** Cartões de atributos são desenhados antes dos botões, preservando clique, foco e narração. */
    private void drawMasteryBackground(GuiGraphics g) {
        for (int i = 0; i < 4; i++) RpgUi.card(g, statRowX(i), statRowY(i), 140, 42, statColor(StatType.values()[i]));
        RpgUi.card(g, left + 10, top + 184, PANEL_W - 20, 34, RpgUi.GOLD);
    }

    private void renderMasteryTab(GuiGraphics g, PlayerStatsAttachment s) {
        int x = left + 10;
        RpgUi.fit(g, font, Component.translatable("screen.solomod.summary", s.getLevel(), s.getXpGlobal()), x, top + 58, 180, RpgUi.TEXT, 0.85F);
        RpgUi.fit(g, font, Component.translatable("screen.solomod.points", s.getPontosDisponiveis()), left + 203, top + 58, 93, RpgUi.GOLD, 0.85F);
        RpgUi.fit(g, font, Component.translatable("hud.solomod.mana", s.getManaAtual(), s.getMaxMana()), x, top + 71, 280, RpgUi.CYAN, 0.75F);
        RpgUi.bar(g, x, top + 82, PANEL_W - 20, 3, (s.getXpGlobal() % PlayerStatsAttachment.XP_PER_POINT) / (float) PlayerStatsAttachment.XP_PER_POINT, RpgUi.GOLD, minecraft.player.tickCount);
        for (int i = 0; i < 4; i++) {
            StatType stat = StatType.values()[i]; int sx = statRowX(i), sy = statRowY(i);
            g.drawString(font, stat.shortName(), sx + 8, sy + 7, statColor(stat), false);
            g.drawString(font, String.valueOf(s.get(stat)), sx + 41, sy + 7, RpgUi.TEXT, false);
            RpgUi.fit(g, font, effectText(stat, s), sx + 8, sy + 23, 124, RpgUi.MUTED, 0.7F);
            RpgUi.bar(g, sx + 8, sy + 35, 124, 2, s.get(stat) / 100F, statColor(stat), minecraft.player.tickCount);
        }
        ItemStack held = minecraft.player.getMainHandItem();
        if (held.getItem() instanceof ModularWeaponItem) {
            WeaponData data = ModularWeaponItem.getData(held);
            boolean ok = s.meets(data.requiredStatType(), data.requiredStatValue());
            g.renderItem(held, x + 7, top + 193);
            RpgUi.fit(g, font, held.getHoverName(), x + 29, top + 190, 250, RpgUi.TEXT, 0.85F);
            Component detail = com.tarcitani.solomod.skill.WeaponTechnique.supports(data.weaponClass())
                    ? Component.translatable("hud.solomod.technique", WeaponControls.selected(data.weaponClass()).displayName(), WeaponControls.CYCLE.getTranslatedKeyMessage())
                    : Component.translatable("tooltip.solomod.requires", data.requiredStatValue(), data.requiredStatType().shortName().getString());
            RpgUi.fit(g, font, detail, x + 29, top + 204, 250, ok ? RpgUi.CYAN : 0xFFE38A8A, 0.75F);
        } else RpgUi.fit(g, font, Component.translatable("screen.solomod.combat_hint"), x + 10, top + 197, 268, RpgUi.MUTED, 0.8F);
    }

    private void renderTestTab(GuiGraphics g, PlayerStatsAttachment s) {
        int y = top + 124;
        RpgUi.fit(g, font, Component.translatable("screen.solomod.test_hint"), left + 10, y, PANEL_W - 20, RpgUi.MUTED, 0.7F);
        Component line = Component.translatable("screen.solomod.test_summary",
                s.get(StatType.FOR), s.get(StatType.AGI), s.get(StatType.INT), s.get(StatType.VIT),
                s.getXpGlobal(), s.getPontosDisponiveis());
        RpgUi.fit(g, font, line, left + 10, top + PANEL_H - 12, PANEL_W - 20, RpgUi.MUTED, 0.7F);
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

}
