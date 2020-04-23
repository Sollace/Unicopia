package com.minelittlepony.unicopia.enchanting.recipe;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class SpecialRecipe extends AbstractSpecialRecipe {
    private final SpellIngredient output;

    public SpecialRecipe(Identifier id, SpellIngredient input, SpellIngredient output, DefaultedList<SpellIngredient> ingredients) {
        super(id, input, ingredients);
        this.output = output;
    }

    @Override
    public ItemStack getOutput() {
        return output.getStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.SPECIAL_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return URecipes.SPELL_BOOK;
    }

    public static class Serializer implements RecipeSerializer<SpecialRecipe> {
        @Override
        public SpecialRecipe read(Identifier id, JsonObject json) {
            return new SpecialRecipe(id,
                    SpellIngredient.single(json.get("input").getAsJsonObject()),
                    SpellIngredient.single(json.get("output").getAsJsonObject()),
                    SpellIngredient.multiple(json)
            );
        }

        @Override
        public SpecialRecipe read(Identifier id, PacketByteBuf buf) {
            SpellIngredient input = SpellIngredient.SERIALIZER.read(buf);
            SpellIngredient output = SpellIngredient.SERIALIZER.read(buf);
            int length = buf.readInt();
            DefaultedList<SpellIngredient> ingredients = DefaultedList.copyOf(SpellIngredient.EMPTY);
            while (ingredients.size() < length) {
                ingredients.add(SpellIngredient.SERIALIZER.read(buf));
            }

            return new SpecialRecipe(id, input, output, ingredients);
        }

        @Override
        public void write(PacketByteBuf buf, SpecialRecipe recipe) {
            recipe.getSpellItem().write(buf);
            recipe.output.write(buf);
            DefaultedList<SpellIngredient> ingredients = recipe.getSpellIngredients();
            buf.writeInt(ingredients.size());
            ingredients.forEach(i -> i.write(buf));
        }
    }

}