package com.tarcitani.solomod.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tarcitani.solomod.core.StatType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

/**
 * Dados de progressão do jogador, guardados como Data Attachment do NeoForge na entidade Player.
 *
 * <p>Classe mutável de propósito: o attachment é lido e alterado no lugar via
 * {@code player.getData(ModAttachments.PLAYER_STATS)}. Depois de alterar, chame
 * {@link com.tarcitani.solomod.event.StatsLogic#onStatsChanged} para reaplicar atributos e sincronizar.
 *
 * <p>Progressão: cada {@link #XP_PER_POINT} de XP Global acumulado concede 1 ponto para distribuir.
 * Árvores usam pontos próprios (um inicial e um por nível) e IDs textuais de nós aprendidos,
 * persistidos com default vazio para mundos antigos, copiados na morte e sincronizados com a mana.
 */
public class PlayerStatsAttachment {
    /** IDs estáveis dos nós aprendidos; mundos antigos começam com a árvore vazia. */
    private final java.util.Set<String> talents = new java.util.HashSet<>();
    public static final int XP_PER_POINT = 100;
    public static final int BASE_MANA = 20;
    public static final int MANA_PER_INT = 5;
    public static final int MAX_STAT = 999;

    /** Persistência em NBT. Nomes de campo em português para casar com o design. */
    public static final Codec<PlayerStatsAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("xpGlobal", 0).forGetter((PlayerStatsAttachment s) -> s.xpGlobal),
            Codec.INT.optionalFieldOf("pontosDisponiveis", 0).forGetter((PlayerStatsAttachment s) -> s.pontosDisponiveis),
            Codec.INT.optionalFieldOf("forca", 0).forGetter((PlayerStatsAttachment s) -> s.forca),
            Codec.INT.optionalFieldOf("agilidade", 0).forGetter((PlayerStatsAttachment s) -> s.agilidade),
            Codec.INT.optionalFieldOf("inteligencia", 0).forGetter((PlayerStatsAttachment s) -> s.inteligencia),
            Codec.INT.optionalFieldOf("vitalidade", 0).forGetter((PlayerStatsAttachment s) -> s.vitalidade),
            Codec.INT.optionalFieldOf("manaAtual", BASE_MANA).forGetter((PlayerStatsAttachment s) -> s.manaAtual),
            Codec.STRING.listOf().optionalFieldOf("habilidades", java.util.List.of()).forGetter((PlayerStatsAttachment s) -> s.talents.stream().sorted().toList())
    ).apply(instance, PlayerStatsAttachment::new));

    private int xpGlobal;
    private int pontosDisponiveis;
    private int forca;
    private int agilidade;
    private int inteligencia;
    private int vitalidade;
    private int manaAtual;

    /** Valores iniciais de um jogador novo. */
    public PlayerStatsAttachment() {
        this(0, 0, 0, 0, 0, 0, BASE_MANA);
    }

    public PlayerStatsAttachment(int xpGlobal, int pontosDisponiveis, int forca, int agilidade,
                                 int inteligencia, int vitalidade, int manaAtual) {
        this.xpGlobal = xpGlobal;
        this.pontosDisponiveis = pontosDisponiveis;
        this.forca = forca;
        this.agilidade = agilidade;
        this.inteligencia = inteligencia;
        this.vitalidade = vitalidade;
        this.manaAtual = manaAtual;
    }

    public PlayerStatsAttachment(int xp, int points, int str, int agi, int intel, int vit, int mana, java.util.List<String> learned) {
        this(xp, points, str, agi, intel, vit, mana);
        learned.stream().filter(id -> com.tarcitani.solomod.skill.SkillTree.byId(id) != null).forEach(talents::add);
    }

    /** Pontos próprios da árvore: um inicial e um por nível, independentes dos atributos. */
    public int getSkillPoints() {
        return Math.max(0, getLevel() - talents.stream().map(com.tarcitani.solomod.skill.SkillTree::byId)
                .filter(java.util.Objects::nonNull).mapToInt(com.tarcitani.solomod.skill.SkillTree.Node::cost).sum());
    }
    public boolean hasTalent(String id) { return talents.contains(id); }
    public boolean unlockTalent(String id) {
        var node = com.tarcitani.solomod.skill.SkillTree.byId(id);
        return node != null && com.tarcitani.solomod.skill.SkillTree.available(this, node) && talents.add(id);
    }

    // ---------------------------------------------------------------- stats

    public int get(StatType type) {
        return switch (type) {
            case FOR -> forca;
            case AGI -> agilidade;
            case INT -> inteligencia;
            case VIT -> vitalidade;
        };
    }

    public void set(StatType type, int value) {
        int v = Mth.clamp(value, 0, MAX_STAT);
        switch (type) {
            case FOR -> forca = v;
            case AGI -> agilidade = v;
            case INT -> inteligencia = v;
            case VIT -> vitalidade = v;
        }
        if (type == StatType.INT) manaAtual = Math.min(manaAtual, getMaxMana());
    }

    /** Requisito de Maestria: compara apenas o stat BASE (bônus de item não destrava a própria arma). */
    public boolean meets(StatType type, int requiredValue) {
        return get(type) >= requiredValue;
    }

    /**
     * Gasta pontos disponíveis em um stat.
     * @return quantos pontos foram efetivamente aplicados
     */
    public int spendPoints(StatType type, int amount) {
        int applied = Math.min(Math.max(amount, 0), pontosDisponiveis);
        applied = Math.min(applied, MAX_STAT - get(type));
        if (applied <= 0) return 0;
        pontosDisponiveis -= applied;
        set(type, get(type) + applied);
        return applied;
    }

    // ---------------------------------------------------------------- XP

    /**
     * Soma XP Global e converte em pontos a cada {@link #XP_PER_POINT}.
     * @return pontos ganhos nesta chamada
     */
    public int addXp(int amount) {
        if (amount <= 0) return 0;
        int before = xpGlobal / XP_PER_POINT;
        xpGlobal = (int) Math.min((long) xpGlobal + amount, Integer.MAX_VALUE);
        int gained = xpGlobal / XP_PER_POINT - before;
        pontosDisponiveis += gained;
        return gained;
    }

    /** Nível "de exibição", derivado do total de pontos já obtidos. */
    public int getLevel() {
        return 1 + xpGlobal / XP_PER_POINT;
    }

    // ---------------------------------------------------------------- mana

    public int getMaxMana() {
        return BASE_MANA + inteligencia * MANA_PER_INT;
    }

    public boolean tryConsumeMana(int cost) {
        if (manaAtual < cost) return false;
        manaAtual -= cost;
        return true;
    }

    public void regenMana(int amount) {
        manaAtual = Math.min(getMaxMana(), manaAtual + amount);
    }

    public void setMana(int value) {
        manaAtual = Mth.clamp(value, 0, getMaxMana());
    }

    // ---------------------------------------------------------------- getters/setters diretos

    public int getXpGlobal() { return xpGlobal; }
    public int getPontosDisponiveis() { return pontosDisponiveis; }
    public int getManaAtual() { return manaAtual; }
    public void setPontosDisponiveis(int pontos) { this.pontosDisponiveis = Math.max(0, pontos); }

    /** Volta tudo ao estado inicial. */
    public void reset() {
        copyFrom(new PlayerStatsAttachment());
    }

    public void copyFrom(PlayerStatsAttachment other) {
        this.talents.clear();
        this.talents.addAll(other.talents);
        this.xpGlobal = other.xpGlobal;
        this.pontosDisponiveis = other.pontosDisponiveis;
        this.forca = other.forca;
        this.agilidade = other.agilidade;
        this.inteligencia = other.inteligencia;
        this.vitalidade = other.vitalidade;
        this.manaAtual = other.manaAtual;
    }

    // ---------------------------------------------------------------- rede (usado por SyncStatsPayload)

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(xpGlobal);
        buf.writeVarInt(pontosDisponiveis);
        buf.writeVarInt(forca);
        buf.writeVarInt(agilidade);
        buf.writeVarInt(inteligencia);
        buf.writeVarInt(vitalidade);
        buf.writeVarInt(manaAtual);
        buf.writeCollection(talents.stream().sorted().toList(), FriendlyByteBuf::writeUtf);
    }

    public static PlayerStatsAttachment read(FriendlyByteBuf buf) {
        return new PlayerStatsAttachment(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readList(FriendlyByteBuf::readUtf));
    }
}
