package com.minelittlepony.unicopia.recipe;

import java.util.Random;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.container.SpellBookInventory;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

/**
 * Spellbook recipe accepting an item to enchant, a number of ingredients to use, an ingredient to compose the output.
 */
public class SpellBookRecipe extends AbstractShapelessPredicatedRecipe<CraftingInventory> {

    private static final Random RANDOM = new Random();

    protected final Ingredient input;

    public SpellBookRecipe(Identifier id, Ingredient input, Ingredient output, DefaultedList<Ingredient> ingredients) {
        super(id, output, ingredients);
        this.input = input;
    }

    @Override
    protected int getInputMultiplier(CraftingInventory inv, World worldIn) {
        ItemStack enchantedStack = ((SpellBookInventory)inv).getCraftResultMatrix().getInvStack(0);

        if (enchantedStack.isEmpty() || enchantedStack.getItem() == null) {
            return 0;
        }

        if (!input.matches(enchantedStack, enchantedStack.getCount())) {
            return 0;
        }

        return enchantedStack.getCount();
    }

    @Override
    public ItemStack getOutput() {
        return output.getStack(RANDOM);
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
                    Ingredient.many(json.get("ingredients").getAsJsonArray())
            );
        }

        @Override
        public SpellBookRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient input = Ingredient.read(buf);
            Ingredient output = Ingredient.read(buf);
            int length = buf.readInt();
            DefaultedList<Ingredient> ingredients = DefaultedList.copyOf(Ingredient.EMPTY);
            while (ingredients.size() < length) {
                ingredients.add(Ingredient.read(buf));
            }

            return new SpellBookRecipe(id, input, output, ingredients);
        }

        @Override
        public void write(PacketByteBuf buf, SpellBookRecipe recipe) {
            recipe.input.write(buf);
            recipe.output.write(buf);
            DefaultedList<Ingredient> ingredients = recipe.ingredients;
            buf.writeInt(ingredients.size());
            ingredients.forEach(i -> i.write(buf));
        }
    }
}