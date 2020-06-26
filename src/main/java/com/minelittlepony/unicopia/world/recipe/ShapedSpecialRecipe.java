package com.minelittlepony.unicopia.world.recipe;

import java.util.Random;

import com.google.gson.JsonObject;
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

public class ShapedSpecialRecipe extends SpecialCraftingRecipe {

    private static final Random RANDOM = new Random();

    private final Pattern pattern;
    private final PredicatedIngredient output;
    private final String group;

    public ShapedSpecialRecipe(Identifier id, String group, Pattern pattern, PredicatedIngredient output) {
        super(id);
        this.group = group;
        this.pattern = pattern;
        this.output = output;
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        return pattern.matches(inv);
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        return getOutput().copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= pattern.size();
    }

    @Override
    public ItemStack getOutput() {
        return output.getStack(RANDOM);
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.CRAFTING_SHAPED;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return false;
    }

    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        return PredicatedIngredient.preview(pattern.matrix);
    }

    public static class Serializer implements RecipeSerializer<ShapedSpecialRecipe> {
        @Override
        public ShapedSpecialRecipe read(Identifier id, JsonObject json) {
            return new ShapedSpecialRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    Pattern.read(json),
                    PredicatedIngredient.one(json.get("result"))
            );
        }

        @Override
        public ShapedSpecialRecipe read(Identifier id, PacketByteBuf buf) {
            return new ShapedSpecialRecipe(id,
                    buf.readString(32767),
                    Pattern.read(buf),
                    PredicatedIngredient.read(buf)
            );
        }

        @Override
        public void write(PacketByteBuf buf, ShapedSpecialRecipe recipe) {
            buf.writeString(recipe.group);
            recipe.pattern.write(buf);
            recipe.output.write(buf);
        }
    }
}
