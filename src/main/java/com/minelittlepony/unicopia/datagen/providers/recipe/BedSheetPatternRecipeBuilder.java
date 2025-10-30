package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Arrays;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.ExclusiveIngredient;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeGenerator;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;

public class BedSheetPatternRecipeBuilder {
    record PatternTemplate(int[] symbols, int[] uniqueSymbols, String[] pattern) {
        static final PatternTemplate ONE_COLOR = new PatternTemplate(new String[] { "###", "# #", " ##" });
        static final PatternTemplate TWO_COLOR = new PatternTemplate(new String[] { "#%#", "% %", " %#" });
        static final PatternTemplate THREE_COLOR = new PatternTemplate(new String[] { "cvc", "h h", " vc" });
        static final PatternTemplate FOUR_COLOR = new PatternTemplate(new String[] { "wgb", "p g", " pw" });
        static final PatternTemplate SEVEN_COLOR = new PatternTemplate(new String[] { "roy", "l b", " pg" });

        PatternTemplate(String[] pattern) {
            this(Arrays.stream(pattern).flatMapToInt(l -> l.chars()).filter(ch -> ch != ' ').toArray(), pattern);
        }

        PatternTemplate(int[] symbols, String[] pattern) {
            this(symbols, Arrays.stream(symbols).distinct().toArray(), pattern);
        }

        void offerWithoutConversion(RecipeGenerator generator, RegistryEntryLookup<Item> items,RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
            offerRecipe(generator, items, this, null, exporter, output, wool);
        }

        void offerTo(RecipeGenerator generator, RegistryEntryLookup<Item> items, RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
            Int2ObjectMap<ItemConvertible> symbolMap = new Int2ObjectOpenHashMap<>();
            offerRecipe(generator, items, this, symbolMap, exporter, output, wool);
            offerBedSheetConversionRecipe(generator, items, exporter, output, Arrays.stream(symbols).mapToObj(symbolMap::get));
        }
    }

    private static void offerRecipe(RecipeGenerator generator, RegistryEntryLookup<Item> items, PatternTemplate template,
            @Nullable Int2ObjectMap<ItemConvertible> symbolMap,
            RecipeExporter exporter,
            ItemConvertible output,
            ItemConvertible...wool) {
        ShapedRecipeJsonBuilder builder = ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output);
        for (int i = 0; i < template.uniqueSymbols().length; i++) {
            builder.input((char)template.uniqueSymbols()[i], wool[i]);
            if (symbolMap != null) {
                symbolMap.put(template.uniqueSymbols()[i], wool[i]);
            }
        }
        for (int i = 0; i < template.pattern().length; i++) {
            builder.pattern(template.pattern()[i]);
        }
        Arrays.asList(wool).stream().distinct().forEach(input -> {
            builder.criterion(RecipeGenerator.hasItem(input), generator.conditionsFromItem(input));
        });
        builder.group("bed_sheet").offerTo(exporter);
    }

    private static void offerBedSheetConversionRecipe(RecipeGenerator generator, RegistryEntryLookup<Item> items, RecipeExporter exporter, ItemConvertible output, Stream<ItemConvertible> wools) {
        var builder = ShapelessRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output, 2)
            .input(new ExclusiveIngredient(Ingredient.fromTag(items.getOrThrow(UTags.Items.WOOL_BED_SHEETS)), Ingredient.ofItems(output)).toVanilla())
            .criterion("has_bed_sheet", generator.conditionsFromTag(UTags.Items.WOOL_BED_SHEETS));
        wools.forEach(input -> {
            builder.input(input).criterion(RecipeGenerator.hasItem(input), generator.conditionsFromItem(input));
        });
        builder
            .group("bed_sheet_convert")
            .offerTo(exporter, RecipeGenerator.convertBetween(output, UItems.WHITE_BED_SHEETS));
    }

}
