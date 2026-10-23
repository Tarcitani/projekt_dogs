package com.tarcitani.solomod.client;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.item.ModularWeaponItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * HUD do mod (canto superior esquerdo):
 * <pre>
 *  Nv 3            XP 245
 *  [====------]  (até o próximo ponto)
 *  MP 35/70
 *  [=========-]  (azul, animado)
 *  +2 pontos! [K]           (pisca)
 *  ── arma na mão ──
 *  Fogo · Ativa / Adormecida
 *  [recarga da habilidade]
 * </pre>
 */
public final class SoloHud {
    private SoloHud() {}

    private static final int X = 6, Y = 6, WIDTH = 110;
    private static final int BG = 0x90000000;

    /** Valor exibido da mana: persegue o valor real para a barra deslizar em vez de pular. */
    private static float shownMana = -1;
    private static float shownManaPrev = -1;
    private static int ticks;

    public static void tick() {
        ticks++;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        int real = player.getData(ModAttachments.PLAYER_STATS).getManaAtual();
        shownManaPrev = shownMana;
        shownMana = shownMana < 0 ? real : Mth.lerp(0.25F, shownMana, real);
        if (shownManaPrev < 0) shownManaPrev = shownMana;
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || player.isSpectator()) return;
        Font font = mc.font;
        PlayerStatsAttachment s = player.getData(ModAttachments.PLAYER_STATS);
        float partial = delta.getGameTimeDeltaPartialTick(false);

        ItemStack held = player.getMainHandItem();
        boolean holdingWeapon = held.getItem() instanceof ModularWeaponItem;
        int height = 46 + (s.getPontosDisponiveis() > 0 ? 11 : 0) + (holdingWeapon ? 26 : 0);
        g.fill(X - 3, Y - 3, X + WIDTH + 3, Y + height, BG);

        // Nível + XP Global
        int y = Y;
        g.drawString(font, Component.translatable("hud.solomod.level", s.getLevel()), X, y, 0xFFD54F, true);
        String xp = "XP " + s.getXpGlobal();
        g.drawString(font, xp, X + WIDTH - font.width(xp), y, 0xFFFFFF, true);
        y += 10;
        float xpProgress = (s.getXpGlobal() % PlayerStatsAttachment.XP_PER_POINT) / (float) PlayerStatsAttachment.XP_PER_POINT;
        bar(g, X, y, WIDTH, 4, xpProgress, 0xFFFFB300, 0xFF3A2E00);
        y += 8;

        // Mana (barra animada)
        float mana = Mth.lerp(partial, shownManaPrev, shownMana);
        g.drawString(font, "MP " + s.getManaAtual() + "/" + s.getMaxMana(), X, y, 0x7FC8FF, true);
        y += 10;
        bar(g, X, y, WIDTH, 6, mana / Math.max(1, s.getMaxMana()), 0xFF2F80ED, 0xFF0B2545);
        y += 10;

        // Pontos disponíveis (piscando)
        if (s.getPontosDisponiveis() > 0) {
            boolean on = (ticks / 10) % 2 == 0;
            Component text = Component.translatable("hud.solomod.points", s.getPontosDisponiveis(),
                    ClientSetup.OPEN_STATS.getTranslatedKeyMessage());
            g.drawString(font, text, X, y, on ? 0x7CFC00 : 0x3E7E00, true);
            y += 11;
        }

        // Arma na mão: elemento, estado da Maestria e recarga da habilidade
        if (holdingWeapon) {
            WeaponData data = ModularWeaponItem.getData(held);
            boolean unlocked = s.meets(data.requiredStatType(), data.requiredStatValue());
            g.fill(X, y, X + WIDTH, y + 1, 0x60FFFFFF);
            y += 4;
            Component state = unlocked
                    ? Component.translatable("hud.solomod.weapon_active", data.elementType().displayName())
                    : Component.translatable("hud.solomod.weapon_dormant", data.requiredStatValue(),
                    data.requiredStatType().shortName().getString());
            g.drawString(font, state, X, y, unlocked ? 0xFFFFFF : 0xFF5555, true);
            y += 10;
            if (data.hasSkill()) {
                float cd = player.getCooldowns().getCooldownPercent(held.getItem(), partial);
                int color = !unlocked ? 0xFF555555 : cd > 0 ? 0xFF9C27B0 : 0xFFE040FB;
                bar(g, X, y, WIDTH, 4, unlocked ? 1.0F - cd : 0F, color, 0xFF2A0033);
            }
        }
    }

    private static void bar(GuiGraphics g, int x, int y, int w, int h, float progress, int fg, int bg) {
        g.fill(x, y, x + w, y + h, bg);
        int filled = (int) (w * Mth.clamp(progress, 0F, 1F));
        if (filled > 0) g.fill(x, y, x + filled, y + h, fg);
        g.fill(x, y, x + filled, y + 1, 0x40FFFFFF); // brilho no topo
    }
}
