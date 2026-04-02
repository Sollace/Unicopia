package com.minelittlepony.unicopia.recipe;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;

public record ExclusiveIngredient(Ingredient include, Ingredient exclude) implements CustomIngredient {
    public static final MapCodec<ExclusiveIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("include").forGetter(ExclusiveIngredient::include),
            Ingredient.CODEC.fieldOf("exclude").forGetter(ExclusiveIngredient::exclude)
    ).apply(i, ExclusiveIngredient::new));
    public static final PacketCodec<RegistryByteBuf, ExclusiveIngredient> PACKET_CODEC = PacketCodec.tuple(
            Ingredient.PACKET_CODEC, ExclusiveIngredient::include,
            Ingredient.PACKET_CODEC, ExclusiveIngredient::exclude,
            ExclusiveIngredient::new
    );

    @Override
    public boolean test(ItemStack stack) {
        return include.test(stack) && !exclude.test(stack);
    }

    @Deprecated
    @Override
    public Stream<RegistryEntry<Item>> getMatchingItems() {
        return include.getMatchingItems().filter(i -> test(i.value().getDefaultStack()));
    }

    @Override
    public boolean requiresTesting() {
        return false;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return URecipes.EXCLUSIVE_INGREDIENT_SERIALIZER;
    }

}
