package com.minelittlepony.unicopia.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public class ZapAppleRecipe extends ShapelessRecipe {

    public ZapAppleRecipe(Identifier id, String group, ItemStack output, DefaultedList<Ingredient> input) {
        super(id, group, output, input);
    }

    public static class Serializer extends ShapelessRecipe.Serializer {
        @Override
        public ShapelessRecipe read(Identifier identifier, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");
            DefaultedList<Ingredient> ingredients = getIngredients(JsonHelper.getArray(json, "ingredients"));

            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (ingredients.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            }

            ItemStack stack = UItems.ZAP_APPLE.getStackForRender();
            stack.getOrCreateTag().putString("appearance", JsonHelper.getString(json, "appearance"));

            return new ZapAppleRecipe(identifier, group, stack, ingredients);
        }

        @Override
        public ShapelessRecipe read(Identifier identifier, PacketByteBuf input) {
            String group = input.readString(32767);

            DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(input.readVarInt(), Ingredient.EMPTY);

            for(int j = 0; j < ingredients.size(); ++j) {
                ingredients.set(j, Ingredient.fromPacket(input));
            }

            return new ZapAppleRecipe(identifier, group, input.readItemStack(), ingredients);
        }

        private static DefaultedList<Ingredient> getIngredients(JsonArray json) {
            DefaultedList<Ingredient> defaultedList = DefaultedList.of();

            for (int i = 0; i < json.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(json.get(i));
                if (!ingredient.isEmpty()) {
                    defaultedList.add(ingredient);
                }
            }

            return defaultedList;
        }
    }
}
