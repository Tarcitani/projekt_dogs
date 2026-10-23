package com.tarcitani.solomod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tarcitani.solomod.client.MobAnimations;
import com.tarcitani.solomod.entity.TierMob;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base dos modelos das criaturas. Cada subclasse posa os membros em {@link #animate} a partir da caminhada,
 * do olhar, do golpe vanilla ({@code attackTime}) e da {@link MobAnimations.Action} enviada pelo servidor.
 *
 * <p>Uma mesma classe anima a forma base e as 4 variantes elementais da criatura: as partes com nomes
 * conhecidos (body, head, arm_r...) recebem a animação do tier e as decorativas, a do prefixo.
 *
 * <p>Também cuida da desintegração do Colosso: cada pedaço do modelo voa numa direção própria, sobe e gira,
 * enquanto o corpo todo fica transparente.
 */
public abstract class TierMobModel extends HierarchicalModel<TierMob> {
    private final ModelPart root;
    private final List<ModelPart> fragments;
    private final List<Map.Entry<String, ModelPart>> decor = new ArrayList<>();
    private float alpha = 1.0F;

    protected TierMobModel(ModelPart root) {
        this.root = root;
        this.fragments = root.getAllParts().filter(p -> p != root).toList();
    }

    @Override
    public ModelPart root() {
        return root;
    }

    /**
     * Parte pelo nome. Cada variante elemental tem só as partes que fazem sentido para ela (o Tritão não tem
     * pernas, a Medusa não tem mãos...): as ausentes viram uma parte solta, fora da árvore, que a animação
     * pode mexer à vontade sem efeito nenhum.
     */
    protected ModelPart part(String name) {
        return getAnyDescendantWithName(name).orElseGet(() -> new ModelPart(List.of(), Map.of()));
    }

    /** Partes decorativas da variante (chamas, nadadeiras, anéis...), animadas pelo prefixo do nome. */
    public TierMobModel withDecor(String[] names) {
        decor.clear();
        for (String name : names) getAnyDescendantWithName(name).ifPresent(p -> decor.add(Map.entry(name, p)));
        return this;
    }

    private void animateDecor(float age) {
        for (int i = 0; i < decor.size(); i++) {
            String name = decor.get(i).getKey();
            ModelPart p = decor.get(i).getValue();
            float phase = i * 1.7F;
            if (name.startsWith("spin_")) {
                p.yRot += age * 0.1F * (i % 2 == 0 ? 1 : -1);
            } else if (name.startsWith("roll_")) {
                p.zRot += age * 0.06F;
            } else if (name.startsWith("orbit_")) {
                p.yRot += age * 0.05F + phase;
            } else if (name.startsWith("flick_")) {
                float f = Mth.sin(age * 0.6F + phase) * 0.18F + Mth.sin(age * 1.3F + phase) * 0.07F;
                p.yScale = 1 + f;
                p.xScale = p.zScale = 1 - f * 0.5F;
            } else if (name.startsWith("sway_")) {
                p.zRot += Mth.sin(age * 0.15F + phase) * 0.25F;
                p.xRot += Mth.cos(age * 0.12F + phase) * 0.15F;
            } else if (name.startsWith("bob_")) {
                p.y += Mth.sin(age * 0.12F + phase) * 1.5F;
            } else if (name.startsWith("pulse_")) {
                float s = 1 + Mth.sin(age * 0.2F + phase) * 0.12F;
                p.xScale = p.yScale = p.zScale = s;
            } else if (name.startsWith("flap_")) {
                float side = name.endsWith("_l") ? -1 : 1;
                p.yRot += side * (0.35F + Mth.sin(age * 0.25F) * 0.35F);
            }
        }
    }

    /** Opacidade atual (1 = sólido); a camada de brilho também usa. */
    public float alpha() {
        return alpha;
    }

    @Override
    public final void setupAnim(TierMob entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partial = age - entity.tickCount;
        animate(entity, limbSwing, limbAmount, age, headYaw * Mth.DEG_TO_RAD, headPitch * Mth.DEG_TO_RAD,
                MobAnimations.action(entity, partial));
        // Em fúria, as decorações (chamas, anéis...) aceleram.
        animateDecor(MobAnimations.isRaging(entity) ? age * 2.2F : age);
        float dissolve = entity.dissolveProgress(partial);
        alpha = 1.0F - dissolve * dissolve;
        if (dissolve > 0) dissolve(dissolve);
    }

    /**
     * @param yaw   giro da cabeça em radianos
     * @param pitch inclinação da cabeça em radianos
     */
    protected abstract void animate(TierMob entity, float limbSwing, float limbAmount, float age, float yaw, float pitch,
                                    MobAnimations.Action action);

    private void dissolve(float p) {
        float e = p * p;
        for (int i = 0; i < fragments.size(); i++) {
            ModelPart part = fragments.get(i);
            // Ângulo áureo: direções bem espalhadas e sempre as mesmas para cada pedaço.
            float angle = i * 2.39996F;
            float spread = 6 + (i % 5) * 3;
            part.x += Mth.cos(angle) * spread * e;
            part.z += Mth.sin(angle) * spread * e;
            part.y -= (10 + (i % 7) * 4) * e;
            part.xRot += (i % 3 - 1) * 2.5F * e;
            part.yRot += (i % 2 == 0 ? 3F : -3F) * e;
            part.zRot += ((i * 7) % 3 - 1) * 2F * e;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        int a = (int) (((color >>> 24) & 0xFF) * alpha);
        super.renderToBuffer(pose, buffer, light, overlay, (a << 24) | (color & 0xFFFFFF));
    }

    /** Ondulação 0 -> 1 -> 0 do golpe vanilla. */
    protected float swing() {
        return Mth.sin(attackTime * Mth.PI);
    }
}
