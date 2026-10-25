package com.tarcitani.solomod.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Malhas dos Santuários de Eco, feitas em código (sem Blockbench). Os {@code texOffs} saem de um {@link Atlas}, que
 * empacota as redes de UV sem sobreposição; assim a textura (tools/gen_sanctuary.py) é só um padrão de cor. Cubos
 * criados com {@code a.glow(...)} acendem na camada emissiva. O gerador lê as chamadas {@code a.add}, {@code a.glow}
 * e {@code legs(...)} deste arquivo em ordem, então nunca as coloque dentro de laços.
 *
 * <p>Nomes animados por {@link SanctuaryModel}: "head", "arm_l", "arm_r", "leg_l", "leg_r", "wing_l", "wing_r",
 * "tail", "tentacle_*", "orbit_*" e "flame_*". A origem fica no chão (y = 24).
 */
public final class SanctuaryModels {
    private SanctuaryModels() {}

    private static final float GROUND_Y = 24.0F;
    private static final float AIR_Y = 12.0F;

    /** Empacotador de redes de UV em prateleiras: cada cubo ganha o próximo espaço livre da textura. */
    private static final class Atlas {
        private final int size;
        private int x, y, shelf;

        Atlas(int size) {
            this.size = size;
        }

        CubeListBuilder add(CubeListBuilder cubes, float ox, float oy, float oz, int w, int h, int d) {
            int cw = 2 * (d + w), ch = d + h;
            if (x + cw > size) {
                x = 0;
                y += shelf;
                shelf = 0;
            }
            if (y + ch > size) throw new IllegalStateException("UV não cabe em " + size + "x" + size);
            cubes.texOffs(x, y).addBox(ox, oy, oz, w, h, d);
            x += cw;
            shelf = Math.max(shelf, ch);
            return cubes;
        }

        /** Igual a {@link #add}; o gerador de textura acende este cubo na camada de brilho. */
        CubeListBuilder glow(CubeListBuilder cubes, float ox, float oy, float oz, int w, int h, int d) {
            return add(cubes, ox, oy, oz, w, h, d);
        }
    }

