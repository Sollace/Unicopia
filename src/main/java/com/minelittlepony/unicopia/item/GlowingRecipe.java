package com.minelittlepony.unicopia.item;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class GlowingRecipe extends SpecialCraftingRecipe {

    public GlowingRecipe(Identifier id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        Pair<ItemStack, ItemStack> result = runMatch(inventory);

        return !result.getLeft().isEmpty() && !result.getRight().isEmpty();
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        Pair<ItemStack, ItemStack> pair = runMatch(inventory);

        ItemStack result = pair.getLeft().copy();
        ((GlowableItem)result.getItem()).setGlowing(result, pair.getRight().getItem() == Items.GLOWSTONE_DUST);

        return result;
    }

    private Pair<ItemStack, ItemStack> runMatch(CraftingInventory inventory) {
        ItemStack bangle = ItemStack.EMPTY;
        ItemStack dust = ItemStack.EMPTY;

        for(int i = 0; i < inventory.size(); i++) {
           ItemStack stack = inventory.getStack(i);

           if (!stack.isEmpty()) {
              if (stack.getItem() instanceof GlowableItem) {
                 if (!bangle.isEmpty()) {
                     return new Pair<>(bangle, dust);
                 }

                 bangle = stack;
              } else {
                 if (!(stack.getItem() == Items.GLOWSTONE_DUST || stack.getItem() == Items.INK_SAC)) {
                     return new Pair<>(bangle, dust);
                 }

                 dust = stack;


              }
           }
        }

        if (!bangle.isEmpty()) {
            if ((dust.getItem() == Items.GLOWSTONE_DUST) == ((GlowableItem)bangle.getItem()).isGlowing(bangle)) {
                return new Pair<>(ItemStack.EMPTY, ItemStack.EMPTY);
            }
        }

        return new Pair<>(bangle, dust);
    }

    @Override
    public boolean fits(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.GLOWING_SERIALIZER;
    }
}
