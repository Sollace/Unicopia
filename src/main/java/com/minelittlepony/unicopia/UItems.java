package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.ItemApple;

import come.minelittlepony.unicopia.forgebullshit.RegistryLockSpinner;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class UItems {
    public static final ItemApple apple = new ItemApple()
            .setSubTypes("apple", "green", "sweet", "rotten", "zap", "zap_cooked")
            .setTypeRarities(10, 20, 10, 30);

    static void registerItems() {
        RegistryLockSpinner.unlock(Item.REGISTRY);

        Item.REGISTRY.register(260, new ResourceLocation("minecraft", "apple"), apple);

        if (UClient.isClientSide()) {
            String[] variants = apple.getVariants();

            ResourceLocation app = new ResourceLocation("minecraft", "apple");

            for (int i = 0; i < variants.length; i++) {
                ModelLoader.setCustomModelResourceLocation(apple, i, new ModelResourceLocation(app, variants[i]));
            }
        }

        RegistryLockSpinner.lock(Item.REGISTRY);
    }

    static void registerFuels() {
        int zap = apple.getZapAppleMetadata();
        FurnaceRecipes.instance().addSmeltingRecipe(
                new ItemStack(UItems.apple, 1, zap),
                new ItemStack(UItems.apple, 1, zap + 1), 0.1F);
    }
}
