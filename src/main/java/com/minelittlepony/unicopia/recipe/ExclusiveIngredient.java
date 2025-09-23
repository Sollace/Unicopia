package com.minelittlepony.unicopia.recipe;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;

public record ExclusiveIngredient(Ingredient include, Ingredient exclude) implements CustomIngredient {
    public static final MapCodec<ExclusiveIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("include").forGetter(ExclusiveIngredient::include),
            Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("exclude").forGetter(ExclusiveIngredient::exclude)
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

    @Override
    public List<ItemStack> getMatchingStacks() {
        return Arrays.stream(include.getMatchingStacks()).filter(this::test).toList();
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
