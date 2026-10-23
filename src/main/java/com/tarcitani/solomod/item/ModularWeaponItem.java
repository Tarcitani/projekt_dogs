package com.tarcitani.solomod.item;

import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.component.ModDataComponents;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.StatsLogic;
import com.tarcitani.solomod.skill.ClassAbility;
import com.tarcitani.solomod.skill.WeaponSkill;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.List;

/**
 * Item base de todas as armas modulares. Existe um item registrado por {@link WeaponClass}
 * (o formato define dano/velocidade vanilla); tudo que varia por exemplar (tier, elemento, requisito,
 * habilidade) vem do Data Component {@link WeaponData} no ItemStack.
 */
public class ModularWeaponItem extends Item {
    private final WeaponClass weaponClass;

    public ModularWeaponItem(WeaponClass weaponClass, Properties properties) {
        super(properties.stacksTo(1).attributes(createAttributes(weaponClass)));
        this.weaponClass = weaponClass;
    }

    public WeaponClass weaponClass() {
        return weaponClass;
    }

    /** Dano e velocidade vanilla da classe (mesmos ids que espadas usam, então substituem o "soco"). */
    private static ItemAttributeModifiers createAttributes(WeaponClass wc) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, wc.baseDamage(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, wc.baseSpeed(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    /** Lê o componente; se a pilha não tiver (ex.: item do menu criativo), usa o padrão NORMAL da classe. */
    public static WeaponData getData(ItemStack stack) {
        WeaponData data = stack.get(ModDataComponents.WEAPON_DATA.get());
        if (data != null) return data;
        if (stack.getItem() instanceof ModularWeaponItem weapon) return WeaponData.defaultFor(weapon.weaponClass);
        return WeaponData.defaultFor(WeaponClass.LONGSWORD);
    }

    /** Cria uma pilha já configurada. */
    public static ItemStack create(ModularWeaponItem item, WeaponData data) {
        ItemStack stack = new ItemStack(item);
        stack.set(ModDataComponents.WEAPON_DATA.get(), data);
        return stack;
    }

    // ------------------------------------------------------------------ nome

    /** Prefixa o nome com o tier e pinta na cor dele, ex.: "[Chefe] Adaga". */
    @Override
    public Component getName(ItemStack stack) {
        WeaponData data = getData(stack);
        MutableComponent name = Component.empty()
                .append(Component.literal("[").append(data.mobTier().displayName()).append("] ").withStyle(data.mobTier().color()))
                .append(super.getName(stack).copy().withStyle(data.mobTier().color()));
        if (ClassAbility.isThrown(stack)) name.append(Component.translatable("item.solomod.dagger.thrown").withStyle(ChatFormatting.GRAY));
        return name;
    }

    /** Brilho de encantamento como "aura" de armas de S. Chefe e Chefe. */
    @Override
    public boolean isFoil(ItemStack stack) {
        return getData(stack).mobTier().hasPureElement() || super.isFoil(stack);
    }

    // ------------------------------------------------------------------ tooltip

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        WeaponData data = getData(stack);
        // Tooltips só são montados no cliente; o acesso ao jogador local fica isolado em outra classe
        // para que o servidor dedicado nunca carregue classes de cliente.
        Player player = FMLEnvironment.dist == Dist.CLIENT ? com.tarcitani.solomod.client.ClientAccess.localPlayer() : null;
        boolean unlocked = player != null && StatsLogic.meetsRequirement(player, data);

        tooltip.add(Component.translatable("tooltip.solomod.class", data.weaponClass().displayName())
                .withStyle(ChatFormatting.GRAY));

        // Requisito: verde se cumprido, vermelho se bloqueado. Mostra também o valor atual do jogador.
        MutableComponent requirement = Component.translatable("tooltip.solomod.requires",
                data.requiredStatValue(), data.requiredStatType().shortName().getString());
        if (player != null) {
            int current = StatsLogic.stats(player).get(data.requiredStatType());
            requirement.append(Component.literal(" (" + current + ")"));
        }
        tooltip.add(requirement.withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.RED));

        if (data.mobTier().statBonus() > 0) {
            tooltip.add(Component.translatable("tooltip.solomod.stat_bonus",
                    data.mobTier().statBonus(), data.requiredStatType().shortName().getString())
                    .withStyle(unlocked ? ChatFormatting.BLUE : ChatFormatting.DARK_GRAY));
        }

        if (data.elementType() != ElementType.NONE) {
            MutableComponent element = Component.translatable("tooltip.solomod.element", data.elementType().displayName());
            if (data.mobTier().hasPureElement()) element.append(Component.translatable("tooltip.solomod.pure"));
            if (!unlocked) element = Component.translatable("tooltip.solomod.dormant", element).withStyle(ChatFormatting.DARK_GRAY);
            tooltip.add(element);
        }

        if (ClassAbility.hasAbility(weaponClass)) {
            tooltip.add(Component.translatable("tooltip.solomod.class_ability", ClassAbility.displayName(weaponClass))
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.solomod.class_ability." + weaponClass.getSerializedName())
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        if (data.hasSkill()) {
            WeaponSkill skill = data.skill();
            MutableComponent line = Component.translatable("tooltip.solomod.skill", skill.displayName(),
                    skill.manaCost(), skill.minInt());
            if (unlocked) {
                tooltip.add(line.withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.add(Component.translatable(ClassAbility.hasRightClick(weaponClass)
                        ? "tooltip.solomod.skill_hint_sneak" : "tooltip.solomod.skill_hint")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                tooltip.add(Component.translatable("tooltip.solomod.dormant", line).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (!unlocked && data.weaponClass().weight().penalizedWhenLocked()) {
            tooltip.add(Component.translatable("tooltip.solomod.heavy_penalty").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
    }

    // ------------------------------------------------------------------ habilidade (clique direito)

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        WeaponData data = getData(stack);

        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);

        // Habilidade do formato (rapieira, adaga, machado): clique direito. A de tier fica em agachar + clique.
        boolean classAbility = ClassAbility.hasRightClick(weaponClass)
                && (!player.isShiftKeyDown() || !data.hasSkill() || ClassAbility.isThrown(stack));
        if (classAbility) {
            if (level.isClientSide()) return InteractionResultHolder.success(stack);
            return ClassAbility.use((ServerPlayer) player, stack, this)
                    ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
        }
        // Habilidade de tier: só se a arma tiver uma.
        if (!data.hasSkill()) {
            return InteractionResultHolder.pass(stack);
        }
        // O cliente só anima o braço; a validação real acontece no servidor.
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        PlayerStatsAttachment stats = StatsLogic.stats(player);
        WeaponSkill skill = data.skill();

        // 1) Maestria da arma.
        if (!StatsLogic.meetsRequirement(player, data)) {
            return fail(player, stack, Component.translatable("message.solomod.skill_locked",
                    data.requiredStatValue(), data.requiredStatType().shortName().getString()));
        }
        // 2) INT mínima para habilidades ativas.
        if (stats.get(com.tarcitani.solomod.core.StatType.INT) < skill.minInt()) {
            return fail(player, stack, Component.translatable("message.solomod.skill_needs_int", skill.minInt()));
        }
        // 3) Recarga.
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        // 4) Mana.
        if (!stats.tryConsumeMana(skill.manaCost())) {
            return fail(player, stack, Component.translatable("message.solomod.no_mana",
                    stats.getManaAtual(), skill.manaCost()));
        }

        int hits = skill.perform(serverPlayer, data);
        player.swing(hand, true); // animação de golpe vista por todos
        player.getCooldowns().addCooldown(this, skill.cooldownTicks());
        player.displayClientMessage(Component.translatable("message.solomod.skill_used",
                skill.displayName(), hits, stats.getManaAtual(), stats.getMaxMana()).withStyle(ChatFormatting.AQUA), true);
        StatsLogic.onStatsChanged(serverPlayer); // mana mudou -> sincroniza
        return InteractionResultHolder.consume(stack);
    }

    private static InteractionResultHolder<ItemStack> fail(Player player, ItemStack stack, MutableComponent message) {
        player.displayClientMessage(message.withStyle(ChatFormatting.RED), true);
        return InteractionResultHolder.fail(stack);
    }
}
