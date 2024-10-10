package com.minelittlepony.unicopia.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.registry.Registries;

public class ZapAppleRecipe extends ShapelessRecipe {
    public static final MapCodec<ZapAppleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ZapAppleRecipe::getGroup),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ZapAppleRecipe::getCategory),
            Registries.ITEM.getCodec().xmap(
                item -> Appearance.set(UItems.ZAP_APPLE.getDefaultStack(), item.getDefaultStack()),
                stack -> Appearance.upwrapAppearance(stack).getItem()
            ).fieldOf("appearance").forGetter(recipe -> recipe.getResult(null)),
            URecipes.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(ZapAppleRecipe::getIngredients)
        ).apply(instance, ZapAppleRecipe::new));
    public static final PacketCodec<RegistryByteBuf, ZapAppleRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ZapAppleRecipe::getGroup,
            CraftingRecipeCategory.PACKET_CODEC, ZapAppleRecipe::getCategory,
            ItemStack.PACKET_CODEC, recipe -> recipe.getResult(null),
            Ingredient.PACKET_CODEC.collect(PacketCodecs.toCollection(i -> DefaultedList.ofSize(i, Ingredient.EMPTY))), ZapAppleRecipe::getIngredients,
            ZapAppleRecipe::new
    );

    public ZapAppleRecipe(String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        super(group, category, output, input);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.ZAP_APPLE_SERIALIZER;
    }
}
