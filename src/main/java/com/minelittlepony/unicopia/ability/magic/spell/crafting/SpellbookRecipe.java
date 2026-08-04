package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.URecipeBookCategories;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.mojang.datafixers.util.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.RecipeInput;

public interface SpellbookRecipe extends Recipe<SpellbookRecipe.Input> {
    @Override
    default RecipeType<? extends SpellbookRecipe> getType() {
        return URecipes.SPELLBOOK;
    }

    @Override
    default String getGroup() {
        return "unicopia:spellbook";
    }

    @Override
    default RecipeBookCategory getRecipeBookCategory() {
        return URecipeBookCategories.SPELLBOOK;
    }

    @Override
    default IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    int getPriority();

    interface CraftingTreeBuilder {
        void input(ItemStack...stacks);

        void input(Trait...traits);

        void input(Trait trait, float value);

        default void mystery(ItemStack...stacks) {
            input(stacks);
        }

        void result(ItemStack...stack);

        default void input(List<ItemStack> stacks) {
            input(stacks.toArray(ItemStack[]::new));
        }

        default void result(List<ItemStack> stacks) {
            result(stacks.toArray(ItemStack[]::new));
        }

        default void mystery(List<ItemStack> stacks) {
            mystery(stacks.toArray(ItemStack[]::new));
        }
    }

    public record Input(ItemStack stackToModify, List<Pair<Float, ItemStack>> stacks, SpellTraits traits, int gemSlotIndex) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return stacks.get(slot).getSecond();
        }

        public float getFactor(int slot) {
            return stacks.get(slot).getFirst();
        }

        @Override
        public int size() {
            return stacks.size();
        }

        public boolean hasIngredients() {
            for (int i = 0; i < size(); i++) {
                if (!getStackInSlot(i).isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        public ItemStack getFallbackStack() {
            if (stackToModify().isOf(UItems.GEMSTONE) || stackToModify().isOf(UItems.BOTCHED_GEM)) {
                return traits().applyTo(UItems.BOTCHED_GEM.getDefaultStack());
            }

            return ItemStack.EMPTY;
        }
    }
}
