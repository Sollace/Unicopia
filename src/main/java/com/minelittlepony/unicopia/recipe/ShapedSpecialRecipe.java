package com.minelittlepony.unicopia.recipe;

import java.util.Random;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.recipe.ingredient.Ingredient;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

public class ShapedSpecialRecipe extends SpecialCraftingRecipe {

    private static final Random RANDOM = new Random();

    private final Pattern pattern;
    private final Ingredient output;
    private final String group;

    public ShapedSpecialRecipe(Identifier id, String group, Pattern pattern, Ingredient output) {
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
        return null;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return false;
    }

    @Override
    public DefaultedList<net.minecraft.recipe.Ingredient> getPreviewInputs() {
        return Ingredient.preview(pattern.matrix);
    }

    public static class Serializer implements RecipeSerializer<ShapedSpecialRecipe> {
        @Override
        public ShapedSpecialRecipe read(Identifier id, JsonObject json) {
            return new ShapedSpecialRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    Pattern.read(json),
                    Ingredient.one(json.get("result"))
            );
        }

        @Override
        public ShapedSpecialRecipe read(Identifier id, PacketByteBuf buf) {
            return new ShapedSpecialRecipe(id,
                    buf.readString(32767),
                    Pattern.read(buf),
                    Ingredient.read(buf)
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
