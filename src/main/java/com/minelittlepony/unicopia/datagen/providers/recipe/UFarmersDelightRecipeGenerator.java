package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.List;
import java.util.Map;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.FarmersDelightContent;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.block.Blocks;
import net.minecraft.data.family.BlockFamily.Variant;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class UFarmersDelightRecipeGenerator extends RecipeGenerator implements CraftingMaterialHelper {
    private final RegistryEntryLookup<Item> items;

    UFarmersDelightRecipeGenerator(WrapperLookup registries, RecipeExporter exporter) {
        super(registries, exporter);
        items = registries.getOrThrow(RegistryKeys.ITEM);
    }

    @Override
    public void generate() {
     // unwaxing
        UBlockFamilies.WAXED_ZAP.getVariants().forEach((variant, waxed) -> {
            if (variant == Variant.WALL_SIGN) return;
            var unwaxed = UBlockFamilies.ZAP.getVariant(variant);
            CuttingBoardRecipeJsonBuilder.create(unwaxed, "axe_strip")
                .input(waxed).criterion(hasItem(waxed), conditionsFromItem(waxed))
                .result(unwaxed)
                .result(Items.HONEYCOMB)
                .sound(SoundEvents.ITEM_AXE_WAX_OFF)
                .offerTo(exporter, recipeId(getItemPath(unwaxed) + "_from_waxed"));
        });
        List.of(UBlockFamilies.ZAP, UBlockFamilies.PALM).forEach(family -> {
            family.getVariants().forEach((variant, block) -> {
                if (variant == Variant.WALL_SIGN) return;
                CuttingBoardRecipeJsonBuilder.create(family.getBaseBlock(), "axe_strip")
                    .input(block).criterion(hasItem(block), conditionsFromItem(block))
                    .result(family.getBaseBlock())
                    .sound(SoundEvents.ITEM_AXE_STRIP)
                    .offerTo(exporter, recipeId(getItemPath(block)));
            });
        });
        CuttingBoardRecipeJsonBuilder.create(UBlocks.PALM_PLANKS, "axe_dig")
            .input(UBlocks.PALM_HANGING_SIGN).criterion(hasItem(UBlocks.PALM_HANGING_SIGN), conditionsFromItem(UBlocks.PALM_HANGING_SIGN))
            .sound(SoundEvents.ITEM_AXE_STRIP)
            .result(UBlocks.PALM_PLANKS)
            .offerTo(exporter);

        Map.of(
                UBlocks.PALM_LOG, UBlocks.STRIPPED_PALM_LOG,
                UBlocks.PALM_WOOD, UBlocks.STRIPPED_PALM_WOOD,
                UBlocks.ZAP_LOG, UBlocks.STRIPPED_ZAP_LOG,
                UBlocks.ZAP_WOOD, UBlocks.STRIPPED_ZAP_WOOD
        ).forEach((unstripped, stripped) -> {
            CuttingBoardRecipeJsonBuilder.create(stripped, "axe_strip")
                .input(unstripped).criterion(hasItem(unstripped), conditionsFromItem(unstripped))
                .sound(SoundEvents.ITEM_AXE_STRIP)
                .result(stripped)
                .result(Identifier.of("farmersdelight:tree_bark"))
                .offerTo(exporter, recipeId(convertBetween(stripped, unstripped)));
        });
        Map.of(
                UBlocks.GOLDEN_OAK_LOG, UBlocks.STRIPPED_GOLDEN_OAK_LOG,
                UBlocks.GOLDEN_OAK_WOOD, UBlocks.STRIPPED_GOLDEN_OAK_WOOD
        ).forEach((unstripped, stripped) -> {
            CuttingBoardRecipeJsonBuilder.create(stripped, "axe_strip")
                .input(unstripped).criterion(hasItem(unstripped), conditionsFromItem(unstripped))
                .sound(SoundEvents.ITEM_AXE_STRIP)
                .result(stripped)
                .result(Items.GOLD_NUGGET, 8)
                .offerTo(exporter, recipeId(convertBetween(stripped, unstripped)));
        });

        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, UItems.APPLE_PIE)
            .input(FarmersDelightContent.APPLE_PIE).criterion(hasItem(FarmersDelightContent.APPLE_PIE), conditionsFromItem(FarmersDelightContent.APPLE_PIE))
            .offerTo(exporter, recipeId("apple_pie_to_apple_pie"));
        ShapelessRecipeJsonBuilder.create(items, RecipeCategory.MISC, FarmersDelightContent.APPLE_PIE)
            .input(UItems.APPLE_PIE).criterion(hasItem(UItems.APPLE_PIE), conditionsFromItem(UItems.APPLE_PIE))
            .offerTo(exporter, recipeId("apple_pie_from_apple_pie"));

        CuttingBoardRecipeJsonBuilder.create(UItems.HAY_FRIES, "axe_dig")
                .input(Blocks.HAY_BLOCK).criterion(hasItem(Blocks.HAY_BLOCK), conditionsFromItem(Blocks.HAY_BLOCK))
                .sound(SoundEvents.ITEM_AXE_SCRAPE)
                .result(UItems.HAY_FRIES, 9)
                .offerTo(exporter);

        CuttingBoardRecipeJsonBuilder.create(UItems.APPLE_PIE_SLICE, Ingredient.fromTag(items.getOrThrow(UConventionalTags.Items.TOOL_KNIVES)))
            .input(UBlocks.APPLE_PIE).criterion(hasItem(UBlocks.APPLE_PIE), conditionsFromItem(UBlocks.APPLE_PIE))
            .sound(USounds.BLOCK_PIE_SLICE)
            .result(UItems.APPLE_PIE_SLICE, 4)
            .offerTo(exporter);
    }

    public void offerSpell(RecipeExporter exporter, ItemConvertible gemstone, SpellType<?> output, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .traits(traits)
            .offerTo(exporter);
    }

    public void offerSpellFromSpell(RecipeExporter exporter, ItemConvertible gemstone, SpellType<?> output, SpellType<?> input, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .input(gemstone, input)
            .traits(traits)
            .offerTo(exporter);
    }

    public void offerSpellFromTwoSpells(RecipeExporter exporter, ItemConvertible gemstone, SpellType<?> output, SpellType<?> input1, SpellType<?> input2, SpellTraits.Builder traits) {
        SpellcraftingRecipeJsonBuilder.create(RecipeCategory.MISC, gemstone, output)
            .input(gemstone, input1)
            .input(gemstone, input2)
            .traits(traits)
            .offerTo(exporter);
    }

    public static RegistryKey<Recipe<?>> recipeId(String key) {
        return RegistryKey.of(RegistryKeys.RECIPE, Unicopia.id(key));
    }

}
