package com.minelittlepony.unicopia.enchanting;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.inventory.gui.SpellBookInventory;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

public abstract class AbstractSpecialRecipe extends Impl<IRecipe> implements IRecipe {

    private final SpellIngredient spellitem;

    private final NonNullList<SpellIngredient> ingredients;

    static NonNullList<SpellIngredient> parseIngrediants(JsonObject json) {
        NonNullList<SpellIngredient> ingredients = NonNullList.create();

        for (JsonElement i : json.get("ingredients").getAsJsonArray()) {
            SpellIngredient ingredient = SpellIngredient.parse(i);

            if (ingredient != null) {
                ingredients.add(ingredient);
            }
        }

        if (ingredients.isEmpty()) {
            throw new JsonParseException("Recipe cannot have 0 ingredients");
        }

        return ingredients;
    }

    static SpellIngredient parseSingleIngredient(JsonObject json) {
        SpellIngredient result = SpellIngredient.parse(json.get("item"));

        if (result == null) {
            throw new JsonParseException("Recipe cannot have no enchantable input");
        }

        return result;
    }

    public AbstractSpecialRecipe(SpellIngredient spellitem, NonNullList<SpellIngredient> ingredients) {
        this.spellitem = spellitem;
        this.ingredients = ingredients;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack enchantedStack = ((SpellBookInventory)inv).getCraftResultMatrix().getStackInSlot(0);

        if (enchantedStack.isEmpty() || enchantedStack.getItem() == null) {
            return false;
        }

        if (!spellitem.matches(enchantedStack, enchantedStack.getCount())) {
            return false;
        }

        int materialMult = enchantedStack.getCount();

        ArrayList<SpellIngredient> toMatch = Lists.newArrayList(ingredients);

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty()) {
                if (toMatch.isEmpty() || !removeMatch(toMatch, stack, materialMult)) {
                    return false;
                }
            }
        }

        return toMatch.isEmpty();
    }

    private boolean removeMatch(List<SpellIngredient> toMatch, ItemStack stack, int materialMult) {
        return toMatch.stream()
                .filter(s -> s.matches(stack, materialMult))
                .findFirst()
                .filter(toMatch::remove)
                .isPresent();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return getRecipeOutput();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height < ingredients.size();
    }

    public SpellIngredient getSpellItem() {
        return spellitem;
    }

    public NonNullList<SpellIngredient> getSpellIngredients() {
        return ingredients;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return spellitem.getStack();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> remainers = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < remainers.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack != null && stack.getItem().hasContainerItem(stack)) {
                remainers.set(i, new ItemStack(stack.getItem().getContainerItem()));
            }
        }

        return remainers;
    }


}