package com.minelittlepony.unicopia.container.spellbook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public record SpellbookRecipes(Map<RegistryKey<net.minecraft.recipe.Recipe<?>>, List<RecipeDisplay>> recipeDisplays) {
    public static final PacketCodec<RegistryByteBuf, SpellbookRecipes> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(HashMap::new, RegistryKey.createPacketCodec(RegistryKeys.RECIPE), RecipeDisplay.STREAM_CODEC.collect(PacketCodecs.toList())), SpellbookRecipes::recipeDisplays,
            SpellbookRecipes::new
    );

    public static SpellbookRecipes of(MinecraftServer server, Map<Identifier, SpellbookChapter> chapters) {
        var recipeManager = server.getRecipeManager();
        return new SpellbookRecipes(chapters.values().stream()
                .flatMap(chapter -> chapter.pages().stream())
                .flatMap(page -> page.elements().stream())
                .map(element -> element instanceof ChapterPageElement.Recipe r ? r.recipe() : null)
                .filter(Objects::nonNull)
                .flatMap(id -> recipeManager.get(id).stream())
                .collect(Collectors.toMap(recipe -> recipe.id(), recipe -> recipe.value().getDisplays())));
    }
}
