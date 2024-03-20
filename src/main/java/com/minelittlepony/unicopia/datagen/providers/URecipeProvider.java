package com.minelittlepony.unicopia.datagen.providers;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.ItemFamilies;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.data.server.recipe.VanillaRecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class URecipeProvider extends FabricRecipeProvider {
    public URecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        Arrays.stream(ItemFamilies.BASKETS).forEach(basket -> {
            offerBasketRecipe(exporter, basket, getMaterial(basket, "_basket", "_planks"));
        });

        offerBoatRecipe(exporter, UItems.PALM_BOAT, UBlocks.PALM_PLANKS);
        offerChestBoatRecipe(exporter, UItems.PALM_CHEST_BOAT, UItems.PALM_BOAT);
        offerHangingSignRecipe(exporter, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_PLANKS);
        offerPlanksRecipe(exporter, UBlocks.PALM_PLANKS, UTags.Items.PALM_LOGS, 4);
        offerPlanksRecipe(exporter, UBlocks.ZAP_PLANKS, UTags.Items.ZAP_LOGS, 4);
        offerPlanksRecipe(exporter, UBlocks.WAXED_ZAP_PLANKS, UTags.Items.WAXED_ZAP_LOGS, 4);
        offerBarkBlockRecipe(exporter, UBlocks.PALM_WOOD, UBlocks.PALM_LOG);
        offerBarkBlockRecipe(exporter, UBlocks.ZAP_WOOD, UBlocks.ZAP_LOG);
        offerBarkBlockRecipe(exporter, UBlocks.WAXED_ZAP_WOOD, UBlocks.WAXED_ZAP_LOG);

        generateFamily(exporter, UBlockFamilies.PALM);
        generateFamily(exporter, UBlockFamilies.ZAP);
        generateFamily(exporter, UBlockFamilies.WAXED_ZAP);
        offerWaxingRecipes(exporter);
    }

    private static Item getMaterial(Item output, String toStrip, String suffex) {
        Identifier id = Registries.ITEM.getId(output).withPath(p -> p.replace(toStrip, "") + suffex);
        return Registries.ITEM.getOrEmpty(id)
                .or(() -> Registries.ITEM.getOrEmpty(new Identifier(Identifier.DEFAULT_NAMESPACE, id.getPath())))
                .orElseThrow(() -> new NoSuchElementException("No item with id " + id));
    }

    public static void offerBasketRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, output)
            .input(Character.valueOf('#'), input)
            .pattern("# #")
            .pattern("# #")
            .pattern("###")
            .group("basket")
            .criterion(VanillaRecipeProvider.hasItem(input), VanillaRecipeProvider.conditionsFromItem(input))
            .offerTo(exporter);
    }

    public static void offerWaxingRecipes(Consumer<RecipeJsonProvider> exporter) {
        UBlockFamilies.WAXED_ZAP.getVariants().forEach((variant, output) -> {
            Block input = UBlockFamilies.ZAP.getVariant(variant);
            offerWaxingRecipe(exporter, output, input);
        });
        offerWaxingRecipe(exporter, UBlocks.WAXED_ZAP_PLANKS, UBlocks.ZAP_PLANKS);
        offerWaxingRecipe(exporter, UBlocks.WAXED_ZAP_WOOD, UBlocks.ZAP_WOOD);
    }

    public static void offerWaxingRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible output, ItemConvertible input) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output)
            .input(input)
            .input(Items.HONEYCOMB).group(RecipeProvider.getItemPath(output))
            .criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input))
            .offerTo(exporter, RecipeProvider.convertBetween(output, Items.HONEYCOMB));
    }
}
