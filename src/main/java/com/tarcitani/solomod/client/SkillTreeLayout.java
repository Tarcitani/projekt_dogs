package com.tarcitani.solomod.client;

import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.skill.SkillTree;
import com.tarcitani.solomod.skill.WeaponSkill;
import java.util.List;

/**
 * Geometria do mapa único: jogador na origem, as quatro armas do catálogo no círculo e habilidades
 * crescendo para fora em raízes. Apenas apresentação; IDs, custos e dependências ficam no catálogo.
 * Cada setor ocupa 90 graus; classes legadas não aparecem no mapa, mas seus dados são preservados.
 */
final class SkillTreeLayout {
    private SkillTreeLayout() {}
    static final List<WeaponClass> WEAPONS = WeaponClass.AVAILABLE;
    static final List<SkillTree.Node> NODES = SkillTree.NODES.stream().filter(n -> WEAPONS.contains(n.weapon())).toList();
    static final int EXTENT = 346;
    record Point(double x, double y) {}
    static double angle(WeaponClass weapon) { return Math.toRadians(-135 + WEAPONS.indexOf(weapon) * 90); }
    static Point polar(WeaponClass weapon, double radius, double offset) {
        double angle = angle(weapon) + Math.toRadians(offset);
        return new Point(Math.cos(angle) * radius, Math.sin(angle) * radius);
    }
    static Point weapon(WeaponClass weapon) { return polar(weapon, 84, 0); }
    static Point node(SkillTree.Node node) {
        if (node.skill() == WeaponSkill.ELEMENTAL_BURST) return polar(node.weapon(), 322, 0);
        if (node.skill() != null) return polar(node.weapon(), node.parent().isEmpty() ? 156 : 248, 0);
        if (node.parent().isEmpty()) return polar(node.weapon(), 150, 0);
        var siblings = SkillTree.forWeapon(node.weapon()).stream()
                .filter(n -> n.skill() == null && !n.parent().isEmpty()).toList();
        int index = siblings.indexOf(node);
        return polar(node.weapon(), 215, siblings.size() == 1 ? -16 : index == 0 ? -16 : 16);
    }
}
