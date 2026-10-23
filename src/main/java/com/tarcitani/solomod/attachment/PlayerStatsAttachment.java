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
 */
public class PlayerStatsAttachment {
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
            Codec.INT.optionalFieldOf("manaAtual", BASE_MANA).forGetter((PlayerStatsAttachment s) -> s.manaAtual)
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
    }

    public static PlayerStatsAttachment read(FriendlyByteBuf buf) {
        return new PlayerStatsAttachment(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }
}
