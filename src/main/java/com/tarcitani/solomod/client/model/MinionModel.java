package com.tarcitani.solomod.client.model;

import com.tarcitani.solomod.client.MobAnimations;
import com.tarcitani.solomod.entity.TierMob;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Diabrete (Lacaio): corre curvado com os braços compridos balançando, bate as asas sem parar,
 * mexe as orelhas e o rabo. Bote = braços e mandíbula para a frente; grito = cabeça para trás.
 */
public class MinionModel extends TierMobModel {
    private final ModelPart body, head, jaw, earL, earR, armR, armL, wingL, wingR, tail, tailTip, legR, legL;

    public MinionModel(ModelPart root) {
        super(root);
        body = part("body");
        head = part("head");
        jaw = part("jaw");
        earL = part("ear_l");
        earR = part("ear_r");
        armR = part("arm_r");
        armL = part("arm_l");
        wingL = part("wing_l");
        wingR = part("wing_r");
        tail = part("tail");
        tailTip = part("tail_tip");
        legR = part("leg_r");
        legL = part("leg_l");
    }

    @Override
    protected void animate(TierMob e, float swing, float amount, float age, float yaw, float pitch, MobAnimations.Action act) {
        float walk = Mth.cos(swing * 0.9F) * 1.2F * amount;
        legR.xRot = walk;
        legL.xRot = -walk;
        // O corpo já nasce curvado 0.45 rad para a frente: cabeça e braços compensam.
        head.yRot = yaw;
        head.xRot = -0.45F + pitch;
        armR.xRot = -0.45F - walk * 0.8F;
        armL.xRot = -0.45F + walk * 0.8F;
        armR.zRot = 0.1F + Mth.sin(age * 0.09F) * 0.05F;
        armL.zRot = -armR.zRot;

        float flap = Mth.sin(age * 0.7F);
        wingL.yRot = -0.5F - flap * 0.45F;
        wingR.yRot = 0.5F + flap * 0.45F;
        float ear = Mth.sin(age * 0.25F) * 0.12F;
        earL.zRot += ear;
        earR.zRot -= ear;
        tail.xRot = -0.9F + Mth.sin(age * 0.2F) * 0.15F;
        tail.yRot = Mth.sin(age * 0.15F) * 0.5F;
        tailTip.yRot = Mth.sin(age * 0.15F - 0.6F) * 0.5F;
        jaw.xRot = 0.05F + Mth.abs(Mth.sin(age * 0.1F)) * 0.08F;

        float hit = swing();
        armR.xRot -= hit * 1.6F;
        armL.xRot -= hit * 1.6F;
        jaw.xRot += hit * 0.6F;

        float t = act.t(), arc = act.arc();
        switch (act.anim()) {
            case WINDUP -> {
                body.xRot += 0.35F * t;
                armR.xRot += 0.9F * t;
                armL.xRot += 0.9F * t;
                jaw.xRot += 0.3F * t;
                wingL.yRot *= 1 - 0.6F * t;
                wingR.yRot *= 1 - 0.6F * t;
            }
            case LUNGE -> {
                armR.xRot -= 2.2F * arc;
                armL.xRot -= 2.2F * arc;
                jaw.xRot += 0.9F * arc;
                wingL.yRot = -1.3F * arc;
                wingR.yRot = 1.3F * arc;
            }
            case HOWL -> {
                head.xRot -= 0.9F * arc;
                jaw.xRot += 0.9F * arc;
                earL.zRot -= 0.5F * arc;
                earR.zRot += 0.5F * arc;
            }
            case DODGE -> body.zRot += 0.4F * arc;
            default -> {}
        }
    }
}
