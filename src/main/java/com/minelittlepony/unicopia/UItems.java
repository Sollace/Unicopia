package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.ItemApple;

import come.minelittlepony.unicopia.forgebullshit.RegistryLockSpinner;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
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

        ResourceLocation res = new ResourceLocation("apple");

        Item.REGISTRY.register(Item.getIdFromItem(Items.APPLE), res, apple);

        if (UClient.isClientSide()) {
            String[] variants = apple.getVariants();

            for (int i = 0; i < variants.length; i++) {
                ModelLoader.setCustomModelResourceLocation(apple, i, new ModelResourceLocation("unicopia:" + variants[i]));
            }
           // ModelBakery.registerItemVariants(apple, NoNameSpacedResource.ofAllDomained("unicopia", apple.getVariants()));
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
