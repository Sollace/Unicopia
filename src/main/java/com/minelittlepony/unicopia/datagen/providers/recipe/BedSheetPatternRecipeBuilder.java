package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Arrays;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.ExclusiveIngredient;

import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;

public class BedSheetPatternRecipeBuilder {
    record PatternTemplate(int[] symbols, int[] uniqueSymbols, String[] pattern) {
        static final PatternTemplate ONE_COLOR = new PatternTemplate("###", "# #", " ##");
        static final PatternTemplate TWO_COLOR = new PatternTemplate("#%#", "% %", " %#");
        static final PatternTemplate THREE_COLOR = new PatternTemplate("cvc", "h h", " vc");
        static final PatternTemplate FOUR_COLOR = new PatternTemplate("wgb", "p g", " pw");
        static final PatternTemplate SEVEN_COLOR = new PatternTemplate("roy", "l b", " pg");

        PatternTemplate(String...pattern) {
            this(Arrays.stream(pattern).flatMapToInt(l -> l.chars()).filter(ch -> ch != ' ').toArray(), pattern);
        }

        PatternTemplate(int[] symbols, String[] pattern) {
            this(symbols, Arrays.stream(symbols).distinct().toArray(), pattern);
        }

        void offerWithoutConversion(RecipeGenerator generator, RegistryEntryLookup<Item> items,RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
            offerRecipe(generator, items, this, exporter, output, wool);
        }

        void offerTo(RecipeGenerator generator, RegistryEntryLookup<Item> items, RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
            offerRecipe(generator, items, this, exporter, output, wool);
            offerBedSheetConversionRecipe(generator, items, this, exporter, output, wool);
        }
    }

    private static void offerRecipe(RecipeGenerator generator, RegistryEntryLookup<Item> items, PatternTemplate template,
            RecipeExporter exporter,
            ItemConvertible output,
            ItemConvertible...wool) {
        var builder = ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output);
        for (int i = 0; i < template.uniqueSymbols().length; i++) {
            builder.input((char)template.uniqueSymbols()[i], wool[i]);
        }
        for (int i = 0; i < template.pattern().length; i++) {
            builder.pattern(template.pattern()[i]);
        }
        Arrays.asList(wool).stream().distinct().forEach(input -> {
            builder.criterion(RecipeGenerator.hasItem(input), generator.conditionsFromItem(input));
        });
        builder.group("bed_sheet").offerTo(exporter);
    }

    private static void offerBedSheetConversionRecipe(RecipeGenerator generator, RegistryEntryLookup<Item> items, PatternTemplate template, RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
        var builder = ShapedRecipeJsonBuilder.create(items, RecipeCategory.DECORATIONS, output, 2);

        for (int i = 0; i < template.uniqueSymbols().length; i++) {
            builder.input((char)template.uniqueSymbols()[i], wool[i]);
        }
        builder.input('X', new ExclusiveIngredient(Ingredient.fromTag(items.getOrThrow(UTags.Items.WOOL_BED_SHEETS)), Ingredient.ofItems(output)).toVanilla());
        for (int i = 0; i < template.pattern().length - 1; i++) {
            builder.pattern(template.pattern()[i]);
        }
        builder.pattern(template.pattern()[template.pattern().length - 1].replace(' ', 'X'));
        builder
            .criterion("has_bed_sheet", generator.conditionsFromItem(output))
            .group("bed_sheet_convert")
            .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Registries.ITEM.getId(output.asItem()).withPath(RecipeGenerator.convertBetween(output, UItems.WHITE_BED_SHEETS))));
    }

}
