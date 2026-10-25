package com.tarcitani.solomod.client.model;

import com.tarcitani.solomod.entity.passive.PassiveEchoEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Modelo genérico da Fauna Pacífica: as formas compartilham esta classe e diferem só nas malhas de
 * {@link PassiveEchoModels}. A animação é escolhida pelos nomes das partes: "wing_*" batem (mais rápido em
 * movimento), "leg_*" andam em diagonal, "head" olha para onde a entidade olha, "tail" e "gill_*" balançam,
 * "claw_*" estalam, "antenna" tremula, "lotus" gira, "pebble_*" orbitam e "wisp_*" ondulam. Corpos voadores
 * flutuam e se inclinam para a frente; os terrestres respiram. Sem pernas nem asas (o Golem), a forma salta.
 */
public class PassiveEchoModel<T extends PassiveEchoEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final float baseY;

    public PassiveEchoModel(ModelPart root) {
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
        boolean flyer = entity.isNoGravity();
        boolean legged = root.hasChild("leg_fl");
        boolean hopper = root.hasChild("pebble_1");

        // Corpo: voadores flutuam e inclinam; terrestres respiram; o Golem salta no ritmo da passada.
        root.y = baseY;
        if (flyer) {
            root.y += Mth.sin(age * 0.1F) * 0.8F;
            root.xRot = move * 0.3F;
            root.zRot = Mth.sin(age * 0.07F) * 0.05F;
        } else if (hopper) {
            root.y -= Math.abs(Mth.sin(limbSwing * 0.8F)) * move * 3.0F;
            root.xRot = Mth.sin(limbSwing * 0.8F) * move * 0.15F;
        } else {
            root.y += Mth.sin(age * 0.12F) * 0.2F;
            root.zRot = Mth.sin(limbSwing * 0.6F) * move * 0.04F;
        }

        if (root.hasChild("head")) {
            ModelPart head = root.getChild("head");
            head.yRot = netHeadYaw * Mth.DEG_TO_RAD + Mth.sin(age * 0.05F) * 0.12F * (1.0F - move);
            head.xRot = headPitch * Mth.DEG_TO_RAD + Mth.sin(age * 0.08F + 1.0F) * 0.06F;
        }
        if (root.hasChild("wing_l") && root.hasChild("wing_r")) {
            float speed = 0.55F + move * 0.45F;
            float flap = Mth.sin(age * speed) * (0.45F + move * 0.35F);
            root.getChild("wing_l").zRot = flap;
            root.getChild("wing_r").zRot = -flap;
            root.getChild("wing_l").yRot = Mth.cos(age * speed) * 0.12F;
            root.getChild("wing_r").yRot = -Mth.cos(age * speed) * 0.12F;
        }
        if (legged) {
            float walk = Mth.cos(limbSwing * 0.7F) * 1.3F * move;
            root.getChild("leg_fl").xRot = walk;
            root.getChild("leg_br").xRot = walk;
            root.getChild("leg_fr").xRot = -walk;
            root.getChild("leg_bl").xRot = -walk;
            if (flyer) {
                // Pernas penduradas de quem voa: pendem e balançam de leve.
                float dangle = 0.35F + Mth.sin(age * 0.15F) * 0.12F;
                for (String leg : new String[] {"leg_fl", "leg_fr", "leg_bl", "leg_br"}) {
                    root.getChild(leg).xRot += dangle;
                }
            }
        }
        if (root.hasChild("tail")) {
            root.getChild("tail").yRot = Mth.sin(age * 0.2F + limbSwing * 0.5F) * (0.25F + move * 0.25F);
        }
        if (root.hasChild("gill_l")) {
            float wave = Mth.sin(age * 0.25F) * 0.3F;
            root.getChild("gill_l").yRot = wave;
            root.getChild("gill_r").yRot = -wave;
        }
        if (root.hasChild("claw_l")) {
            float snap = Mth.sin(age * 0.18F) * 0.25F + Mth.cos(limbSwing * 0.7F) * 0.5F * move;
            root.getChild("claw_l").xRot = -0.3F + snap;
            root.getChild("claw_r").xRot = -0.3F - snap;
        }
        if (root.hasChild("antenna")) {
            root.getChild("antenna").xRot = Mth.sin(age * 0.3F) * 0.3F;
            root.getChild("antenna").zRot = Mth.cos(age * 0.23F) * 0.15F;
        }
        if (root.hasChild("lotus")) {
            root.getChild("lotus").yRot = age * 0.04F;
        }
        if (hopper) {
            for (int i = 1; i <= 4; i++) {
                ModelPart pebble = root.getChild("pebble_" + i);
                float angle = age * 0.05F + i * Mth.HALF_PI;
                pebble.x = Mth.cos(angle) * 5.5F;
                pebble.z = Mth.sin(angle) * 5.5F;
                pebble.y = -4.0F + Mth.sin(age * 0.1F + i) * 1.2F;
            }
        }
        for (int i = 1; i <= 3; i++) {
            String name = "wisp_" + i;
            if (root.hasChild(name)) {
                root.getChild(name).x = Mth.sin(age * 0.12F + i * 1.3F) * (0.6F * i);
                root.getChild(name).z = Mth.cos(age * 0.1F + i) * (0.4F * i);
            }
        }
    }
}
