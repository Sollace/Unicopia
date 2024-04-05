package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.item.GlowableItem;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Pair;

public class GlowingRecipe extends ItemCombinationRecipe {

    public GlowingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public final ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        Pair<ItemStack, ItemStack> pair = runMatch(inventory);

        ItemStack result = pair.getLeft().copy();

        ((GlowableItem)result.getItem()).setGlowing(result, pair.getRight().getItem() == Items.GLOWSTONE_DUST);

        return result;
    }

    @Override
    protected boolean isContainerItem(ItemStack stack) {
        return stack.getItem() instanceof GlowableItem;
    }

    @Override
    protected boolean isInsertItem(ItemStack stack) {
        return stack.getItem() == Items.GLOWSTONE_DUST || stack.getItem() == Items.INK_SAC;
    }

    @Override
    protected boolean isCombinationInvalid(ItemStack bangle, ItemStack dust) {
        return (dust.getItem() == Items.GLOWSTONE_DUST) == ((GlowableItem)bangle.getItem()).isGlowing(bangle);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.GLOWING_SERIALIZER;
    }
}
