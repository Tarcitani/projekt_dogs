package com.tarcitani.solomod.client;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.StatType;
import com.tarcitani.solomod.skill.SkillTree;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Ícones próprios 16×16, gerados por tools/gen_player_presentation.py; habilidades não usam itens genéricos. */
final class TalentIcons {
    private TalentIcons() {}
    static void draw(GuiGraphics g, String id, int x, int y) {
        g.blit(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "textures/gui/talents/" + id + ".png"), x, y, 0, 0, 16, 16, 16, 16);
    }
    static void draw(GuiGraphics g, SkillTree.Node node, int x, int y) {
        draw(g, node.id().substring(node.id().indexOf('.') + 1), x, y);
    }
    static void draw(GuiGraphics g, StatType stat, int x, int y) { draw(g, "stat_" + stat.getSerializedName(), x, y); }
}
