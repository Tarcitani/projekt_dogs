package com.tarcitani.solomod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fragmento de Dissonância (lore: Era VII): o que sobra da morte do Soberano. Não vibra em nenhuma Voz nem no
 * Silêncio, então nada do mundo o destrói. No chão ele vira um {@link DissonanceShardEntity}: imune a fogo, lava,
 * cacto, explosões e qualquer dano, nunca some por tempo e, se cair no vazio, volta à superfície.
 *
 * <p>O tooltip alterna entre ciano (#00E5FF) e vermelho (#FF1744) a cada 0,4 s: o mesmo par de cores da
 * Dissonância Absoluta. Ainda não tem uso: é o gancho para a próxima atualização.
 */
public class DissonanceShardItem extends Item {
    private static final int CYAN = 0x00E5FF, RED = 0xFF1744;
    private static final long BLINK_MS = 400;

    public DissonanceShardItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // O tooltip é montado de novo a cada quadro em que aparece: a cor muda sozinha com o relógio.
        int color = (System.currentTimeMillis() / BLINK_MS) % 2 == 0 ? CYAN : RED;
        tooltip.add(Component.translatable("item.solomod.dissonance_shard.tooltip")
                .withStyle(style -> style.withColor(TextColor.fromRgb(color)).withItalic(true)));
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    /** Todo fragmento no chão (largado, dropado ou carregado do disco) vira a versão indestrutível. */
    @Override
    public @Nullable Entity createEntity(Level level, Entity location, ItemStack stack) {
        DissonanceShardEntity shard = new DissonanceShardEntity(level, location.getX(), location.getY(), location.getZ(), stack);
        shard.setDeltaMovement(location.getDeltaMovement());
        if (location instanceof ItemEntity original) {
            shard.setThrower(original.getOwner());
        }
        shard.setPickUpDelay(DissonanceShardEntity.PICKUP_DELAY);
        return shard;
    }
}
