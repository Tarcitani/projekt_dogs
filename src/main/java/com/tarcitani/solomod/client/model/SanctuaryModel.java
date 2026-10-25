package com.tarcitani.solomod.client.model;

import com.tarcitani.solomod.entity.sanctuary.SanctuaryMob;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Modelo genérico dos mobs dos Santuários de Eco. Animação pelos nomes das partes: "head" olha, "leg_l/leg_r" andam
 * em oposição, "arm_l" balança contra a perna e "arm_r" (a arma) sobe e desce com {@link SanctuaryMob#swing};
 * "wing_l/wing_r" batem (e se abrem com o swing), "tail" e "tentacle_*" balançam, "flame_*" tremulam e "orbit_*"
 * giram em torno do centro, na altura da própria peça. Voadores (sem gravidade) flutuam em vez de andar.
 */
public class SanctuaryModel<T extends SanctuaryMob> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final float baseY;

    public SanctuaryModel(ModelPart root) {
        this.root = root;
        this.baseY = root.y;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float move = Mth.clamp(limbSwingAmount * 2.0F, 0.0F, 1.0F);
        float swing = entity.swing(age - entity.tickCount);
        boolean flying = entity.isNoGravity();

        root.y = baseY + (flying ? Mth.sin(age * 0.1F) * 1.2F : Mth.sin(age * 0.12F) * 0.25F);
        if (root.hasChild("head")) {
            ModelPart head = root.getChild("head");
            head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
            head.xRot = headPitch * Mth.DEG_TO_RAD;
        }
        if (root.hasChild("leg_l")) {
            float walk = Mth.cos(limbSwing * 0.55F) * 1.1F * move;
            root.getChild("leg_l").xRot = walk;
            root.getChild("leg_r").xRot = -walk;
            if (root.hasChild("arm_l")) root.getChild("arm_l").xRot = -walk * 0.7F;
            root.xRot = move * 0.06F;
        }
        if (root.hasChild("arm_r")) {
            ModelPart arm = root.getChild("arm_r");
            float walk = root.hasChild("leg_l") ? Mth.cos(limbSwing * 0.55F) * 0.5F * move : 0.0F;
            // Erguido por cima da cabeça no aviso, desce rápido na pancada (swing cai de 1 a 0).
            arm.xRot = -0.35F + walk - swing * 2.5F;
            arm.zRot = 0.08F + swing * 0.1F;
        }
        for (int i = 1; i <= 3; i++) {
            if (root.hasChild("flame_" + i)) {
                ModelPart flame = root.getChild("flame_" + i);
                flame.yScale = 0.8F + 0.35F * Mth.sin(age * 0.4F + i * 1.7F);
                flame.xRot = Mth.sin(age * 0.23F + i) * 0.15F;
            }
        }
        for (int i = 1; i <= 4; i++) {
            if (root.hasChild("orbit_" + i)) {
                float angle = age * 0.12F + i * Mth.HALF_PI;
                ModelPart orbit = root.getChild("orbit_" + i);
                orbit.x = Mth.cos(angle) * 8.0F;
                orbit.z = Mth.sin(angle) * 8.0F;
                orbit.y += Mth.sin(age * 0.15F + i) * 2.0F;
                orbit.yRot = angle;
            }
        }
        if (root.hasChild("wing_l") && root.hasChild("wing_r")) {
            // Voadores batem rápido; em terra (o Totem) as "asas" são cata-ventos que só oscilam.
            float speed = flying ? 0.6F + move * 0.4F : 0.15F;
            float flap = Mth.sin(age * speed) * (flying ? 0.5F + move * 0.3F : 0.15F) + swing * 0.6F;
            root.getChild("wing_l").zRot = flap;
            root.getChild("wing_r").zRot = -flap;
        }
        if (root.hasChild("tail")) {
            root.getChild("tail").yRot = Mth.sin(age * 0.2F + limbSwing * 0.5F) * (0.2F + move * 0.3F);
        }
        float kneel = entity.kneel(age - entity.tickCount);
        if (kneel > 0) {
            // Um joelho no chão, a outra perna dobrada à frente, cabeça e braços baixos.
            root.y += kneel * 5.0F;
            if (root.hasChild("leg_l")) {
                root.getChild("leg_l").xRot = -1.45F * kneel;
                root.getChild("leg_r").xRot = 1.2F * kneel;
            }
            if (root.hasChild("head")) root.getChild("head").xRot += 0.7F * kneel;
            if (root.hasChild("arm_l")) root.getChild("arm_l").xRot = -0.25F * kneel;
            if (root.hasChild("arm_r")) root.getChild("arm_r").xRot = -0.25F * kneel;
        }
        for (int i = 1; i <= 4; i++) {
            if (root.hasChild("tentacle_" + i)) {
                ModelPart tentacle = root.getChild("tentacle_" + i);
                tentacle.xRot = Mth.sin(age * 0.18F + i * 1.1F) * 0.3F;
                tentacle.zRot = Mth.cos(age * 0.14F + i * 0.7F) * 0.2F;
            }
        }
    }
}
