package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Liga os cérebros ao ciclo de vida: dano, mortes, risada do chefe vencedor e poses ao iniciar tracking. */
public final class MobEvents {
    private MobEvents() {}

    /** A IA não é salva: reinstala sempre que um mob com perfil entra no mundo (carregar chunk, /summon...). */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof TierMob mob && !mob.hasData(ModAttachments.MOB_PROFILE)) {
            // Criatura que entrou sem passar por finalizeSpawn (ex.: carregada de um mundo antigo).
            TierIdentity.apply(mob, mob.tier(), mob.randomElement());
        } else if (event.getEntity() instanceof PathfinderMob mob && mob.hasData(ModAttachments.MOB_PROFILE)) {
            if (mob instanceof TierMob creature) creature.setElement(mob.getData(ModAttachments.MOB_PROFILE).element());
            TierBrain.install(mob);
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof PathfinderMob mob && TierBrain.of(mob) != null) {
            TierBrain.uninstall(mob);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        TierBrain brain = TierBrain.of(event.getEntity());
        if (brain != null && event.getEntity().isAlive()) brain.baseTick();
    }

    /** Aparar (Elite), invulnerabilidade nos rugidos e resistência/fragilidade da Fúria. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        TierBrain brain = TierBrain.of(event.getEntity());
        if (brain == null) return;
        if (brain.blocksDamage() || brain.onIncomingDamage(event.getSource(), event.getAmount())) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(event.getAmount() * brain.damageTakenMultiplier());
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        TierBrain brain = TierBrain.of(event.getEntity());
        if (brain != null) brain.onHurt(event.getSource(), event.getNewDamage());
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = false)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        TierBrain brain = TierBrain.of(event.getEntity());
        if (brain != null) brain.onDeath();
        if (event.getEntity() instanceof ServerPlayer player) {
            // getKillCredit cobre mortes por queimadura/ambiente logo após um golpe do chefe.
            net.minecraft.world.entity.Entity killer = event.getSource().getEntity();
            if (killer == null && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {
                killer = Hazards.magmaOwnerAt(player);
            }
            if (killer == null) killer = player.getKillCredit();
            TierBrain winner = TierBrain.of(killer);
            if (winner != null) winner.onPlayerDefeated(player);
        }
    }

    /** O cliente só sabe o tier de um mob quando passa a enxergá-lo: manda a identidade nessa hora. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity living
                && living.hasData(ModAttachments.MOB_PROFILE)) {
            TierBrain brain = TierBrain.of(living);
            int stun = brain == null ? 0 : brain.remainingStunTicks();
            int laugh = brain == null ? 0 : brain.remainingLaughTicks();
            ModNetwork.sendMobAnim(player, TierIdentity.identity(living, laugh > 0 ? MobAnim.LAUGH
                    : stun > 0 ? MobAnim.KNOCKED_DOWN : MobAnim.NONE, Math.max(laugh, stun)));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Hazards.tick();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        Hazards.clear();
    }
}
