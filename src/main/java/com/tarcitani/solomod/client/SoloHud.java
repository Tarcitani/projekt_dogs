package com.tarcitani.solomod.client;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.StatsLogic.WeaponAbilities;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.skill.ClassAbility;
import com.tarcitani.solomod.skill.WeaponTechnique;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import java.util.UUID;

/**
 * HUD compacta (164 × 62 com arma): brasão rúnico, vida/mana/XP e faixa da técnica.
 * Vida inclui absorção e cores de veneno/wither; substitui só PLAYER_HEALTH, via ClientGameEvents.
 * Barras são suavizadas, o brasão pulsa em perigo/subida de nível e a carga da rapieira expande só 12 px.
 */
public final class SoloHud {
    private SoloHud() {}
    private static float mana, health, xp, lastHealth;
    private static UUID owner;
    private static int ticks, lastLevel, levelFlash, hurtFlash;

    public static void reset() { owner = null; }
    public static boolean visible() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && !mc.options.hideGui && !mc.player.isSpectator();
    }
    public static void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) { reset(); return; }
        var stats = player.getData(ModAttachments.PLAYER_STATS);
        float nextXp = (stats.getXpGlobal() % PlayerStatsAttachment.XP_PER_POINT) / (float) PlayerStatsAttachment.XP_PER_POINT;
        if (!player.getUUID().equals(owner)) {
            owner = player.getUUID(); mana = stats.getManaAtual(); health = lastHealth = player.getHealth(); xp = nextXp;
            lastLevel = stats.getLevel(); levelFlash = hurtFlash = 0;
        }
        if (stats.getLevel() != lastLevel) { levelFlash = 30; xp = 0; lastLevel = stats.getLevel(); }
        if (player.getHealth() < lastHealth) hurtFlash = 10;
        lastHealth = player.getHealth();
        mana = Mth.lerp(0.22F, mana, stats.getManaAtual());
        health = Mth.lerp(0.25F, health, player.getHealth());
        xp = Mth.lerp(0.2F, xp, nextXp);
        if (levelFlash > 0) levelFlash--;
        if (hurtFlash > 0) hurtFlash--;
        ticks++;
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        if (!visible()) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (!player.getUUID().equals(owner)) tick();
        var font = mc.font;
        var s = player.getData(ModAttachments.PLAYER_STATS);
        var held = player.getMainHandItem();
        boolean weapon = held.getItem() instanceof ModularWeaponItem;
        boolean charging = weapon && ((ModularWeaponItem) held.getItem()).weaponClass() == WeaponClass.RAPIER
                && player.isUsingItem() && player.getUseItem() == held;
        int x = 8, y = 8, bx = x + 40, bw = 124;
        float time = ticks + delta.getGameTimeDeltaPartialTick(false);
        float hp = player.getHealth() / Math.max(1, player.getMaxHealth());
        int accent = levelFlash > 0 ? 0xFFFFF0B8 : hp < 0.3F && Mth.sin(time * 0.3F) > 0 ? 0xFFEF6961 : RpgUi.GOLD;
        HudArt.crest(g, x + 2, y + 4, accent, time);
        Component level = Component.literal(String.valueOf(s.getLevel()));
        float scale = Math.min(1.1F, 23F / Math.max(1, font.width(level)));
        RpgUi.fit(g, font, level, x + 17 - Math.round(font.width(level) * scale / 2), y + 18, 23, RpgUi.TEXT, scale);
        int lifeColor = player.hasEffect(MobEffects.WITHER) ? 0xFF8A819D : player.hasEffect(MobEffects.POISON) ? 0xFF88AF42 : 0xFFDC595C;
        if (hurtFlash > 0 && hurtFlash % 4 < 2) lifeColor = 0xFFFFB9A1;
        HudArt.rail(g, bx, y + 5, bw, 11, health / Math.max(1, player.getMaxHealth()), lifeColor, time);
        Component life = Component.translatable("hud.solomod.health_compact", Math.round(player.getHealth()), Math.round(player.getMaxHealth()));
        if (player.getAbsorptionAmount() > 0) life = Component.translatable("hud.solomod.health_absorption", Math.round(player.getHealth()), Math.round(player.getMaxHealth()), Math.round(player.getAbsorptionAmount()));
        RpgUi.fit(g, font, life, bx + 5, y + 7, bw - 10, RpgUi.TEXT, 0.85F);
        // A absorção tem trilho dourado próprio sobre a borda da vida, além do número +escudo.
        if (player.getAbsorptionAmount() > 0) g.fill(bx, y + 3, bx + Math.max(1, (int) ((bw - 2) * Math.min(1, player.getAbsorptionAmount() / player.getMaxHealth()))), y + 4, RpgUi.GOLD);
        HudArt.rail(g, bx, y + 22, bw, 8, mana / Math.max(1, s.getMaxMana()), RpgUi.CYAN, time);
        RpgUi.fit(g, font, Component.translatable("hud.solomod.mana_compact", s.getManaAtual(), s.getMaxMana()), bx + 5, y + 22, bw - 10, RpgUi.TEXT, 0.75F);
        RpgUi.bar(g, bx, y + 33, bw - 1, 2, xp, RpgUi.GOLD, time);
        // Pequenas aletas ligam os trilhos ao brasão sem um painel retangular ocupando a tela.
        g.fill(x + 31, y + 11, bx - 2, y + 12, RpgUi.GOLD);
        g.fill(x + 30, y + 27, bx - 2, y + 28, 0xFF60778A);
        if (s.getPontosDisponiveis() > 0) {
            RpgUi.fit(g, font, Component.translatable("hud.solomod.points_compact", s.getPontosDisponiveis(), ClientSetup.OPEN_STATS.getTranslatedKeyMessage()),
                    x, y + 47, 36, accent, 0.75F);
        }
        if (weapon) {
            var data = ModularWeaponItem.getData(held);
            boolean unlocked = s.meets(data.requiredStatType(), data.requiredStatValue());
            int row = y + 41;
            g.fill(bx - 2, row - 2, bx + bw + 2, row + 20, 0xA0080E18);
            g.fill(bx - 2, row - 2, bx + 14, row - 1, accent);
            g.renderItem(held, bx, row);
            boolean selectable = WeaponTechnique.supports(data.weaponClass());
            Component technique = selectable ? WeaponControls.selected(data.weaponClass()).displayName()
                    : ClassAbility.hasAbility(data.weaponClass()) ? ClassAbility.displayName(data.weaponClass()) : data.weaponClass().displayName();
            RpgUi.fit(g, font, technique, bx + 20, row + 1, bw - 37, RpgUi.TEXT, 0.85F);
            if (selectable) {
                g.fill(bx + bw - 12, row, bx + bw - 1, row + 10, 0xFF3A3440);
                RpgUi.fit(g, font, WeaponControls.CYCLE.getTranslatedKeyMessage(), bx + bw - 10, row + 2, 8, accent, 0.85F);
            }
            float cd = player.getCooldowns().getCooldownPercent(held.getItem(), delta.getGameTimeDeltaPartialTick(false));
            RpgUi.bar(g, bx + 20, row + 13, bw - 24, 2, 1 - cd, cd > 0 ? 0xFF8E7FB8 : accent, time);
            int rune = unlocked ? ClientSetup.tintFor(data.elementType()) | 0xFF000000 : 0xFFE26E71;
            g.fill(bx + bw - 2, row + 12, bx + bw, row + 15, rune);
        }
        if (charging) {
            int row = y + 65;
            double distance = Math.round(ClassAbility.rapierDistance(player.getTicksUsingItem()) * 10) / 10.0;
            RpgUi.fit(g, font, Component.translatable("hud.solomod.rapier_charge", distance), bx, row, bw, RpgUi.CYAN, 0.75F);
            RpgUi.bar(g, bx, row + 8, bw - 1, 2, player.getTicksUsingItem() / (float) WeaponAbilities.RAPIER_CHARGE_TICKS, RpgUi.CYAN, time);
        }
    }
}