    private static PartDefinition root(MeshDefinition mesh, float y) {
        return mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, y, 0));
    }

    private static CubeListBuilder cubes() {
        return CubeListBuilder.create();
    }

    /**
     * Duas pernas bípedes de altura {@code h} e largura {@code w}, afastadas {@code spread} do centro, cada uma com um
     * pé de altura {@code foot}. São 4 cubos, nesta ordem: perna esquerda, pé esquerdo, perna direita, pé direito.
     */
    private static void legs(PartDefinition root, Atlas a, float spread, int h, int w, int foot) {
        CubeListBuilder legL = cubes();
        a.add(legL, -w / 2.0F, 0, -w / 2.0F, w, h, w);
        a.add(legL, -w / 2.0F, h - foot, -w / 2.0F - 1, w, foot, w + 2);
        root.addOrReplaceChild("leg_l", legL, PartPose.offset(-spread, -h, 0));
        CubeListBuilder legR = cubes();
        a.add(legR, -w / 2.0F, 0, -w / 2.0F, w, h, w);
        a.add(legR, -w / 2.0F, h - foot, -w / 2.0F - 1, w, foot, w + 2);
        root.addOrReplaceChild("leg_r", legR, PartPose.offset(spread, -h, 0));
    }

    // ================================================================== Forja Banhada em Cinzas (Fogo)

    /** Cinzagrim: golem de basalto de ~3 blocos, ombreiras, placa no peito com a forja acesa e martelo de bigorna. */
    public static LayerDefinition cinzagrim() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(256);
        CubeListBuilder body = cubes();
        a.add(body, -8, -38, -5, 16, 20, 10);
        a.glow(body, -6, -34, -6, 12, 10, 1);
        a.add(body, -12, -41, -6, 7, 5, 12);
        a.add(body, 5, -41, -6, 7, 5, 12);
        a.add(body, -8, -21, -6, 16, 3, 12);
        a.glow(body, -5, -37, 5, 10, 14, 3);
        a.add(body, -3, -40, 5, 2, 4, 2);
        a.add(body, 1, -40, 5, 2, 4, 2);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -5, -10, -5, 10, 10, 10);
        a.glow(head, -4, -6, -6, 8, 2, 1);
        a.add(head, -8, -13, -2, 3, 6, 3);
        a.add(head, 5, -13, -2, 3, 6, 3);
        a.add(head, -1, -14, -5, 2, 4, 9);
        a.glow(head, -3, -2, -6, 6, 3, 2);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -38, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -3, 0, -3, 6, 18, 6);
        a.add(armL, -4, 16, -4, 8, 6, 8);
        a.add(armL, -4, 3, -4, 8, 2, 8);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-12, -36, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -3, 0, -3, 6, 18, 6);
        a.add(armR, -4, 3, -4, 8, 2, 8);
        a.add(armR, -4, 16, -4, 8, 6, 8);
        a.add(armR, -1, 18, -22, 3, 3, 20);
        a.add(armR, -6, 14, -32, 12, 10, 10);
        a.glow(armR, -4, 12, -30, 8, 2, 6);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(12, -36, 0));
        CubeListBuilder legL = cubes();
        a.add(legL, -3, 0, -3, 6, 18, 6);
        a.add(legL, -4, 15, -5, 8, 3, 9);
        a.add(legL, -4, 4, -4, 8, 2, 8);
        root.addOrReplaceChild("leg_l", legL, PartPose.offset(-4.5F, -18, 0));
        CubeListBuilder legR = cubes();
        a.add(legR, -3, 0, -3, 6, 18, 6);
        a.add(legR, -4, 15, -5, 8, 3, 9);
        a.add(legR, -4, 4, -4, 8, 2, 8);
        root.addOrReplaceChild("leg_r", legR, PartPose.offset(4.5F, -18, 0));
        CubeListBuilder flame1 = cubes();
        a.glow(flame1, -1.5F, -4, -1.5F, 3, 6, 3);
        root.addOrReplaceChild("flame_1", flame1, PartPose.offset(-4, -38, 7));
        CubeListBuilder flame2 = cubes();
        a.glow(flame2, -1.5F, -4, -1.5F, 3, 7, 3);
        root.addOrReplaceChild("flame_2", flame2, PartPose.offset(0, -38, 7));
        CubeListBuilder flame3 = cubes();
        a.glow(flame3, -1.5F, -4, -1.5F, 3, 8, 3);
        root.addOrReplaceChild("flame_3", flame3, PartPose.offset(4, -38, 7));
        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Escória Viva: poça de metal em pé, com um olho de brasa e pingos escorrendo pelo corpo. */
    public static LayerDefinition livingSlag() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(64);
        CubeListBuilder body = cubes();
        a.add(body, -7, -9, -7, 14, 9, 14);
        a.add(body, -5, -14, -5, 10, 5, 10);
        a.glow(body, -8, -4, -3, 2, 4, 5);
        a.glow(body, 6, -3, -4, 2, 3, 5);
        a.glow(body, -3, -2, 7, 6, 2, 2);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.glow(head, -3, -12, -6, 6, 3, 1);
        a.glow(head, -1, -16, -2, 2, 3, 4);
        root.addOrReplaceChild("head", head, PartPose.ZERO);
        CubeListBuilder flame1 = cubes();
        a.glow(flame1, -1, -3, -1, 2, 4, 2);
        root.addOrReplaceChild("flame_1", flame1, PartPose.offset(-3, -14, 0));
        CubeListBuilder flame2 = cubes();
        a.glow(flame2, -1, -3, -1, 2, 5, 2);
        root.addOrReplaceChild("flame_2", flame2, PartPose.offset(3, -14, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Fagulha da Forja: núcleo de brasa com quatro estilhaços orbitando e uma chama no topo. */
    public static LayerDefinition forgeSpark() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, AIR_Y);
        Atlas a = new Atlas(64);
        CubeListBuilder body = cubes();
        a.glow(body, -4, -4, -4, 8, 8, 8);
        a.glow(body, -2, -6, -2, 4, 2, 4);
        a.glow(body, -2, 4, -2, 4, 2, 4);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.glow(head, -3, -2, -5, 6, 2, 1);
        root.addOrReplaceChild("head", head, PartPose.ZERO);
        CubeListBuilder orbit1 = cubes();
        a.glow(orbit1, -1, -1, -1, 2, 2, 2);
        root.addOrReplaceChild("orbit_1", orbit1, PartPose.ZERO);
        CubeListBuilder orbit2 = cubes();
        a.glow(orbit2, -1, -1, -1, 2, 2, 2);
        root.addOrReplaceChild("orbit_2", orbit2, PartPose.ZERO);
        CubeListBuilder orbit3 = cubes();
        a.glow(orbit3, -1, -1, -1, 2, 2, 2);
        root.addOrReplaceChild("orbit_3", orbit3, PartPose.ZERO);
        CubeListBuilder orbit4 = cubes();
        a.glow(orbit4, -1, -1, -1, 2, 2, 2);
        root.addOrReplaceChild("orbit_4", orbit4, PartPose.ZERO);
        CubeListBuilder flame = cubes();
        a.glow(flame, -1.5F, -4, -1.5F, 3, 5, 3);
        root.addOrReplaceChild("flame_1", flame, PartPose.offset(0, -6, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Ferreiro Ressequido: esqueleto de ferreiro com avental, óculos de solda e marreta. */
    public static LayerDefinition witheredSmith() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -4, -24, -2, 8, 12, 4);
        a.add(body, -3, -20, -3, 6, 9, 1);
        a.add(body, -5, -25, -3, 10, 3, 6);
        a.add(body, -1, -12, -2, 2, 2, 4);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4, -8, -4, 8, 8, 8);
        a.glow(head, -4, -6, -5, 8, 2, 1);
        a.add(head, -3, -9, -3, 6, 1, 6);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -24, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -2, 0, -2, 3, 12, 3);
        a.add(armL, -2, 10, -2, 3, 3, 3);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-6, -23, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -1, 0, -2, 3, 12, 3);
        a.add(armR, -1, 10, -2, 3, 3, 3);
        a.add(armR, 0, 10, -14, 1, 1, 12);
        a.glow(armR, -3, 8, -18, 7, 6, 5);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(6, -23, 0));
        legs(root, a, 2, 12, 3, 2);
        return LayerDefinition.create(mesh, 128, 128);
    }

    // ================================================================== Biblioteca do Esquecimento (Neutro)

    /** O Arquivista: espectro encapuzado de manto longo, auréola de páginas, livro aberto na mão e folhas orbitando. */
    public static LayerDefinition archivist() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -5, -32, -3, 10, 16, 6);
        a.add(body, -6, -18, -4, 12, 10, 8);
        a.add(body, -5, -8, -3, 10, 4, 6);
        a.add(body, -7, -33, -3.5F, 14, 3, 7);
        a.glow(body, -1, -28, -3.5F, 2, 10, 1);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4.5F, -9, -4.5F, 9, 9, 9);
        a.glow(head, -3, -6, -4.8F, 6, 2, 1);
        a.glow(head, -6, -12, 0, 12, 1, 1);
        a.add(head, -2, -12, -2, 4, 3, 4);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -32, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -2, 0, -2, 3, 13, 3);
        a.add(armL, -2.5F, 11, -2.5F, 4, 2, 4);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-6.5F, -30, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -1, 0, -2, 3, 13, 3);
        a.add(armR, -3, 12, -7, 7, 1, 6);
        a.glow(armR, -2, 11, -6, 5, 1, 4);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(6.5F, -30, 0));
        CubeListBuilder orbit1 = cubes();
        a.glow(orbit1, -1.5F, -2, 0, 3, 4, 1);
        root.addOrReplaceChild("orbit_1", orbit1, PartPose.offset(0, -24, 0));
        CubeListBuilder orbit2 = cubes();
        a.glow(orbit2, -1.5F, -2, 0, 3, 4, 1);
        root.addOrReplaceChild("orbit_2", orbit2, PartPose.offset(0, -24, 0));
        CubeListBuilder orbit3 = cubes();
        a.glow(orbit3, -1.5F, -2, 0, 3, 4, 1);
        root.addOrReplaceChild("orbit_3", orbit3, PartPose.offset(0, -24, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Tomo Voador: livro de capa grossa que bate as capas como asas, com uma runa acesa sobre a lombada. */
    public static LayerDefinition flyingTome() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(64);
        CubeListBuilder body = cubes();
        a.add(body, -1, -7, -4, 2, 2, 8);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.glow(head, -1, -11, -1, 2, 2, 2);
        root.addOrReplaceChild("head", head, PartPose.ZERO);
        CubeListBuilder wingL = cubes();
        a.add(wingL, -7, -0.5F, -4, 7, 1, 8);
        a.glow(wingL, -6, -1.5F, -3, 6, 1, 6);
        root.addOrReplaceChild("wing_l", wingL, PartPose.offset(-1, -6, 0));
        CubeListBuilder wingR = cubes();
        a.add(wingR, 0, -0.5F, -4, 7, 1, 8);
        a.glow(wingR, 0, -1.5F, -3, 6, 1, 6);
        root.addOrReplaceChild("wing_r", wingR, PartPose.offset(1, -6, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Sentinela de Tinta: golem atarracado de tinta seca, olho em fenda e braços longos que pingam. */
    public static LayerDefinition inkSentinel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -6, -24, -4, 12, 12, 8);
        a.add(body, -5, -13, -3, 10, 3, 6);
        a.glow(body, -7, -21, -1, 1, 5, 2);
        a.glow(body, 6, -19, 0, 1, 4, 2);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4, -7, -4, 8, 7, 8);
        a.glow(head, -3, -5, -4.5F, 6, 2, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -24, -1));
        CubeListBuilder armL = cubes();
        a.add(armL, -3, 0, -2, 4, 14, 4);
        a.add(armL, -3.5F, 12, -2.5F, 5, 4, 5);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-7, -23, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -1, 0, -2, 4, 14, 4);
        a.add(armR, -1.5F, 12, -2.5F, 5, 4, 5);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(7, -23, 0));
        legs(root, a, 3, 10, 4, 2);
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Escriba Espectral: fantasma de copista com chapéu pontudo, pena acesa numa mão e pergaminho na outra. */
    public static LayerDefinition spectralScribe() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -4, -24, -2.5F, 8, 12, 5);
        a.add(body, -3, -12, -2, 6, 7, 4);
        a.add(body, -2, -5, -1.5F, 4, 4, 3);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -3.5F, -7, -3.5F, 7, 7, 7);
        a.glow(head, -2.5F, -5, -3.8F, 5, 1, 1);
        a.add(head, -4.5F, -8, -4.5F, 9, 1, 9);
        a.add(head, -2, -12, -2, 4, 4, 4);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -24, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -1, 0, -1.5F, 2, 9, 3);
        a.add(armL, -2, 8, -3, 4, 2, 6);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-5, -22, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -1, 0, -1.5F, 2, 9, 3);
        a.glow(armR, -0.5F, 8, -5, 1, 1, 5);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(5, -22, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    // ================================================================== Santuário das Marés (Água)

    /** A Carpideira: vestido longo de algas, coroa e ombros de coral rosa, véu caído nas costas e braços longos. */
    public static LayerDefinition weeper() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(256);
        CubeListBuilder body = cubes();
        a.add(body, -7, -20, -5, 14, 20, 10);
        a.add(body, -5, -34, -3, 10, 14, 6);
        a.glow(body, -8, -37, -2, 3, 6, 3);
        a.glow(body, 5, -37, -2, 3, 6, 3);
        a.add(body, -8, -6, -6, 16, 3, 12);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4, -8, -4, 8, 8, 8);
        a.glow(head, -3, -5, -4.3F, 6, 1, 1);
        a.add(head, -5, -9, 3.5F, 10, 16, 1);
        a.glow(head, -3, -12, -1, 2, 4, 2);
        a.glow(head, 1, -13, -1, 2, 5, 2);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -34, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -2, 0, -2, 3, 18, 3);
        a.glow(armL, -2.5F, 16, -2.5F, 4, 2, 4);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-6.5F, -33, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -1, 0, -2, 3, 18, 3);
        a.glow(armR, -1.5F, 16, -2.5F, 4, 2, 4);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(6.5F, -33, 0));
        CubeListBuilder tentacle1 = cubes();
        a.add(tentacle1, -1, 0, -1, 2, 8, 2);
        root.addOrReplaceChild("tentacle_1", tentacle1, PartPose.offset(-5, -3, 5));
        CubeListBuilder tentacle2 = cubes();
        a.add(tentacle2, -1, 0, -1, 2, 6, 2);
        root.addOrReplaceChild("tentacle_2", tentacle2, PartPose.offset(5, -3, 5));
        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Afogado do Coro: humanoide inchado coberto de coral, boca aberta que brilha ao cantar. */
    public static LayerDefinition choirDrowned() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -4, -24, -2, 8, 12, 4);
        a.glow(body, 1, -23, -3, 3, 3, 1);
        a.glow(body, -4, -16, -3, 2, 2, 1);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4, -8, -4, 8, 8, 8);
        a.glow(head, -2, -3, -4.3F, 4, 2, 1);
        a.glow(head, 2, -10, -1, 2, 3, 2);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -24, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -3, 0, -2, 3, 12, 3);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-4, -23, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, 0, 0, -2, 3, 12, 3);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(4, -23, 0));
        legs(root, a, 2, 12, 4, 2);
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Água-viva de Coral: sino translúcido com borda, núcleo aceso e quatro tentáculos que balançam. */
    public static LayerDefinition coralJelly() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -5, -16, -5, 10, 6, 10);
        a.glow(body, -3, -18, -3, 6, 2, 6);
        a.add(body, -6, -11, -6, 12, 1, 12);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.glow(head, -2, -14, -5.3F, 4, 2, 1);
        root.addOrReplaceChild("head", head, PartPose.ZERO);
        CubeListBuilder t1 = cubes();
        a.glow(t1, -0.5F, 0, -0.5F, 1, 9, 1);
        root.addOrReplaceChild("tentacle_1", t1, PartPose.offset(-3, -10, -3));
        CubeListBuilder t2 = cubes();
        a.add(t2, -0.5F, 0, -0.5F, 1, 8, 1);
        root.addOrReplaceChild("tentacle_2", t2, PartPose.offset(3, -10, -3));
        CubeListBuilder t3 = cubes();
        a.add(t3, -0.5F, 0, -0.5F, 1, 8, 1);
        root.addOrReplaceChild("tentacle_3", t3, PartPose.offset(-3, -10, 3));
        CubeListBuilder t4 = cubes();
        a.glow(t4, -0.5F, 0, -0.5F, 1, 9, 1);
        root.addOrReplaceChild("tentacle_4", t4, PartPose.offset(3, -10, 3));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Caranguejo de Coral: carapaça larga com coral no dorso, olhos em haste e duas pinças. */
    public static LayerDefinition coralCrab() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -8, -9, -6, 16, 5, 12);
        a.add(body, -6, -12, -4, 12, 3, 8);
        a.glow(body, -4, -15, -1, 2, 3, 2);
        a.glow(body, 2, -14, -2, 2, 2, 2);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -3, -4, -1, 1, 4, 1);
        a.add(head, 2, -4, -1, 1, 4, 1);
        a.glow(head, -3.5F, -5, -1.5F, 2, 1, 2);
        a.glow(head, 1.5F, -5, -1.5F, 2, 1, 2);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -9, -6));
        CubeListBuilder armL = cubes();
        a.add(armL, -4, -2, -4, 5, 4, 5);
        a.add(armL, -4, -1, -8, 3, 2, 4);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-8, -7, -5));
        CubeListBuilder armR = cubes();
        a.add(armR, -1, -2, -4, 5, 4, 5);
        a.add(armR, 1, -1, -8, 3, 2, 4);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(8, -7, -5));
        CubeListBuilder legL = cubes();
        a.add(legL, -3, 0, -1, 3, 4, 2);
        root.addOrReplaceChild("leg_l", legL, PartPose.offset(-6, -4, 0));
        CubeListBuilder legR = cubes();
        a.add(legR, 0, 0, -1, 3, 4, 2);
        root.addOrReplaceChild("leg_r", legR, PartPose.offset(6, -4, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    // ================================================================== Jardim Petrificado (Terra)

    /** Raiz-Mãe: tronco-fóssil com rosto aceso, copa larga com frutos de âmbar, galhos-braços e pés de raiz. */
    public static LayerDefinition rootMother() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(256);
        CubeListBuilder body = cubes();
        a.add(body, -8, -40, -7, 16, 30, 14);
        a.glow(body, -5, -34, -7.3F, 3, 2, 1);
        a.glow(body, 2, -34, -7.3F, 3, 2, 1);
        a.glow(body, -3, -28, -7.3F, 6, 2, 1);
        a.add(body, -11, -12, -9, 22, 3, 18);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -13, -14, -11, 26, 12, 22);
        a.add(head, -9, -18, -7, 18, 4, 14);
        a.glow(head, -11, -3, -8, 2, 2, 2);
        a.glow(head, 8, -4, 5, 2, 2, 2);
        a.glow(head, 3, -3, -10, 2, 2, 2);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -38, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -3, 0, -3, 5, 22, 5);
        a.add(armL, -6, 18, -2, 4, 6, 3);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-9, -36, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -2, 0, -3, 5, 22, 5);
        a.add(armR, 2, 18, -2, 4, 6, 3);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(9, -36, 0));
        legs(root, a, 5, 10, 8, 3);
        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Broto Espinhoso: bulbo verde com espinhos, flor acesa no topo e perninhas de raiz. */
    public static LayerDefinition thornSprout() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(64);
        CubeListBuilder body = cubes();
        a.add(body, -4, -9, -4, 8, 7, 8);
        a.add(body, -5, -8, -1, 1, 2, 2);
        a.add(body, 4, -6, -1, 1, 2, 2);
        a.add(body, -1, -10, 4, 2, 2, 1);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -3, -4, -3, 6, 4, 6);
        a.glow(head, -1.5F, -6, -1.5F, 3, 2, 3);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -9, 0));
        CubeListBuilder legL = cubes();
        a.add(legL, -1, 0, -1, 2, 2, 2);
        root.addOrReplaceChild("leg_l", legL, PartPose.offset(-2, -2, 0));
        CubeListBuilder legR = cubes();
        a.add(legR, -1, 0, -1, 2, 2, 2);
        root.addOrReplaceChild("leg_r", legR, PartPose.offset(2, -2, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Golem de Musgo: bloco de pedra com manto de musgo, cristal de âmbar no topo e punhos pesados. */
    public static LayerDefinition mossGolem() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -7, -26, -5, 14, 14, 10);
        a.add(body, -7.5F, -28, -5.5F, 15, 2, 11);
        a.glow(body, -2, -31, -1, 3, 3, 3);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4, -6, -6, 8, 6, 6);
        a.glow(head, -3, -4, -6.3F, 6, 1, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -24, -2));
        CubeListBuilder armL = cubes();
        a.add(armL, -4, 0, -3, 5, 18, 6);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-8, -24, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -1, 0, -3, 5, 18, 6);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(8, -24, 0));
        legs(root, a, 3.5F, 12, 6, 2);
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Vespa de Âmbar: tórax, abdômen listrado com ferrão aceso, olhos compostos e asas finas. */
    public static LayerDefinition amberWasp() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(64);
        CubeListBuilder body = cubes();
        a.add(body, -2, -8, -3, 4, 4, 5);
        a.add(body, -2.5F, -8.5F, 2, 5, 5, 6);
        a.glow(body, -1, -7, 8, 2, 2, 2);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -2, -2, -3, 4, 4, 3);
        a.glow(head, -2.5F, -1.5F, -3.3F, 5, 1, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -6, -3));
        CubeListBuilder wingL = cubes();
        a.add(wingL, -7, -0.5F, -2, 7, 1, 4);
        root.addOrReplaceChild("wing_l", wingL, PartPose.offset(-2, -8, 0));
        CubeListBuilder wingR = cubes();
        a.add(wingR, 0, -0.5F, -2, 7, 1, 4);
        root.addOrReplaceChild("wing_r", wingR, PartPose.offset(2, -8, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    // ================================================================== Torre dos Ventos Quebrados (Ar)

    /** O Arauto: ave-tempestade com envergadura de 3 blocos, crista acesa, sigilo no peito e cauda em leque. */
    public static LayerDefinition herald() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(256);
        CubeListBuilder body = cubes();
        a.add(body, -5, -18, -6, 10, 10, 12);
        a.glow(body, -2, -15, -6.3F, 4, 4, 1);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder tail = cubes();
        a.add(tail, -4, -1, 0, 8, 2, 8);
        a.add(tail, -5, -0.5F, 7, 10, 1, 4);
        root.addOrReplaceChild("tail", tail, PartPose.offset(0, -13, 6));
        CubeListBuilder head = cubes();
        a.add(head, -3, -6, -5, 6, 6, 6);
        a.add(head, -1, -4, -8, 2, 2, 3);
        a.glow(head, -0.5F, -10, -2, 1, 4, 5);
        a.glow(head, -3.5F, -4, -3, 7, 1, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -16, -6));
        CubeListBuilder wingL = cubes();
        a.add(wingL, -16, -0.5F, -4, 16, 1, 10);
        a.glow(wingL, -20, -0.5F, 1, 6, 1, 6);
        root.addOrReplaceChild("wing_l", wingL, PartPose.offset(-5, -16, -2));
        CubeListBuilder wingR = cubes();
        a.add(wingR, 0, -0.5F, -4, 16, 1, 10);
        a.glow(wingR, 14, -0.5F, 1, 6, 1, 6);
        root.addOrReplaceChild("wing_r", wingR, PartPose.offset(5, -16, -2));
        CubeListBuilder legL = cubes();
        a.add(legL, -1, 0, -1, 2, 6, 2);
        a.add(legL, -1.5F, 5, -2, 3, 1, 3);
        root.addOrReplaceChild("leg_l", legL, PartPose.offset(-2, -8, 0));
        CubeListBuilder legR = cubes();
        a.add(legR, -1, 0, -1, 2, 6, 2);
        a.add(legR, -1.5F, 5, -2, 3, 1, 3);
        root.addOrReplaceChild("leg_r", legR, PartPose.offset(2, -8, 0));
        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Harpia Cinzenta: corpo pequeno com cabeça de cabelos ao vento, asas cinzentas e garras. */
    public static LayerDefinition grayHarpy() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -3, -14, -2, 6, 8, 4);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -3, -6, -3, 6, 6, 6);
        a.add(head, -3.5F, -7, 0, 7, 6, 4);
        a.glow(head, -2, -4, -3.3F, 4, 1, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -14, 0));
        CubeListBuilder wingL = cubes();
        a.add(wingL, -11, -0.5F, -2, 11, 1, 7);
        root.addOrReplaceChild("wing_l", wingL, PartPose.offset(-3, -13, 0));
        CubeListBuilder wingR = cubes();
        a.add(wingR, 0, -0.5F, -2, 11, 1, 7);
        root.addOrReplaceChild("wing_r", wingR, PartPose.offset(3, -13, 0));
        CubeListBuilder legL = cubes();
        a.add(legL, -1, 0, -1, 2, 5, 2);
        a.add(legL, -1.5F, 4.5F, -2, 3, 1, 3);
        root.addOrReplaceChild("leg_l", legL, PartPose.offset(-1.5F, -6, 0));
        CubeListBuilder legR = cubes();
        a.add(legR, -1, 0, -1, 2, 5, 2);
        a.add(legR, -1.5F, 4.5F, -2, 3, 1, 3);
        root.addOrReplaceChild("leg_r", legR, PartPose.offset(1.5F, -6, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Totem do Vendaval: pilar entalhado sobre base larga, rosto aceso no topo e cata-ventos de pedra dos lados. */
    public static LayerDefinition galeTotem() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -6, -6, -6, 12, 6, 12);
        a.add(body, -4, -30, -4, 8, 24, 8);
        a.glow(body, -2, -26, -4.3F, 4, 8, 1);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -5, -8, -5, 10, 8, 10);
        a.glow(head, -4, -5, -5.3F, 8, 2, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -30, 0));
        CubeListBuilder wingL = cubes();
        a.add(wingL, -6, -1, -1, 6, 2, 2);
        a.add(wingL, -7, -4, -0.5F, 2, 7, 1);
        root.addOrReplaceChild("wing_l", wingL, PartPose.offset(-5, -34, 0));
        CubeListBuilder wingR = cubes();
        a.add(wingR, 0, -1, -1, 6, 2, 2);
        a.add(wingR, 5, -4, -0.5F, 2, 7, 1);
        root.addOrReplaceChild("wing_r", wingR, PartPose.offset(5, -34, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Elemental de Brisa: núcleo aceso, cauda em redemoinho que afina e três anéis de vento orbitando. */
    public static LayerDefinition breezeElemental() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(64);
        CubeListBuilder body = cubes();
        a.glow(body, -3, -18, -3, 6, 6, 6);
        a.add(body, -2, -12, -2, 4, 4, 4);
        a.add(body, -1.5F, -8, -1.5F, 3, 4, 3);
        a.add(body, -1, -4, -1, 2, 3, 2);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -2.5F, -5, -2.5F, 5, 5, 5);
        a.glow(head, -2, -3, -2.8F, 4, 1, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -18, 0));
        CubeListBuilder orbit1 = cubes();
        a.add(orbit1, -1, -1, -1, 2, 2, 2);
        root.addOrReplaceChild("orbit_1", orbit1, PartPose.offset(0, -14, 0));
        CubeListBuilder orbit2 = cubes();
        a.add(orbit2, -1, -1, -1, 2, 2, 2);
        root.addOrReplaceChild("orbit_2", orbit2, PartPose.offset(0, -10, 0));
        CubeListBuilder orbit3 = cubes();
        a.add(orbit3, -1, -1, -1, 2, 2, 2);
        root.addOrReplaceChild("orbit_3", orbit3, PartPose.offset(0, -6, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    // ================================================================== Catacumba Sussurrante (Neutro)

    /** Caçador de Ecos: capuz com máscara de olhos acesos, capa nas costas, cinto e uma lâmina em cada mão. */
    public static LayerDefinition echoHunter() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -4, -24, -2.5F, 8, 12, 5);
        a.add(body, -5, -25, 2.5F, 10, 19, 1);
        a.add(body, -4.5F, -13, -3, 9, 1, 6);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4.5F, -9, -4.5F, 9, 9, 9);
        a.glow(head, -3, -5, -4.8F, 6, 1, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -24, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -3, 0, -2, 3, 12, 3);
        a.glow(armL, -2, 11, -7, 1, 1, 7);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-4, -23, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, 0, 0, -2, 3, 12, 3);
        a.glow(armR, 1, 11, -7, 1, 1, 7);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(4, -23, 0));
        legs(root, a, 2, 12, 4, 2);
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Lamentador: manto rasgado que se desfaz em farrapos, rosto oco com olhos acesos e braços finos. */
    public static LayerDefinition wailingShade() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -4, -22, -3, 8, 12, 6);
        a.add(body, -3.5F, -10, -2.5F, 7, 6, 5);
        a.add(body, -2.5F, -4, -2, 5, 4, 4);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4, -8, -4, 8, 8, 8);
        a.glow(head, -3, -5, -4.3F, 6, 2, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -22, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -2, 0, -1.5F, 2, 10, 3);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-4, -21, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, 0, 0, -1.5F, 2, 10, 3);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(4, -21, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Ossada Sussurrante: esqueleto com costelas, alma acesa no peito, olhos acesos e lâmina enferrujada. */
    public static LayerDefinition whisperingBones() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -4, -24, -2, 8, 10, 4);
        a.add(body, -4, -13, -2, 8, 2, 4);
        a.add(body, -1, -14, -1, 2, 2, 2);
        a.glow(body, -1.5F, -21, -1, 3, 3, 2);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -4, -8, -4, 8, 8, 8);
        a.glow(head, -3, -5, -4.3F, 6, 1, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -24, 0));
        CubeListBuilder armL = cubes();
        a.add(armL, -1, 0, -1, 2, 12, 2);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-5, -23, 0));
        CubeListBuilder armR = cubes();
        a.add(armR, -1, 0, -1, 2, 12, 2);
        a.add(armR, -0.5F, 10, -8, 1, 1, 8);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(5, -23, 0));
        legs(root, a, 2, 11, 2, 1);
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Sombra Rastejante: quadrúpede baixo de espinhos nas costas, olhos e bocarra acesos e cauda longa. */
    public static LayerDefinition creepingShadow() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = root(mesh, GROUND_Y);
        Atlas a = new Atlas(128);
        CubeListBuilder body = cubes();
        a.add(body, -4, -11, -7, 8, 6, 14);
        a.add(body, -1, -13, -5, 2, 2, 10);
        root.addOrReplaceChild("body", body, PartPose.ZERO);
        CubeListBuilder head = cubes();
        a.add(head, -3.5F, -4, -6, 7, 6, 6);
        a.glow(head, -3, -2, -6.3F, 6, 1, 1);
        a.glow(head, -2, 0, -6.3F, 4, 1, 1);
        root.addOrReplaceChild("head", head, PartPose.offset(0, -9, -7));
        CubeListBuilder armL = cubes();
        a.add(armL, -2, 0, -1, 2, 7, 2);
        root.addOrReplaceChild("arm_l", armL, PartPose.offset(-4, -7, -5));
        CubeListBuilder armR = cubes();
        a.add(armR, 0, 0, -1, 2, 7, 2);
        root.addOrReplaceChild("arm_r", armR, PartPose.offset(4, -7, -5));
        CubeListBuilder legL = cubes();
        a.add(legL, -2, 0, -1, 2, 7, 2);
        root.addOrReplaceChild("leg_l", legL, PartPose.offset(-4, -7, 5));
        CubeListBuilder legR = cubes();
        a.add(legR, 0, 0, -1, 2, 7, 2);
        root.addOrReplaceChild("leg_r", legR, PartPose.offset(4, -7, 5));
        CubeListBuilder tail = cubes();
        a.add(tail, -1, -1, 0, 2, 2, 10);
        root.addOrReplaceChild("tail", tail, PartPose.offset(0, -9, 7));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
