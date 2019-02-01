package com.minelittlepony.unicopia.forgebullshit;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * An Ore Replacer.
 * Similar to what the OreDictionary does
 * except this is configurable and works for vanilla and modded recipes.
 */
public class OreReplacer {

    /**
     * The vanilla remapper. Supports shaped recipes and shapeless recipes, and that's about it.
     */
    public static final IIngredientRemapper VANILLA = new IIngredientRemapper() {

        @Override
        public boolean canRemap(IRecipe recipe) {
            return recipe.getClass() == ShapedRecipes.class
                || recipe.getClass() == ShapelessRecipes.class;
        }

        @Override
        public int replaceIngredients(OreReplacer sender, IRecipe recipe) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();

            int replacements = 0;

            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);

                NonNullList<ItemStack> newStacks = NonNullList.create();

                boolean altered = false;

                ItemStack[] stacks = ingredient.getMatchingStacks();

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
                    ingredients.set(i, Ingredient.fromStacks(newStacks.stream().distinct().toArray(ItemStack[]::new)));
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
        Streams.stream(ForgeRegistries.RECIPES).forEach(recipe -> {
            remappers.stream()
                .filter(remapper -> remapper.canRemap(recipe))
                .findFirst()
                .ifPresent(remapper -> remapper.replaceIngredients(this, recipe));
        });
        log.info("Replaced {} ingredients.", replacements);
    }

    public boolean replaceOre(ItemStack stack, NonNullList<ItemStack> newStacks) {
        return ores.stream().filter(ore -> ore.matches(stack)).map(ore -> {
            ore.getSubItems(stack, newStacks);

            return ore;
        }).findFirst().isPresent();
    }

    public interface IIngredientRemapper {

        boolean canRemap(IRecipe recipe);

        int replaceIngredients(OreReplacer sender, IRecipe recipe);
    }

    @FunctionalInterface
    public interface IOre {
        boolean matches(ItemStack stack);

        default void getSubItems(ItemStack stack, NonNullList<ItemStack> newStacks) {
            NonNullList<ItemStack> newList = NonNullList.create();

            stack.getItem().getSubItems(CreativeTabs.SEARCH, newList);

            if (stack.hasTagCompound()) {
                newList.forEach(i -> i.setTagCompound(stack.getTagCompound().copy()));
            }

            newStacks.addAll(newList);
        }
    }
}