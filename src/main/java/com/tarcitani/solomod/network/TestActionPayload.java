package com.tarcitani.solomod.network;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.component.WeaponData;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.core.StatType;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.StatsLogic;
import com.tarcitani.solomod.entity.ModEntities;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.item.ModItems;
import com.tarcitani.solomod.item.ModularWeaponItem;
import com.tarcitani.solomod.mob.TierIdentity;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Comparator;
import java.util.function.IntFunction;

/**
 * Cliente -> servidor: botões da aba "Teste" (equivalentes aos comandos /modtest).
 * Exige permissão de operador (nível 2) — no singleplayer, mundo com cheats ligados.
 *
 * @param action qual ação
 * @param a      1º argumento (ordinal de classe/stat/tier, ou quantidade)
 * @param b      2º argumento
 * @param c      3º argumento
 */
public record TestActionPayload(Action action, int a, int b, int c) implements CustomPacketPayload {
    public enum Action {
        /** a = WeaponClass, b = MobTier, c = ElementType */
        GIVE_WEAPON,
        /** a = quantidade */
        ADD_XP,
        /** a = StatType, b = delta (pode ser negativo) */
        ADD_STAT,
        FILL_MANA,
        RESET,
        /** a = MobTier, b = ElementType: invoca a criatura do tier à frente do jogador */
        SPAWN_MOB,
        /** a = MobTier, b = ElementType: marca o mob mais próximo (até 10 blocos) */
        MARK_NEAREST;

        private static final IntFunction<Action> BY_ID =
                ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, Action> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }

    public static final Type<TestActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, "test_action"));

    public static final StreamCodec<ByteBuf, TestActionPayload> STREAM_CODEC = StreamCodec.composite(
            Action.STREAM_CODEC, TestActionPayload::action,
            ByteBufCodecs.VAR_INT, TestActionPayload::a,
            ByteBufCodecs.VAR_INT, TestActionPayload::b,
            ByteBufCodecs.VAR_INT, TestActionPayload::c,
            TestActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TestActionPayload p, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2)) {
                player.displayClientMessage(Component.translatable("message.solomod.test_no_permission")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
            execute(player, p);
            StatsLogic.onStatsChanged(player);
        });
    }

    private static void execute(ServerPlayer player, TestActionPayload p) {
        PlayerStatsAttachment stats = StatsLogic.stats(player);
        switch (p.action()) {
            case GIVE_WEAPON -> {
                WeaponClass wc = pick(WeaponClass.values(), p.a());
                WeaponData data = WeaponData.of(wc, pick(MobTier.values(), p.b()), pick(ElementType.values(), p.c()));
                ItemStack stack = ModularWeaponItem.create(ModItems.get(wc), data);
                ItemStack previous = player.getMainHandItem();
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                if (!previous.isEmpty() && !player.getInventory().add(previous)) player.drop(previous, false);
            }
            case ADD_XP -> stats.addXp(Math.max(0, p.a()));
            case ADD_STAT -> {
                StatType stat = pick(StatType.values(), p.a());
                stats.set(stat, stats.get(stat) + p.b());
            }
            case FILL_MANA -> stats.setMana(stats.getMaxMana());
            case RESET -> stats.reset();
            case SPAWN_MOB -> spawnTierMob(player, pick(MobTier.values(), p.a()), pick(ElementType.values(), p.b()));
            case MARK_NEAREST -> player.serverLevel()
                    .getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10),
                            e -> e != player && e.isAlive() && !(e instanceof Player))
                    .stream()
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                    .ifPresent(e -> mark(e, pick(MobTier.values(), p.a()), pick(ElementType.values(), p.b())));
        }
    }

    /** Invoca a criatura do tier (Diabrete, Duelista, Oráculo ou Colosso) à frente do jogador. */
    public static void spawnTierMob(ServerPlayer player, MobTier tier, ElementType element) {
        TierMob mob = ModEntities.forTier(tier).create(player.serverLevel());
        if (mob == null) return;
        Vec3 look = player.getLookAngle();
        double distance = 3 + mob.getBbWidth();
        Vec3 pos = player.position().add(look.x * distance, 0, look.z * distance);
        mob.moveTo(pos.x, pos.y, pos.z, player.getYRot() + 180F, 0F);
        mob.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.COMMAND, null);
        mark(mob, tier, element);
        player.serverLevel().addFreshEntity(mob);
    }

    /** Mesmo efeito de /modtest mob: identidade completa do tier (tamanho, atributos, título, IA). */
    public static void mark(LivingEntity living, MobTier tier, ElementType element) {
        TierIdentity.apply(living, tier, element);
    }

    private static <T> T pick(T[] values, int index) {
        return values[Math.floorMod(index, values.length)];
    }
}
