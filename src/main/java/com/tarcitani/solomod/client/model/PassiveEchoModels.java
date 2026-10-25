package com.tarcitani.solomod.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Malhas das 15 formas da Fauna Pacífica, em texturas de 64x64. A origem fica no chão (y = 24 unidades); os voadores
 * usam origem mais alta, mas o eixo é o mesmo. Feitas em código, com vários cubos por forma (dentes, escamas, asas
 * dobradas, penacho). Nomes de partes animadas, usados por {@link PassiveEchoModel}: "head", "wing_l", "wing_r",
 * "leg_fl", "leg_fr", "leg_bl", "leg_br". Não há arquivo do Blockbench.
 */
public final class PassiveEchoModels {
    private PassiveEchoModels() {}

    private static final int TEX = 64;
    private static final float GROUND_Y = 24.0F;
    private static final float FLYER_Y = 20.0F;

    /** Batedor Espectral: ave de névoa esguia, máscara de marfim com fenda, asas em leque e fumaça no lugar das patas. */
    public static LayerDefinition spectralScout() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, GROUND_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2, -14, -2, 4, 8, 4)
                .texOffs(52, 0).addBox(-2.5F, -13, -2.5F, 5, 5, 1)
                .texOffs(15, 12).addBox(-2.6F, -10, -1.5F, 5, 1, 3), PartPose.ZERO);
        root.addOrReplaceChild("wisp_1", CubeListBuilder.create().texOffs(0, 12).addBox(-1.5F, -6, -1.5F, 3, 3, 3), PartPose.ZERO);
        root.addOrReplaceChild("wisp_2", CubeListBuilder.create().texOffs(0, 18).addBox(-1, -3, -1, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("wisp_3", CubeListBuilder.create().texOffs(48, 18).addBox(-0.5F, -1, -0.5F, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-2, -18, -2, 4, 4, 4)
                .texOffs(32, 0).addBox(-2.5F, -18.5F, -2.5F, 5, 1, 5)
                .texOffs(12, 12).addBox(-0.5F, -17, -2.3F, 1, 4, 0.4F)
                .texOffs(24, 18).addBox(-0.5F, -21, -0.5F, 1, 3, 1)
                .texOffs(28, 18).addBox(-1.5F, -15, -2.4F, 3, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(31, 12).addBox(0, -1, -1, 6, 2, 2)
                .texOffs(8, 18).addBox(5, -1.5F, -0.5F, 3, 3, 1)
                .texOffs(36, 18).addBox(7, -1, 0, 2, 1, 1), PartPose.offset(2, -13, 0));
        root.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(47, 12).addBox(-6, -1, -1, 6, 2, 2)
                .texOffs(16, 18).addBox(-8, -1.5F, -0.5F, 3, 3, 1)
                .texOffs(42, 18).addBox(-9, -1, 0, 2, 1, 1), PartPose.offset(-2, -13, 0));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Morcego Eco-Rúnico: quiróptero com orelhas tripartidas, olho rúnico no peito e asas com três dedos cada. */
    public static LayerDefinition echoRunicBat() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, FLYER_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-1.5F, -2, -2, 3, 3, 4)
                .texOffs(0, 12).addBox(-0.5F, -1.5F, -2.3F, 1, 1, 0.4F)
                .texOffs(0, 7).addBox(-1, 1, -2, 2, 2, 3), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(10, 7).addBox(-1, -4, -1, 2, 2, 2)
                .texOffs(34, 7).addBox(-1.5F, -6, -0.5F, 1, 2, 1)
                .texOffs(38, 7).addBox(0.5F, -6, -0.5F, 1, 2, 1)
                .texOffs(58, 7).addBox(-0.5F, -6.5F, -0.5F, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0, 0, -3, 6, 1, 6)
                .texOffs(18, 7).addBox(5, 0, -4, 1, 1, 3)
                .texOffs(42, 7).addBox(2, 0, 3, 3, 1, 1), PartPose.offset(1.5F, -1, 0));
        root.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-6, 0, -3, 6, 1, 6)
                .texOffs(26, 7).addBox(-6, 0, -4, 1, 1, 3)
                .texOffs(50, 7).addBox(-5, 0, 3, 3, 1, 1), PartPose.offset(-1.5F, -1, 0));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Autômato Relíquia: construto quadrúpede de pedra, placa de cobre no dorso, engrenagens e cabeça com olho. */
    public static LayerDefinition relicAutomaton() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, GROUND_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6, -10, -5, 12, 6, 10)
                .texOffs(22, 16).addBox(-4, -11, -4, 8, 1, 8)
                .texOffs(6, 38).addBox(-5, -11.5F, -1.5F, 2, 1, 3)
                .texOffs(16, 38).addBox(3, -11.5F, -1.5F, 2, 1, 3)
                .texOffs(58, 33).addBox(-6.5F, -8, -3, 1, 3, 2)
                .texOffs(0, 38).addBox(5.5F, -8, -3, 1, 3, 2), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3, -16, -7, 6, 5, 5)
                .texOffs(26, 38).addBox(-1, -14, -7.5F, 2, 1, 0.5F)
                .texOffs(31, 38).addBox(-2, -11, -7.5F, 1, 1, 1)
                .texOffs(35, 38).addBox(1, -11, -7.5F, 1, 1, 1)
                .texOffs(48, 33).addBox(-1, -18, -5, 2, 2, 3), PartPose.ZERO);
        root.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-1.5F, 0, -1.5F, 3, 4, 3)
                .texOffs(48, 26).addBox(-2, 3, -2, 4, 1, 4), PartPose.offset(-4, -4, -4));
        root.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(12, 26).addBox(-1.5F, 0, -1.5F, 3, 4, 3)
                .texOffs(0, 33).addBox(-2, 3, -2, 4, 1, 4), PartPose.offset(4, -4, -4));
        root.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(24, 26).addBox(-1.5F, 0, -1.5F, 3, 4, 3)
                .texOffs(16, 33).addBox(-2, 3, -2, 4, 1, 4), PartPose.offset(-4, -4, 4));
        root.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(36, 26).addBox(-1.5F, 0, -1.5F, 3, 4, 3)
                .texOffs(32, 33).addBox(-2, 3, -2, 4, 1, 4), PartPose.offset(4, -4, 4));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Salamandra de Núcleo Ígneo: réptil rasteiro, escamas em fileira no dorso, cauda afilada e coroa de brasas. */
    public static LayerDefinition igneousSalamander() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, GROUND_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3, -4, -6, 6, 4, 12)
                .texOffs(0, 16).addBox(-2, -5, -3, 4, 1, 6)
                .texOffs(20, 28).addBox(-1, -5.5F, -5, 2, 1, 1)
                .texOffs(26, 28).addBox(-1, -5.5F, -1, 2, 1, 1)
                .texOffs(32, 28).addBox(-1, -5.5F, 3, 2, 1, 1)
                .texOffs(20, 16).addBox(-1, -4, 6, 2, 3, 4)
                .texOffs(0, 23).addBox(-0.5F, -3, 10, 1, 2, 3), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(36, 0).addBox(-2.5F, -5, -10, 5, 4, 4)
                .texOffs(8, 23).addBox(-2.5F, -3, -12, 5, 2, 2)
                .texOffs(38, 28).addBox(-1, -6, -8, 2, 1, 1)
                .texOffs(12, 28).addBox(-1, -7, -7, 2, 2, 2)
                .texOffs(44, 28).addBox(-2, -1.5F, -11, 1, 1, 1)
                .texOffs(48, 28).addBox(1, -1.5F, -11, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(32, 16).addBox(-1, 0, -1, 2, 4, 2)
                .texOffs(22, 23).addBox(-1.5F, 3, -2, 3, 1, 3), PartPose.offset(-3, -4, -4));
        root.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-1, 0, -1, 2, 4, 2)
                .texOffs(34, 23).addBox(-1.5F, 3, -2, 3, 1, 3), PartPose.offset(3, -4, -4));
        root.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(48, 16).addBox(-1, 0, -1, 2, 4, 2)
                .texOffs(46, 23).addBox(-1.5F, 3, -2, 3, 1, 3), PartPose.offset(-3, -4, 4));
        root.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(56, 16).addBox(-1, 0, -1, 2, 4, 2)
                .texOffs(0, 28).addBox(-1.5F, 3, -2, 3, 1, 3), PartPose.offset(3, -4, 4));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Escaravelho de Sílica Dourada: cúpula de vidro, élitros que se abrem e patas com articulações. */
    public static LayerDefinition goldenSilicaScarab() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, 20, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4, -6, -5, 8, 6, 10)
                .texOffs(0, 36).addBox(-3, -7, -3, 6, 1, 6)
                .texOffs(36, 0).addBox(-4.5F, -4, -4, 1, 3, 8)
                .texOffs(0, 16).addBox(3.5F, -4, -4, 1, 3, 8), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(24, 36).addBox(-2, -5, -8, 4, 3, 3)
                .texOffs(40, 43).addBox(-2.5F, -4, -8.5F, 1, 1, 1)
                .texOffs(44, 43).addBox(1.5F, -4, -8.5F, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(18, 16).addBox(0, 0, -4, 4, 1, 8)
                .texOffs(24, 27).addBox(3, 0, -4, 1, 0.5F, 8), PartPose.offset(0, -6, 0));
        root.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-4, 0, -4, 4, 1, 8)
                .texOffs(42, 27).addBox(-4, 0, -4, 1, 0.5F, 8), PartPose.offset(0, -6, 0));
        root.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(38, 36).addBox(-1, 0, -1, 2, 3, 2)
                .texOffs(8, 43).addBox(-1.5F, 2, -2, 3, 1, 1), PartPose.offset(-4, -2, -3));
        root.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(46, 36).addBox(-1, 0, -1, 2, 3, 2)
                .texOffs(16, 43).addBox(-1.5F, 2, -2, 3, 1, 1), PartPose.offset(4, -2, -3));
        root.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(54, 36).addBox(-1, 0, -1, 2, 3, 2)
                .texOffs(24, 43).addBox(-1.5F, 2, -2, 3, 1, 1), PartPose.offset(-4, -2, 3));
        root.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(0, 43).addBox(-1, 0, -1, 2, 3, 2)
                .texOffs(32, 43).addBox(-1.5F, 2, -2, 3, 1, 1), PartPose.offset(4, -2, 3));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Poldro de Cinzas Vulcânicas: wyvern pequeno, asas membranosas dobradas em três dedos e cauda com ponta de brasa. */
    public static LayerDefinition volcanicAshColt() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, FLYER_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2, -6, -4, 4, 5, 7)
                .texOffs(38, 12).addBox(-1.5F, -1, 3, 3, 3, 3)
                .texOffs(0, 19).addBox(-1, -7, 3, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(24, 12).addBox(-1.5F, -9, -7, 3, 3, 4)
                .texOffs(20, 23).addBox(-1, -10, -3, 2, 1, 2)
                .texOffs(44, 23).addBox(-1.5F, -8, -8, 1, 1, 1)
                .texOffs(48, 23).addBox(0.5F, -8, -8, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(38, 0).addBox(0, 0, -3, 6, 1, 6)
                .texOffs(28, 23).addBox(5, 0, -3, 2, 1, 2)
                .texOffs(40, 19).addBox(3, 0, 3, 3, 1, 2), PartPose.offset(2, -5, 0));
        root.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-6, 0, -3, 6, 1, 6)
                .texOffs(36, 23).addBox(-7, 0, -3, 2, 1, 2)
                .texOffs(50, 19).addBox(-6, 0, 3, 3, 1, 2), PartPose.offset(-2, -5, 0));
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(22, 0).addBox(-1, -2, 3, 2, 2, 6)
                .texOffs(50, 12).addBox(-1.5F, -2.5F, 9, 3, 3, 3), PartPose.ZERO);
        root.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(8, 19).addBox(-1, 0, -1, 2, 2, 2)
                .texOffs(0, 23).addBox(-1.5F, 2, -2, 3, 1, 2), PartPose.offset(-1.5F, -1, -2));
        root.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(16, 19).addBox(-1, 0, -1, 2, 2, 2)
                .texOffs(10, 23).addBox(-1.5F, 2, -2, 3, 1, 2), PartPose.offset(1.5F, -1, -2));
        root.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(24, 19).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(-1.5F, -1, 2));
        root.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(32, 19).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(1.5F, -1, 2));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Axolote de Mana Líquida: corpo translúcido, brânquias em fita dos dois lados e cauda longa. */
    public static LayerDefinition liquidManaAxolotl() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, FLYER_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(26, 0).addBox(-3, -2, -5, 6, 4, 6)
                .texOffs(30, 10).addBox(-2, 2, -4, 4, 1, 4), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4, -3, -9, 8, 5, 5)
                .texOffs(36, 20).addBox(-1, -4, -7, 2, 1, 2)
                .texOffs(44, 20).addBox(-1.5F, -2.5F, -9.5F, 3, 2, 1), PartPose.ZERO);
        root.addOrReplaceChild("gill_l", CubeListBuilder.create()
                .texOffs(18, 10).addBox(-1, -3, -1, 1, 5, 2)
                .texOffs(24, 20).addBox(-2, -4, -1, 1, 2, 2), PartPose.offset(-3, -1, -7));
        root.addOrReplaceChild("gill_r", CubeListBuilder.create()
                .texOffs(24, 10).addBox(0, -3, -1, 1, 5, 2)
                .texOffs(30, 20).addBox(1, -4, -1, 1, 2, 2), PartPose.offset(3, -1, -7));
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 10).addBox(-1, -1, 1, 2, 3, 7)
                .texOffs(46, 10).addBox(-0.5F, -2, 8, 1, 2, 3), PartPose.ZERO);
        root.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(54, 10).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(-3, -2, -3));
        root.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(3, -2, -3));
        root.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(8, 20).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(-3, -2, 3));
        root.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(16, 20).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(3, -2, 3));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Caranguejo-Tartaruga de Lótus: casco largo com placas, pinças com dentes, olhos em haste e flor nas costas. */
    public static LayerDefinition lotusTurtleCrab() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, GROUND_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-6, -2, -4, 12, 2, 8)
                .texOffs(0, 23).addBox(-5, 0, -3, 10, 1, 6), PartPose.ZERO);
        root.addOrReplaceChild("shell", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7, -5, -5, 14, 3, 10)
                .texOffs(32, 23).addBox(-5, -6, -3, 10, 1, 6)
                .texOffs(16, 36).addBox(-4, -6.5F, -1, 2, 1, 2)
                .texOffs(24, 36).addBox(2, -6.5F, -1, 2, 1, 2), PartPose.ZERO);
        root.addOrReplaceChild("lotus", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-2, -7, -2, 4, 2, 4)
                .texOffs(32, 36).addBox(-3, -6.5F, -3, 2, 1, 2)
                .texOffs(40, 36).addBox(1, -6.5F, -3, 2, 1, 2)
                .texOffs(48, 36).addBox(-1, -8, -1, 2, 1, 2), PartPose.ZERO);
        root.addOrReplaceChild("claw_l", CubeListBuilder.create()
                .texOffs(16, 30).addBox(-3, -1, -2, 3, 2, 4)
                .texOffs(20, 40).addBox(-4, -0.5F, -2, 1, 1, 1)
                .texOffs(24, 40).addBox(-4, -0.5F, 1, 1, 1, 1)
                .texOffs(0, 40).addBox(-7, -1, -2, 4, 1, 1), PartPose.offset(-7, -3, -3));
        root.addOrReplaceChild("claw_r", CubeListBuilder.create()
                .texOffs(30, 30).addBox(0, -1, -2, 3, 2, 4)
                .texOffs(28, 40).addBox(3, -0.5F, -2, 1, 1, 1)
                .texOffs(32, 40).addBox(3, -0.5F, 1, 1, 1, 1)
                .texOffs(10, 40).addBox(3, -1, -2, 4, 1, 1), PartPose.offset(7, -3, -3));
        root.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(44, 30).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(-5, -2, -3));
        root.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(52, 30).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(5, -2, -3));
        root.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(-5, -2, 3));
        root.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(8, 36).addBox(-1, 0, -1, 2, 2, 2), PartPose.offset(5, -2, 3));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Rêmora de Fossa Abissal: corpo hidrodinâmico com escamas, nadadeiras dorsais dobráveis, antena e ventosa. */
    public static LayerDefinition abyssalTrenchRemora() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, FLYER_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -2, -4, 3, 4, 8)
                .texOffs(22, 0).addBox(-0.5F, 2, -3, 1, 1, 5)
                .texOffs(24, 12).addBox(-1, -2.5F, 1, 2, 1, 2), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-1, -2, -6, 2, 3, 2)
                .texOffs(48, 12).addBox(-1.5F, -1, -6.5F, 3, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("antenna", CubeListBuilder.create()
                .texOffs(20, 12).addBox(-0.5F, -5, -1, 1, 3, 1)
                .texOffs(56, 12).addBox(-1, -6, -1, 2, 1, 1), PartPose.offset(0, -2, -4));
        root.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(34, 0).addBox(0, 0, -2, 3, 1, 4)
                .texOffs(32, 12).addBox(2, 0, 2, 2, 1, 2), PartPose.offset(1.5F, 0, 0));
        root.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-3, 0, -2, 3, 1, 4)
                .texOffs(40, 12).addBox(-4, 0, 2, 2, 1, 2), PartPose.offset(-1.5F, 0, 0));
        root.addOrReplaceChild("sucker", CubeListBuilder.create()
                .texOffs(8, 12).addBox(-1.5F, 2, -2, 3, 1, 3), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Mini-Tank Silvestre: urso robusto, dorso com placas de granito, focinho, chifres de madeira e garras. */
    public static LayerDefinition sylvanMiniTank() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, GROUND_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5, -8, -6, 10, 8, 12)
                .texOffs(0, 20).addBox(-4, -9, -5, 8, 1, 10)
                .texOffs(56, 38).addBox(-3, -10, -4, 2, 1, 2)
                .texOffs(0, 43).addBox(1, -10, -4, 2, 1, 2)
                .texOffs(8, 43).addBox(-1, -10, 1, 2, 1, 2), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(36, 20).addBox(-3, -13, -10, 6, 6, 5)
                .texOffs(16, 43).addBox(-1.5F, -9, -11, 3, 2, 1)
                .texOffs(48, 38).addBox(-4, -16, -8, 1, 3, 1)
                .texOffs(52, 38).addBox(3, -16, -8, 1, 3, 1)
                .texOffs(24, 43).addBox(-1, -8, -11, 2, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-1.5F, 0, -1.5F, 3, 4, 3)
                .texOffs(48, 31).addBox(-2, 3, -2.5F, 4, 1, 4), PartPose.offset(-3.5F, -4, -4));
        root.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(12, 31).addBox(-1.5F, 0, -1.5F, 3, 4, 3)
                .texOffs(0, 38).addBox(-2, 3, -2.5F, 4, 1, 4), PartPose.offset(3.5F, -4, -4));
        root.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(24, 31).addBox(-1.5F, 0, -1.5F, 3, 4, 3)
                .texOffs(16, 38).addBox(-2, 3, -2.5F, 4, 1, 4), PartPose.offset(-3.5F, -4, 4));
        root.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(36, 31).addBox(-1.5F, 0, -1.5F, 3, 4, 3)
                .texOffs(32, 38).addBox(-2, 3, -2.5F, 4, 1, 4), PartPose.offset(3.5F, -4, 4));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Golem Pedregulho Rúnico: esfera de pedra com facetas, olhos âmbar e seixos orbitando a cintura. */
    public static LayerDefinition runicBoulderGolem() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, GROUND_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
                .texOffs(32, 0).addBox(-4.5F, -7, -3, 1, 6, 6)
                .texOffs(46, 0).addBox(3.5F, -7, -3, 1, 6, 6)
                .texOffs(0, 16).addBox(-2, -9, -2, 4, 1, 4), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(48, 16).addBox(-2.5F, -6, -4.3F, 1, 2, 1)
                .texOffs(52, 16).addBox(1.5F, -6, -4.3F, 1, 2, 1), PartPose.ZERO);
        root.addOrReplaceChild("pebble_1", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-1, -1, -1, 2, 2, 2), PartPose.offset(-5.5F, -4, 0));
        root.addOrReplaceChild("pebble_2", CubeListBuilder.create()
                .texOffs(24, 16).addBox(-1, -1, -1, 2, 2, 2), PartPose.offset(5.5F, -4, 0));
        root.addOrReplaceChild("pebble_3", CubeListBuilder.create()
                .texOffs(32, 16).addBox(-1, -1, -1, 2, 2, 2), PartPose.offset(0, -4, -5.5F));
        root.addOrReplaceChild("pebble_4", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-1, -1, -1, 2, 2, 2), PartPose.offset(0, -4, 5.5F));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Gazela das Raízes de Âmbar: cervo esbelto, galhada ramificada, orelhas e cascos separados. */
    public static LayerDefinition amberRootGazelle() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, GROUND_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3, -12, -6, 6, 5, 9)
                .texOffs(28, 14).addBox(-2, -13, 3, 4, 4, 3), PartPose.ZERO);
        root.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(16, 14).addBox(-1.5F, -16, -8, 3, 5, 3), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-2, -19, -10, 4, 4, 4)
                .texOffs(52, 22).addBox(-1, -17, -11, 2, 2, 1)
                .texOffs(36, 22).addBox(-3.5F, -22, -8, 1, 4, 1)
                .texOffs(40, 22).addBox(2.5F, -22, -8, 1, 4, 1)
                .texOffs(58, 22).addBox(-4.5F, -21, -8, 2, 1, 1)
                .texOffs(0, 27).addBox(2.5F, -21, -8, 2, 1, 1)
                .texOffs(44, 22).addBox(-1.5F, -25, -8, 1, 3, 1)
                .texOffs(48, 22).addBox(0.5F, -25, -8, 1, 3, 1)
                .texOffs(6, 27).addBox(-1.5F, -23, -10.5F, 1, 1, 1)
                .texOffs(10, 27).addBox(0.5F, -23, -10.5F, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-1, 0, -1, 2, 7, 2)
                .texOffs(42, 14).addBox(-1.5F, 5, -2, 3, 2, 3), PartPose.offset(-2, -7, -4));
        root.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-1, 0, -1, 2, 7, 2)
                .texOffs(0, 22).addBox(-1.5F, 5, -2, 3, 2, 3), PartPose.offset(2, -7, -4));
        root.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-1, 0, -1, 2, 7, 2)
                .texOffs(12, 22).addBox(-1.5F, 5, -2, 3, 2, 3), PartPose.offset(-2, -7, 4));
        root.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-1, 0, -1, 2, 7, 2)
                .texOffs(24, 22).addBox(-1.5F, 5, -2, 3, 2, 3), PartPose.offset(2, -7, 4));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Arraia das Nuvens: corpo achatado com lobos cefálicos, asas largas com pontas dobradas e cauda com anéis. */
    public static LayerDefinition cloudMantaRay() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, FLYER_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(28, 10).addBox(-2, -1, -3, 4, 1, 6)
                .texOffs(48, 10).addBox(-3, -1, -4, 6, 1, 2)
                .texOffs(28, 19).addBox(-1, -1.5F, -5, 2, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(20, 0).addBox(0, -0.5F, -5, 6, 1, 8)
                .texOffs(0, 19).addBox(5, -0.5F, 2, 2, 1, 2)
                .texOffs(34, 19).addBox(5, 0, -6, 2, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 10).addBox(-6, -0.5F, -5, 6, 1, 8)
                .texOffs(8, 19).addBox(-7, -0.5F, 2, 2, 1, 2)
                .texOffs(40, 19).addBox(-7, 0, -6, 2, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -0.5F, 3, 1, 1, 9)
                .texOffs(16, 19).addBox(-1, -1, 6, 2, 2, 1)
                .texOffs(22, 19).addBox(-1, -1, 9, 2, 2, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Coruja Tempestuosa de Sentinela: corpo robusto, penacho em forma de elmo alado, asas com penas e garras. */
    public static LayerDefinition stormSentinelOwl() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, FLYER_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3, -6, -3, 6, 8, 6)
                .texOffs(38, 14).addBox(-2, -2, 3, 4, 3, 1)
                .texOffs(0, 23).addBox(-2.5F, 1, -2.5F, 5, 2, 1), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-3, -9, -3, 6, 4, 5)
                .texOffs(20, 23).addBox(-3, -11, -1, 1, 2, 1)
                .texOffs(24, 23).addBox(2, -11, -1, 1, 2, 1)
                .texOffs(12, 23).addBox(-1.5F, -10, -4, 3, 2, 1)
                .texOffs(28, 23).addBox(-1.5F, -8, -3.5F, 3, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(24, 0).addBox(0, 0, -2, 1, 6, 4)
                .texOffs(22, 14).addBox(1, 0, -2, 1, 4, 3)
                .texOffs(48, 14).addBox(0, 5, -1, 1, 2, 2), PartPose.offset(3, -5, 0));
        root.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-1, 0, -2, 1, 6, 4)
                .texOffs(30, 14).addBox(-2, 0, -2, 1, 4, 3)
                .texOffs(54, 14).addBox(-1, 5, -1, 1, 2, 2), PartPose.offset(-3, -5, 0));
        return LayerDefinition.create(mesh, TEX, TEX);
    }

    /** Mariposa da Fenda Dimensional: corpo felpudo com antenas, asas em camadas com nervuras e cauda de pelos. */
    public static LayerDefinition dimensionalRiftMoth() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, FLYER_Y, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1, -2, -5, 2, 3, 8)
                .texOffs(52, 11).addBox(-1.5F, -1, 3, 3, 2, 3), PartPose.ZERO);
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(40, 11).addBox(-1.5F, -3, -7, 3, 3, 3)
                .texOffs(14, 24).addBox(-2.5F, -5, -7, 1, 3, 1)
                .texOffs(18, 24).addBox(1.5F, -5, -7, 1, 3, 1), PartPose.ZERO);
        root.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(20, 0).addBox(0, 0, -6, 12, 1, 8)
                .texOffs(0, 20).addBox(2, 0, 2, 8, 1, 3)
                .texOffs(44, 20).addBox(6, 1, -7, 4, 1, 3), PartPose.offset(1, -2, -2));
        root.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 11).addBox(-12, 0, -6, 12, 1, 8)
                .texOffs(22, 20).addBox(-10, 0, 2, 8, 1, 3)
                .texOffs(0, 24).addBox(-10, 1, -7, 4, 1, 3), PartPose.offset(-1, -2, -2));
        return LayerDefinition.create(mesh, TEX, TEX);
    }
}
