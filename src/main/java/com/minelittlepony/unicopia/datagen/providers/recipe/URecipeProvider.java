package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement.Ingredients;
import com.minelittlepony.unicopia.datagen.providers.SpellbookChapterProvider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.Advancement.Builder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;

public class URecipeProvider extends FabricRecipeProvider {
    private final Map<RegistryKey<Recipe<?>>, RegistryEntry<Recipe<?>>> recipes = new HashMap<>();
    private final Map<RegistryKey<Recipe<?>>, Ingredients> ingredients = new HashMap<>();

    public URecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Unicopia Recipes";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends Recipe<?>> RegistryEntry<T> getOrThrow(RegistryKey<T> key) {
        return Objects.requireNonNull((RegistryEntry)recipes.get(key), "Recipe does not exist: " + key);
    }

    public Map<RegistryKey<Recipe<?>>, Ingredients> getIngredients() {
        return Map.copyOf(ingredients);
    }

    @Nullable
    public <T extends Recipe<?>> Ingredients getIngredientsOrThrow(RegistryKey<T> key) {
        return Objects.requireNonNull(ingredients.get(key), "Recipe does not provide spellbook ingredients: " + key);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(WrapperLookup registryLookup, RecipeExporter exporter) {
        return new URecipeGenerator(registryLookup, withCollecting(exporter), withConditions(exporter, ResourceConditions.allModsLoaded("farmersdelight")));
    }

    protected RecipeExporter withCollecting(RecipeExporter exporter) {
        return new SpellbookChapterProvider.MagicRecipeExporter() {
            @Override
            public void accept(RegistryKey<Recipe<?>> key, Recipe<?> recipe, AdvancementEntry advancement) {
                recipes.put(key, new RegistryEntryReference(key, recipe));
                exporter.accept(key, recipe, advancement);
            }

            @Override
            public Builder getAdvancementBuilder() {
                return exporter.getAdvancementBuilder();
            }

            @Override
            public void addRootAdvancement() {
                exporter.addRootAdvancement();
            }

            @Override
            public void acceptChapterIngredients(RegistryKey<Recipe<?>> key, Ingredients value) {
                ingredients.put(key, value);
            }
        };
    }

    class RegistryEntryReference extends RegistryEntry.Reference<Recipe<?>> {
        static final RegistryEntryOwner<Recipe<?>> OWNER = new RegistryEntryOwner<>() {

        };
        protected RegistryEntryReference(RegistryKey<Recipe<?>> registryKey, Recipe<?> value) {
            super(Type.STAND_ALONE, OWNER, registryKey, value);
        }

    }
}
