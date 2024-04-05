package com.minelittlepony.unicopia.datagen.providers.recipe;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public class ComplexSpellcraftingRecipeJsonBuilder {
    private final RecipeSerializer<?> serializer;

    private final ItemConvertible material;

    public ComplexSpellcraftingRecipeJsonBuilder(RecipeSerializer<?> serializer, ItemConvertible material) {
        this.serializer = serializer;
        this.material = material;
    }

    public static ComplexSpellcraftingRecipeJsonBuilder create(RecipeSerializer<?> serializer, ItemConvertible material) {
        return new ComplexSpellcraftingRecipeJsonBuilder(serializer, material);
    }

    public void offerTo(RecipeExporter exporter, final String recipeId) {
        exporter.accept(new RecipeJsonProvider() {
            @Override
            public void serialize(JsonObject json) {
                json.add("material", Ingredient.ofItems(material).toJson(false));
            }

            @Override
            public Identifier id() {
                return Unicopia.id(recipeId);
            }

            @Override
            public RecipeSerializer<?> serializer() {
                return serializer;
            }

            @Nullable
            @Override
            public AdvancementEntry advancement() {
                return null;
            }
        });
    }
}
