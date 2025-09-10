package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeGenerator;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;

public class BedSheetPatternRecipeBuilder {
    record PatternTemplate(List<Character> symbols, List<Character> uniqueSymbols, String[] pattern) {
        static final PatternTemplate ONE_COLOR = new PatternTemplate(new String[] { "###", "# #", " ##" });
        static final PatternTemplate TWO_COLOR = new PatternTemplate(new String[] { "#%#", "% %", " %#" });
        static final PatternTemplate THREE_COLOR = new PatternTemplate(new String[] { "cvc", "h h", " vc" });
        static final PatternTemplate FOUR_COLOR = new PatternTemplate(new String[] { "wgb", "p g", " pw" });
        static final PatternTemplate SEVEN_COLOR = new PatternTemplate(new String[] { "roy", "l b", " pg" });

        PatternTemplate(String[] pattern) {
            this(List.of(
                    pattern[1].charAt(0),
                    pattern[0].charAt(0),
                    pattern[0].charAt(1),
                    pattern[0].charAt(2),
                    pattern[1].charAt(2),
                    pattern[2].charAt(2),
                    pattern[2].charAt(1)
                ), pattern);
        }

        PatternTemplate(List<Character> symbols, String[] pattern) {
            this(symbols, symbols.stream().distinct().toList(), pattern);
        }

        void offerWithoutConversion(RecipeGenerator generator, RegistryEntryLookup<Item> items,RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
            offerRecipe(generator, items, this, null, exporter, output, wool);
        }

        void offerTo(RecipeGenerator generator, RegistryEntryLookup<Item> items, RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
            Map<Character, ItemConvertible> symbolMap = new HashMap<>();
            offerRecipe(generator, items, this, symbolMap, exporter, output, wool);
            offerBedSheetConversionRecipe(generator, items, exporter, output, symbols.stream().map(symbolMap::get));
        }
    }

    private static void offerRecipe(RecipeGenerator generator, RegistryEntryLookup<Item> items, PatternTemplate template, @Nullable Map<Character, ItemConvertible> symbolMap, RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
        ShapedRecipeJsonBuilder builder = ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output);
        for (int i = 0; i < template.uniqueSymbols().size(); i++) {
            builder.input(template.uniqueSymbols().get(i), wool[i]);
            if (symbolMap != null) {
                symbolMap.put(template.uniqueSymbols().get(i), wool[i]);
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

    private static void offerBedSheetConversionRecipe(RecipeGenerator generator, RegistryEntryLookup<Item> items,RecipeExporter exporter, ItemConvertible output, Stream<ItemConvertible> wools) {
        var builder = ShapelessRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output)
            .input(UTags.Items.WOOL_BED_SHEETS).criterion("has_bed_sheet", generator.conditionsFromTag(UTags.Items.WOOL_BED_SHEETS));
        wools.forEach(builder::input);
        builder
            .group("bed_sheet")
            .offerTo(exporter, RecipeGenerator.convertBetween(output, UItems.WHITE_BED_SHEETS));
    }

}
