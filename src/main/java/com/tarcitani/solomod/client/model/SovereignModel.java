package com.tarcitani.solomod.client.model;

import com.tarcitani.solomod.client.MobAnimations;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.mob.MobAnim;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * O Soberano: anda e ataca como o {@link MonarchModel Colosso} (as partes têm os mesmos nomes) e acrescenta:
 * <ul>
 *   <li>Agonia ({@link MobAnim#AGONY}): ajoelha, aperta o peito com os dois braços, joga a cabeça para trás
 *       com a boca escancarada e se contorce.</li>
 *   <li>Estágios: a coroa treme a partir do estágio 4 e as asas batem mais forte na Fúria.</li>
 * </ul>
 */
public class SovereignModel extends MonarchModel {
    private final ModelPart chest, head, jaw, crown, armR, armL, forearmR, forearmL, legR, legL, shinR, shinL;

    public SovereignModel(ModelPart root) {
        super(root);
        chest = part("chest");
        head = part("head");
        jaw = part("jaw");
        crown = part("crown");
        armR = part("arm_r");
        armL = part("arm_l");
        forearmR = part("forearm_r");
        forearmL = part("forearm_l");
        legR = part("leg_r");
        legL = part("leg_l");
        shinR = part("shin_r");
        shinL = part("shin_l");
    }

    @Override
    protected void animate(TierMob e, float swing, float amount, float age, float yaw, float pitch, MobAnimations.Action act) {
        super.animate(e, swing, amount, age, yaw, pitch, act);
        if (MobAnimations.phase(e) >= 4) crown.zRot += Mth.sin(age * 1.1F) * 0.04F;

        if (!act.is(MobAnim.AGONY)) return;
        // Entra e sai em 1/5 da animação cada; no meio, contorce sem parar.
        float t = act.t();
        float k = Mth.clamp(t * 5F, 0F, 1F) * Mth.clamp((1F - t) * 5F, 0F, 1F);
        float writhe = Mth.sin(age * 0.9F) * 0.12F * k;
        legR.xRot = -1.0F * k;
        shinR.xRot = 1.6F * k;
        legL.xRot = -0.3F * k;
        shinL.xRot = 0.5F * k;
        chest.xRot += 0.35F * k + writhe;
        chest.zRot = Mth.sin(age * 0.4F) * 0.08F * k;
        head.xRot = Mth.lerp(k, head.xRot, -0.75F + writhe);
        head.yRot = Mth.lerp(k, head.yRot, Mth.sin(age * 0.5F) * 0.3F);
        jaw.xRot = Mth.lerp(k, jaw.xRot, 0.85F);
        armR.xRot = Mth.lerp(k, armR.xRot, -1.1F);
        armL.xRot = Mth.lerp(k, armL.xRot, -1.1F);
        armR.zRot = Mth.lerp(k, armR.zRot, -0.45F);
        armL.zRot = Mth.lerp(k, armL.zRot, 0.45F);
        forearmR.xRot = Mth.lerp(k, forearmR.xRot, -0.9F);
        forearmL.xRot = Mth.lerp(k, forearmL.xRot, -0.9F);
    }
}
