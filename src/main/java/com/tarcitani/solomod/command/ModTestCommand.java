package com.tarcitani.solomod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.core.StatType;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.Sovereign;
import com.tarcitani.solomod.event.StatsLogic;
import com.tarcitani.solomod.item.ModItems;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.mob.SovereignBrain;
import com.tarcitani.solomod.mob.TierIdentity;
import com.tarcitani.solomod.network.TestActionPayload;
import com.tarcitani.solomod.skill.WeaponSkill;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

/**
 * Comandos de teste (permissão de operador, nível 2):
 *
 * <pre>
 * /modtest stats                                   mostra stats, XP, pontos e mana
 * /modtest set &lt;for|agi|int|vit&gt; &lt;valor&gt;           define um stat direto
 * /modtest spend &lt;stat&gt; &lt;qtd&gt;                      gasta pontos disponíveis (fluxo "real")
 * /modtest addxp &lt;qtd&gt;                             soma XP Global (gera pontos a cada 100)
 * /modtest points &lt;qtd&gt;                            define os pontos disponíveis
 * /modtest mana                                    enche a mana
 * /modtest reset                                   zera tudo
 * /modtest weapon &lt;classe&gt; &lt;tier&gt; [elemento] [requisito] [habilidade]
 *                                                  cria a arma configurada na mão principal
 * /modtest mob &lt;alvos&gt; &lt;tier&gt; &lt;elemento&gt;            transforma mobs em "mobs do mod"
 * /modtest unmark &lt;alvos&gt;                          remove o perfil
 * /modtest spawn &lt;tier&gt; &lt;elemento&gt;              invoca a criatura do tier (modelo próprio)
 * /modtest sovereign [estágio]                     invoca o Soberano (chefe final), opcionalmente já no
 *                                                  estágio 1-6 (pula as cinemáticas do caminho)
 * </pre>
 */
public final class ModTestCommand {
    private ModTestCommand() {}

    private static final DynamicCommandExceptionType INVALID =
            new DynamicCommandExceptionType(value -> Component.literal("Valor inválido: " + value));

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("modtest")
                .requires(src -> src.hasPermission(2))

                .then(Commands.literal("stats").executes(ctx -> showStats(ctx.getSource().getPlayerOrException(), ctx.getSource())))

