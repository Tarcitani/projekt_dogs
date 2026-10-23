package com.tarcitani.solomod.client.model;

import com.tarcitani.solomod.client.MobAnimations;
import com.tarcitani.solomod.entity.TierMob;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Duelista Espectral (Elite): guarda alta com a espada à frente e o escudo de lado; capa e penacho
 * esvoaçam com a corrida. Mira = espada erguida; investida = estocada reta; combo = três cortes
 * alternando os lados; aparar = escudo levantado.
 */
public class EliteModel extends TierMobModel {
    private final ModelPart body, head, armR, armL, legR, legL, cape, plumeTail;

    public EliteModel(ModelPart root) {
        super(root);
        body = part("body");
        head = part("head");
        armR = part("arm_r");
        armL = part("arm_l");
        legR = part("leg_r");
        legL = part("leg_l");
        cape = part("cape");
        plumeTail = part("plume_tail");
    }

    @Override
    protected void animate(TierMob e, float swing, float amount, float age, float yaw, float pitch, MobAnimations.Action act) {
        float walk = Mth.cos(swing * 0.6662F) * 0.8F * amount;
        legR.xRot = walk;
        legL.xRot = -walk;
        head.yRot = yaw;
        head.xRot = pitch;
        armR.xRot = -0.55F - walk * 0.3F;
        armR.yRot = -0.1F;
        armL.xRot = -0.35F + walk * 0.3F;
        armL.yRot = 0.35F;
        cape.xRot = 0.12F + amount * 0.9F + Mth.sin(age * 0.12F) * 0.05F;
        plumeTail.xRot += amount * 0.5F + Mth.sin(age * 0.2F) * 0.08F;

        float hit = swing();
        armR.xRot -= hit * 1.6F;
        armR.yRot += hit * 0.8F;
        body.yRot = hit * 0.35F;

        float t = act.t(), arc = act.arc();
        switch (act.anim()) {
            case WINDUP -> {
                armR.xRot = -0.55F - 2.4F * t;
                armR.zRot = 0.3F * t;
                body.yRot = 0.35F * t;
                armL.xRot -= 0.5F * t;
            }
            case DASH -> {
                armR.xRot = -1.5F;
                armR.yRot = 0;
                body.xRot = 0.3F;
                armL.xRot = 0.6F;
                cape.xRot = 1.2F;
                legR.xRot = -0.6F;
                legL.xRot = 0.6F;
            }
            case COMBO -> {
                float phase = (t * 3) % 1;
                int cut = Math.min(2, (int) (t * 3));
                float side = cut == 1 ? -1 : 1;
                armR.xRot = -0.6F - Mth.sin(phase * Mth.PI) * 1.7F;
                armR.yRot = side * (phase * 2 - 1);
                body.yRot = side * (phase * 2 - 1) * 0.5F;
            }
            case DODGE -> {
                armL.xRot = -0.35F - 1.4F * arc;
                armL.yRot = 0.5F;
                body.zRot = 0.2F * arc;
            }
            default -> {}
        }
    }
}
