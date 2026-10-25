package com.tarcitani.solomod.skill;

import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.StatsLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Catálogo comum das árvores por arma: IDs estáveis e dependências do mapa radial único.
 * Desbloqueios pertencem ao jogador, nunca ao ItemStack. O servidor valida pontos e pré-requisitos.
 * Custos de progressão e mana ficam em StatsLogic.Talents; habilidades de tier mantêm seus custos.
 */
public final class SkillTree {
    private SkillTree() {}

    public record Node(String id, WeaponClass weapon, Component name, String parent,
                       int cost, int mana, WeaponTechnique technique, WeaponSkill skill) {}

    public static final List<Node> NODES = create();

    private static List<Node> create() {
        List<Node> nodes = new ArrayList<>();
        for (WeaponClass weapon : WeaponClass.values()) {
            String prefix = weapon.getSerializedName() + ".";
            String root = "";
            if (WeaponTechnique.supports(weapon)) {
                var techniques = WeaponTechnique.options(weapon);
                for (int i = 0; i < techniques.size(); i++) {
                    WeaponTechnique t = techniques.get(i);
                    String id = prefix + t.name().toLowerCase(Locale.ROOT);
                    nodes.add(new Node(id, weapon, t.displayName(), i == 0 ? "" : root,
                            i == 0 ? StatsLogic.Talents.ROOT_COST : StatsLogic.Talents.BRANCH_COST,
                            StatsLogic.Talents.techniqueMana(t), t, null));
                    if (i == 0) root = id;
                }
            } else if (weapon == WeaponClass.DAGGER || weapon == WeaponClass.AXE) {
                root = prefix + (weapon == WeaponClass.DAGGER ? "throw" : "leap");
                nodes.add(new Node(root, weapon, ClassAbility.displayName(weapon), "", StatsLogic.Talents.ROOT_COST,
                        weapon == WeaponClass.DAGGER ? StatsLogic.Talents.THROW_MANA : StatsLogic.Talents.LEAP_MANA,
                        null, null));
                if (weapon == WeaponClass.DAGGER) nodes.add(new Node(prefix + "teleport", weapon,
                        Component.translatable("talent.solomod.teleport"), root, StatsLogic.Talents.BRANCH_COST,
                        StatsLogic.Talents.TELEPORT_MANA, null, null));
            }
            WeaponSkill tier = WeaponSkill.byId(weapon.defaultSkillId());
            String tierId = prefix + tier.id();
            nodes.add(new Node(tierId, weapon, tier.displayName(), root, StatsLogic.Talents.TIER_COST,
                    tier.manaCost(), null, tier));
            nodes.add(new Node(prefix + WeaponSkill.ELEMENTAL_BURST.id(), weapon,
                    WeaponSkill.ELEMENTAL_BURST.displayName(), tierId, StatsLogic.Talents.ULTIMATE_COST,
                    WeaponSkill.ELEMENTAL_BURST.manaCost(), null, WeaponSkill.ELEMENTAL_BURST));
        }
        return List.copyOf(nodes);
    }

    public static Node byId(String id) { return NODES.stream().filter(n -> n.id().equals(id)).findFirst().orElse(null); }
    public static List<Node> forWeapon(WeaponClass weapon) { return NODES.stream().filter(n -> n.weapon() == weapon).toList(); }
    public static Node forTechnique(WeaponTechnique technique) { return NODES.stream().filter(n -> n.technique() == technique).findFirst().orElseThrow(); }
    public static Node forSkill(WeaponClass weapon, WeaponSkill skill) { return byId(weapon.getSerializedName() + "." + skill.id()); }
    /** Shift + direito usa a habilidade avançada aprendida; a suprema substitui a anterior ao ser liberada. */
    public static WeaponSkill activeSkill(PlayerStatsAttachment stats, WeaponClass weapon) {
        if (stats.hasTalent(weapon.getSerializedName() + "." + WeaponSkill.ELEMENTAL_BURST.id())) return WeaponSkill.ELEMENTAL_BURST;
        WeaponSkill skill = WeaponSkill.byId(weapon.defaultSkillId());
        return stats.hasTalent(weapon.getSerializedName() + "." + skill.id()) ? skill : WeaponSkill.NONE;
    }
    public static Node classNode(ServerPlayer player, WeaponClass weapon) {
        return WeaponTechnique.supports(weapon) ? forTechnique(ClassAbility.technique(player, weapon))
                : byId(weapon.getSerializedName() + (weapon == WeaponClass.DAGGER ? ".throw" : ".leap"));
    }

    public static boolean available(PlayerStatsAttachment stats, Node node) {
        return !stats.hasTalent(node.id()) && (node.parent().isEmpty() || stats.hasTalent(node.parent()))
                && stats.getSkillPoints() >= node.cost();
    }

    /** Falhas não gastam mana; cada ativação válida cobra uma única vez, inclusive sem acertar alvos. */
    public static boolean consume(ServerPlayer player, Node node) {
        var stats = StatsLogic.stats(player);
        if (node == null || !stats.hasTalent(node.id())) {
            player.displayClientMessage(Component.translatable("message.solomod.talent_locked").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!stats.tryConsumeMana(node.mana())) {
            player.displayClientMessage(Component.translatable("message.solomod.no_mana", stats.getManaAtual(), node.mana())
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        StatsLogic.onStatsChanged(player);
        return true;
    }
}
