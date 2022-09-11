package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.container.inventory.SpellbookInventory;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.URecipes;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;

public interface SpellbookRecipe extends Recipe<SpellbookInventory> {
    @Override
    default RecipeType<?> getType() {
        return URecipes.SPELLBOOK;
    }

    @Override
    default String getGroup() {
        return "unicopia:spellbook";
    }

    @Override
    default ItemStack createIcon() {
        return new ItemStack(UItems.SPELLBOOK);
    }

    void buildCraftingTree(CraftingTreeBuilder builder);

    int getPriority();

    interface CraftingTreeBuilder {
        void input(ItemStack...stack);

        void input(Trait trait, float value);

        void mystery(ItemStack...stacks);

        void result(ItemStack...stack);

        default void input(List<ItemStack> stacks) {
            input(stacks.toArray(ItemStack[]::new));
        }

        default void mystery(List<ItemStack> stacks) {
            mystery(stacks.toArray(ItemStack[]::new));
        }
    }
}
