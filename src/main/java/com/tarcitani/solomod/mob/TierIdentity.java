package com.tarcitani.solomod.mob;

import com.tarcitani.solomod.SoloMod;
import com.tarcitani.solomod.attachment.MobProfile;
import com.tarcitani.solomod.attachment.ModAttachments;
import com.tarcitani.solomod.core.ElementType;
import com.tarcitani.solomod.core.MobTier;
import com.tarcitani.solomod.entity.Sovereign;
import com.tarcitani.solomod.entity.TierMob;
import com.tarcitani.solomod.network.MobAnimPayload;
import com.tarcitani.solomod.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Identidade física de cada tier: tamanho, atributos, título e brilho.
 *
 * <table>
 *   <tr><th>Tier</th><th>Título</th><th>Tamanho</th><th>Vida</th><th>Dano</th><th>Armadura</th><th>Firmeza</th><th>Velocidade</th></tr>
 *   <tr><td>NORMAL</td><td>Lacaio</td><td>0.85x</td><td>1x</td><td>1x</td><td>+0</td><td>0%</td><td>+12%</td></tr>
 *   <tr><td>OP</td><td>Elite</td><td>1.2x</td><td>2.5x</td><td>1.5x</td><td>+4</td><td>30%</td><td>+15%</td></tr>
 *   <tr><td>SUB_BOSS</td><td>Guardião</td><td>1.6x</td><td>6x</td><td>2x</td><td>+8</td><td>60%</td><td>+0%</td></tr>
 *   <tr><td>BOSS</td><td>Monarca</td><td>2.3x</td><td>14x</td><td>2.8x</td><td>+12</td><td>100%</td><td>-5%</td></tr>
 * </table>
 *
 * <p>Os modificadores são permanentes (salvos com o mob); a IA ({@link TierBrain}) não é salva e é
 * reinstalada sempre que o mob entra no mundo (ver {@link MobEvents}).
 * Também centraliza o balanceamento das Bolas de Fogo Reais compartilhadas pelo Rei Vulcânico e Soberano.
 */
public final class TierIdentity {
    private TierIdentity() {}

    public static final double ROYAL_FIREBALL_SPEED = 0.35;
    public static final double ROYAL_FIREBALL_RETURN_SPEED = 0.45;
    public static final int ROYAL_FIREBALL_STUN_TICKS = 100;
    public static final int ROYAL_FIREBALL_LIFE = 200;
    public static final float ROYAL_FIREBALL_DAMAGE_MULTIPLIER = 1.3F;
    /** Diâmetro físico do núcleo de magma (a coroa de chamas ultrapassa um pouco essa medida). */
    public static final float ROYAL_FIREBALL_SIZE = 2.4F;
    public static final double ROYAL_FIREBALL_MIN_RANGE = 6;
    public static final int BOSS_LAUGH_TICKS = 80;
    public static final int RAGE_CRY_INTERVAL = 100;
    public static final int MAGMA_TRAIL_RADIUS = 2;
    public static final float MAGMA_TRAIL_CHANCE = 0.28F;

    /** Valores por tier. {@code scale}, {@code health}, {@code damage} e {@code speed} são multiplicadores. */
    public record Stats(String symbol, double scale, double health, double damage, double armor,
                        double knockbackResistance, double speed, double followRange) {}

    public static Stats stats(MobTier tier) {
        return switch (tier) {
            case NORMAL   -> new Stats("•", 0.85, 1.0, 1.0, 0, 0.0, 1.12, 0);
            case OP       -> new Stats("★", 1.20, 2.5, 1.5, 4, 0.3, 1.15, 8);
            case SUB_BOSS -> new Stats("✦", 1.60, 6.0, 2.0, 8, 0.6, 1.00, 16);
            case BOSS     -> new Stats("♛", 2.30, 14.0, 2.8, 12, 1.0, 0.95, 24);
        };
    }

    private static final ResourceLocation SCALE = id("tier_scale");
    private static final ResourceLocation HEALTH = id("tier_health");
    private static final ResourceLocation DAMAGE = id("tier_damage");
    private static final ResourceLocation ARMOR = id("tier_armor");
    private static final ResourceLocation KNOCKBACK = id("tier_knockback");
    private static final ResourceLocation SPEED = id("tier_speed");
    private static final ResourceLocation FOLLOW = id("tier_follow");

