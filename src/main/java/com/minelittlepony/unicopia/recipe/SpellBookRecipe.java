package com.minelittlepony.unicopia.recipe;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.container.SpellBookInventory;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.ingredient.Ingredient;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

/**
 * Spellbook recipe accepting an item to enchant, a number of ingredients to use, an ingredient to compose the output.
 */
public class SpellBookRecipe implements Recipe<CraftingInventory> {

    private final Ingredient input;

    private final Ingredient output;

    private final DefaultedList<Ingredient> ingredients;

    private final Identifier id;

    public SpellBookRecipe(Identifier id, Ingredient input, Ingredient output, DefaultedList<Ingredient> ingredients) {
        this.id = id;
        this.output = output;
        this.ingredients = ingredients;
        this.input = input;
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {

        ItemStack enchantedStack = ((SpellBookInventory)inv).getCraftResultMatrix().getInvStack(0);

        if (enchantedStack.isEmpty() || enchantedStack.getItem() == null) {
            return false;
        }

        if (!input.matches(enchantedStack, enchantedStack.getCount())) {
            return false;
        }

        return Utils.matchShapeless(ingredients, inv, enchantedStack.getCount());
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        return getOutput();
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height < ingredients.size();
    }

    @Override
    public DefaultedList<net.minecraft.recipe.Ingredient> getPreviewInputs() {
        return Ingredient.preview(ingredients, DefaultedList.copyOf(null, input.getPreview()));
    }

    @Override
    public ItemStack getOutput() {
        return output.getStack(Utils.RANDOM);
    }

    @Override
    public ItemStack getRecipeKindIcon() {
       return new ItemStack(UItems.GEM);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.ENCHANTING_SPELL_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return URecipes.SPELL_BOOK;
    }

    static class Serializer implements RecipeSerializer<SpellBookRecipe> {
        @Override
        public SpellBookRecipe read(Identifier id, JsonObject json) {
            return new SpellBookRecipe(id,
                    Ingredient.one(json.get("input")),
                    Ingredient.one(json.get("result")),
                    Ingredient.many(JsonHelper.getArray(json, "ingredients"))
            );
        }

        @Override
        public SpellBookRecipe read(Identifier id, PacketByteBuf buf) {
            return new SpellBookRecipe(id,
                    Ingredient.read(buf),
                    Ingredient.read(buf),
                    Utils.read(buf, Ingredient.EMPTY, Ingredient::read)
            );
        }

        @Override
        public void write(PacketByteBuf buf, SpellBookRecipe recipe) {
            recipe.input.write(buf);
            recipe.output.write(buf);
            Utils.write(buf, recipe.ingredients, Ingredient::write);
        }
    }
}