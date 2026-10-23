package com.tarcitani.solomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tarcitani.solomod.network.HitFeedbackPayload;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Números de dano flutuantes (estilo RPG): sobem, crescem um pouco no impacto e somem.
 * Cores: branco = físico, cor do elemento = golpe elemental, maior com "!" = vantagem, cinza = desvantagem.
 */
public final class DamageNumbers {
    private DamageNumbers() {}

    private static final int LIFETIME = 30; // ticks (1,5 s)
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static final class Entry {
        final Vec3 origin;
        final String text;
        final int rgb;
        final float scale;
        int age;

        Entry(Vec3 origin, String text, int rgb, float scale) {
            this.origin = origin;
            this.text = text;
            this.rgb = rgb;
            this.scale = scale;
        }
    }

    public static void onHit(HitFeedbackPayload hit) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(hit.entityId());
        if (entity == null) return;

        Vec3 pos = entity.position().add((RANDOM.nextDouble() - 0.5) * 0.6, entity.getBbHeight() + 0.3,
                (RANDOM.nextDouble() - 0.5) * 0.6);
        String value = String.format("%.1f", hit.amount());
        int rgb = ClientSetup.tintFor(hit.element());
        Entry entry = switch (hit.kind()) {
            case HitFeedbackPayload.ADVANTAGE -> new Entry(pos, value + "!", rgb, 1.6F);
            case HitFeedbackPayload.DISADVANTAGE -> new Entry(pos, value, 0x777777, 0.9F);
            case HitFeedbackPayload.SKILL -> new Entry(pos, "✦" + value, rgb, 1.35F);
            case HitFeedbackPayload.CRIT -> new Entry(pos.add(0, 0.4, 0), "CRÍTICO " + value + "!", 0xFFD21F, 2.1F);
            default -> new Entry(pos, value, hit.element() == com.tarcitani.solomod.core.ElementType.NONE ? 0xFFFFFF : rgb, 1.0F);
        };
        ENTRIES.add(entry);
        if (ENTRIES.size() > 64) ENTRIES.remove(0);
    }

    public static void tick() {
        Iterator<Entry> it = ENTRIES.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            if (++e.age > LIFETIME) it.remove();
        }
    }

    public static void render(PoseStack pose, Camera camera) {
        if (ENTRIES.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Vec3 cam = camera.getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        for (Entry e : ENTRIES) {
            float t = e.age / (float) LIFETIME;
            // "Pop" no início (escala 1.4x -> 1x nos primeiros 4 ticks) e subida constante.
            float pop = e.age < 4 ? 1.4F - 0.1F * e.age : 1.0F;
            float alpha = t > 0.66F ? 1.0F - (t - 0.66F) / 0.34F : 1.0F;
            int argb = (Math.max(0x10, (int) (alpha * 255)) << 24) | e.rgb;

            pose.pushPose();
            pose.translate(e.origin.x - cam.x, e.origin.y - cam.y + t * 0.8, e.origin.z - cam.z);
            pose.mulPose(camera.rotation()); // sempre de frente para a câmera
            float s = 0.025F * e.scale * pop;
            pose.scale(s, -s, s);
            Matrix4f matrix = pose.last().pose();
            float x = -font.width(e.text) / 2.0F;
            font.drawInBatch(e.text, x, 0, argb, true, matrix, buffers, Font.DisplayMode.SEE_THROUGH, 0,
                    LightTexture.FULL_BRIGHT);
            pose.popPose();
        }
        buffers.endBatch();
    }
}
