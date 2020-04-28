package com.minelittlepony.unicopia.recipe;

import java.util.List;
import com.google.common.collect.Lists;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Basic crafting recipe that uses our custom ingredients.
 */
public abstract class AbstractShapelessPredicatedRecipe<C extends Inventory> implements Recipe<C> {

    protected final Ingredient output;

    protected final DefaultedList<Ingredient> ingredients;

    private final Identifier id;

    public AbstractShapelessPredicatedRecipe(Identifier id, Ingredient output, DefaultedList<Ingredient> ingredients) {
        this.id = id;
        this.output = output;
        this.ingredients = ingredients;
    }

    protected abstract int getInputMultiplier(C inv, World worldIn);

    @Override
    public boolean matches(C inv, World worldIn) {
        int materialMult = getInputMultiplier(inv, worldIn);

        if (materialMult == 0) {
            return false;
        }

        List<Ingredient> toMatch = Lists.newArrayList(ingredients);

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

    private boolean removeMatch(List<Ingredient> toMatch, ItemStack stack, int materialMult) {
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
    public ItemStack craft(C inv) {
        return getOutput();
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height < ingredients.size();
    }

}