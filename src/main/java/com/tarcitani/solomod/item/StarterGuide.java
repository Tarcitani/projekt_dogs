package com.tarcitani.solomod.item;

import com.tarcitani.solomod.attachment.ModAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Livro inicial "Ecos do Despertar": oito páginas de um manuscrito anônimo sobre a Canção e os Ecos.
 * Apenas narrativa e suspense, sem tutorial, destinos, chefes ou revelações sobre a trama futura.
 * PlayerEvents tenta entregar no login e a cada dois segundos enquanto faltar espaço no inventário.
 * A marca serializada GUIDE_RECEIVED só é gravada após a inserção e sobrevive à morte;
 * trocar dimensão/reconectar não duplica o livro, inclusive em mundos anteriores à introdução do guia.
 * Páginas são componentes traduzíveis já resolvidos: o cliente escolhe pt-BR/en-US ao abrir o livro.
 * Cópias do antigo guia de 14 páginas são reconhecidas pelas chaves/autor e atualizadas no inventário,
 * preservando nome personalizado, geração e demais componentes; não reinicia a marca de entrega única.
 * isGuide reconhece ambas as edições para o modelo exclusivo de client/GuideBookModel;
 * o item continua sendo WRITTEN_BOOK, preservando leitura, cópia e uso em púlpitos vanilla.
 */
public final class StarterGuide {
    private static final int PAGE_COUNT = 8;
    private static final int LEGACY_PAGE_COUNT = 14;

    private StarterGuide() {}

    /** Identidade pelo conteúdo, não pelo nome visível: renomear o livro não remove sua capa. */
    public static boolean isGuide(ItemStack stack) {
        WrittenBookContent book = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (!stack.is(Items.WRITTEN_BOOK) || book == null || !book.title().raw().equals("Solo Mod")) return false;
        int count = book.pages().size();
        if (!((count == PAGE_COUNT && book.author().equals("???"))
                || (count == LEGACY_PAGE_COUNT && book.author().equals("Solo Mod")))) return false;
        for (int page = 0; page < count; page++) {
            if (!(book.pages().get(page).raw().getContents() instanceof TranslatableContents text)
                    || !text.getKey().equals("book.solomod.guide.page_" + (page + 1))) return false;
        }
        return true;
    }

    /** Somente o servidor entrega; inventário cheio adia a entrega sem jogar o livro no chão. */
    public static void giveIfNeeded(ServerPlayer player) {
        refreshLegacyCopies(player);
        if (player.getData(ModAttachments.GUIDE_RECEIVED) || player.getInventory().getFreeSlot() < 0) return;
        if (player.getInventory().add(create())) {
            player.setData(ModAttachments.GUIDE_RECEIVED, true);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            player.sendSystemMessage(Component.translatable("message.solomod.guide_received"));
        }
    }

    private static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.CUSTOM_NAME, Component.translatable("book.solomod.guide.title"));
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content(0));
        return book;
    }

    private static WrittenBookContent content(int generation) {
        List<Filterable<Component>> pages = IntStream.rangeClosed(1, PAGE_COUNT)
                .mapToObj(page -> Filterable.<Component>passThrough(
                        Component.translatable("book.solomod.guide.page_" + page)))
                .toList();
        return new WrittenBookContent(Filterable.passThrough("Solo Mod"), "???", generation, pages, true);
    }

    /** Atualiza só o antigo guia do mod, inclusive quando uma cópia é retirada de um baú depois do login. */
    private static void refreshLegacyCopies(ServerPlayer player) {
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            WrittenBookContent old = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (old == null || old.pages().size() != LEGACY_PAGE_COUNT || !isGuide(stack)) continue;
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, content(old.generation()));
            changed = true;
        }
        if (changed) {
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }
}
