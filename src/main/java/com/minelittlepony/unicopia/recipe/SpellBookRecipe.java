package com.minelittlepony.unicopia.recipe;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.container.SpellBookInventory;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.ingredient.PredicatedIngredient;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
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

    private final Identifier id;
    private final PredicatedIngredient input;
    private final PredicatedIngredient output;
    private final DefaultedList<PredicatedIngredient> ingredients;


    public SpellBookRecipe(Identifier id, String group, PredicatedIngredient input, PredicatedIngredient output, DefaultedList<PredicatedIngredient> ingredients) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.ingredients = ingredients;
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
    public DefaultedList<Ingredient> getPreviewInputs() {
        return PredicatedIngredient.preview(ingredients, DefaultedList.copyOf(null, input.getPreview()));
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
                    JsonHelper.getString(json, "group", ""),
                    PredicatedIngredient.one(JsonHelper.getObject(json, "input")),
                    PredicatedIngredient.one(JsonHelper.getObject(json, "result")),
                    PredicatedIngredient.many(JsonHelper.getArray(json, "ingredients"))
            );
        }

        @Override
        public SpellBookRecipe read(Identifier id, PacketByteBuf buf) {
            return new SpellBookRecipe(id,
                    buf.readString(32767),
                    PredicatedIngredient.read(buf),
                    PredicatedIngredient.read(buf),
                    Utils.read(buf, PredicatedIngredient.EMPTY, PredicatedIngredient::read)
            );
        }

        @Override
        public void write(PacketByteBuf buf, SpellBookRecipe recipe) {
            recipe.input.write(buf);
            recipe.output.write(buf);
            Utils.write(buf, recipe.ingredients, PredicatedIngredient::write);
        }
    }
}