                .then(Commands.literal("set")
                        .then(enumArg("stat", StatType.values())
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, PlayerStatsAttachment.MAX_STAT))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            StatType stat = parse(ctx, "stat", StatType::byName);
                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                            StatsLogic.stats(player).set(stat, value);
                                            StatsLogic.onStatsChanged(player);
                                            return feedback(ctx, "%s = %d", stat.name(), value);
                                        }))))

                .then(Commands.literal("spend")
                        .then(enumArg("stat", StatType.values())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            StatType stat = parse(ctx, "stat", StatType::byName);
                                            int applied = StatsLogic.stats(player).spendPoints(stat, IntegerArgumentType.getInteger(ctx, "amount"));
                                            StatsLogic.onStatsChanged(player);
                                            return feedback(ctx, "+%d %s (restam %d pontos)", applied, stat.name(),
                                                    StatsLogic.stats(player).getPontosDisponiveis());
                                        }))))

                .then(Commands.literal("addxp")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int gained = StatsLogic.stats(player).addXp(IntegerArgumentType.getInteger(ctx, "amount"));
                                    StatsLogic.onStatsChanged(player);
                                    return feedback(ctx, "XP Global = %d (+%d pontos)", StatsLogic.stats(player).getXpGlobal(), gained);
                                })))

                .then(Commands.literal("points")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    StatsLogic.stats(player).setPontosDisponiveis(IntegerArgumentType.getInteger(ctx, "amount"));
                                    StatsLogic.onStatsChanged(player);
                                    return feedback(ctx, "Pontos disponíveis = %d", StatsLogic.stats(player).getPontosDisponiveis());
                                })))

                .then(Commands.literal("mana").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PlayerStatsAttachment stats = StatsLogic.stats(player);
                    stats.setMana(stats.getMaxMana());
                    StatsLogic.onStatsChanged(player);
                    return feedback(ctx, "Mana cheia (%d)", stats.getMaxMana());
                }))

                .then(Commands.literal("reset").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    StatsLogic.stats(player).reset();
                    StatsLogic.onStatsChanged(player);
                    return feedback(ctx, "Stats zerados.");
                }))

                .then(Commands.literal("weapon")
                        .then(enumArg("class", WeaponClass.values())
                                .then(enumArg("tier", MobTier.values())
                                        .executes(ctx -> giveWeapon(ctx, false, false, false))
                                        .then(enumArg("element", ElementType.values())
                                                .executes(ctx -> giveWeapon(ctx, true, false, false))
                                                .then(Commands.argument("required", IntegerArgumentType.integer(0, PlayerStatsAttachment.MAX_STAT))
                                                        .executes(ctx -> giveWeapon(ctx, true, true, false))
                                                        .then(Commands.argument("skill", StringArgumentType.word())
                                                                .suggests(suggest(Arrays.stream(WeaponSkill.values()).map(WeaponSkill::id).toArray(String[]::new)))
                                                                .executes(ctx -> giveWeapon(ctx, true, true, true))))))))

                .then(Commands.literal("mob")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(enumArg("tier", MobTier.values())
                                        .then(enumArg("element", ElementType.values())
                                                .executes(ModTestCommand::markMobs)))))

                .then(Commands.literal("spawn")
                        .then(enumArg("tier", MobTier.values())
                                .then(enumArg("element", ElementType.values())
                                        .executes(ctx -> {
                                            MobTier tier = parse(ctx, "tier", MobTier::byName);
                                            ElementType element = parse(ctx, "element", ElementType::byName);
                                            TestActionPayload.spawnTierMob(ctx.getSource().getPlayerOrException(), tier, element);
                                            return feedback(ctx, "Invocado: %s / %s", tier.name(), element.name());
                                        }))))

                .then(Commands.literal("sovereign")
                        .executes(ctx -> spawnSovereign(ctx, 1))
                        .then(Commands.argument("stage", IntegerArgumentType.integer(1, SovereignBrain.STAGES))
                                .executes(ctx -> spawnSovereign(ctx, IntegerArgumentType.getInteger(ctx, "stage")))))

                .then(Commands.literal("unmark")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(ctx -> {
                                    int count = 0;
                                    for (Entity e : EntityArgument.getEntities(ctx, "targets")) {
                                        if (e instanceof LivingEntity living && e.hasData(ModAttachments.MOB_PROFILE)) {
                                            TierIdentity.clear(living);
                                            count++;
                                        }
                                    }
                                    return feedback(ctx, "Perfil removido de %d entidade(s).", count);
                                })))
        );
    }

    // ------------------------------------------------------------------ ações

    private static int showStats(ServerPlayer player, CommandSourceStack source) {
        PlayerStatsAttachment s = StatsLogic.stats(player);
        source.sendSuccess(() -> Component.literal("=== Maestria de " + player.getName().getString() + " ===").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal(String.format("Nível %d | XP Global %d | Pontos %d",
                s.getLevel(), s.getXpGlobal(), s.getPontosDisponiveis())).withStyle(ChatFormatting.YELLOW), false);
        for (StatType stat : StatType.values()) {
            source.sendSuccess(() -> Component.literal(stat.name() + ": " + s.get(stat)).withStyle(stat.color()), false);
        }
        source.sendSuccess(() -> Component.literal(String.format("Mana %d/%d | Vida máx. %.0f",
                s.getManaAtual(), s.getMaxMana(), player.getMaxHealth())).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int giveWeapon(CommandContext<CommandSourceStack> ctx, boolean hasElement, boolean hasRequired,
                                  boolean hasSkill) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        WeaponClass wc = parse(ctx, "class", WeaponClass::byName);
        MobTier tier = parse(ctx, "tier", MobTier::byName);
        ElementType element = hasElement ? parse(ctx, "element", ElementType::byName) : ElementType.NONE;

        WeaponData data = WeaponData.of(wc, tier, element);
        if (hasRequired) data = data.withRequirement(IntegerArgumentType.getInteger(ctx, "required"));
        if (hasSkill) {
            String skillId = StringArgumentType.getString(ctx, "skill");
            if (WeaponSkill.byId(skillId) == WeaponSkill.NONE && !skillId.equalsIgnoreCase("none")) throw INVALID.create(skillId);
            data = data.withSkill(skillId.toLowerCase());
        }

        ItemStack stack = ModularWeaponItem.create(ModItems.get(wc), data);
        ItemStack previous = player.getMainHandItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        if (!previous.isEmpty() && !player.getInventory().add(previous)) {
            player.drop(previous, false);
        }
        StatsLogic.applyLockedWeaponPenalty(player);

        WeaponData finalData = data;
        ctx.getSource().sendSuccess(() -> Component.literal("Arma criada: ")
                .append(stack.getHoverName())
                .append(Component.literal(String.format(" | requer %d %s | %s | habilidade: %s",
                        finalData.requiredStatValue(), finalData.requiredStatType().name(),
                        finalData.elementType().name(), finalData.skillId()))), false);
        if (tier == MobTier.NORMAL && element != ElementType.NONE) {
            ctx.getSource().sendSuccess(() -> Component.literal("Aviso: armas NORMAL não têm elemento; elemento ignorado.")
                    .withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }

    private static int markMobs(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        MobTier tier = parse(ctx, "tier", MobTier::byName);
        ElementType element = parse(ctx, "element", ElementType::byName);
        int count = 0;
        for (Entity e : targets) {
            if (e instanceof LivingEntity living && !(living instanceof ServerPlayer)) {
                TierIdentity.apply(living, tier, element);
                count++;
            }
        }
        return feedback(ctx, "%d mob(s) marcados como %s / %s.", count, tier.name(), element.name());
    }

    /** Invoca o Soberano à frente do jogador, já no estágio pedido. */
    private static int spawnSovereign(CommandContext<CommandSourceStack> ctx, int stage) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Sovereign boss = ModEntities.SOVEREIGN.get().create(player.serverLevel());
        if (boss == null) return 0;
        Vec3 look = player.getLookAngle();
        double distance = 4 + boss.getBbWidth();
        Vec3 pos = player.position().add(look.x * distance, 0, look.z * distance);
        boss.moveTo(pos.x, pos.y, pos.z, player.getYRot() + 180F, 0F);
        boss.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(boss.blockPosition()),
                MobSpawnType.COMMAND, null);
        player.serverLevel().addFreshEntity(boss);
        // Depois de entrar no mundo: o MobEvents reinstala o cérebro ao entrar.
        if (stage > 1) SovereignBrain.skipToStage(boss, stage);
        return feedback(ctx, "Soberano invocado no estágio %d/%d.", stage, SovereignBrain.STAGES);
    }

    // ------------------------------------------------------------------ helpers

    /** Argumento de texto com sugestões a partir de um enum StringRepresentable. */
    private static RequiredArgumentBuilder<CommandSourceStack, String> enumArg(String name, StringRepresentable[] values) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests(suggest(Arrays.stream(values).map(StringRepresentable::getSerializedName).toArray(String[]::new)));
    }

    private static SuggestionProvider<CommandSourceStack> suggest(String[] options) {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(options, builder);
    }

    private static <T> T parse(CommandContext<CommandSourceStack> ctx, String arg, Function<String, T> parser)
            throws CommandSyntaxException {
        String raw = StringArgumentType.getString(ctx, arg);
        T value = parser.apply(raw);
        if (value == null) throw INVALID.create(raw);
        return value;
    }

    private static int feedback(CommandContext<CommandSourceStack> ctx, String format, Object... args) {
        String text = String.format(format, args);
        ctx.getSource().sendSuccess(() -> Component.literal(text).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
