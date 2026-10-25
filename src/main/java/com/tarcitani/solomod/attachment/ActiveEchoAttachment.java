package com.tarcitani.solomod.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Ressonância de Alma Única: o companheiro ativo do jogador (um só por vez). Os outros vinculados ficam em espera.
 * {@code echo} é o UUID da Fauna Pacífica ativa, ou null quando não há nenhuma.
 */
public record ActiveEchoAttachment(@Nullable UUID echo) {
    public static final ActiveEchoAttachment NONE = new ActiveEchoAttachment(null);

    public static final Codec<ActiveEchoAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("echo").forGetter(a -> Optional.ofNullable(a.echo()))
    ).apply(instance, id -> new ActiveEchoAttachment(id.orElse(null))));
}
