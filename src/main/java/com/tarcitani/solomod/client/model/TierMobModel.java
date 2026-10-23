package com.tarcitani.solomod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tarcitani.solomod.client.MobAnimations;
import com.tarcitani.solomod.entity.Sovereign;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.mob.MobAnim;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Base dos modelos das criaturas. Cada subclasse posa os membros em {@link #animate} a partir da caminhada,
 * do olhar, do golpe vanilla ({@code attackTime}) e da {@link MobAnimations.Action} enviada pelo servidor.
 *
 * <p>Uma mesma classe anima a forma base e as 4 variantes elementais da criatura: as partes com nomes
 * conhecidos (body, head, arm_r...) recebem a animação do tier e as decorativas, a do prefixo.
 *
 * <p>Também cuida da desintegração do Colosso: cada pedaço do modelo voa numa direção própria, sobe e gira,
 * enquanto o corpo todo fica transparente.
 * Poses comuns de chefes são aplicadas após a animação de cada tier: recuperação agachada e risada
 * com mandíbula, ombros e tronco sacudindo (inclui Guardiões e Soberano).
 */
public abstract class TierMobModel extends HierarchicalModel<TierMob> {
    private final ModelPart root;
    private final List<ModelPart> fragments;
    private final List<Map.Entry<String, ModelPart>> decor = new ArrayList<>();
    private final Map<String, ModelPart> namedParts = new HashMap<>();
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
        return namedParts.computeIfAbsent(name, key -> getAnyDescendantWithName(key)
                .orElseGet(() -> new ModelPart(List.of(), Map.of())));
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
        MobAnimations.Action action = MobAnimations.action(entity, partial);
        animate(entity, limbSwing, limbAmount, age, headYaw * Mth.DEG_TO_RAD, headPitch * Mth.DEG_TO_RAD, action);
        bossPose(action, age);
        // Em fúria, as decorações (chamas, anéis...) aceleram.
        animateDecor(MobAnimations.isRaging(entity) ? age * 2.2F : age);
        float dissolve = entity.dissolveProgress(partial);
        if (entity instanceof Sovereign) {
            // O Soberano não se desfaz em cinzas: ele morre "bugando".
            alpha = 1.0F;
            if (dissolve > 0) glitch(dissolve, age);
        } else {
            alpha = 1.0F - dissolve * dissolve;
            if (dissolve > 0) dissolve(dissolve);
        }
    }

    /**
     * @param yaw   giro da cabeça em radianos
     * @param pitch inclinação da cabeça em radianos
     */
    protected abstract void animate(TierMob entity, float limbSwing, float limbAmount, float age, float yaw, float pitch,
                                    MobAnimations.Action action);

    private void bossPose(MobAnimations.Action action, float age) {
        float k = Math.min(1F, action.t() * 10F) * Math.min(1F, (1F - action.t()) * 10F);
        if (action.is(MobAnim.KNOCKED_DOWN) || action.is(MobAnim.EXHAUSTED)) {
            float breath = Mth.sin(age * 0.22F) * 0.045F;
            part("body").y += 6F * k;
            part("body").xRot = (0.12F + breath) * k;
            part("chest").xRot = (0.3F + breath) * k;
            part("head").xRot = 0.35F * k;
            part("jaw").xRot = 0.16F * k;
            part("leg_r").xRot = part("leg_l").xRot = -0.9F * k;
            part("leg_r").y += 4.7F * k;
            part("leg_l").y += 4.7F * k;
            part("shin_r").xRot = part("shin_l").xRot = 1.45F * k;
            part("arm_r").xRot = part("arm_l").xRot = -0.7F * k;
            part("forearm_r").xRot = part("forearm_l").xRot = -0.55F * k;
            part("hand_r").y += 4F * k;
            part("hand_l").y += 4F * k;
        } else if (action.is(MobAnim.LAUGH)) {
            float laugh = Mth.sin(age * 0.9F);
            part("chest").xRot = (-0.15F + laugh * 0.07F) * k;
            part("body").xRot = laugh * 0.04F * k;
            part("head").xRot = (-0.55F + laugh * 0.1F) * k;
            part("jaw").xRot = (0.5F + Math.abs(laugh) * 0.4F) * k;
            part("arm_r").xRot = -0.8F * k;
            part("forearm_r").xRot = -1.1F * k;
            part("arm_l").zRot = -0.55F * k;
            part("hand_r").y -= (3F + laugh) * k;
            part("hand_l").y -= (3F - laugh) * k;
        }
    }

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

    /**
     * Morte do Soberano: o modelo "trava". A cada 2 ticks é sorteado um quadro novo e o anterior fica congelado
     * (aspecto de programa travando); em cada quadro, uma fração crescente das peças salta para um lugar
     * aleatório, gira e muda de escala, o corpo todo treme de lado e pisca (some quase por completo em alguns
     * quadros). No último décimo ele some de vez. O sorteio vem do número do quadro, então o mesmo quadro
     * sempre sai igual (sem tremida entre o desenho do corpo e das camadas).
     */
    private void glitch(float p, float age) {
        int frame = (int) (age * 0.5F);
        java.util.Random whole = new java.util.Random(frame * 7919L);
        float chance = 0.15F + 0.75F * p;
        float spread = 4F + 40F * p;
        for (int i = 0; i < fragments.size(); i++) {
            java.util.Random r = new java.util.Random(frame * 31L + i * 131L);
            if (r.nextFloat() >= chance) continue;
            ModelPart part = fragments.get(i);
            part.x += (r.nextFloat() - 0.5F) * 2F * spread;
            part.y += (r.nextFloat() - 0.5F) * 2F * spread * 0.6F;
            part.z += (r.nextFloat() - 0.5F) * 2F * spread;
            part.xRot += (r.nextFloat() - 0.5F) * 2.4F * p;
            part.yRot += (r.nextFloat() - 0.5F) * 2.4F * p;
            part.zRot += (r.nextFloat() - 0.5F) * 2.4F * p;
            if (r.nextFloat() < 0.35F) {
                part.xScale = 0.4F + r.nextFloat() * 1.4F;
                part.yScale = 0.4F + r.nextFloat() * 1.4F;
                part.zScale = 0.4F + r.nextFloat() * 1.4F;
            }
        }
        // O corpo inteiro "rasga" de lado em alguns quadros.
        if (whole.nextFloat() < 0.3F + 0.4F * p) root.x += (whole.nextFloat() - 0.5F) * 2F * (6F + 30F * p);
        alpha = whole.nextFloat() < p * 0.45F ? 0.12F : 1.0F;
        if (p > 0.9F) alpha *= (1F - p) * 10F;
    }

    /** O quadro "congelado" do glitch (o mesmo número usado no sorteio): as camadas de cor o repetem. */
    public static int glitchFrame(float age) {
        return (int) (age * 0.5F);
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
