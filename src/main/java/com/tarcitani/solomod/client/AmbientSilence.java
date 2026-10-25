package com.tarcitani.solomod.client;

import com.tarcitani.solomod.SoloMod;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

import java.util.Set;

/**
 * O silêncio do sino da Torre dos Ventos Quebrados ({@code AmbientSilencePayload}). Enquanto dura, nenhum som de
 * ambiente, clima, música ou disco toca; ao começar, os que já tocavam param. Combate, passos e blocos continuam:
 * só o "mundo em volta" cala.
 */
@EventBusSubscriber(modid = SoloMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class AmbientSilence {
    private AmbientSilence() {}

    private static final Set<SoundSource> MUTED = Set.of(SoundSource.AMBIENT, SoundSource.WEATHER, SoundSource.MUSIC,
            SoundSource.RECORDS);

    private static long silentUntil;

    public static void silenceFor(int ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!active()) {
            for (SoundSource source : MUTED) mc.getSoundManager().stop(null, source);
            mc.getMusicManager().stopPlaying();
        }
        silentUntil = mc.level.getGameTime() + ticks;
    }

    private static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.getGameTime() < silentUntil;
    }

    @SubscribeEvent
    public static void onSound(PlaySoundEvent event) {
        if (event.getSound() != null && active() && MUTED.contains(event.getSound().getSource())) event.setSound(null);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        silentUntil = 0;
    }
}
