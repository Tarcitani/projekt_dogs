package com.tarcitani.solomod.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.util.Map;

/**
 * Malhas dos mobs do mod: a forma base de cada criatura e uma variante por elemento.
 * ARQUIVO GERADO por tools/gen_mob_art.py — edite o script, não este arquivo,
 * senão as UVs deixam de bater com as texturas.
 */
@SuppressWarnings("unused")
public final class MobMeshes {
    private MobMeshes() {}

    /** Todas as variantes (nome = textura em textures/entity/&lt;nome&gt;.png). */
    public static final String[] VARIANTS = {"minion", "elite", "guardian", "monarch", "minion_fire", "minion_water", "minion_earth", "minion_air", "elite_fire", "elite_water", "elite_earth", "elite_air", "guardian_fire", "guardian_water", "guardian_earth", "guardian_air", "monarch_fire", "monarch_water", "monarch_earth", "monarch_air", "sovereign"};

    /** Partes decorativas de cada variante, animadas por prefixo em TierMobModel. */
    public static final Map<String, String[]> DECOR = Map.ofEntries(
            Map.entry("minion", new String[]{}),
            Map.entry("elite", new String[]{}),
            Map.entry("guardian", new String[]{}),
            Map.entry("monarch", new String[]{}),
            Map.entry("minion_fire", new String[]{"flick_crest_1", "flick_crest_2", "flick_crest_3", "flick_jet_l", "flick_jet_r"}),
            Map.entry("minion_water", new String[]{"sway_dorsal", "sway_lure", "pulse_bulb"}),
            Map.entry("minion_earth", new String[]{"pulse_crystal_1", "pulse_crystal_2", "pulse_crystal_3", "orbit_pebbles"}),
            Map.entry("minion_air", new String[]{"sway_crest", "spin_ring"}),
            Map.entry("elite_fire", new String[]{"flick_crown", "flick_crown2", "sway_chain", "sway_chain2", "sway_chain3"}),
            Map.entry("elite_water", new String[]{"sway_tail_1", "sway_tail_2", "sway_tail_3", "sway_fluke", "sway_crest"}),
            Map.entry("elite_earth", new String[]{"pulse_shoulder_r", "pulse_shoulder_l"}),
            Map.entry("elite_air", new String[]{"flap_helm_l", "flap_helm_r", "flap_wing_l", "flap_wing_r"}),
            Map.entry("guardian_fire", new String[]{"roll_corona"}),
            Map.entry("guardian_water", new String[]{"pulse_core", "sway_tent_5", "sway_tent_6", "sway_tent_7", "sway_tent_8"}),
            Map.entry("guardian_earth", new String[]{"spin_ring"}),
            Map.entry("guardian_air", new String[]{"flick_bolt_1", "flick_bolt_2", "flick_bolt_3", "spin_ring_a", "roll_ring_b"}),
            Map.entry("monarch_fire", new String[]{"flick_plume", "flick_plume2", "flick_plume3", "flick_shoulder_r", "flick_drip_r", "flick_shoulder_l", "flick_drip_l"}),
            Map.entry("monarch_water", new String[]{"sway_beard_1", "sway_beard_2", "sway_beard_3", "sway_beard_4", "sway_pincer_r", "sway_pincer_l"}),
            Map.entry("monarch_earth", new String[]{"orbit_pollen"}),
            Map.entry("monarch_air", new String[]{"bob_cloud", "flap_wing_l", "flap_wing_r", "flap_helm_l", "flap_helm_r", "spin_halo"}),
            Map.entry("sovereign", new String[]{"flick_lava_r", "flick_plume", "flick_plume2", "flick_plume3", "flap_wing_l", "flap_wing_r", "sway_beard_1", "sway_beard_2", "sway_beard_3", "sway_beard_4", "spin_halo", "sway_pincer_l", "orbit_shards"}));

    public static LayerDefinition get(String variant) {
        return switch (variant) {
            case "minion" -> minion();
            case "elite" -> elite();
            case "guardian" -> guardian();
            case "monarch" -> monarch();
            case "minion_fire" -> minionFire();
            case "minion_water" -> minionWater();
            case "minion_earth" -> minionEarth();
            case "minion_air" -> minionAir();
            case "elite_fire" -> eliteFire();
            case "elite_water" -> eliteWater();
            case "elite_earth" -> eliteEarth();
            case "elite_air" -> eliteAir();
            case "guardian_fire" -> guardianFire();
            case "guardian_water" -> guardianWater();
            case "guardian_earth" -> guardianEarth();
            case "guardian_air" -> guardianAir();
            case "monarch_fire" -> monarchFire();
            case "monarch_water" -> monarchWater();
            case "monarch_earth" -> monarchEarth();
            case "monarch_air" -> monarchAir();
            case "sovereign" -> sovereign();
            default -> throw new IllegalArgumentException("Variante desconhecida: " + variant);
        };
    }

