package com.minelittlepony.unicopia.enchanting;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;

public class SpecialRecipe extends AbstractSpecialRecipe {

    private final SpellIngredient output;

    public static IRecipe deserialize(JsonObject json) {
        return new SpecialRecipe(
                parseSingleIngredient(json.get("input").getAsJsonObject()),
                parseSingleIngredient(json.get("output").getAsJsonObject()),
                parseIngrediants(json)
        );
    }

    public SpecialRecipe(SpellIngredient input, SpellIngredient output, NonNullList<SpellIngredient> ingredients) {
        super(input, ingredients);

        this.output = output;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output.getStack();
    }
}