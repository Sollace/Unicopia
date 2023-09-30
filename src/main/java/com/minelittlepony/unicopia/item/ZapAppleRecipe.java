package com.minelittlepony.unicopia.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.registry.Registries;

public class ZapAppleRecipe extends ShapelessRecipe {
    public ZapAppleRecipe(String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        super(group, category, output, input);
    }

    public static class Serializer implements RecipeSerializer<ZapAppleRecipe> {
        private static final Codec<ZapAppleRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ZapAppleRecipe::getGroup),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ZapAppleRecipe::getCategory),
            Registries.ITEM.getCodec().xmap(
                item -> UItems.ZAP_APPLE.setAppearance(UItems.ZAP_APPLE.getDefaultStack(), item.getDefaultStack()),
                stack -> UItems.ZAP_APPLE.getAppearance(stack)
            ).fieldOf("appearance").forGetter(recipe -> recipe.getResult(null)),
            URecipes.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(ZapAppleRecipe::getIngredients)
        ).apply(instance, ZapAppleRecipe::new));

        @Override
        public Codec<ZapAppleRecipe> codec() {
            return CODEC;
        }

        @Override
        public ZapAppleRecipe read(PacketByteBuf input) {
            String group = input.readString(32767);
            CraftingRecipeCategory category = input.readEnumConstant(CraftingRecipeCategory.class);

            DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(input.readVarInt(), Ingredient.EMPTY);

            for(int j = 0; j < ingredients.size(); ++j) {
                ingredients.set(j, Ingredient.fromPacket(input));
            }

            return new ZapAppleRecipe(group, category, input.readItemStack(), ingredients);
        }

        @Override
        public void write(PacketByteBuf buffer, ZapAppleRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.write(buffer);
            }
            buffer.writeItemStack(recipe.getResult(null));
        }
    }
}