    /** Transforma o mob em "mob do mod" com a identidade completa do tier. */
    public static void apply(LivingEntity living, MobTier tier, ElementType element) {
        living.setData(ModAttachments.MOB_PROFILE, new MobProfile(tier, element));
        Stats s = stats(tier);

        // As criaturas do mod já têm o tamanho do próprio modelo; só mobs vanilla marcados mudam de escala.
        double scale = living instanceof TierMob ? 1.0 : s.scale();
        modifier(living, Attributes.SCALE, SCALE, scale - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(living, Attributes.MAX_HEALTH, HEALTH, s.health() - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(living, Attributes.ATTACK_DAMAGE, DAMAGE, s.damage() - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(living, Attributes.ARMOR, ARMOR, s.armor(), AttributeModifier.Operation.ADD_VALUE);
        modifier(living, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK, s.knockbackResistance(), AttributeModifier.Operation.ADD_VALUE);
        modifier(living, Attributes.MOVEMENT_SPEED, SPEED, s.speed() - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(living, Attributes.FOLLOW_RANGE, FOLLOW, s.followRange(), AttributeModifier.Operation.ADD_VALUE);
        living.setHealth(living.getMaxHealth());

        if (living instanceof TierMob creature) creature.setElement(element);
        living.setCustomName(title(living, tier, element));
        living.setCustomNameVisible(true);
        // Só o Monarca tem contorno brilhante: é visto através das paredes.
        living.setGlowingTag(tier == MobTier.BOSS);

        if (living instanceof PathfinderMob mob) TierBrain.install(mob);
        sync(living);
    }

    /** Desfaz {@link #apply}: remove perfil, atributos, título, brilho e IA. */
    public static void clear(LivingEntity living) {
        living.removeData(ModAttachments.MOB_PROFILE);
        modifier(living, Attributes.SCALE, SCALE, 0, AttributeModifier.Operation.ADD_VALUE);
        modifier(living, Attributes.MAX_HEALTH, HEALTH, 0, AttributeModifier.Operation.ADD_VALUE);
        modifier(living, Attributes.ATTACK_DAMAGE, DAMAGE, 0, AttributeModifier.Operation.ADD_VALUE);
        modifier(living, Attributes.ARMOR, ARMOR, 0, AttributeModifier.Operation.ADD_VALUE);
        modifier(living, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK, 0, AttributeModifier.Operation.ADD_VALUE);
        modifier(living, Attributes.MOVEMENT_SPEED, SPEED, 0, AttributeModifier.Operation.ADD_VALUE);
        modifier(living, Attributes.FOLLOW_RANGE, FOLLOW, 0, AttributeModifier.Operation.ADD_VALUE);
        living.setHealth(Math.min(living.getHealth(), living.getMaxHealth()));
        living.setCustomName(null);
        living.setCustomNameVisible(false);
        living.setGlowingTag(false);
        if (living instanceof PathfinderMob mob) TierBrain.uninstall(mob);
    }

    /** "♛ Monarca Husk · Fogo", com o tier na cor dele e o nome do mob na cor do elemento. */
    public static Component title(LivingEntity living, MobTier tier, ElementType element) {
        // O Soberano está acima dos tiers: só o nome, sem título nem elemento.
        if (living instanceof Sovereign) {
            return Component.literal("☬ ").withStyle(ChatFormatting.DARK_PURPLE)
                    .append(living.getType().getDescription().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        var name = Component.literal(stats(tier).symbol() + " ").withStyle(tier.color())
                .append(Component.translatable("mob_title.solomod." + tier.getSerializedName())
                        .withStyle(tier.color(), ChatFormatting.BOLD))
                .append(Component.literal(" "))
                .append(creatureName(living, element).withStyle(element.color()));
        if (element != ElementType.NONE) {
            name.append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY)).append(element.displayName());
        }
        return name;
    }

    /** Criaturas do mod têm um nome por elemento (ex.: "Rei Vulcânico"); mobs vanilla usam o nome do tipo. */
    private static MutableComponent creatureName(LivingEntity living, ElementType element) {
        if (living instanceof TierMob && element != ElementType.NONE) {
            return Component.translatable(living.getType().getDescriptionId() + "." + element.getSerializedName());
        }
        return living.getType().getDescription().copy();
    }

    /** Manda a identidade para todos os jogadores que enxergam o mob. */
    public static void sync(LivingEntity living) {
        if (!living.hasData(ModAttachments.MOB_PROFILE)) return;
        ModNetwork.sendMobAnim(living, identity(living, MobAnim.NONE, 0));
    }

    public static MobAnimPayload identity(LivingEntity living, MobAnim anim, int duration) {
        MobProfile profile = living.getData(ModAttachments.MOB_PROFILE);
        TierBrain brain = TierBrain.of(living);
        int phase = brain != null ? brain.phase() : 1;
        boolean rage = brain != null && brain.isRaging();
        return new MobAnimPayload(living.getId(), profile.tier(), profile.element(), phase, rage, anim, duration);
    }

    private static void modifier(LivingEntity living, Holder<Attribute> attribute, ResourceLocation id, double amount,
                                 AttributeModifier.Operation op) {
        AttributeInstance inst = living.getAttribute(attribute);
        if (inst == null) return; // ex.: mobs passivos não têm ATTACK_DAMAGE
        if (amount == 0) inst.removeModifier(id);
        else inst.addOrReplacePermanentModifier(new AttributeModifier(id, amount, op));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SoloMod.MOD_ID, path);
    }
}
