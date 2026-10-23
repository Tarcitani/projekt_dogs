package com.tarcitani.solomod.client.model;

import com.tarcitani.solomod.client.MobAnimations;
import com.tarcitani.solomod.entity.TierMob;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Colosso Rei (Monarca): passos pesados, braços de gorila quase no chão, peito arfando. Golpe = soco
 * de cima para baixo; preparação = braços acima da cabeça; impacto = esmaga à frente; investida =
 * ombros para a frente; conjurar/rugir = braços abertos e mandíbula escancarada. Na fase 3 os cristais
 * das costas tremem.
 * Recuperação agachada e risada de vitória são aplicadas pela base TierMobModel, também no Soberano.
 */
public class MonarchModel extends TierMobModel {
    private final ModelPart chest, head, jaw, armR, armL, forearmR, forearmL, legR, legL, shinR, shinL, cape;
    private final ModelPart[] shards = new ModelPart[3];

    public MonarchModel(ModelPart root) {
        super(root);
        chest = part("chest");
        head = part("head");
        jaw = part("jaw");
        armR = part("arm_r");
        armL = part("arm_l");
        forearmR = part("forearm_r");
        forearmL = part("forearm_l");
        legR = part("leg_r");
        legL = part("leg_l");
        shinR = part("shin_r");
        shinL = part("shin_l");
        cape = part("cape");
        for (int i = 0; i < 3; i++) shards[i] = part("shard_" + (i + 1));
    }

    @Override
    protected void animate(TierMob e, float swing, float amount, float age, float yaw, float pitch, MobAnimations.Action act) {
        float walk = Mth.cos(swing * 0.5F) * 0.7F * amount;
        legR.xRot = walk;
        legL.xRot = -walk;
        shinR.xRot = Math.max(0, -walk) * 0.6F;
        shinL.xRot = Math.max(0, walk) * 0.6F;

        chest.xRot += amount * 0.1F + Mth.sin(age * 0.08F) * 0.02F;
        armR.xRot = -0.2F - walk * 0.6F;
        armL.xRot = -0.2F + walk * 0.6F;
        armR.zRot = 0.12F;
        armL.zRot = -0.12F;
        forearmR.xRot = -0.25F;
        forearmL.xRot = -0.25F;
        head.yRot = yaw * 0.7F;
        head.xRot = pitch * 0.5F - 0.2F;
        jaw.xRot = 0.05F + Mth.sin(age * 0.05F) * 0.03F;
        cape.xRot = 0.15F + amount * 0.4F + Mth.sin(age * 0.07F) * 0.03F;
        if (MobAnimations.phase(e) == 3) {
            for (int i = 0; i < 3; i++) shards[i].xRot += Mth.sin(age * 0.9F + i) * 0.06F;
        }

        float hit = swing();
        armR.xRot -= hit * 2.0F;
        forearmR.xRot -= hit * 0.6F;
        chest.yRot = hit * 0.3F;

        float t = act.t(), arc = act.arc();
        switch (act.anim()) {
            case WINDUP -> {
                armR.xRot = armL.xRot = -0.2F - 2.7F * t;
                forearmR.xRot = forearmL.xRot = -0.25F - 0.4F * t;
                head.xRot -= 0.3F * t;
                jaw.xRot += 0.5F * t;
                chest.xRot -= 0.25F * t;
            }
            case SLAM -> {
                float k = 1 - t;
                armR.xRot = armL.xRot = -0.2F - 1.3F * k;
                chest.xRot += 0.45F * k;
                jaw.xRot += 0.6F * k;
            }
            case DASH -> {
                chest.xRot += 0.4F;
                armR.xRot = armL.xRot = 0.7F;
                head.xRot -= 0.4F;
            }
            case CAST -> {
                armR.zRot = 0.12F + 1.3F * arc;
                armL.zRot = -armR.zRot;
                armR.xRot = armL.xRot = -0.2F - 0.6F * arc;
                head.xRot -= 0.3F * arc;
            }
            case ROAR, PHASE -> {
                armR.zRot = 0.12F + 1.9F * arc;
                armL.zRot = -armR.zRot;
                armR.xRot = armL.xRot = -0.2F - 0.8F * arc;
                head.xRot -= 0.6F * arc;
                jaw.xRot += 0.9F * arc;
            }
            case BREATH -> {
                jaw.xRot += 0.7F;
                head.xRot += 0.25F;
            }
            case LEAP -> armR.xRot = armL.xRot = -0.2F - 2.5F * arc;
            default -> {}
        }
    }
}
