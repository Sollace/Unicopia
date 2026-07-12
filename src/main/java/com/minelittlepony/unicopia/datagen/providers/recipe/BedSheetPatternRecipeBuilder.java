package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Arrays;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.ExclusiveIngredient;

import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
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

        void offerWithoutConversion(RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
            offerRecipe(this, exporter, output, wool);
        }

        void offerTo(RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
            offerRecipe(this, exporter, output, wool);
            offerBedSheetConversionRecipe(this, exporter, output, wool);
        }
    }

    private static void offerRecipe(PatternTemplate template,
            RecipeExporter exporter,
            ItemConvertible output,
            ItemConvertible...wool) {
        var builder = ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output);
        for (int i = 0; i < template.uniqueSymbols().length; i++) {
            builder.input((char)template.uniqueSymbols()[i], wool[i]);
        }
        for (int i = 0; i < template.pattern().length; i++) {
            builder.pattern(template.pattern()[i]);
        }
        Arrays.asList(wool).stream().distinct().forEach(input -> {
            builder.criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input));
        });
        builder.group("bed_sheet").offerTo(exporter);
    }

    private static void offerBedSheetConversionRecipe(PatternTemplate template, RecipeExporter exporter, ItemConvertible output, ItemConvertible...wool) {
        var builder = ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 2);

        for (int i = 0; i < template.uniqueSymbols().length; i++) {
            builder.input((char)template.uniqueSymbols()[i], wool[i]);
        }
        builder.input('X', new ExclusiveIngredient(Ingredient.fromTag(UTags.Items.WOOL_BED_SHEETS), Ingredient.ofItems(output)).toVanilla());
        for (int i = 0; i < template.pattern().length - 1; i++) {
            builder.pattern(template.pattern()[i]);
        }
        builder.pattern(template.pattern()[template.pattern().length - 1].replace(' ', 'X'));
        builder
            .criterion("has_bed_sheet", RecipeProvider.conditionsFromItem(output))
            .group("bed_sheet_convert")
            .offerTo(exporter, Registries.ITEM.getId(output.asItem()).withPath(RecipeProvider.convertBetween(output, UItems.WHITE_BED_SHEETS)));
    }

}
