package com.minelittlepony.unicopia.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;

/**
 * An Ore Replacer.
 * Similar to what the OreDictionary does
 * except this is configurable and works for vanilla and modded recipes.
 */
@Deprecated
public class OreReplacer {

    /**
     * The vanilla remapper. Supports shaped recipes and shapeless recipes, and that's about it.
     */
    public static final IIngredientRemapper VANILLA = new IIngredientRemapper() {

        @Override
        public boolean canRemap(Recipe<?> recipe) {
            return recipe.getType() == RecipeType.CRAFTING;
        }

        @Override
        public int replaceIngredients(OreReplacer sender, Recipe<?> recipe) {
            DefaultedList<Ingredient> ingredients = recipe.getPreviewInputs();

            int replacements = 0;

            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);

                DefaultedList<ItemStack> newStacks = DefaultedList.of();

                boolean altered = false;

                ItemStack[] stacks = ingredient.getMatchingStacksClient();

                for (int k = 0; k < stacks.length; k++) {
                    ItemStack stack = stacks[k];

                    boolean found = !stack.isEmpty() && sender.replaceOre(stack, newStacks);

                    if (!found) {
                        newStacks.add(stack);
                    }

                    altered |= found;
                }

                if (altered) {
                    replacements++;
                    ingredients.set(i, Ingredient.ofStacks(newStacks.stream().distinct().toArray(ItemStack[]::new)));
                }
            }

            return replacements;
        }

    };

    private static final Logger log = LogManager.getLogger();

    private int replacements = 0;

    private List<IOre> ores = new ArrayList<>();

    private final List<IIngredientRemapper> remappers = Lists.newArrayList(VANILLA);

    /**
     * Adds additional recipe handlers.
     * By default only the vanilla crafting recipes are supported.
     */
    public OreReplacer registerRecipeTypeHandler(IIngredientRemapper... remappers) {
        this.remappers.addAll(Lists.newArrayList(remappers));

        return this;
    }

    /**
     * Adds all the specified ore conversions to be processed by this replacer.
     */
    public OreReplacer registerAll(IOre... ores) {
        this.ores.addAll(Lists.newArrayList(ores));

        return this;
    }

    public void done() {
        log.info("Searching for ore replacements...");
        /*Streams.stream(ForgeRegistries.RECIPES).forEach(recipe ->
            remappers.stream()
                .filter(remapper -> remapper.canRemap(recipe))
                .findFirst()
                .ifPresent(remapper -> remapper.replaceIngredients(this, recipe))
        );*/
        log.info("Replaced {} ingredients.", replacements);
    }

    public boolean replaceOre(ItemStack stack, DefaultedList<ItemStack> newStacks) {
        return ores.stream().filter(ore -> ore.matches(stack)).peek(ore ->
            ore.getSubItems(stack, newStacks)
        ).findFirst().isPresent();
    }

    public interface IIngredientRemapper {

        boolean canRemap(Recipe<?> recipe);

        int replaceIngredients(OreReplacer sender, Recipe<?> recipe);
    }

    @FunctionalInterface
    public interface IOre {
        boolean matches(ItemStack stack);

        default void getSubItems(ItemStack stack, DefaultedList<ItemStack> newStacks) {
            DefaultedList<ItemStack> newList = DefaultedList.of();

            stack.getItem().appendStacks(ItemGroup.SEARCH, newList);

            if (stack.hasTag()) {
                newList.forEach(i -> i.setTag(stack.getTag().copy()));
            }

            newStacks.addAll(newList);
        }
    }
}