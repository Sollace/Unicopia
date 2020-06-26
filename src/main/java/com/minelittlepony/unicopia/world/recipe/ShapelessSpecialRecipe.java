package com.minelittlepony.unicopia.world.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.world.recipe.ingredient.PredicatedIngredient;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;

public class ShapelessSpecialRecipe extends SpecialCraftingRecipe {

    private final String group;
    private final PredicatedIngredient output;

    private final DefaultedList<PredicatedIngredient> input;

    public ShapelessSpecialRecipe(Identifier id, String group, PredicatedIngredient output, DefaultedList<PredicatedIngredient> input) {
        super(id);
        this.group = group;
        this.output = output;
        this.input = input;

        if (input.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }
        if (input.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
        }
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        return Utils.matchShapeless(input, inv, 1);
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        return getOutput().copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= input.size();
    }

    @Override
    public ItemStack getOutput() {
        return output.getStack(Utils.RANDOM);
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.CRAFTING_SHAPELESS;
    }

    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        return PredicatedIngredient.preview(input);
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return false;
    }

    public static class Serializer implements RecipeSerializer<ShapelessSpecialRecipe> {
        @Override
        public ShapelessSpecialRecipe read(Identifier identifier, JsonObject json) {
            return new ShapelessSpecialRecipe(identifier,
                    JsonHelper.getString(json, "group", ""),
                    PredicatedIngredient.one(json.get("result")),
                    PredicatedIngredient.many(JsonHelper.getArray(json, "ingredients"))
            );
        }

        @Override
        public ShapelessSpecialRecipe read(Identifier identifier, PacketByteBuf buf) {
            return new ShapelessSpecialRecipe(identifier,
                    buf.readString(32767),
                    PredicatedIngredient.read(buf),
                    Utils.read(buf, PredicatedIngredient.EMPTY, PredicatedIngredient::read));
        }

        @Override
        public void write(PacketByteBuf buf, ShapelessSpecialRecipe recipe) {
            buf.writeString(recipe.group);
            recipe.output.write(buf);
            buf.writeVarInt(recipe.input.size());
            Utils.write(buf, recipe.input, PredicatedIngredient::write);
        }
    }
}
