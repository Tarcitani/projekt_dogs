package com.tarcitani.solomod.event;

import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.item.ModularWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.network.HitFeedbackPayload;
import com.tarcitani.solomod.network.ModNetwork;
import com.tarcitani.solomod.skill.ClassAbility;
import com.tarcitani.solomod.skill.DodgeRoll;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Coração do MVP: conecta Jogador (attachment), Arma (data component) e Mob (perfil elemental).
 *
 * <p>Usa {@link LivingIncomingDamageEvent}, que roda ANTES de armadura/resistências: assim o
 * multiplicador elemental age sobre o dano bruto e a armadura do alvo continua valendo depois.
 *
 * <p>Fórmula do golpe corpo a corpo com arma modular:
 * <pre>
 *   travada:    dano_vanilla x tier
 *   destravada: dano_vanilla x tier x (1 + 2% x stat_efetivo) x multiplicador_elemental
 * </pre>
 */
public final class CombatEvents {
    private CombatEvents() {}

    /** Ataque do jogador -> mob. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerMeleeHit(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        // Só golpes corpo a corpo diretos (habilidades usam dano mágico e calculam o próprio elemento).
        if (!source.is(DamageTypes.PLAYER_ATTACK)) return;
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player) return;
        if (player instanceof ServerPlayer server && DodgeRoll.isRolling(server)) {
            event.setCanceled(true);
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof ModularWeaponItem)) return;
        // Adaga arremessada: a mão está vazia (só o "fantasma" da arma), o golpe vira um soco.
        if (ClassAbility.isThrown(weapon)) {
            event.setAmount(1.0F);
            return;
        }
        event.setAmount(weaponDamage(player, ModularWeaponItem.getData(weapon), target, event.getAmount(), true));
    }

    /**
     * Dano de um golpe com arma modular (corpo a corpo, adaga arremessada).
     * Sem Maestria: só o multiplicador do tier — elemento e escalonamento ficam adormecidos
     * (a penalidade de velocidade das armas pesadas é aplicada em PlayerEvents).
     *
     * @param effects aplica a passiva elemental e mostra o aviso de vantagem/desvantagem
     */
    public static float weaponDamage(Player player, WeaponData data, LivingEntity target, float raw, boolean effects) {
        float damage = raw * data.mobTier().damageMultiplier();
        if (!StatsLogic.meetsRequirement(player, data)) return damage;

        damage *= StatsLogic.weaponScaling(player, data);
        float elementMult = ElementalCombat.multiplier(data, target);
        damage *= elementMult;
        if (!effects) return damage;

        if (ElementalCombat.hasAdvantage(data, target)) {
            ElementalCombat.applyPassive(data.elementType(), data.mobTier().elementPotency(), target);
            player.displayClientMessage(Component.translatable("message.solomod.advantage",
                    data.elementType().displayName()).withStyle(ChatFormatting.GOLD), true);
        } else if (elementMult < 1.0F) {
            player.displayClientMessage(Component.translatable("message.solomod.disadvantage")
                    .withStyle(ChatFormatting.GRAY), true);
        }
        return damage;
    }

    /**
     * Tipo de número de dano forçado para os golpes que estão sendo aplicados agora (ex.: crítico do
     * Salto do machado). -1 = decidir normalmente. Use {@link #withKind}.
     */
    private static int forcedKind = -1;

    /** Executa {@code hits} com todos os números de dano mostrados como {@code kind}. */
    public static void withKind(int kind, Runnable hits) {
        forcedKind = kind;
        try {
            hits.run();
        } finally {
            forcedKind = -1;
        }
    }

    /**
     * Depois de armadura/absorção: envia o dano FINAL para o cliente desenhar o número flutuante.
     * Só para golpes de jogadores, para não poluir a tela com dano ambiental.
     */
    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide() || event.getNewDamage() <= 0) return;
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ElementType element = ElementType.NONE;
        int kind = HitFeedbackPayload.NORMAL;
        ItemStack weapon = player.getMainHandItem();
        if (weapon.getItem() instanceof ModularWeaponItem) {
            WeaponData data = ModularWeaponItem.getData(weapon);
            if (StatsLogic.meetsRequirement(player, data)) {
                element = data.elementType();
                if (!source.is(DamageTypes.PLAYER_ATTACK)) kind = HitFeedbackPayload.SKILL;
                else if (ElementalCombat.hasAdvantage(data, target)) kind = HitFeedbackPayload.ADVANTAGE;
                else if (ElementalCombat.multiplier(data, target) < 1.0F) kind = HitFeedbackPayload.DISADVANTAGE;
            }
        }
        if (forcedKind >= 0) kind = forcedKind;
        if (kind == HitFeedbackPayload.ADVANTAGE) {
            target.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.6F);
        }
        ModNetwork.sendHit(target, event.getNewDamage(), element, kind);
    }

    /** Qualquer dano -> jogador: VIT reduz o dano recebido (até 50%). */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;
        // Dano que deve ignorar tudo (/kill, void) não é reduzido.
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        event.setAmount(event.getAmount() * StatsLogic.vitDamageMultiplier(player));
    }
}
