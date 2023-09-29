package com.minelittlepony.unicopia.compat.emi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiShapedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

public class MagicalShapedEmiRecipe extends EmiCraftingRecipe {
    public MagicalShapedEmiRecipe(RecipeEntry<? extends CraftingRecipe> recipe, CustomisedSpellType<?> spellEffect, ItemStack output) {
        super(padIngredients(recipe, spellEffect), EmiStack.of(output),
                new Identifier(recipe.id().getNamespace(), recipe.id().getPath() + "/" + spellEffect.type().getId().getPath()), false);
        EmiShapedRecipe.setRemainders(input, recipe.value());
    }

    private static List<EmiIngredient> padIngredients(RecipeEntry<? extends CraftingRecipe> recipe, CustomisedSpellType<?> spellEffect) {
        List<EmiIngredient> list = recipe.value().getIngredients().stream()
                .map(ingredient -> remapIngredient(ingredient, spellEffect))
                .collect(Collectors.toList());
        while (list.size() < 9) {
            list.add(EmiStack.EMPTY);
        }
        return list;
    }

    private static EmiIngredient remapIngredient(Ingredient ingredient, CustomisedSpellType<?> spellEffect) {
        ItemStack[] stacks = ingredient.getMatchingStacks();

        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i].getItem() instanceof EnchantableItem e) {
                stacks = Arrays.copyOf(stacks, stacks.length);
                stacks[i] = EnchantableItem.enchant(stacks[i].copy(), spellEffect.type());
                return EmiIngredient.of(Arrays.stream(stacks).map(EmiStack::of).toList());
            }
        }

        return EmiIngredient.of(ingredient);
    }
}
