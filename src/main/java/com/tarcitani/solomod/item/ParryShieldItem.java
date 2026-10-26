package com.tarcitani.solomod.item;

import com.tarcitani.solomod.event.StatsLogic.Parry;
import com.tarcitani.solomod.skill.ClassAbility;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Escudo de Aparada: usa BLOCK vanilla nas duas mãos, inclusive com jogador canhoto.
 * Os primeiros {@link Parry#WINDOW_TICKS} ticks permitem aparar ataques frontais de chefes
 * no servidor (ParryGuard); depois continua como escudo comum. Levantar já inicia a recarga
 * (sem cancelar a guarda atual), para trocar de mão/item não reabrir a janela por spam.
 * Soltar renova a recuperação. Técnicas comuns ficam bloqueadas com escudo; Z usa ShieldAbility.
 */
public final class ParryShieldItem extends ShieldItem {
    public ParryShieldItem(Properties properties) {
        super(properties.durability(Parry.DURABILITY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this) || !player.isAlive() || player.isSpectator()
                || player.isPassenger() || player.isSleeping()
                || player instanceof ServerPlayer server && !ClassAbility.canStart(server)) {
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer) player.getCooldowns().addCooldown(this, Parry.COOLDOWN_TICKS);
        return super.use(level, player, hand);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof ServerPlayer player) player.getCooldowns().addCooldown(this, Parry.COOLDOWN_TICKS);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.solomod.parry_window", Parry.WINDOW_TICKS / 20.0)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.solomod.parry_controls").withStyle(ChatFormatting.GRAY));
        Component key = net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT
                ? com.tarcitani.solomod.client.ClientAccess.shieldAbilityKey() : Component.literal("Z");
        tooltip.add(Component.translatable("tooltip.solomod.shield_key", key).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.solomod.parry_rage").withStyle(ChatFormatting.DARK_GRAY));
    }
}
