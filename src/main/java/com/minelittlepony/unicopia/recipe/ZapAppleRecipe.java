package com.minelittlepony.unicopia.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.Appearance;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;

public class ZapAppleRecipe extends ShapelessRecipe {
    public static final MapCodec<ZapAppleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ZapAppleRecipe::getGroup),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ZapAppleRecipe::getCategory),
            Registries.ITEM.getCodec().xmap(
                item -> Appearance.set(UItems.ZAP_APPLE.getDefaultStack(), item.getDefaultStack()),
                stack -> Appearance.upwrapAppearance(stack).getItem()
            ).fieldOf("appearance").forGetter(recipe -> recipe.result),
            URecipes.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(r -> r.ingredients)
        ).apply(instance, ZapAppleRecipe::new));
    public static final PacketCodec<RegistryByteBuf, ZapAppleRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ZapAppleRecipe::getGroup,
            CraftingRecipeCategory.PACKET_CODEC, ZapAppleRecipe::getCategory,
            ItemStack.PACKET_CODEC, recipe -> recipe.result,
            Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), r -> r.ingredients,
            ZapAppleRecipe::new
    );

    final ItemStack result;
    final List<Ingredient> ingredients;

    public ZapAppleRecipe(String group, CraftingRecipeCategory category, ItemStack output, List<Ingredient> input) {
        super(group, category, output, input);
        this.result = output;
        this.ingredients = input;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer)URecipes.ZAP_APPLE_SERIALIZER;
    }
}
