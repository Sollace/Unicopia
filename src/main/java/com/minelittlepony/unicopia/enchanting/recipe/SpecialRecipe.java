package com.minelittlepony.unicopia.enchanting.recipe;

import com.google.gson.JsonObject;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;

public class SpecialRecipe extends AbstractSpecialRecipe {

    private final SpellIngredient output;

    public static Recipe<CraftingInventory> deserialize(JsonObject json) {
        return new SpecialRecipe(null,
                SpellIngredient.single(json.get("input").getAsJsonObject()),
                SpellIngredient.single(json.get("output").getAsJsonObject()),
                SpellIngredient.multiple(json)
        );
    }

    public SpecialRecipe(Identifier id, SpellIngredient input, SpellIngredient output, DefaultedList<SpellIngredient> ingredients) {
        super(id, input, ingredients);

        this.output = output;
    }

    @Override
    public ItemStack getOutput() {
        return output.getStack();
    }

    @Override
    public Identifier getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RecipeType<?> getType() {
        // TODO Auto-generated method stub
        return null;
    }
}