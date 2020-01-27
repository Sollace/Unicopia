package com.minelittlepony.unicopia.enchanting.recipe;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.inventory.gui.SpellBookInventory;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public abstract class AbstractSpecialRecipe implements Recipe<CraftingInventory> {

    private final SpellIngredient spellitem;

    private final DefaultedList<SpellIngredient> ingredients;

    private final Identifier id;

    public AbstractSpecialRecipe(Identifier id, SpellIngredient spellitem, DefaultedList<SpellIngredient> ingredients) {
        this.id = id;
        this.spellitem = spellitem;
        this.ingredients = ingredients;
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack enchantedStack = ((SpellBookInventory)inv).getCraftResultMatrix().getInvStack(0);

        if (enchantedStack.isEmpty() || enchantedStack.getItem() == null) {
            return false;
        }

        if (!spellitem.matches(enchantedStack, enchantedStack.getCount())) {
            return false;
        }

        int materialMult = enchantedStack.getCount();

        ArrayList<SpellIngredient> toMatch = Lists.newArrayList(ingredients);

        for (int i = 0; i < inv.getInvSize(); i++) {
            ItemStack stack = inv.getInvStack(i);

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
    public ItemStack getRecipeKindIcon() {
       return new ItemStack(UItems.spell);
    }

    public SpellIngredient getSpellItem() {
        return spellitem;
    }

    public DefaultedList<SpellIngredient> getSpellIngredients() {
        return ingredients;
    }

    @Override
    public ItemStack getOutput() {
        return spellitem.getStack();
    }
}