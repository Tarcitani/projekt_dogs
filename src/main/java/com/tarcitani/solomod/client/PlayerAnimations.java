package com.tarcitani.solomod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.network.PlayerAnimPayload;
import com.tarcitani.solomod.network.SpinAttackPayload;
import com.tarcitani.solomod.skill.PlayerAnim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

/**
 * Animações das habilidades das armas no jogador:
 * <ul>
 *   <li>Giro da Claymore: o corpo todo dá uma volta de 360° (terceira pessoa); em primeira pessoa a espada
 *       varre a tela de um lado ao outro e a câmera inclina.</li>
 *   <li>Estocada: corpo inclinado e esticado para a frente.</li>
 *   <li>Salto do machado: mortal para a frente; no impacto o corpo achata e a câmera treme.</li>
 * </ul>
 * Também troca o ataque da espada longa pelo giro (o clique vira {@link SpinAttackPayload}), salvo
 * ao mirar uma bola de fogo real, que recebe o golpe direto para poder ser rebatida.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class PlayerAnimations {
    private PlayerAnimations() {}

    private record Playing(PlayerAnim anim, long start, int duration) {}

    private static final Map<Integer, Playing> PLAYING = new HashMap<>();
    private static boolean pushed;

    public static void onPayload(PlayerAnimPayload p) {
        start(p.entityId(), p.anim(), p.duration());
    }

    private static void start(int entityId, PlayerAnim anim, int duration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) PLAYING.put(entityId, new Playing(anim, mc.level.getGameTime(), Math.max(1, duration)));
    }

    /** Progresso 0..1 da animação do jogador, ou -1 se não houver. */
    private static float progress(Player player, PlayerAnim anim, float partial) {
        Playing p = PLAYING.get(player.getId());
        if (p == null || p.anim() != anim) return -1;
        float t = (player.level().getGameTime() - p.start() + partial) / p.duration();
        if (t >= 1) {
            PLAYING.remove(player.getId());
            return -1;
        }
        return Math.max(0, t);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PLAYING.clear();
    }

    // ------------------------------------------------------------------ espada longa: ataque vira giro

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof ModularWeaponItem)
                || ModularWeaponItem.getData(held).weaponClass() != WeaponClass.LONGSWORD) return;
        // Mirando um bloco o clique continua quebrando blocos normalmente.
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) return;
        if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit
                && hit.getEntity() instanceof com.tarcitani.solomod.entity.RoyalFireball) return;

        event.setCanceled(true);
        event.setSwingHand(false);
        if (player.getAttackStrengthScale(0.0F) < 0.9F) return;
        PacketDistributor.sendToServer(SpinAttackPayload.INSTANCE);
        player.resetAttackStrengthTicker();
        start(player.getId(), PlayerAnim.SPIN, 8);
    }

    // ------------------------------------------------------------------ corpo (terceira pessoa / outros jogadores)

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        Playing playing = PLAYING.get(player.getId());
        if (playing == null) return;
        float partial = event.getPartialTick();
        float t = progress(player, playing.anim(), partial);
        if (t < 0) return;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pushed = true;
        float yaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        float arc = Mth.sin(t * Mth.PI);
        switch (playing.anim()) {
            case SPIN -> {
                float ease = 1 - (1 - t) * (1 - t);
                pose.mulPose(Axis.YP.rotationDegrees(-360F * ease));
            }
            case THRUST -> {
                pose.mulPose(new Quaternionf().rotateAxis(35F * arc * Mth.DEG_TO_RAD, Mth.cos(yaw), 0, Mth.sin(yaw)));
                pose.scale(1F, 1F - 0.08F * arc, 1F);
            }
            case LEAP_FLIP -> {
                pose.translate(0, 0.9, 0);
                pose.mulPose(new Quaternionf().rotateAxis(360F * t * Mth.DEG_TO_RAD, Mth.cos(yaw), 0, Mth.sin(yaw)));
                pose.translate(0, -0.9, 0);
            }
            case SLAM -> {
                float k = 1 - t;
                pose.scale(1F + 0.2F * k, 1F - 0.3F * k, 1F + 0.2F * k);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPost(RenderPlayerEvent.Post event) {
        if (pushed) {
            pushed = false;
            event.getPoseStack().popPose();
        }
    }

    // ------------------------------------------------------------------ primeira pessoa

    /** Durante o giro a mão com a claymore varre a tela da direita para a esquerda. */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || event.getHand() != InteractionHand.MAIN_HAND) return;
        float t = progress(player, PlayerAnim.SPIN, event.getPartialTick());
        if (t < 0) return;
        float ease = 1 - (1 - t) * (1 - t);
        PoseStack pose = event.getPoseStack();
        pose.translate(0, -0.1, -0.4);
        pose.mulPose(Axis.YP.rotationDegrees(70F - 220F * ease));
        pose.mulPose(Axis.ZP.rotationDegrees(-50F * Mth.sin(t * Mth.PI)));
        pose.translate(0, 0.1, 0.4);
    }

    /** Câmera: inclina no giro e treme no impacto do machado. */
    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        float partial = (float) event.getPartialTick();
        float spin = progress(player, PlayerAnim.SPIN, partial);
        if (spin >= 0) event.setRoll(event.getRoll() + Mth.sin(spin * Mth.PI) * 6F);
        float slam = progress(player, PlayerAnim.SLAM, partial);
        if (slam >= 0) {
            float amp = (1 - slam) * 3.5F;
            float time = player.tickCount + partial;
            event.setPitch(event.getPitch() + Mth.sin(time * 3.7F) * amp);
            event.setYaw(event.getYaw() + Mth.cos(time * 3.1F) * amp);
            event.setRoll(event.getRoll() + Mth.sin(time * 4.3F) * amp);
        }
    }
}
