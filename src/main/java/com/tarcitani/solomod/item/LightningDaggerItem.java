package com.tarcitani.solomod.item;

import com.tarcitani.solomod.component.ModDataComponents;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.entity.LightningDagger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**
 * Adaga Relâmpago: protótipo de tier chefe e tema raio. Clique direito arremessa; com ela no ar ou cravada,
 * clique direito de novo teletransporta o dono até ela (e ela volta para o inventário). Se ninguém a chamar,
 * ela volta sozinha 1 s depois de tocar o chão. Reaproveita o estado "fantasma" da adaga
 * ({@link ModDataComponents#THROWN_DAGGER}) para mostrar o contorno na mão enquanto está fora.
 */
public class LightningDaggerItem extends Item {
    private static final int THROW_COOLDOWN = 8;

    public LightningDaggerItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC).attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                        WeaponClass.DAGGER.baseDamage() + 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID,
                        WeaponClass.DAGGER.baseSpeed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build()));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.empty()
                .append(Component.literal("[").append(MobTier.BOSS.displayName()).append("] ").withStyle(MobTier.BOSS.color()))
                .append(super.getName(stack).copy().withStyle(MobTier.BOSS.color()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.solomod.lightning_dagger.prototype").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.solomod.lightning_dagger.throw").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.solomod.lightning_dagger.blink").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.solomod.lightning_dagger.return")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);
        ServerPlayer serverPlayer = (ServerPlayer) player;

        UUID id = stack.get(ModDataComponents.THROWN_DAGGER.get());
        if (id != null) {
            Entity entity = ((ServerLevel) level).getEntity(id);
            if (entity instanceof LightningDagger dagger && dagger.isAlive()) {
                // Já voltando sozinha: nada a fazer (ela chega em instantes).
                if (!dagger.isReturning()) {
                    if (dagger.teleportOwner(serverPlayer)) {
                        player.swing(InteractionHand.MAIN_HAND, true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.solomod.no_room").withStyle(ChatFormatting.RED), true);
                    }
                }
                return InteractionResultHolder.consume(stack);
            }
            // A adaga ficou num chunk descarregado ou sumiu: rematerializa na mão.
            stack.remove(ModDataComponents.THROWN_DAGGER.get());
            level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 1.4F);
            return InteractionResultHolder.consume(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        LightningDagger dagger = new LightningDagger(level, player, stack);
        dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.4F, 0.4F);
        level.addFreshEntity(dagger);
        stack.set(ModDataComponents.THROWN_DAGGER.get(), dagger.getUUID());
        player.swing(InteractionHand.MAIN_HAND, true);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.6F);
        player.getCooldowns().addCooldown(this, THROW_COOLDOWN);
        return InteractionResultHolder.consume(stack);
    }
}
