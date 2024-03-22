package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Unicopia;
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

    public void offerTo(Consumer<RecipeJsonProvider> exporter, final String recipeId) {
        exporter.accept(new RecipeJsonProvider() {
            @Override
            public void serialize(JsonObject json) {
                json.add("material", Ingredient.ofItems(material).toJson());
            }

            @Override
            public Identifier getRecipeId() {
                return Unicopia.id(recipeId);
            }

            @Override
            public RecipeSerializer<?> getSerializer() {
                return serializer;
            }

            @Override
            public JsonObject toAdvancementJson() {
                return null;
            }

            @Override
            public Identifier getAdvancementId() {
                return new Identifier("");
            }
        });
    }
}
