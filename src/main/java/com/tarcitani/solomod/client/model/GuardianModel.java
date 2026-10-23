package com.tarcitani.solomod.client.model;

import com.tarcitani.solomod.client.MobAnimations;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.mob.MobAnim;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Oráculo (Guardião): o olho do peito segue o alvo, a auréola gira, os tentáculos do manto ondulam,
 * as mãos soltas flutuam e três orbes orbitam o corpo. Conjurar = mãos sobem e os orbes se afastam;
 * invocar = mãos ao céu; repulsão = mãos recolhem e explodem para fora.
 */
public class GuardianModel extends TierMobModel {
    private final ModelPart body, head, eye, halo, handR, handL;
    private final ModelPart[] tendrils = new ModelPart[4];
    private final ModelPart[] tips = new ModelPart[4];
    private final ModelPart[] orbs = new ModelPart[3];

    public GuardianModel(ModelPart root) {
        super(root);
        body = part("body");
        head = part("head");
        eye = part("eye");
        halo = part("halo");
        handR = part("hand_r");
        handL = part("hand_l");
        for (int i = 0; i < 4; i++) {
            tendrils[i] = part("tendril_" + (i + 1));
            tips[i] = part("tendril_" + (i + 1) + "_tip");
        }
        for (int i = 0; i < 3; i++) orbs[i] = part("orb_" + (i + 1));
    }

    @Override
    protected void animate(TierMob e, float swing, float amount, float age, float yaw, float pitch, MobAnimations.Action act) {
        float t = act.t(), arc = act.arc();
        head.yRot = yaw;
        head.xRot = pitch;
        eye.yRot = yaw * 0.4F;
        eye.xRot = pitch * 0.4F;
        halo.zRot = age * 0.04F;
        body.xRot = amount * 0.25F;

        float flare = act.is(MobAnim.LEAP) ? arc : 0F;
        for (int i = 0; i < 4; i++) {
            tendrils[i].xRot = Mth.sin(age * 0.12F + i) * 0.3F + amount * 0.7F + flare * (i < 2 ? -1F : 1F);
            tendrils[i].zRot = Mth.cos(age * 0.1F + i * 1.3F) * 0.2F;
            tips[i].xRot = Mth.sin(age * 0.15F + i) * 0.4F;
        }

        float bob = Mth.sin(age * 0.1F) * 1.5F;
        float handX = 12, handY = -2;
        float orbRadius = 15, orbLift = 0;
        switch (act.anim()) {
            case CAST -> {
                handY -= 8 * arc;
                handX += 4 * arc;
                orbRadius *= 1 + 0.4F * arc;
                halo.zRot += t * 6;
            }
            case SUMMON -> {
                handY -= 14 * arc;
                handX += 6 * arc;
                orbLift = -6 * arc;
            }
            case WINDUP -> {
                handX = Mth.lerp(t, 12, 6);
                handY = Mth.lerp(t, -2, -6);
            }
            case SLAM -> handX = 12 + 6 * (1 - t);
            default -> {}
        }
        handR.x = -handX;
        handR.y = handY + bob;
        handL.x = handX;
        handL.y = handY - bob;
        handR.zRot = Mth.sin(age * 0.08F) * 0.2F;
        handL.zRot = -handR.zRot;
        float hit = swing();
        handR.z -= hit * 10;
        handR.y -= hit * 2;

        for (int i = 0; i < 3; i++) {
            float a = age * 0.08F + i * 2.094F;
            orbs[i].x = Mth.cos(a) * orbRadius;
            orbs[i].z = Mth.sin(a) * orbRadius;
            orbs[i].y = -6 + Mth.sin(age * 0.1F + i) * 3 + orbLift;
            orbs[i].yRot = age * 0.2F;
            orbs[i].xRot = age * 0.15F;
        }
    }
}