    /** Gerado de tools/gen_mob_art.py (minion). */
    private static LayerDefinition minion() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3F, -7F, -2F, 6F, 7F, 4F)
                .texOffs(32, 35).addBox(-2F, -6F, -2.6F, 4F, 5F, 1F)
                .texOffs(26, 47).addBox(-0.5F, -7F, 2F, 1F, 2F, 1F)
                .texOffs(30, 47).addBox(-0.5F, -4.5F, 2F, 1F, 2F, 1F)
                .texOffs(34, 47).addBox(-0.5F, -2F, 2F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 18.5F, 0F, 0.45F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -8F, -5F, 9F, 8F, 8F)
                .texOffs(20, 47).addBox(-1F, -7.5F, -5.6F, 2F, 2F, 1F),
                PartPose.offset(0F, -7F, -1F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(20, 16).addBox(-4F, 0F, -4.5F, 8F, 2F, 6F),
                PartPose.offset(0F, -1F, -1F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(0, 43).addBox(0F, -1.5F, 0F, 7F, 3F, 1F),
                PartPose.offsetAndRotation(4.5F, -5F, -1F, 0F, 0.35F, -0.35F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(16, 43).addBox(-7F, -1.5F, 0F, 7F, 3F, 1F),
                PartPose.offsetAndRotation(-4.5F, -5F, -1F, 0F, -0.35F, 0.35F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(42, 35).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(2.5F, -8F, -2F, -0.3F, 0F, 0.3F));
        PartDefinition p_horn_l_tip = p_horn_l.addOrReplaceChild("horn_l_tip", CubeListBuilder.create()
                .texOffs(12, 47).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, -4F, 0F, -0.5F, 0F, 0.2F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(46, 35).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-2.5F, -8F, -2F, -0.3F, 0F, -0.3F));
        PartDefinition p_horn_r_tip = p_horn_r.addOrReplaceChild("horn_r_tip", CubeListBuilder.create()
                .texOffs(16, 47).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, -4F, 0F, -0.5F, 0F, -0.2F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-1.5F, -1F, -1F, 2F, 10F, 2F),
                PartPose.offset(-3.5F, -6F, 0F));
        PartDefinition p_claw_r = p_arm_r.addOrReplaceChild("claw_r", CubeListBuilder.create()
                .texOffs(8, 35).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(-0.5F, 9F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(42, 0).addBox(-0.5F, -1F, -1F, 2F, 10F, 2F),
                PartPose.offset(3.5F, -6F, 0F));
        PartDefinition p_claw_l = p_arm_l.addOrReplaceChild("claw_l", CubeListBuilder.create()
                .texOffs(20, 35).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0.5F, 9F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(0, 27).addBox(0F, -6F, 0F, 9F, 8F, 0F)
                .texOffs(38, 47).addBox(0F, -6.5F, -0.5F, 9F, 1F, 1F),
                PartPose.offset(2F, -6F, 2F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(18, 27).addBox(-9F, -6F, 0F, 9F, 8F, 0F)
                .texOffs(0, 51).addBox(-9F, -6.5F, -0.5F, 9F, 1F, 1F),
                PartPose.offset(-2F, -6F, 2F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(36, 27).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F),
                PartPose.offset(0F, -0.5F, 1.5F));
        PartDefinition p_tail_tip = p_tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(32, 43).addBox(-1.5F, -0.5F, 0F, 3F, 1F, 3F),
                PartPose.offset(0F, 0F, 7F));
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(52, 27).addBox(-1F, 0F, -1F, 2F, 6F, 2F)
                .texOffs(44, 43).addBox(-1.5F, 5F, -2.5F, 3F, 1F, 3F),
                PartPose.offset(-1.7F, 18F, 0.5F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-1F, 0F, -1F, 2F, 6F, 2F)
                .texOffs(0, 47).addBox(-1.5F, 5F, -2.5F, 3F, 1F, 3F),
                PartPose.offset(1.7F, 18F, 0.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Gerado de tools/gen_mob_art.py (elite). */
    private static LayerDefinition elite() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(90, 0).addBox(-1.5F, 0F, -1.5F, 3F, 15F, 3F)
                .texOffs(36, 63).addBox(-2F, 5F, -2.2F, 4F, 3F, 4F)
                .texOffs(0, 63).addBox(-2F, 13F, -3.5F, 4F, 2F, 5F),
                PartPose.offset(-2.3F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(102, 0).addBox(-1.5F, 0F, -1.5F, 3F, 15F, 3F)
                .texOffs(52, 63).addBox(-2F, 5F, -2.2F, 4F, 3F, 4F)
                .texOffs(18, 63).addBox(-2F, 13F, -3.5F, 4F, 2F, 5F),
                PartPose.offset(2.3F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(82, 42).addBox(-3.5F, -4F, -2.5F, 7F, 5F, 5F)
                .texOffs(52, 42).addBox(-4.5F, -1F, -3F, 9F, 5F, 6F)
                .texOffs(48, 25).addBox(-5F, -14F, -3F, 10F, 10F, 6F)
                .texOffs(92, 54).addBox(-3F, -16F, -3F, 6F, 2F, 6F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(80, 25).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F),
                PartPose.offset(0F, -16F, 0F));
        PartDefinition p_plume = p_head.addOrReplaceChild("plume", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-0.5F, -6F, -3F, 1F, 6F, 11F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_plume_tail = p_plume.addOrReplaceChild("plume_tail", CubeListBuilder.create()
                .texOffs(108, 25).addBox(-0.5F, 0F, 0F, 1F, 12F, 2F),
                PartPose.offsetAndRotation(0F, -2F, 8F, 0.25F, 0F, 0F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(86, 63).addBox(0F, -1F, -1F, 5F, 2F, 2F),
                PartPose.offsetAndRotation(3.5F, -5F, 0F, 0F, 0F, -0.6F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(100, 63).addBox(-5F, -1F, -1F, 5F, 2F, 2F),
                PartPose.offsetAndRotation(-3.5F, -5F, 0F, 0F, 0F, 0.6F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(24, 25).addBox(-1.5F, -1F, -1.5F, 3F, 14F, 3F),
                PartPose.offset(-6.5F, -13F, 0F));
        PartDefinition p_pauldron_r = p_arm_r.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-3.5F, -3F, -3.5F, 6F, 5F, 7F, new CubeDeformation(0.2F))
                .texOffs(0, 54).addBox(-4F, 1F, -4F, 7F, 1F, 8F)
                .texOffs(0, 70).addBox(-2.5F, -6F, -0.5F, 1F, 3F, 1F)
                .texOffs(24, 70).addBox(-0.5F, -5F, -0.5F, 1F, 2F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gauntlet_r = p_arm_r.addOrReplaceChild("gauntlet_r", CubeListBuilder.create()
                .texOffs(60, 54).addBox(-2F, 0F, -2F, 4F, 5F, 4F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_sword = p_arm_r.addOrReplaceChild("sword", CubeListBuilder.create()
                .texOffs(76, 63).addBox(-0.5F, -0.5F, -1F, 1F, 1F, 4F)
                .texOffs(114, 63).addBox(-1F, -1F, 3F, 2F, 2F, 2F)
                .texOffs(8, 70).addBox(-3.5F, -1F, -2F, 7F, 2F, 1F)
                .texOffs(0, 0).addBox(-0.5F, -1.5F, -24F, 1F, 3F, 22F)
                .texOffs(122, 63).addBox(-0.5F, -1F, -26F, 1F, 2F, 2F),
                PartPose.offset(0F, 12F, -1F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(36, 25).addBox(-1.5F, -1F, -1.5F, 3F, 14F, 3F),
                PartPose.offset(6.5F, -13F, 0F));
        PartDefinition p_pauldron_l = p_arm_l.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(26, 42).addBox(-2.5F, -3F, -3.5F, 6F, 5F, 7F, new CubeDeformation(0.2F))
                .texOffs(30, 54).addBox(-3F, 1F, -4F, 7F, 1F, 8F)
                .texOffs(4, 70).addBox(1.5F, -6F, -0.5F, 1F, 3F, 1F)
                .texOffs(28, 70).addBox(-0.5F, -5F, -0.5F, 1F, 2F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gauntlet_l = p_arm_l.addOrReplaceChild("gauntlet_l", CubeListBuilder.create()
                .texOffs(76, 54).addBox(-2F, 0F, -2F, 4F, 5F, 4F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_shield = p_arm_l.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(68, 0).addBox(0F, -5F, -5F, 1F, 10F, 10F)
                .texOffs(68, 63).addBox(1F, -1.5F, -1.5F, 1F, 3F, 3F),
                PartPose.offset(2F, 7F, 0F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-5F, 0F, 0F, 10F, 22F, 1F),
                PartPose.offset(0F, -15F, 3F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Gerado de tools/gen_mob_art.py (guardian). */
    private static LayerDefinition guardian() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(36, 0).addBox(-6F, -10F, -4F, 12F, 10F, 8F)
                .texOffs(0, 19).addBox(-8F, -12F, -5F, 16F, 3F, 10F)
                .texOffs(60, 32).addBox(-7F, -18F, 3F, 14F, 7F, 2F)
                .texOffs(76, 0).addBox(-7F, 0F, -5F, 14F, 5F, 10F)
                .texOffs(52, 19).addBox(-5F, 5F, -4F, 10F, 5F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_eye = p_body.addOrReplaceChild("eye", CubeListBuilder.create()
                .texOffs(8, 32).addBox(-4F, -4F, -1.5F, 8F, 8F, 2F)
                .texOffs(96, 32).addBox(-3F, -3F, -2F, 6F, 6F, 1F),
                PartPose.offset(0F, -5F, -4F));
        PartDefinition p_tendril_1 = p_body.addOrReplaceChild("tendril_1", CubeListBuilder.create()
                .texOffs(28, 32).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(-3F, 10F, -2.5F));
        PartDefinition p_tendril_1_tip = p_tendril_1.addOrReplaceChild("tendril_1_tip", CubeListBuilder.create()
                .texOffs(86, 43).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tendril_2 = p_body.addOrReplaceChild("tendril_2", CubeListBuilder.create()
                .texOffs(36, 32).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(3F, 10F, -2.5F));
        PartDefinition p_tendril_2_tip = p_tendril_2.addOrReplaceChild("tendril_2_tip", CubeListBuilder.create()
                .texOffs(90, 43).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tendril_3 = p_body.addOrReplaceChild("tendril_3", CubeListBuilder.create()
                .texOffs(44, 32).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(-3F, 10F, 2.5F));
        PartDefinition p_tendril_3_tip = p_tendril_3.addOrReplaceChild("tendril_3_tip", CubeListBuilder.create()
                .texOffs(94, 43).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tendril_4 = p_body.addOrReplaceChild("tendril_4", CubeListBuilder.create()
                .texOffs(52, 32).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(3F, 10F, 2.5F));
        PartDefinition p_tendril_4_tip = p_tendril_4.addOrReplaceChild("tendril_4_tip", CubeListBuilder.create()
                .texOffs(98, 43).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -10F, -4.5F, 9F, 10F, 9F),
                PartPose.offset(0F, -12F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(88, 19).addBox(-5F, -1F, -5F, 10F, 2F, 10F)
                .texOffs(102, 43).addBox(-4.5F, -6F, -4.5F, 1F, 5F, 1F)
                .texOffs(106, 43).addBox(3.5F, -6F, -4.5F, 1F, 5F, 1F)
                .texOffs(92, 32).addBox(-0.5F, -8F, -5F, 1F, 7F, 1F)
                .texOffs(118, 43).addBox(-4.5F, -5F, 3.5F, 1F, 4F, 1F)
                .texOffs(122, 43).addBox(3.5F, -5F, 3.5F, 1F, 4F, 1F)
                .texOffs(16, 50).addBox(-1F, -3F, -5.5F, 2F, 2F, 1F),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_halo = p_head.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(22, 50).addBox(-6F, -6F, 0F, 12F, 1F, 1F)
                .texOffs(48, 50).addBox(-6F, 5F, 0F, 12F, 1F, 1F)
                .texOffs(0, 32).addBox(-6F, -5F, 0F, 1F, 10F, 1F)
                .texOffs(4, 32).addBox(5F, -5F, 0F, 1F, 10F, 1F),
                PartPose.offset(0F, -12F, 5F));
        PartDefinition p_hand_r = root.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(110, 32).addBox(-2F, -2F, -1.5F, 4F, 4F, 3F)
                .texOffs(14, 43).addBox(-2.5F, -4F, -2F, 5F, 2F, 4F)
                .texOffs(0, 50).addBox(-2F, 2F, -1.5F, 1F, 4F, 1F)
                .texOffs(110, 43).addBox(-0.5F, 2F, -1.5F, 1F, 5F, 1F)
                .texOffs(4, 50).addBox(1F, 2F, -1.5F, 1F, 4F, 1F),
                PartPose.offset(-12F, -2F, -3F));
        PartDefinition p_hand_l = root.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(0, 43).addBox(-2F, -2F, -1.5F, 4F, 4F, 3F)
                .texOffs(32, 43).addBox(-2.5F, -4F, -2F, 5F, 2F, 4F)
                .texOffs(8, 50).addBox(-2F, 2F, -1.5F, 1F, 4F, 1F)
                .texOffs(114, 43).addBox(-0.5F, 2F, -1.5F, 1F, 5F, 1F)
                .texOffs(12, 50).addBox(1F, 2F, -1.5F, 1F, 4F, 1F),
                PartPose.offset(12F, -2F, -3F));
        PartDefinition p_orb_1 = root.addOrReplaceChild("orb_1", CubeListBuilder.create()
                .texOffs(50, 43).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_2 = root.addOrReplaceChild("orb_2", CubeListBuilder.create()
                .texOffs(62, 43).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_3 = root.addOrReplaceChild("orb_3", CubeListBuilder.create()
                .texOffs(74, 43).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** Gerado de tools/gen_mob_art.py (monarch). */
    private static LayerDefinition monarch() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(112, 64).addBox(-4F, 0F, -4F, 8F, 10F, 8F),
                PartPose.offset(-7F, 6F, 0F));
        PartDefinition p_shin_r = p_leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(40, 64).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F)
                .texOffs(0, 111).addBox(-5F, -1F, -5.5F, 10F, 4F, 2F)
                .texOffs(172, 84).addBox(-5F, 6F, -7F, 10F, 3F, 11F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(144, 64).addBox(-4F, 0F, -4F, 8F, 10F, 8F),
                PartPose.offset(7F, 6F, 0F));
        PartDefinition p_shin_l = p_leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(76, 64).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F)
                .texOffs(24, 111).addBox(-5F, -1F, -5.5F, 10F, 4F, 2F)
                .texOffs(214, 84).addBox(-5F, 6F, -7F, 10F, 3F, 11F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(176, 64).addBox(-9F, -4F, -5F, 18F, 6F, 10F)
                .texOffs(38, 99).addBox(-5F, 2F, -5.5F, 10F, 10F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12F, -24F, -8F, 24F, 24F, 16F)
                .texOffs(92, 99).addBox(-7F, -10F, -8.5F, 14F, 8F, 1F)
                .texOffs(122, 99).addBox(-4F, -19F, -9F, 8F, 8F, 1F),
                PartPose.offsetAndRotation(0F, -4F, 0F, 0.2F, 0F, 0F));
        PartDefinition p_shard_1 = p_chest.addOrReplaceChild("shard_1", CubeListBuilder.create()
                .texOffs(136, 84).addBox(-1.5F, -12F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(-6F, -18F, 7F, -0.5F, 0F, -0.35F));
        PartDefinition p_shard_2 = p_chest.addOrReplaceChild("shard_2", CubeListBuilder.create()
                .texOffs(148, 84).addBox(-1.5F, -12F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(0F, -18F, 7F, -0.5F, 0F, 0F));
        PartDefinition p_shard_3 = p_chest.addOrReplaceChild("shard_3", CubeListBuilder.create()
                .texOffs(160, 84).addBox(-1.5F, -12F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(6F, -18F, 7F, -0.5F, 0F, 0.35F));
        PartDefinition p_cape = p_chest.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-11F, 0F, 0F, 22F, 26F, 1F),
                PartPose.offset(0F, -23F, 8F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-5F, -9F, -6F, 10F, 10F, 10F),
                PartPose.offset(0F, -24F, -6F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 99).addBox(-5F, 0F, -5.5F, 10F, 3F, 9F)
                .texOffs(64, 111).addBox(-5F, -3F, -6F, 1F, 4F, 1F)
                .texOffs(68, 111).addBox(4F, -3F, -6F, 1F, 4F, 1F),
                PartPose.offset(0F, 0F, -1F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(0, 84).addBox(-6F, -2F, -7F, 12F, 3F, 12F)
                .texOffs(140, 99).addBox(-5.5F, -9F, -6.5F, 2F, 7F, 2F)
                .texOffs(84, 99).addBox(-1F, -11F, -7F, 2F, 9F, 2F)
                .texOffs(148, 99).addBox(3.5F, -9F, -6.5F, 2F, 7F, 2F)
                .texOffs(204, 99).addBox(-5.5F, -7F, 3.5F, 2F, 5F, 2F)
                .texOffs(212, 99).addBox(3.5F, -7F, 3.5F, 2F, 5F, 2F)
                .texOffs(72, 111).addBox(-1F, -4F, -7.5F, 2F, 2F, 1F),
                PartPose.offset(0F, -9F, 0F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(156, 99).addBox(-8F, -2F, -2F, 8F, 4F, 4F),
                PartPose.offsetAndRotation(-5F, -6F, -1F, 0F, 0F, -0.4F));
        PartDefinition p_horn_r2 = p_horn_r.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(60, 99).addBox(-1.5F, -8F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(-8F, 0F, 0F, 0F, 0F, 0.9F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(220, 99).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0F, 0.5F));
        PartDefinition p_arm_r = p_chest.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(126, 0).addBox(-4F, -2F, -4.5F, 8F, 16F, 9F),
                PartPose.offset(-15F, -20F, 0F));
        PartDefinition p_pauldron_r = p_arm_r.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(136, 40).addBox(-6F, -5F, -6F, 11F, 8F, 12F)
                .texOffs(228, 99).addBox(-3F, -10F, -1F, 2F, 5F, 2F)
                .texOffs(48, 111).addBox(1F, -9F, -1F, 2F, 4F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(194, 0).addBox(-5F, 0F, -5F, 10F, 14F, 10F)
                .texOffs(48, 84).addBox(-5.5F, 2F, -5.5F, 11F, 4F, 11F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_r = p_forearm_r.addOrReplaceChild("fist_r", CubeListBuilder.create()
                .texOffs(40, 40).addBox(-6F, 0F, -6F, 12F, 9F, 12F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(180, 99).addBox(0F, -2F, -2F, 8F, 4F, 4F),
                PartPose.offsetAndRotation(5F, -6F, -1F, 0F, 0F, 0.4F));
        PartDefinition p_horn_l2 = p_horn_l.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(72, 99).addBox(-1.5F, -8F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(8F, 0F, 0F, 0F, 0F, -0.9F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(236, 99).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0F, -0.5F));
        PartDefinition p_arm_l = p_chest.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(160, 0).addBox(-4F, -2F, -4.5F, 8F, 16F, 9F),
                PartPose.offset(15F, -20F, 0F));
        PartDefinition p_pauldron_l = p_arm_l.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(182, 40).addBox(-5F, -5F, -6F, 11F, 8F, 12F)
                .texOffs(244, 99).addBox(-3F, -10F, -1F, 2F, 5F, 2F)
                .texOffs(56, 111).addBox(1F, -9F, -1F, 2F, 4F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-5F, 0F, -5F, 10F, 14F, 10F)
                .texOffs(92, 84).addBox(-5.5F, 2F, -5.5F, 11F, 4F, 11F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_l = p_forearm_l.addOrReplaceChild("fist_l", CubeListBuilder.create()
                .texOffs(88, 40).addBox(-6F, 0F, -6F, 12F, 9F, 12F),
                PartPose.offset(0F, 14F, 0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    /** Gerado de tools/gen_mob_art.py (minion_fire). */
    private static LayerDefinition minionFire() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(48, 14).addBox(-1F, 0F, -1F, 2F, 6F, 2F)
                .texOffs(0, 38).addBox(-1.5F, 5F, -2.5F, 3F, 1F, 3F),
                PartPose.offset(-1.7F, 18F, 0.5F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(56, 14).addBox(-1F, 0F, -1F, 2F, 6F, 2F)
                .texOffs(12, 38).addBox(-1.5F, 5F, -2.5F, 3F, 1F, 3F),
                PartPose.offset(1.7F, 18F, 0.5F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-3F, -7F, -2F, 6F, 7F, 4F)
                .texOffs(36, 32).addBox(-2F, -6F, -2.6F, 4F, 4F, 1F),
                PartPose.offsetAndRotation(0F, 18.5F, 0F, 0.35F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -7F, -4.5F, 8F, 7F, 7F),
                PartPose.offset(0F, -7F, -1F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-3.5F, 0F, -4F, 7F, 2F, 5F),
                PartPose.offset(0F, -0.5F, -1F));
        PartDefinition p_flick_crest_1 = p_head.addOrReplaceChild("flick_crest_1", CubeListBuilder.create()
                .texOffs(8, 14).addBox(-3F, -4F, -3F, 6F, 4F, 6F),
                PartPose.offset(0F, -7F, 0F));
        PartDefinition p_flick_crest_2 = p_flick_crest_1.addOrReplaceChild("flick_crest_2", CubeListBuilder.create()
                .texOffs(32, 14).addBox(-2F, -4F, -2F, 4F, 4F, 4F),
                PartPose.offset(0F, -4F, 0.5F));
        PartDefinition p_flick_crest_3 = p_flick_crest_2.addOrReplaceChild("flick_crest_3", CubeListBuilder.create()
                .texOffs(46, 32).addBox(-1F, -3F, -1F, 2F, 3F, 2F),
                PartPose.offset(0F, -4F, 0.5F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(24, 38).addBox(0F, -1F, -0.5F, 3F, 2F, 1F),
                PartPose.offsetAndRotation(4F, -5F, -1F, 0F, 0F, -0.5F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(32, 38).addBox(-3F, -1F, -0.5F, 3F, 2F, 1F),
                PartPose.offsetAndRotation(-4F, -5F, -1F, 0F, 0F, 0.5F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(50, 0).addBox(-1.5F, -1F, -1F, 2F, 9F, 2F),
                PartPose.offset(-3.5F, -6F, 0F));
        PartDefinition p_claw_r = p_arm_r.addOrReplaceChild("claw_r", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(-0.5F, 8F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-0.5F, -1F, -1F, 2F, 9F, 2F),
                PartPose.offset(3.5F, -6F, 0F));
        PartDefinition p_claw_l = p_arm_l.addOrReplaceChild("claw_l", CubeListBuilder.create()
                .texOffs(12, 32).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0.5F, 8F, 0F));
        PartDefinition p_flick_jet_l = p_body.addOrReplaceChild("flick_jet_l", CubeListBuilder.create()
                .texOffs(38, 25).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(2F, -6F, 2F, -0.6F, 0F, 0.3F));
        PartDefinition p_flick_jet_r = p_body.addOrReplaceChild("flick_jet_r", CubeListBuilder.create()
                .texOffs(46, 25).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(-2F, -6F, 2F, -0.6F, 0F, -0.3F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(24, 25).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offset(0F, -0.5F, 1.5F));
        PartDefinition p_tail_tip = p_tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(24, 32).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 3F),
                PartPose.offset(0F, 0F, 6F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Gerado de tools/gen_mob_art.py (minion_water). */
    private static LayerDefinition minionWater() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(38, 17).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F)
                .texOffs(24, 36).addBox(-2F, 5F, -3.5F, 4F, 1F, 4F),
                PartPose.offset(-2F, 18F, 0.5F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(50, 17).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F)
                .texOffs(40, 36).addBox(-2F, 5F, -3.5F, 4F, 1F, 4F),
                PartPose.offset(2F, 18F, 0.5F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-3.5F, -6F, -2.5F, 7F, 6F, 5F)
                .texOffs(0, 42).addBox(-2.5F, -5F, -3F, 5F, 4F, 1F),
                PartPose.offsetAndRotation(0F, 18.5F, 0F, 0.3F, 0F, 0F));
        PartDefinition p_sway_dorsal = p_body.addOrReplaceChild("sway_dorsal", CubeListBuilder.create()
                .texOffs(0, 17).addBox(0F, -5F, -1F, 0F, 5F, 6F),
                PartPose.offset(0F, -6F, 2F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -8F, -6F, 10F, 8F, 9F),
                PartPose.offset(0F, -6F, -1F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-4.5F, 0F, -4.5F, 9F, 2F, 6F),
                PartPose.offset(0F, 0F, -2F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(48, 28).addBox(0F, -3F, 0F, 6F, 6F, 0F),
                PartPose.offsetAndRotation(5F, -5F, 0F, 0F, 0.4F, 0F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-6F, -3F, 0F, 6F, 6F, 0F),
                PartPose.offsetAndRotation(-5F, -5F, 0F, 0F, -0.4F, 0F));
        PartDefinition p_sway_lure = p_head.addOrReplaceChild("sway_lure", CubeListBuilder.create()
                .texOffs(44, 28).addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(0F, -8F, -3F, -0.6F, 0F, 0F));
        PartDefinition p_pulse_bulb = p_sway_lure.addOrReplaceChild("pulse_bulb", CubeListBuilder.create()
                .texOffs(12, 36).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(22, 17).addBox(-1F, -1F, -1F, 2F, 8F, 2F),
                PartPose.offset(-4F, -5F, 0F));
        PartDefinition p_claw_r = p_arm_r.addOrReplaceChild("claw_r", CubeListBuilder.create()
                .texOffs(12, 42).addBox(-2F, 0F, -1.5F, 3F, 3F, 1F),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(30, 17).addBox(-1F, -1F, -1F, 2F, 8F, 2F),
                PartPose.offset(4F, -5F, 0F));
        PartDefinition p_claw_l = p_arm_l.addOrReplaceChild("claw_l", CubeListBuilder.create()
                .texOffs(20, 42).addBox(-1F, 0F, -1.5F, 3F, 3F, 1F),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(30, 28).addBox(-1F, -1F, 0F, 2F, 2F, 5F),
                PartPose.offset(0F, -1F, 2F));
        PartDefinition p_tail_tip = p_tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(12, 17).addBox(0F, -3F, 0F, 0F, 6F, 5F),
                PartPose.offset(0F, 0F, 5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Gerado de tools/gen_mob_art.py (minion_earth). */
    private static LayerDefinition minionEarth() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(28, 25).addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F),
                PartPose.offset(-2.2F, 20F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(40, 25).addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F),
                PartPose.offset(2.2F, 20F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -8F, -3.5F, 9F, 8F, 7F)
                .texOffs(0, 25).addBox(-4F, -9F, -3F, 8F, 1F, 6F),
                PartPose.offsetAndRotation(0F, 20F, 0F, 0.15F, 0F, 0F));
        PartDefinition p_pulse_crystal_1 = p_body.addOrReplaceChild("pulse_crystal_1", CubeListBuilder.create()
                .texOffs(52, 25).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(-2F, -8F, 1F, -0.3F, 0F, -0.3F));
        PartDefinition p_pulse_crystal_2 = p_body.addOrReplaceChild("pulse_crystal_2", CubeListBuilder.create()
                .texOffs(22, 15).addBox(-1F, -7F, -1F, 2F, 7F, 2F),
                PartPose.offsetAndRotation(1.5F, -8F, 2F, -0.2F, 0F, 0.2F));
        PartDefinition p_pulse_crystal_3 = p_body.addOrReplaceChild("pulse_crystal_3", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1F, -4F, -1F, 2F, 4F, 2F),
                PartPose.offsetAndRotation(0F, -6F, 3F, 0.4F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-3F, -4F, -3F, 6F, 5F, 5F),
                PartPose.offset(0F, -6F, -3F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(8, 32).addBox(-2.5F, 0F, -2.5F, 5F, 1F, 4F),
                PartPose.offset(0F, 1F, -1F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-2F, -1F, -2F, 3F, 8F, 3F),
                PartPose.offset(-5F, -6F, 0F));
        PartDefinition p_claw_r = p_arm_r.addOrReplaceChild("claw_r", CubeListBuilder.create()
                .texOffs(30, 15).addBox(-2.5F, 0F, -2.5F, 4F, 4F, 4F)
                .texOffs(26, 32).addBox(-2.5F, -0.5F, -2.5F, 4F, 1F, 4F),
                PartPose.offset(-0.5F, 7F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(44, 0).addBox(-1F, -1F, -2F, 3F, 8F, 3F),
                PartPose.offset(5F, -6F, 0F));
        PartDefinition p_claw_l = p_arm_l.addOrReplaceChild("claw_l", CubeListBuilder.create()
                .texOffs(46, 15).addBox(-1.5F, 0F, -2.5F, 4F, 4F, 4F)
                .texOffs(42, 32).addBox(-1.5F, -0.5F, -2.5F, 4F, 1F, 4F),
                PartPose.offset(0.5F, 7F, 0F));
        PartDefinition p_orbit_pebbles = root.addOrReplaceChild("orbit_pebbles", CubeListBuilder.create()
                .texOffs(0, 38).addBox(7F, -1F, -1F, 2F, 2F, 2F)
                .texOffs(8, 38).addBox(-9F, 0F, 0F, 2F, 2F, 2F)
                .texOffs(16, 38).addBox(0F, -2F, 7F, 2F, 2F, 2F),
                PartPose.offset(0F, 12F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Gerado de tools/gen_mob_art.py (minion_air). */
    private static LayerDefinition minionAir() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(24, 25).addBox(-3F, -6F, -2F, 6F, 6F, 4F)
                .texOffs(54, 35).addBox(-2F, -5F, -2.6F, 4F, 4F, 1F),
                PartPose.offsetAndRotation(0F, 12F, 0F, 0.2F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(8, 35).addBox(-2F, 0F, -1.5F, 4F, 4F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail_tip = p_tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(22, 35).addBox(-1F, 0F, -1F, 2F, 5F, 2F),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -7F, -3.5F, 7F, 7F, 7F),
                PartPose.offset(0F, -6F, -1F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-1F, -1F, -3F, 2F, 2F, 3F),
                PartPose.offset(0F, -2F, -3.5F));
        PartDefinition p_sway_crest = p_head.addOrReplaceChild("sway_crest", CubeListBuilder.create()
                .texOffs(44, 25).addBox(-0.5F, -5F, -1F, 1F, 5F, 4F),
                PartPose.offsetAndRotation(0F, -7F, 0F, -0.4F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(10, 44).addBox(0F, -1F, -1F, 4F, 2F, 2F),
                PartPose.offsetAndRotation(3.5F, -5F, 0F, 0F, 0F, -0.3F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(22, 44).addBox(-4F, -1F, -1F, 4F, 2F, 2F),
                PartPose.offsetAndRotation(-3.5F, -5F, 0F, 0F, 0F, 0.3F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(22, 14).addBox(0F, -8F, 0F, 12F, 10F, 0F)
                .texOffs(34, 44).addBox(0F, -8.5F, -0.5F, 12F, 1F, 1F),
                PartPose.offset(2F, -5F, 1.5F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-12F, -8F, 0F, 12F, 10F, 0F)
                .texOffs(0, 49).addBox(-12F, -8.5F, -0.5F, 12F, 1F, 1F),
                PartPose.offset(-2F, -5F, 1.5F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(54, 25).addBox(-1F, -1F, -1F, 2F, 7F, 2F),
                PartPose.offset(-3.5F, -5F, 0F));
        PartDefinition p_claw_r = p_arm_r.addOrReplaceChild("claw_r", CubeListBuilder.create()
                .texOffs(30, 35).addBox(-1.5F, 0F, -1.5F, 3F, 2F, 3F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-1F, -1F, -1F, 2F, 7F, 2F),
                PartPose.offset(3.5F, -5F, 0F));
        PartDefinition p_claw_l = p_arm_l.addOrReplaceChild("claw_l", CubeListBuilder.create()
                .texOffs(42, 35).addBox(-1.5F, 0F, -1.5F, 3F, 2F, 3F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_spin_ring = root.addOrReplaceChild("spin_ring", CubeListBuilder.create()
                .texOffs(26, 49).addBox(-6F, 0F, -6F, 12F, 1F, 1F)
                .texOffs(0, 51).addBox(-6F, 0F, 5F, 12F, 1F, 1F)
                .texOffs(28, 0).addBox(-6F, 0F, -5F, 1F, 1F, 10F)
                .texOffs(0, 14).addBox(5F, 0F, -5F, 1F, 1F, 10F),
                PartPose.offset(0F, 14F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Gerado de tools/gen_mob_art.py (elite_fire). */
    private static LayerDefinition eliteFire() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-2F, 0F, -2F, 4F, 15F, 4F)
                .texOffs(94, 59).addBox(-2.5F, 4F, -2.7F, 5F, 3F, 5F)
                .texOffs(50, 59).addBox(-2.5F, 13F, -3.5F, 5F, 2F, 6F),
                PartPose.offset(-2.5F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-2F, 0F, -2F, 4F, 15F, 4F)
                .texOffs(0, 70).addBox(-2.5F, 4F, -2.7F, 5F, 3F, 5F)
                .texOffs(72, 59).addBox(-2.5F, 13F, -3.5F, 5F, 2F, 6F),
                PartPose.offset(2.5F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(24, 59).addBox(-4F, -4F, -2.5F, 8F, 5F, 5F)
                .texOffs(92, 42).addBox(-5F, -1F, -3F, 10F, 6F, 6F)
                .texOffs(0, 24).addBox(-6F, -15F, -3.5F, 12F, 11F, 7F)
                .texOffs(0, 78).addBox(-2F, -12F, -4F, 4F, 4F, 1F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-4F, -9F, -4F, 8F, 9F, 8F),
                PartPose.offset(0F, -15F, 0F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(48, 70).addBox(0F, -1.5F, -1.5F, 4F, 3F, 3F)
                .texOffs(32, 70).addBox(3F, -6F, -1F, 2F, 5F, 2F),
                PartPose.offset(4F, -7F, 0F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(62, 70).addBox(-4F, -1.5F, -1.5F, 4F, 3F, 3F)
                .texOffs(40, 70).addBox(-5F, -6F, -1F, 2F, 5F, 2F),
                PartPose.offset(-4F, -7F, 0F));
        PartDefinition p_flick_crown = p_head.addOrReplaceChild("flick_crown", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-3F, -5F, -3F, 6F, 5F, 6F),
                PartPose.offset(0F, -9F, 0F));
        PartDefinition p_flick_crown2 = p_flick_crown.addOrReplaceChild("flick_crown2", CubeListBuilder.create()
                .texOffs(20, 70).addBox(-1.5F, -4F, -1.5F, 3F, 4F, 3F),
                PartPose.offset(0F, -5F, 0F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(38, 24).addBox(-2F, -1F, -2F, 4F, 14F, 4F),
                PartPose.offset(-7.5F, -13F, 0F));
        PartDefinition p_pauldron_r = p_arm_r.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(32, 42).addBox(-4F, -4F, -4F, 7F, 6F, 8F)
                .texOffs(100, 70).addBox(-3F, -8F, -1F, 2F, 4F, 2F)
                .texOffs(10, 78).addBox(0F, -7F, -1F, 2F, 3F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(54, 24).addBox(-2F, -1F, -2F, 4F, 14F, 4F),
                PartPose.offset(7.5F, -13F, 0F));
        PartDefinition p_pauldron_l = p_arm_l.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(62, 42).addBox(-3F, -4F, -4F, 7F, 6F, 8F)
                .texOffs(108, 70).addBox(-3F, -8F, -1F, 2F, 4F, 2F)
                .texOffs(18, 78).addBox(0F, -7F, -1F, 2F, 3F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sword = p_arm_r.addOrReplaceChild("sword", CubeListBuilder.create()
                .texOffs(76, 70).addBox(-0.5F, -0.5F, -1F, 1F, 1F, 5F)
                .texOffs(34, 78).addBox(-2F, -1.5F, -2F, 4F, 3F, 1F)
                .texOffs(0, 0).addBox(-0.5F, -3F, -20F, 1F, 6F, 18F)
                .texOffs(70, 24).addBox(-0.5F, -4F, -19F, 1F, 1F, 16F)
                .texOffs(116, 70).addBox(-0.5F, -3F, -22F, 1F, 4F, 2F),
                PartPose.offset(0F, 12F, -1F));
        PartDefinition p_sway_chain = p_arm_l.addOrReplaceChild("sway_chain", CubeListBuilder.create()
                .texOffs(26, 78).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_sway_chain2 = p_sway_chain.addOrReplaceChild("sway_chain2", CubeListBuilder.create()
                .texOffs(30, 78).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_sway_chain3 = p_sway_chain2.addOrReplaceChild("sway_chain3", CubeListBuilder.create()
                .texOffs(88, 70).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-5F, 0F, 0F, 10F, 20F, 1F),
                PartPose.offset(0F, -15F, 3.5F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Gerado de tools/gen_mob_art.py (elite_water). */
    private static LayerDefinition eliteWater() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.5F, -13F, -2.5F, 9F, 13F, 5F)
                .texOffs(0, 49).addBox(-5F, -13F, -3F, 10F, 6F, 6F)
                .texOffs(0, 61).addBox(-4.5F, -1F, -3F, 9F, 2F, 6F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_sway_tail_1 = p_body.addOrReplaceChild("sway_tail_1", CubeListBuilder.create()
                .texOffs(84, 49).addBox(-3.5F, 0F, -2.5F, 7F, 6F, 5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sway_tail_2 = p_sway_tail_1.addOrReplaceChild("sway_tail_2", CubeListBuilder.create()
                .texOffs(108, 49).addBox(-2.5F, 0F, -2F, 5F, 6F, 4F),
                PartPose.offset(0F, 6F, 0.5F));
        PartDefinition p_sway_tail_3 = p_sway_tail_2.addOrReplaceChild("sway_tail_3", CubeListBuilder.create()
                .texOffs(68, 61).addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F),
                PartPose.offset(0F, 6F, 1F));
        PartDefinition p_sway_fluke = p_sway_tail_3.addOrReplaceChild("sway_fluke", CubeListBuilder.create()
                .texOffs(40, 61).addBox(-4F, 0F, -1F, 8F, 1F, 6F),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(52, 31).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F)
                .texOffs(112, 61).addBox(3.5F, -6F, 0F, 3F, 5F, 0F)
                .texOffs(118, 61).addBox(-6.5F, -6F, 0F, 3F, 5F, 0F)
                .texOffs(16, 69).addBox(-3F, -10F, -3F, 1F, 2F, 1F)
                .texOffs(20, 69).addBox(2F, -10F, -3F, 1F, 2F, 1F)
                .texOffs(124, 61).addBox(-0.5F, -11F, -3.5F, 1F, 3F, 1F),
                PartPose.offset(0F, -13F, 0F));
        PartDefinition p_sway_crest = p_head.addOrReplaceChild("sway_crest", CubeListBuilder.create()
                .texOffs(80, 31).addBox(0F, -4F, -3F, 0F, 6F, 9F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(28, 31).addBox(-1.5F, -1F, -1.5F, 3F, 13F, 3F),
                PartPose.offset(-6F, -11F, 0F));
        PartDefinition p_pauldron_r = p_arm_r.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(32, 49).addBox(-3.5F, -3F, -3.5F, 6F, 4F, 7F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(40, 31).addBox(-1.5F, -1F, -1.5F, 3F, 13F, 3F),
                PartPose.offset(6F, -11F, 0F));
        PartDefinition p_pauldron_l = p_arm_l.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(58, 49).addBox(-2.5F, -3F, -3.5F, 6F, 4F, 7F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sword = p_arm_r.addOrReplaceChild("sword", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -0.5F, -24F, 1F, 1F, 30F)
                .texOffs(0, 69).addBox(-3F, -0.5F, -26F, 6F, 1F, 2F)
                .texOffs(92, 61).addBox(-3F, -0.5F, -30F, 1F, 1F, 4F)
                .texOffs(102, 61).addBox(2F, -0.5F, -30F, 1F, 1F, 4F)
                .texOffs(80, 61).addBox(-0.5F, -0.5F, -31F, 1F, 1F, 5F),
                PartPose.offset(0F, 11F, -1F));
        PartDefinition p_shield = p_arm_l.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(62, 0).addBox(0F, -5F, -5F, 1F, 10F, 10F)
                .texOffs(30, 61).addBox(1F, -2F, -2F, 1F, 4F, 4F),
                PartPose.offset(2F, 7F, 0F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(84, 0).addBox(-4F, 0F, 0F, 8F, 18F, 1F),
                PartPose.offset(0F, -12F, 3F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Gerado de tools/gen_mob_art.py (elite_earth). */
    private static LayerDefinition eliteEarth() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(44, 30).addBox(-2.5F, 0F, -2.5F, 5F, 15F, 5F)
                .texOffs(44, 76).addBox(-3F, 13F, -4F, 6F, 2F, 7F)
                .texOffs(96, 76).addBox(-3F, 4F, -3F, 6F, 3F, 6F),
                PartPose.offset(-3F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(64, 30).addBox(-2.5F, 0F, -2.5F, 5F, 15F, 5F)
                .texOffs(70, 76).addBox(-3F, 13F, -4F, 6F, 2F, 7F)
                .texOffs(0, 85).addBox(-3F, 4F, -3F, 6F, 3F, 6F),
                PartPose.offset(3F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(60, 64).addBox(-5F, -4F, -3F, 10F, 5F, 6F)
                .texOffs(0, 64).addBox(-5.5F, -1F, -3.5F, 11F, 5F, 7F)
                .texOffs(0, 30).addBox(-7F, -16F, -4F, 14F, 12F, 8F)
                .texOffs(0, 76).addBox(-7F, -17F, -4F, 14F, 1F, 8F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(64, 50).addBox(-3.5F, -7F, -4F, 7F, 7F, 7F),
                PartPose.offset(0F, -16F, -1F));
        PartDefinition p_antler_l = p_head.addOrReplaceChild("antler_l", CubeListBuilder.create()
                .texOffs(40, 85).addBox(0F, -5F, -0.5F, 1F, 5F, 1F)
                .texOffs(62, 85).addBox(0F, -4F, -0.5F, 3F, 1F, 1F)
                .texOffs(54, 85).addBox(2F, -7F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(3F, -7F, 0F, 0F, 0F, -0.3F));
        PartDefinition p_antler_r = p_head.addOrReplaceChild("antler_r", CubeListBuilder.create()
                .texOffs(44, 85).addBox(-1F, -5F, -0.5F, 1F, 5F, 1F)
                .texOffs(70, 85).addBox(-3F, -4F, -0.5F, 3F, 1F, 1F)
                .texOffs(58, 85).addBox(-3F, -7F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-3F, -7F, 0F, 0F, 0F, 0.3F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(84, 30).addBox(-2.5F, -1F, -2.5F, 5F, 14F, 5F),
                PartPose.offset(-8.5F, -14F, 0F));
        PartDefinition p_pauldron_r = p_arm_r.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-5F, -4F, -4F, 8F, 6F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pulse_shoulder_r = p_arm_r.addOrReplaceChild("pulse_shoulder_r", CubeListBuilder.create()
                .texOffs(24, 85).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(104, 30).addBox(-2.5F, -1F, -2.5F, 5F, 14F, 5F),
                PartPose.offset(8.5F, -14F, 0F));
        PartDefinition p_pauldron_l = p_arm_l.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(32, 50).addBox(-3F, -4F, -4F, 8F, 6F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pulse_shoulder_l = p_arm_l.addOrReplaceChild("pulse_shoulder_l", CubeListBuilder.create()
                .texOffs(32, 85).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_sword = p_arm_r.addOrReplaceChild("sword", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-0.5F, -0.5F, -16F, 1F, 1F, 20F)
                .texOffs(36, 64).addBox(-3F, -3F, -21F, 6F, 6F, 6F)
                .texOffs(48, 85).addBox(-3.2F, -1F, -19F, 1F, 2F, 2F),
                PartPose.offset(0F, 12F, -1F));
        PartDefinition p_shield = p_arm_l.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, -9F, -6F, 2F, 18F, 12F)
                .texOffs(92, 50).addBox(0F, -10F, -6F, 2F, 1F, 12F),
                PartPose.offset(2.5F, 6F, 0F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-6F, 0F, 0F, 12F, 22F, 1F),
                PartPose.offset(0F, -16F, 4F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Gerado de tools/gen_mob_art.py (elite_air). */
    private static LayerDefinition eliteAir() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(50, 0).addBox(-1.5F, 0F, -1.5F, 3F, 15F, 3F)
                .texOffs(90, 39).addBox(-2F, 9F, -2F, 4F, 4F, 4F),
                PartPose.offset(-2F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-1.5F, 0F, -1.5F, 3F, 15F, 3F)
                .texOffs(106, 39).addBox(-2F, 9F, -2F, 4F, 4F, 4F),
                PartPose.offset(2F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(70, 39).addBox(-3F, -4F, -2F, 6F, 5F, 4F)
                .texOffs(20, 39).addBox(-4F, -1F, -2.5F, 8F, 6F, 5F)
                .texOffs(64, 25).addBox(-4.5F, -13F, -2.5F, 9F, 9F, 5F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(92, 25).addBox(-3F, -7F, -3F, 6F, 7F, 6F),
                PartPose.offset(0F, -13F, 0F));
        PartDefinition p_flap_helm_l = p_head.addOrReplaceChild("flap_helm_l", CubeListBuilder.create()
                .texOffs(46, 39).addBox(0F, -6F, -1F, 1F, 6F, 5F),
                PartPose.offsetAndRotation(3F, -5F, 0F, 0F, 0F, -0.4F));
        PartDefinition p_flap_helm_r = p_head.addOrReplaceChild("flap_helm_r", CubeListBuilder.create()
                .texOffs(58, 39).addBox(-1F, -6F, -1F, 1F, 6F, 5F),
                PartPose.offsetAndRotation(-3F, -5F, 0F, 0F, 0F, 0.4F));
        PartDefinition p_flap_wing_l = p_body.addOrReplaceChild("flap_wing_l", CubeListBuilder.create()
                .texOffs(0, 25).addBox(0F, -10F, 0F, 16F, 14F, 0F)
                .texOffs(0, 52).addBox(0F, -10.5F, -0.5F, 16F, 1F, 1F),
                PartPose.offset(2F, -11F, 2.5F));
        PartDefinition p_flap_wing_r = p_body.addOrReplaceChild("flap_wing_r", CubeListBuilder.create()
                .texOffs(32, 25).addBox(-16F, -10F, 0F, 16F, 14F, 0F)
                .texOffs(34, 52).addBox(-16F, -10.5F, -0.5F, 16F, 1F, 1F),
                PartPose.offset(-2F, -11F, 2.5F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(84, 0).addBox(-1F, -1F, -1F, 2F, 13F, 2F),
                PartPose.offset(-5.5F, -12F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(92, 0).addBox(-1F, -1F, -1F, 2F, 13F, 2F),
                PartPose.offset(5.5F, -12F, 0F));
        PartDefinition p_sword = p_arm_r.addOrReplaceChild("sword", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -0.5F, -18F, 1F, 1F, 24F)
                .texOffs(0, 39).addBox(-0.5F, -3F, -27F, 1F, 4F, 9F)
                .texOffs(122, 39).addBox(-0.5F, -0.5F, 6F, 1F, 1F, 2F),
                PartPose.offset(0F, 11F, -1F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(74, 0).addBox(-2F, 0F, 0F, 4F, 16F, 1F),
                PartPose.offset(0F, -13F, 2.5F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** Gerado de tools/gen_mob_art.py (guardian_fire). */
    private static LayerDefinition guardianFire() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -6F, -6F, 12F, 12F, 12F)
                .texOffs(56, 0).addBox(-4F, -4F, -6.5F, 8F, 8F, 1F),
                PartPose.offset(0F, -2F, 0F));
        PartDefinition p_roll_corona = p_body.addOrReplaceChild("roll_corona", CubeListBuilder.create()
                .texOffs(48, 24).addBox(-1F, -12F, -1F, 2F, 6F, 2F)
                .texOffs(56, 24).addBox(-1F, 6F, -1F, 2F, 6F, 2F)
                .texOffs(56, 32).addBox(-12F, -1F, -1F, 6F, 2F, 2F)
                .texOffs(72, 32).addBox(6F, -1F, -1F, 6F, 2F, 2F)
                .texOffs(0, 32).addBox(-10F, -10F, -1F, 3F, 3F, 2F)
                .texOffs(10, 32).addBox(7F, -10F, -1F, 3F, 3F, 2F)
                .texOffs(20, 32).addBox(-10F, 7F, -1F, 3F, 3F, 2F)
                .texOffs(30, 32).addBox(7F, 7F, -1F, 3F, 3F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(74, 0).addBox(-4F, -4F, -1.5F, 8F, 8F, 1F),
                PartPose.offset(0F, 0F, -6F));
        PartDefinition p_halo = p_body.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(88, 32).addBox(-9F, -9F, -0.5F, 18F, 1F, 1F)
                .texOffs(0, 37).addBox(-9F, 8F, -0.5F, 18F, 1F, 1F)
                .texOffs(48, 0).addBox(-9F, -8F, -0.5F, 1F, 16F, 1F)
                .texOffs(52, 0).addBox(8F, -8F, -0.5F, 1F, 16F, 1F),
                PartPose.offset(0F, -2F, 5F));
        PartDefinition p_tendril_1 = p_body.addOrReplaceChild("tendril_1", CubeListBuilder.create()
                .texOffs(64, 24).addBox(-1F, 0F, -1F, 2F, 6F, 2F),
                PartPose.offset(-3F, 6F, -3F));
        PartDefinition p_tendril_1_tip = p_tendril_1.addOrReplaceChild("tendril_1_tip", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_tendril_2 = p_body.addOrReplaceChild("tendril_2", CubeListBuilder.create()
                .texOffs(72, 24).addBox(-1F, 0F, -1F, 2F, 6F, 2F),
                PartPose.offset(3F, 6F, -3F));
        PartDefinition p_tendril_2_tip = p_tendril_2.addOrReplaceChild("tendril_2_tip", CubeListBuilder.create()
                .texOffs(44, 32).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_tendril_3 = p_body.addOrReplaceChild("tendril_3", CubeListBuilder.create()
                .texOffs(80, 24).addBox(-1F, 0F, -1F, 2F, 6F, 2F),
                PartPose.offset(-3F, 6F, 3F));
        PartDefinition p_tendril_3_tip = p_tendril_3.addOrReplaceChild("tendril_3_tip", CubeListBuilder.create()
                .texOffs(48, 32).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_tendril_4 = p_body.addOrReplaceChild("tendril_4", CubeListBuilder.create()
                .texOffs(88, 24).addBox(-1F, 0F, -1F, 2F, 6F, 2F),
                PartPose.offset(3F, 6F, 3F));
        PartDefinition p_tendril_4_tip = p_tendril_4.addOrReplaceChild("tendril_4_tip", CubeListBuilder.create()
                .texOffs(52, 32).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_hand_r = root.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(92, 0).addBox(-2F, -2F, -2F, 4F, 4F, 4F)
                .texOffs(96, 24).addBox(-1.5F, -4F, -1.5F, 3F, 2F, 3F),
                PartPose.offset(-12F, -2F, -3F));
        PartDefinition p_hand_l = root.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(108, 0).addBox(-2F, -2F, -2F, 4F, 4F, 4F)
                .texOffs(108, 24).addBox(-1.5F, -4F, -1.5F, 3F, 2F, 3F),
                PartPose.offset(12F, -2F, -3F));
        PartDefinition p_orb_1 = root.addOrReplaceChild("orb_1", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-2F, -2F, -2F, 4F, 4F, 4F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_2 = root.addOrReplaceChild("orb_2", CubeListBuilder.create()
                .texOffs(16, 24).addBox(-2F, -2F, -2F, 4F, 4F, 4F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_3 = root.addOrReplaceChild("orb_3", CubeListBuilder.create()
                .texOffs(32, 24).addBox(-2F, -2F, -2F, 4F, 4F, 4F),
                PartPose.offset(0F, -6F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** Gerado de tools/gen_mob_art.py (guardian_water). */
    private static LayerDefinition guardianWater() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8F, -6F, -8F, 16F, 6F, 16F)
                .texOffs(0, 41).addBox(-6F, -9F, -6F, 12F, 3F, 12F)
                .texOffs(0, 56).addBox(-3F, -11F, -3F, 6F, 2F, 6F)
                .texOffs(0, 22).addBox(-9F, 0F, -9F, 18F, 1F, 18F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_eye = p_body.addOrReplaceChild("eye", CubeListBuilder.create()
                .texOffs(60, 56).addBox(-2F, -2F, -1F, 4F, 4F, 1F),
                PartPose.offset(0F, -3F, -8F));
        PartDefinition p_pulse_core = p_body.addOrReplaceChild("pulse_core", CubeListBuilder.create()
                .texOffs(48, 41).addBox(-3F, -3F, -3F, 6F, 6F, 6F),
                PartPose.offset(0F, -3F, 0F));
        PartDefinition p_tendril_1 = p_body.addOrReplaceChild("tendril_1", CubeListBuilder.create()
                .texOffs(72, 41).addBox(-1F, 0F, -1F, 2F, 10F, 2F),
                PartPose.offset(-5F, 1F, -5F));
        PartDefinition p_tendril_1_tip = p_tendril_1.addOrReplaceChild("tendril_1_tip", CubeListBuilder.create()
                .texOffs(104, 41).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_tendril_2 = p_body.addOrReplaceChild("tendril_2", CubeListBuilder.create()
                .texOffs(80, 41).addBox(-1F, 0F, -1F, 2F, 10F, 2F),
                PartPose.offset(5F, 1F, -5F));
        PartDefinition p_tendril_2_tip = p_tendril_2.addOrReplaceChild("tendril_2_tip", CubeListBuilder.create()
                .texOffs(108, 41).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_tendril_3 = p_body.addOrReplaceChild("tendril_3", CubeListBuilder.create()
                .texOffs(88, 41).addBox(-1F, 0F, -1F, 2F, 10F, 2F),
                PartPose.offset(-5F, 1F, 5F));
        PartDefinition p_tendril_3_tip = p_tendril_3.addOrReplaceChild("tendril_3_tip", CubeListBuilder.create()
                .texOffs(112, 41).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_tendril_4 = p_body.addOrReplaceChild("tendril_4", CubeListBuilder.create()
                .texOffs(96, 41).addBox(-1F, 0F, -1F, 2F, 10F, 2F),
                PartPose.offset(5F, 1F, 5F));
        PartDefinition p_tendril_4_tip = p_tendril_4.addOrReplaceChild("tendril_4_tip", CubeListBuilder.create()
                .texOffs(116, 41).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_sway_tent_5 = p_body.addOrReplaceChild("sway_tent_5", CubeListBuilder.create()
                .texOffs(72, 22).addBox(-1.5F, 0F, 0F, 3F, 18F, 0F),
                PartPose.offset(-2F, 1F, -2F));
        PartDefinition p_sway_tent_6 = p_body.addOrReplaceChild("sway_tent_6", CubeListBuilder.create()
                .texOffs(78, 22).addBox(-1.5F, 0F, 0F, 3F, 18F, 0F),
                PartPose.offset(2F, 1F, -2F));
        PartDefinition p_sway_tent_7 = p_body.addOrReplaceChild("sway_tent_7", CubeListBuilder.create()
                .texOffs(84, 22).addBox(-1.5F, 0F, 0F, 3F, 18F, 0F),
                PartPose.offset(-2F, 1F, 2F));
        PartDefinition p_sway_tent_8 = p_body.addOrReplaceChild("sway_tent_8", CubeListBuilder.create()
                .texOffs(90, 22).addBox(-1.5F, 0F, 0F, 3F, 18F, 0F),
                PartPose.offset(2F, 1F, 2F));
        PartDefinition p_orb_1 = root.addOrReplaceChild("orb_1", CubeListBuilder.create()
                .texOffs(24, 56).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_2 = root.addOrReplaceChild("orb_2", CubeListBuilder.create()
                .texOffs(36, 56).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_3 = root.addOrReplaceChild("orb_3", CubeListBuilder.create()
                .texOffs(48, 56).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** Gerado de tools/gen_mob_art.py (guardian_earth). */
    private static LayerDefinition guardianEarth() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -10F, -5F, 10F, 20F, 10F)
                .texOffs(40, 0).addBox(-6F, 10F, -6F, 12F, 3F, 12F)
                .texOffs(0, 30).addBox(-6F, -12F, -6F, 12F, 2F, 12F),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(48, 30).addBox(-4F, -4F, -4F, 8F, 4F, 8F)
                .texOffs(60, 44).addBox(-2.5F, -7F, -2.5F, 5F, 3F, 5F)
                .texOffs(118, 44).addBox(-1F, -9F, -1F, 2F, 2F, 2F),
                PartPose.offset(0F, -12F, 0F));
        PartDefinition p_eye = p_body.addOrReplaceChild("eye", CubeListBuilder.create()
                .texOffs(80, 44).addBox(-3F, -3F, -1F, 6F, 6F, 1F),
                PartPose.offset(0F, -4F, -5F));
        PartDefinition p_spin_ring = p_body.addOrReplaceChild("spin_ring", CubeListBuilder.create()
                .texOffs(106, 44).addBox(-9F, -1F, -0.5F, 2F, 4F, 1F)
                .texOffs(112, 44).addBox(7F, -1F, -0.5F, 2F, 4F, 1F)
                .texOffs(94, 44).addBox(-0.5F, -1F, -9F, 1F, 4F, 2F)
                .texOffs(100, 44).addBox(-0.5F, -1F, 7F, 1F, 4F, 2F),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_hand_r = root.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(80, 30).addBox(-3F, -3F, -3F, 6F, 6F, 6F)
                .texOffs(0, 54).addBox(-3F, -4F, -3.5F, 6F, 1F, 1F),
                PartPose.offset(-12F, -2F, -3F));
        PartDefinition p_hand_l = root.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(104, 30).addBox(-3F, -3F, -3F, 6F, 6F, 6F)
                .texOffs(14, 54).addBox(-3F, -4F, -3.5F, 6F, 1F, 1F),
                PartPose.offset(12F, -2F, -3F));
        PartDefinition p_orb_1 = root.addOrReplaceChild("orb_1", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_2 = root.addOrReplaceChild("orb_2", CubeListBuilder.create()
                .texOffs(20, 44).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_3 = root.addOrReplaceChild("orb_3", CubeListBuilder.create()
                .texOffs(40, 44).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F),
                PartPose.offset(0F, -6F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** Gerado de tools/gen_mob_art.py (guardian_air). */
    private static LayerDefinition guardianAir() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-7F, -5F, -6F, 14F, 9F, 12F)
                .texOffs(60, 25).addBox(-10F, -3F, -4F, 5F, 6F, 8F)
                .texOffs(86, 25).addBox(5F, -3F, -4F, 5F, 6F, 8F)
                .texOffs(0, 46).addBox(-5F, -8F, -4F, 10F, 4F, 8F)
                .texOffs(36, 46).addBox(-4F, 4F, -4F, 8F, 3F, 8F),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_eye = p_body.addOrReplaceChild("eye", CubeListBuilder.create()
                .texOffs(36, 58).addBox(-3F, -3F, -1F, 6F, 6F, 1F),
                PartPose.offset(0F, -1F, -6F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(68, 46).addBox(-3F, -4F, -3F, 6F, 4F, 6F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_flick_bolt_1 = p_body.addOrReplaceChild("flick_bolt_1", CubeListBuilder.create()
                .texOffs(86, 58).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F)
                .texOffs(98, 58).addBox(0.5F, 4F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(-4F, 7F, -1F));
        PartDefinition p_flick_bolt_2 = p_body.addOrReplaceChild("flick_bolt_2", CubeListBuilder.create()
                .texOffs(90, 58).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F)
                .texOffs(102, 58).addBox(0.5F, 4F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 7F, -1F));
        PartDefinition p_flick_bolt_3 = p_body.addOrReplaceChild("flick_bolt_3", CubeListBuilder.create()
                .texOffs(94, 58).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F)
                .texOffs(106, 58).addBox(0.5F, 4F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(4F, 7F, -1F));
        PartDefinition p_tendril_1 = p_body.addOrReplaceChild("tendril_1", CubeListBuilder.create()
                .texOffs(20, 58).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offset(-5F, 7F, -3F));
        PartDefinition p_tendril_1_tip = p_tendril_1.addOrReplaceChild("tendril_1_tip", CubeListBuilder.create()
                .texOffs(110, 58).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tendril_2 = p_body.addOrReplaceChild("tendril_2", CubeListBuilder.create()
                .texOffs(24, 58).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offset(5F, 7F, -3F));
        PartDefinition p_tendril_2_tip = p_tendril_2.addOrReplaceChild("tendril_2_tip", CubeListBuilder.create()
                .texOffs(114, 58).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tendril_3 = p_body.addOrReplaceChild("tendril_3", CubeListBuilder.create()
                .texOffs(28, 58).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offset(-5F, 7F, 3F));
        PartDefinition p_tendril_3_tip = p_tendril_3.addOrReplaceChild("tendril_3_tip", CubeListBuilder.create()
                .texOffs(118, 58).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tendril_4 = p_body.addOrReplaceChild("tendril_4", CubeListBuilder.create()
                .texOffs(32, 58).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offset(5F, 7F, 3F));
        PartDefinition p_tendril_4_tip = p_tendril_4.addOrReplaceChild("tendril_4_tip", CubeListBuilder.create()
                .texOffs(122, 58).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_spin_ring_a = root.addOrReplaceChild("spin_ring_a", CubeListBuilder.create()
                .texOffs(0, 68).addBox(-13F, 0F, -13F, 26F, 1F, 1F)
                .texOffs(54, 68).addBox(-13F, 0F, 12F, 26F, 1F, 1F)
                .texOffs(0, 0).addBox(-13F, 0F, -12F, 1F, 1F, 24F)
                .texOffs(50, 0).addBox(12F, 0F, -12F, 1F, 1F, 24F),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_roll_ring_b = root.addOrReplaceChild("roll_ring_b", CubeListBuilder.create()
                .texOffs(0, 70).addBox(-11F, -11F, -0.5F, 22F, 1F, 1F)
                .texOffs(46, 70).addBox(-11F, 10F, -0.5F, 22F, 1F, 1F)
                .texOffs(52, 25).addBox(-11F, -10F, -0.5F, 1F, 20F, 1F)
                .texOffs(56, 25).addBox(10F, -10F, -0.5F, 1F, 20F, 1F),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_hand_r = root.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(92, 46).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F),
                PartPose.offset(-12F, -2F, -3F));
        PartDefinition p_hand_l = root.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F),
                PartPose.offset(12F, -2F, -3F));
        PartDefinition p_orb_1 = root.addOrReplaceChild("orb_1", CubeListBuilder.create()
                .texOffs(50, 58).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_2 = root.addOrReplaceChild("orb_2", CubeListBuilder.create()
                .texOffs(62, 58).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_orb_3 = root.addOrReplaceChild("orb_3", CubeListBuilder.create()
                .texOffs(74, 58).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, -6F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Gerado de tools/gen_mob_art.py (monarch_fire). */
    private static LayerDefinition monarchFire() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(176, 54).addBox(-4.5F, 0F, -4.5F, 9F, 10F, 9F),
                PartPose.offset(-7F, 6F, 0F));
        PartDefinition p_shin_r = p_leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(96, 54).addBox(-5F, 0F, -5F, 10F, 9F, 10F)
                .texOffs(132, 74).addBox(-6F, 6F, -7F, 12F, 3F, 12F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(212, 54).addBox(-4.5F, 0F, -4.5F, 9F, 10F, 9F),
                PartPose.offset(7F, 6F, 0F));
        PartDefinition p_shin_l = p_leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(136, 54).addBox(-5F, 0F, -5F, 10F, 9F, 10F)
                .texOffs(180, 74).addBox(-6F, 6F, -7F, 12F, 3F, 12F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 74).addBox(-10F, -4F, -6F, 20F, 6F, 12F)
                .texOffs(138, 92).addBox(-6F, 2F, -6.5F, 12F, 8F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13F, -12F, -9F, 26F, 12F, 18F)
                .texOffs(0, 30).addBox(-11F, -20F, -8F, 22F, 8F, 16F)
                .texOffs(180, 30).addBox(-9F, -26F, -7F, 18F, 6F, 14F)
                .texOffs(164, 92).addBox(-4F, -16F, -9.5F, 8F, 8F, 1F),
                PartPose.offsetAndRotation(0F, -4F, 0F, 0.2F, 0F, 0F));
        PartDefinition p_flick_plume = p_chest.addOrReplaceChild("flick_plume", CubeListBuilder.create()
                .texOffs(100, 74).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, -26F, 2F));
        PartDefinition p_flick_plume2 = p_flick_plume.addOrReplaceChild("flick_plume2", CubeListBuilder.create()
                .texOffs(54, 92).addBox(-2.5F, -7F, -2.5F, 5F, 7F, 5F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_flick_plume3 = p_flick_plume2.addOrReplaceChild("flick_plume3", CubeListBuilder.create()
                .texOffs(182, 92).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offset(0F, -7F, 0F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(64, 74).addBox(-4.5F, -8F, -5F, 9F, 9F, 9F),
                PartPose.offset(0F, -20F, -8F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(74, 92).addBox(-4F, 0F, -4.5F, 8F, 2F, 8F),
                PartPose.offset(0F, 1F, -1F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(12, 92).addBox(-5F, -2F, -5.5F, 10F, 2F, 11F)
                .texOffs(198, 92).addBox(-4.5F, -6F, -5F, 2F, 4F, 2F)
                .texOffs(190, 92).addBox(-1F, -7F, -5.5F, 2F, 5F, 2F)
                .texOffs(206, 92).addBox(2.5F, -6F, -5F, 2F, 4F, 2F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_shard_1 = p_chest.addOrReplaceChild("shard_1", CubeListBuilder.create()
                .texOffs(228, 74).addBox(-1.5F, -12F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(-7F, -16F, 8F, -0.5F, 0F, -0.35F));
        PartDefinition p_shard_2 = p_chest.addOrReplaceChild("shard_2", CubeListBuilder.create()
                .texOffs(240, 74).addBox(-1.5F, -12F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(0F, -16F, 8F, -0.5F, 0F, 0F));
        PartDefinition p_shard_3 = p_chest.addOrReplaceChild("shard_3", CubeListBuilder.create()
                .texOffs(0, 92).addBox(-1.5F, -12F, -1.5F, 3F, 12F, 3F),
                PartPose.offsetAndRotation(7F, -16F, 8F, -0.5F, 0F, 0.35F));
        PartDefinition p_arm_r = p_chest.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-5F, -2F, -5F, 10F, 16F, 10F)
                .texOffs(0, 54).addBox(-7F, -6F, -6F, 12F, 8F, 12F),
                PartPose.offset(-16F, -18F, 0F));
        PartDefinition p_flick_shoulder_r = p_arm_r.addOrReplaceChild("flick_shoulder_r", CubeListBuilder.create()
                .texOffs(106, 92).addBox(-2F, -6F, -2F, 4F, 6F, 4F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(168, 0).addBox(-5.5F, 0F, -5.5F, 11F, 14F, 11F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_r = p_forearm_r.addOrReplaceChild("fist_r", CubeListBuilder.create()
                .texOffs(76, 30).addBox(-6.5F, 0F, -6.5F, 13F, 9F, 13F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_flick_drip_r = p_fist_r.addOrReplaceChild("flick_drip_r", CubeListBuilder.create()
                .texOffs(214, 92).addBox(-1F, 0F, -1F, 2F, 4F, 2F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_arm_l = p_chest.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(128, 0).addBox(-5F, -2F, -5F, 10F, 16F, 10F)
                .texOffs(48, 54).addBox(-5F, -6F, -6F, 12F, 8F, 12F),
                PartPose.offset(16F, -18F, 0F));
        PartDefinition p_flick_shoulder_l = p_arm_l.addOrReplaceChild("flick_shoulder_l", CubeListBuilder.create()
                .texOffs(122, 92).addBox(-2F, -6F, -2F, 4F, 6F, 4F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(212, 0).addBox(-5.5F, 0F, -5.5F, 11F, 14F, 11F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_l = p_forearm_l.addOrReplaceChild("fist_l", CubeListBuilder.create()
                .texOffs(128, 30).addBox(-6.5F, 0F, -6.5F, 13F, 9F, 13F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_flick_drip_l = p_fist_l.addOrReplaceChild("flick_drip_l", CubeListBuilder.create()
                .texOffs(222, 92).addBox(-1F, 0F, -1F, 2F, 4F, 2F),
                PartPose.offset(0F, 9F, 0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    /** Gerado de tools/gen_mob_art.py (monarch_water). */
    private static LayerDefinition monarchWater() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(118, 62).addBox(-4F, 0F, -4F, 8F, 10F, 8F),
                PartPose.offset(-7F, 6F, 0F));
        PartDefinition p_shin_r = p_leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(46, 62).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F)
                .texOffs(0, 80).addBox(-5.5F, 6F, -8F, 11F, 3F, 13F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(150, 62).addBox(-4F, 0F, -4F, 8F, 10F, 8F),
                PartPose.offset(7F, 6F, 0F));
        PartDefinition p_shin_l = p_leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(82, 62).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F)
                .texOffs(48, 80).addBox(-5.5F, 6F, -8F, 11F, 3F, 13F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(182, 62).addBox(-9F, -4F, -5F, 18F, 6F, 10F)
                .texOffs(146, 96).addBox(-6F, 2F, -5.5F, 12F, 10F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12F, -24F, -8F, 24F, 24F, 16F)
                .texOffs(192, 80).addBox(-8F, -18F, -8.5F, 16F, 14F, 1F)
                .texOffs(180, 96).addBox(-3F, -20F, -9F, 6F, 6F, 1F),
                PartPose.offsetAndRotation(0F, -4F, 0F, 0.2F, 0F, 0F));
        PartDefinition p_shard_1 = p_chest.addOrReplaceChild("shard_1", CubeListBuilder.create()
                .texOffs(96, 80).addBox(-2F, -12F, -2F, 4F, 12F, 4F),
                PartPose.offsetAndRotation(-6F, -18F, 7F, -0.5F, 0F, -0.4F));
        PartDefinition p_shard_2 = p_chest.addOrReplaceChild("shard_2", CubeListBuilder.create()
                .texOffs(112, 80).addBox(-2F, -12F, -2F, 4F, 12F, 4F),
                PartPose.offsetAndRotation(0F, -18F, 7F, -0.5F, 0F, 0F));
        PartDefinition p_shard_3 = p_chest.addOrReplaceChild("shard_3", CubeListBuilder.create()
                .texOffs(128, 80).addBox(-2F, -12F, -2F, 4F, 12F, 4F),
                PartPose.offsetAndRotation(6F, -18F, 7F, -0.5F, 0F, 0.4F));
        PartDefinition p_cape = p_chest.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-10F, 0F, 0F, 20F, 26F, 1F),
                PartPose.offset(0F, -23F, 8F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(190, 0).addBox(-6F, -10F, -7F, 12F, 11F, 11F),
                PartPose.offset(0F, -24F, -6F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(108, 96).addBox(-5F, 0F, -6F, 10F, 2F, 9F),
                PartPose.offset(0F, 1F, -1F));
        PartDefinition p_sway_beard_1 = p_jaw.addOrReplaceChild("sway_beard_1", CubeListBuilder.create()
                .texOffs(76, 96).addBox(-1F, 0F, -1F, 2F, 12F, 2F)
                .texOffs(210, 96).addBox(-1F, 12F, -1F, 2F, 2F, 2F),
                PartPose.offset(-4.5F, 2F, -5F));
        PartDefinition p_sway_beard_2 = p_jaw.addOrReplaceChild("sway_beard_2", CubeListBuilder.create()
                .texOffs(84, 96).addBox(-1F, 0F, -1F, 2F, 12F, 2F)
                .texOffs(218, 96).addBox(-1F, 12F, -1F, 2F, 2F, 2F),
                PartPose.offset(-1.5F, 2F, -5F));
        PartDefinition p_sway_beard_3 = p_jaw.addOrReplaceChild("sway_beard_3", CubeListBuilder.create()
                .texOffs(92, 96).addBox(-1F, 0F, -1F, 2F, 12F, 2F)
                .texOffs(226, 96).addBox(-1F, 12F, -1F, 2F, 2F, 2F),
                PartPose.offset(1.5F, 2F, -5F));
        PartDefinition p_sway_beard_4 = p_jaw.addOrReplaceChild("sway_beard_4", CubeListBuilder.create()
                .texOffs(100, 96).addBox(-1F, 0F, -1F, 2F, 12F, 2F)
                .texOffs(234, 96).addBox(-1F, 12F, -1F, 2F, 2F, 2F),
                PartPose.offset(4.5F, 2F, -5F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(144, 80).addBox(-6F, -2F, -7F, 12F, 3F, 12F)
                .texOffs(194, 96).addBox(-5.5F, -7F, -6.5F, 2F, 5F, 2F)
                .texOffs(172, 96).addBox(-1F, -9F, -7F, 2F, 7F, 2F)
                .texOffs(202, 96).addBox(3.5F, -7F, -6.5F, 2F, 5F, 2F),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_arm_r = p_chest.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(122, 0).addBox(-4F, -2F, -4.5F, 8F, 16F, 9F)
                .texOffs(80, 40).addBox(-6F, -5F, -6F, 11F, 7F, 12F),
                PartPose.offset(-15F, -20F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-5F, 0F, -5F, 10F, 12F, 10F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_r = p_forearm_r.addOrReplaceChild("fist_r", CubeListBuilder.create()
                .texOffs(172, 40).addBox(-5F, 0F, -10F, 10F, 5F, 13F),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_sway_pincer_r = p_fist_r.addOrReplaceChild("sway_pincer_r", CubeListBuilder.create()
                .texOffs(0, 96).addBox(-4F, 0F, -9F, 8F, 3F, 11F),
                PartPose.offset(0F, 5F, 0F));
        PartDefinition p_arm_l = p_chest.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(156, 0).addBox(-4F, -2F, -4.5F, 8F, 16F, 9F)
                .texOffs(126, 40).addBox(-5F, -5F, -6F, 11F, 7F, 12F),
                PartPose.offset(15F, -20F, 0F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(40, 40).addBox(-5F, 0F, -5F, 10F, 12F, 10F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_l = p_forearm_l.addOrReplaceChild("fist_l", CubeListBuilder.create()
                .texOffs(0, 62).addBox(-5F, 0F, -10F, 10F, 5F, 13F),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_sway_pincer_l = p_fist_l.addOrReplaceChild("sway_pincer_l", CubeListBuilder.create()
                .texOffs(38, 96).addBox(-4F, 0F, -9F, 8F, 3F, 11F),
                PartPose.offset(0F, 5F, 0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    /** Gerado de tools/gen_mob_art.py (monarch_earth). */
    private static LayerDefinition monarchEarth() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(188, 65).addBox(-4F, 0F, -4F, 8F, 10F, 8F),
                PartPose.offset(-7F, 6F, 0F));
        PartDefinition p_shin_r = p_leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(116, 65).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F)
                .texOffs(0, 83).addBox(-6F, 6F, -8F, 12F, 3F, 14F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(220, 65).addBox(-4F, 0F, -4F, 8F, 10F, 8F),
                PartPose.offset(7F, 6F, 0F));
        PartDefinition p_shin_l = p_leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(152, 65).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F)
                .texOffs(52, 83).addBox(-6F, 6F, -8F, 12F, 3F, 14F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 65).addBox(-9F, -4F, -6F, 18F, 6F, 12F)
                .texOffs(152, 83).addBox(-9.5F, -4.5F, -6.5F, 19F, 2F, 13F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11F, -24F, -8F, 22F, 24F, 16F)
                .texOffs(106, 40).addBox(-11.5F, -24F, -8.5F, 23F, 4F, 17F)
                .texOffs(198, 100).addBox(-4F, -17F, -9F, 8F, 8F, 1F),
                PartPose.offsetAndRotation(0F, -4F, 0F, 0.15F, 0F, 0F));
        PartDefinition p_shard_1 = p_chest.addOrReplaceChild("shard_1", CubeListBuilder.create()
                .texOffs(104, 83).addBox(-2F, -12F, -2F, 4F, 12F, 4F),
                PartPose.offsetAndRotation(-6F, -18F, 7F, -0.5F, 0F, -0.4F));
        PartDefinition p_shard_2 = p_chest.addOrReplaceChild("shard_2", CubeListBuilder.create()
                .texOffs(120, 83).addBox(-2F, -12F, -2F, 4F, 12F, 4F),
                PartPose.offsetAndRotation(0F, -18F, 7F, -0.5F, 0F, 0F));
        PartDefinition p_shard_3 = p_chest.addOrReplaceChild("shard_3", CubeListBuilder.create()
                .texOffs(136, 83).addBox(-2F, -12F, -2F, 4F, 12F, 4F),
                PartPose.offsetAndRotation(6F, -18F, 7F, -0.5F, 0F, 0.4F));
        PartDefinition p_cape = p_chest.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(152, 0).addBox(-10F, 0F, 0F, 20F, 24F, 1F),
                PartPose.offset(0F, -23F, 8F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(186, 40).addBox(-5F, -9F, -5F, 10F, 10F, 10F),
                PartPose.offset(0F, -24F, -5F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(164, 100).addBox(-4.5F, 0F, -4.5F, 9F, 2F, 8F),
                PartPose.offset(0F, 0F, -1F));
        PartDefinition p_canopy = p_head.addOrReplaceChild("canopy", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-10F, -8F, -9F, 20F, 8F, 18F)
                .texOffs(60, 65).addBox(-7F, -12F, -7F, 14F, 4F, 14F)
                .texOffs(116, 114).addBox(-9F, -3F, -9.5F, 2F, 2F, 1F)
                .texOffs(122, 114).addBox(6F, -5F, -9.5F, 2F, 2F, 1F)
                .texOffs(128, 114).addBox(-2F, -7F, -9.5F, 2F, 2F, 1F),
                PartPose.offset(0F, -9F, 0F));
        PartDefinition p_branch_l = p_head.addOrReplaceChild("branch_l", CubeListBuilder.create()
                .texOffs(40, 114).addBox(0F, -1.5F, -1.5F, 8F, 3F, 3F)
                .texOffs(232, 100).addBox(6F, -7F, -1F, 2F, 6F, 2F),
                PartPose.offsetAndRotation(5F, -6F, -1F, 0F, 0F, -0.4F));
        PartDefinition p_branch_r = p_head.addOrReplaceChild("branch_r", CubeListBuilder.create()
                .texOffs(62, 114).addBox(-8F, -1.5F, -1.5F, 8F, 3F, 3F)
                .texOffs(240, 100).addBox(-8F, -7F, -1F, 2F, 6F, 2F),
                PartPose.offsetAndRotation(-5F, -6F, -1F, 0F, 0F, 0.4F));
        PartDefinition p_arm_r = p_chest.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(194, 0).addBox(-4F, -2F, -4.5F, 8F, 16F, 9F)
                .texOffs(0, 100).addBox(-5F, -3F, -5.5F, 10F, 3F, 11F),
                PartPose.offset(-15F, -20F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(34, 40).addBox(-4.5F, 0F, -4.5F, 9F, 14F, 9F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_r = p_forearm_r.addOrReplaceChild("fist_r", CubeListBuilder.create()
                .texOffs(84, 100).addBox(-5F, 0F, -5F, 10F, 4F, 10F)
                .texOffs(248, 100).addBox(-5F, 4F, -5F, 2F, 6F, 2F)
                .texOffs(216, 100).addBox(-1F, 4F, -5F, 2F, 7F, 2F)
                .texOffs(0, 114).addBox(3F, 4F, -5F, 2F, 6F, 2F)
                .texOffs(24, 114).addBox(-1F, 4F, 3F, 2F, 5F, 2F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_arm_l = p_chest.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-4F, -2F, -4.5F, 8F, 16F, 9F)
                .texOffs(42, 100).addBox(-5F, -3F, -5.5F, 10F, 3F, 11F),
                PartPose.offset(15F, -20F, 0F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(70, 40).addBox(-4.5F, 0F, -4.5F, 9F, 14F, 9F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_l = p_forearm_l.addOrReplaceChild("fist_l", CubeListBuilder.create()
                .texOffs(124, 100).addBox(-5F, 0F, -5F, 10F, 4F, 10F)
                .texOffs(8, 114).addBox(-5F, 4F, -5F, 2F, 6F, 2F)
                .texOffs(224, 100).addBox(-1F, 4F, -5F, 2F, 7F, 2F)
                .texOffs(16, 114).addBox(3F, 4F, -5F, 2F, 6F, 2F)
                .texOffs(32, 114).addBox(-1F, 4F, 3F, 2F, 5F, 2F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_orbit_pollen = root.addOrReplaceChild("orbit_pollen", CubeListBuilder.create()
                .texOffs(84, 114).addBox(17F, 0F, 0F, 2F, 2F, 2F)
                .texOffs(92, 114).addBox(-18F, 4F, 3F, 2F, 2F, 2F)
                .texOffs(100, 114).addBox(2F, -3F, 16F, 2F, 2F, 2F)
                .texOffs(108, 114).addBox(-4F, 2F, -18F, 2F, 2F, 2F),
                PartPose.offset(0F, -20F, 0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    /** Gerado de tools/gen_mob_art.py (monarch_air). */
    private static LayerDefinition monarchAir() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(132, 59).addBox(-3.5F, 0F, -3.5F, 7F, 10F, 7F),
                PartPose.offset(-6F, 6F, 0F));
        PartDefinition p_shin_r = p_leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(68, 59).addBox(-4F, 0F, -4F, 8F, 9F, 8F)
                .texOffs(76, 96).addBox(-4.5F, 6F, -6F, 9F, 3F, 10F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(160, 59).addBox(-3.5F, 0F, -3.5F, 7F, 10F, 7F),
                PartPose.offset(6F, 6F, 0F));
        PartDefinition p_shin_l = p_leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(100, 59).addBox(-4F, 0F, -4F, 8F, 9F, 8F)
                .texOffs(114, 96).addBox(-4.5F, 6F, -6F, 9F, 3F, 10F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_bob_cloud = root.addOrReplaceChild("bob_cloud", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-14F, 0F, -10F, 28F, 3F, 20F),
                PartPose.offset(0F, 21F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(188, 59).addBox(-8F, -4F, -5F, 16F, 6F, 10F)
                .texOffs(180, 96).addBox(-7F, 2F, -5.5F, 14F, 8F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-10F, -22F, -7F, 20F, 22F, 14F)
                .texOffs(0, 96).addBox(-6F, -24F, -6F, 12F, 2F, 12F)
                .texOffs(210, 96).addBox(-3F, -16F, -7.5F, 6F, 6F, 1F),
                PartPose.offsetAndRotation(0F, -4F, 0F, 0.1F, 0F, 0F));
        PartDefinition p_flap_wing_l = p_chest.addOrReplaceChild("flap_wing_l", CubeListBuilder.create()
                .texOffs(96, 36).addBox(0F, -16F, 0F, 28F, 22F, 0F)
                .texOffs(8, 110).addBox(0F, -16.5F, -1F, 28F, 2F, 2F),
                PartPose.offset(6F, -20F, 7F));
        PartDefinition p_flap_wing_r = p_chest.addOrReplaceChild("flap_wing_r", CubeListBuilder.create()
                .texOffs(152, 36).addBox(-28F, -16F, 0F, 28F, 22F, 0F)
                .texOffs(68, 110).addBox(-28F, -16.5F, -1F, 28F, 2F, 2F),
                PartPose.offset(-6F, -20F, 7F));
        PartDefinition p_cape = p_chest.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(68, 0).addBox(-9F, 0F, 0F, 18F, 24F, 1F),
                PartPose.offset(0F, -21F, 7F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(32, 59).addBox(-4.5F, -9F, -5F, 9F, 9F, 9F),
                PartPose.offset(0F, -24F, -4F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(224, 96).addBox(-3F, 0F, -3.5F, 6F, 1F, 5F),
                PartPose.offset(0F, 0F, -1F));
        PartDefinition p_flap_helm_l = p_head.addOrReplaceChild("flap_helm_l", CubeListBuilder.create()
                .texOffs(48, 96).addBox(0F, -8F, -1F, 1F, 8F, 6F),
                PartPose.offsetAndRotation(4.5F, -6F, 0F, 0F, 0F, -0.3F));
        PartDefinition p_flap_helm_r = p_head.addOrReplaceChild("flap_helm_r", CubeListBuilder.create()
                .texOffs(62, 96).addBox(-1F, -8F, -1F, 1F, 8F, 6F),
                PartPose.offsetAndRotation(-4.5F, -6F, 0F, 0F, 0F, 0.3F));
        PartDefinition p_spin_halo = p_head.addOrReplaceChild("spin_halo", CubeListBuilder.create()
                .texOffs(128, 110).addBox(-8F, 0F, -8F, 16F, 1F, 1F)
                .texOffs(162, 110).addBox(-8F, 0F, 7F, 16F, 1F, 1F)
                .texOffs(152, 80).addBox(-8F, 0F, -7F, 1F, 1F, 14F)
                .texOffs(182, 80).addBox(7F, 0F, -7F, 1F, 1F, 14F),
                PartPose.offset(0F, -12F, 0F));
        PartDefinition p_arm_r = p_chest.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-3.5F, -2F, -4F, 7F, 16F, 8F)
                .texOffs(0, 80).addBox(-6F, -5F, -5F, 10F, 6F, 10F)
                .texOffs(152, 96).addBox(-0.5F, -9F, -1F, 1F, 5F, 6F),
                PartPose.offset(-13F, -20F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(208, 36).addBox(-4F, 0F, -4F, 8F, 13F, 8F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_r = p_forearm_r.addOrReplaceChild("fist_r", CubeListBuilder.create()
                .texOffs(80, 80).addBox(-4.5F, 0F, -4.5F, 9F, 7F, 9F)
                .texOffs(246, 96).addBox(-1F, 7F, -1F, 2F, 3F, 2F),
                PartPose.offset(0F, 13F, 0F));
        PartDefinition p_arm_l = p_chest.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(136, 0).addBox(-3.5F, -2F, -4F, 7F, 16F, 8F)
                .texOffs(40, 80).addBox(-4F, -5F, -5F, 10F, 6F, 10F)
                .texOffs(166, 96).addBox(-0.5F, -9F, -1F, 1F, 5F, 6F),
                PartPose.offset(13F, -20F, 0F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-4F, 0F, -4F, 8F, 13F, 8F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_fist_l = p_forearm_l.addOrReplaceChild("fist_l", CubeListBuilder.create()
                .texOffs(116, 80).addBox(-4.5F, 0F, -4.5F, 9F, 7F, 9F)
                .texOffs(0, 110).addBox(-1F, 7F, -1F, 2F, 3F, 2F),
                PartPose.offset(0F, 13F, 0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    /** Gerado de tools/gen_mob_art.py (sovereign). */
    private static LayerDefinition sovereign() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(0, 92).addBox(-4.5F, 0F, -4.5F, 9F, 10F, 9F),
                PartPose.offset(-7F, 6F, 0F));
        PartDefinition p_shin_r = p_leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(188, 70).addBox(-5F, 0F, -5F, 10F, 9F, 10F)
                .texOffs(68, 128).addBox(-6F, 6F, -7F, 12F, 3F, 12F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_flick_lava_r = p_shin_r.addOrReplaceChild("flick_lava_r", CubeListBuilder.create()
                .texOffs(148, 155).addBox(-1.5F, 0F, -1F, 3F, 5F, 1F),
                PartPose.offset(0F, 2F, -5F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(36, 92).addBox(-4.5F, 0F, -4.5F, 9F, 10F, 9F)
                .texOffs(156, 128).addBox(-5F, -1F, -5F, 10F, 3F, 10F),
                PartPose.offset(7F, 6F, 0F));
        PartDefinition p_shin_l = p_leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(136, 92).addBox(-4.5F, 0F, -4.5F, 9F, 9F, 9F)
                .texOffs(172, 92).addBox(-6F, 6F, -8F, 12F, 3F, 14F)
                .texOffs(72, 164).addBox(4F, 7F, -2F, 4F, 2F, 2F),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(72, 92).addBox(-10F, -4F, -6F, 20F, 6F, 12F)
                .texOffs(0, 128).addBox(-10.5F, -4.5F, -6.5F, 21F, 2F, 13F)
                .texOffs(64, 143).addBox(-7F, 2F, -6.5F, 14F, 10F, 1F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13F, -26F, -9F, 26F, 26F, 18F)
                .texOffs(162, 143).addBox(-9F, -12F, -9.5F, 18F, 8F, 1F)
                .texOffs(228, 143).addBox(-4F, -22F, -10F, 8F, 8F, 1F)
                .texOffs(84, 164).addBox(-9F, -24F, -9.5F, 3F, 3F, 1F)
                .texOffs(92, 164).addBox(6F, -24F, -9.5F, 3F, 3F, 1F)
                .texOffs(100, 164).addBox(-9F, -15F, -9.5F, 3F, 3F, 1F)
                .texOffs(108, 164).addBox(6F, -15F, -9.5F, 3F, 3F, 1F)
                .texOffs(94, 143).addBox(-13F, -30F, -1F, 7F, 4F, 7F),
                PartPose.offsetAndRotation(0F, -4F, 0F, 0.15F, 0F, 0F));
        PartDefinition p_flick_plume = p_chest.addOrReplaceChild("flick_plume", CubeListBuilder.create()
                .texOffs(0, 143).addBox(-3F, -6F, -3F, 6F, 6F, 6F),
                PartPose.offset(-9.5F, -30F, 2.5F));
        PartDefinition p_flick_plume2 = p_flick_plume.addOrReplaceChild("flick_plume2", CubeListBuilder.create()
                .texOffs(0, 155).addBox(-2F, -5F, -2F, 4F, 5F, 4F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_flick_plume3 = p_flick_plume2.addOrReplaceChild("flick_plume3", CubeListBuilder.create()
                .texOffs(156, 155).addBox(-1F, -4F, -1F, 2F, 4F, 2F),
                PartPose.offset(0F, -5F, 0F));
        PartDefinition p_canopy = p_chest.addOrReplaceChild("canopy", CubeListBuilder.create()
                .texOffs(0, 111).addBox(-6F, -7F, -5F, 12F, 7F, 10F)
                .texOffs(200, 143).addBox(-4F, -10F, -3F, 8F, 3F, 6F)
                .texOffs(148, 164).addBox(-5F, -4F, -5.5F, 2F, 2F, 1F)
                .texOffs(154, 164).addBox(3F, -6F, -5.5F, 2F, 2F, 1F)
                .texOffs(164, 155).addBox(-1F, 0F, -1F, 2F, 4F, 2F),
                PartPose.offset(9F, -26F, 2F));
        PartDefinition p_flap_wing_l = p_chest.addOrReplaceChild("flap_wing_l", CubeListBuilder.create()
                .texOffs(174, 0).addBox(0F, -20F, 0F, 34F, 26F, 0F)
                .texOffs(180, 155).addBox(0F, -20.5F, -1F, 34F, 2F, 2F),
                PartPose.offset(7F, -22F, 9F));
        PartDefinition p_flap_wing_r = p_chest.addOrReplaceChild("flap_wing_r", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-34F, -20F, 0F, 34F, 26F, 0F)
                .texOffs(0, 164).addBox(-34F, -20.5F, -1F, 34F, 2F, 2F),
                PartPose.offset(-7F, -22F, 9F));
        PartDefinition p_cape = p_chest.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-11F, 0F, 0F, 22F, 28F, 1F),
                PartPose.offset(0F, -25F, 9F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 70).addBox(-5.5F, -10F, -6F, 11F, 11F, 11F),
                PartPose.offset(0F, -26F, -6F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(196, 128).addBox(-5F, 0F, -5.5F, 10F, 3F, 9F),
                PartPose.offset(0F, 1F, -1F));
        PartDefinition p_sway_beard_1 = p_jaw.addOrReplaceChild("sway_beard_1", CubeListBuilder.create()
                .texOffs(24, 143).addBox(-1F, 0F, -1F, 2F, 10F, 2F)
                .texOffs(116, 164).addBox(-1F, 10F, -1F, 2F, 2F, 2F),
                PartPose.offset(-4F, 3F, -4.5F));
        PartDefinition p_sway_beard_2 = p_jaw.addOrReplaceChild("sway_beard_2", CubeListBuilder.create()
                .texOffs(32, 143).addBox(-1F, 0F, -1F, 2F, 10F, 2F)
                .texOffs(124, 164).addBox(-1F, 10F, -1F, 2F, 2F, 2F),
                PartPose.offset(-1.5F, 3F, -4.5F));
        PartDefinition p_sway_beard_3 = p_jaw.addOrReplaceChild("sway_beard_3", CubeListBuilder.create()
                .texOffs(40, 143).addBox(-1F, 0F, -1F, 2F, 10F, 2F)
                .texOffs(132, 164).addBox(-1F, 10F, -1F, 2F, 2F, 2F),
                PartPose.offset(1.5F, 3F, -4.5F));
        PartDefinition p_sway_beard_4 = p_jaw.addOrReplaceChild("sway_beard_4", CubeListBuilder.create()
                .texOffs(48, 143).addBox(-1F, 0F, -1F, 2F, 10F, 2F)
                .texOffs(140, 164).addBox(-1F, 10F, -1F, 2F, 2F, 2F),
                PartPose.offset(4F, 3F, -4.5F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(112, 111).addBox(-6.5F, -2F, -7F, 13F, 3F, 13F)
                .texOffs(64, 155).addBox(-6F, -8F, -6.5F, 2F, 6F, 2F)
                .texOffs(146, 143).addBox(-3.5F, -10F, -7F, 2F, 8F, 2F)
                .texOffs(56, 143).addBox(-1F, -12F, -7F, 2F, 10F, 2F)
                .texOffs(154, 143).addBox(1.5F, -10F, -7F, 2F, 8F, 2F)
                .texOffs(72, 155).addBox(4F, -8F, -6.5F, 2F, 6F, 2F),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_spin_halo = p_head.addOrReplaceChild("spin_halo", CubeListBuilder.create()
                .texOffs(160, 164).addBox(-9F, 0F, -9F, 18F, 1F, 1F)
                .texOffs(198, 164).addBox(-9F, 0F, 8F, 18F, 1F, 1F)
                .texOffs(44, 111).addBox(-9F, 0F, -8F, 1F, 1F, 16F)
                .texOffs(78, 111).addBox(8F, 0F, -8F, 1F, 1F, 16F),
                PartPose.offset(0F, -17F, 0F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(16, 155).addBox(-8F, -2F, -2F, 8F, 4F, 4F),
                PartPose.offsetAndRotation(-5.5F, -7F, -1F, 0F, 0F, -0.4F));
        PartDefinition p_horn_r2 = p_horn_r.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(122, 143).addBox(-1.5F, -8F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(-8F, 0F, 0F, 0F, 0F, 0.9F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(40, 155).addBox(0F, -2F, -2F, 8F, 4F, 4F),
                PartPose.offsetAndRotation(5.5F, -7F, -1F, 0F, 0F, 0.4F));
        PartDefinition p_horn_l2 = p_horn_l.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(134, 143).addBox(-1.5F, -8F, -1.5F, 3F, 8F, 3F),
                PartPose.offsetAndRotation(8F, 0F, 0F, 0F, 0F, -0.9F));
        PartDefinition p_arm_r = p_chest.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(134, 0).addBox(-5F, -2F, -5F, 10F, 17F, 10F),
                PartPose.offset(-16F, -22F, 0F));
        PartDefinition p_pauldron_r = p_arm_r.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(44, 70).addBox(-7F, -5F, -6F, 12F, 8F, 12F)
                .texOffs(80, 155).addBox(-4F, -10F, -1F, 2F, 5F, 2F)
                .texOffs(172, 155).addBox(0F, -9F, -1F, 2F, 4F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(104, 44).addBox(-5.5F, 0F, -5.5F, 11F, 14F, 11F)
                .texOffs(164, 111).addBox(-6F, 2F, -6F, 12F, 4F, 12F),
                PartPose.offset(0F, 15F, 0F));
        PartDefinition p_fist_r = p_forearm_r.addOrReplaceChild("fist_r", CubeListBuilder.create()
                .texOffs(148, 44).addBox(-6.5F, 0F, -6.5F, 13F, 10F, 13F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_arm_l = p_chest.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(68, 44).addBox(-4.5F, -2F, -4.5F, 9F, 17F, 9F),
                PartPose.offset(16F, -22F, 0F));
        PartDefinition p_pauldron_l = p_arm_l.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(92, 70).addBox(-5F, -5F, -6F, 12F, 7F, 12F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(200, 44).addBox(-5F, 0F, -5F, 10F, 13F, 10F),
                PartPose.offset(0F, 15F, 0F));
        PartDefinition p_fist_l = p_forearm_l.addOrReplaceChild("fist_l", CubeListBuilder.create()
                .texOffs(140, 70).addBox(-5F, 0F, -11F, 10F, 5F, 14F),
                PartPose.offset(0F, 13F, 0F));
        PartDefinition p_sway_pincer_l = p_fist_l.addOrReplaceChild("sway_pincer_l", CubeListBuilder.create()
                .texOffs(116, 128).addBox(-4F, 0F, -10F, 8F, 3F, 12F),
                PartPose.offset(0F, 5F, 0F));
        PartDefinition p_orbit_shards = root.addOrReplaceChild("orbit_shards", CubeListBuilder.create()
                .texOffs(88, 155).addBox(22F, 0F, 0F, 3F, 3F, 3F)
                .texOffs(100, 155).addBox(-25F, 4F, 4F, 3F, 3F, 3F)
                .texOffs(112, 155).addBox(4F, -3F, 22F, 3F, 3F, 3F)
                .texOffs(124, 155).addBox(-6F, 2F, -25F, 3F, 3F, 3F)
                .texOffs(136, 155).addBox(15F, 6F, -18F, 3F, 3F, 3F),
                PartPose.offset(0F, -12F, 0F));
        return LayerDefinition.create(mesh, 256, 256);
    }
}
