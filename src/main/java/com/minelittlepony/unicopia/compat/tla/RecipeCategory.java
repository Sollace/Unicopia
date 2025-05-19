package com.minelittlepony.unicopia.compat.tla;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.item.UItems;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.CategoryIcon;
import io.github.mattidragon.tlaapi.api.recipe.TlaCategory;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;

public record RecipeCategory(Identifier id, CategoryIcon icon, TlaIngredient stations, int width, int height) implements TlaCategory {
    static final Map<RecipeCategory, @Nullable BiConsumer<RecipeCategory, PluginContext>> REGISTRY = new HashMap<>();

    static final RecipeCategory SPELL_BOOK = register("spellbook", UItems.SPELLBOOK, 150, 75, SpellbookTlaRecipe::generate);
    static final RecipeCategory CLOUD_SHAPING = register("cloud_shaping", UBlocks.SHAPING_BENCH, 76, 18, CloudShapingTlaRecipe::generate);
    static final RecipeCategory GROWING = register("growing", Blocks.FARMLAND, 130, 85, StructureInteractionTlaRecipe::generateFarmingRecipes);
    static final RecipeCategory ALTAR = register("altar", Blocks.CRYING_OBSIDIAN, 130, 160, StructureInteractionTlaRecipe::generateAltarRecipes);

    static RecipeCategory register(String name, ItemConvertible item, int width, int height, @Nullable BiConsumer<RecipeCategory, PluginContext> recipeConstructor) {
        return register(new RecipeCategory(Unicopia.id(name), CategoryIcon.item(item), TlaStack.of(item).asIngredient(), width, height), recipeConstructor);
    }

    static RecipeCategory register(RecipeCategory category, @Nullable BiConsumer<RecipeCategory, PluginContext> recipeConstructor) {
        REGISTRY.put(category, recipeConstructor);
        return category;
    }

    static void bootstrap(PluginContext registry) {
        REGISTRY.forEach((category, recipeConstructor) -> {
            registry.addCategory(category);
            registry.addWorkstation(category, category.stations());
            try {
                if (recipeConstructor != null) {
                    recipeConstructor.accept(category, registry);
                }
            } catch (Throwable t) {
                Unicopia.LOGGER.fatal("Error occured whilst registering recipes for category " + category.getId(), t);
            }
        });
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public int getDisplayHeight() {
        return height;
    }

    @Override
    public int getDisplayWidth() {
        return width;
    }

    @Override
    public CategoryIcon getIcon() {
        return icon;
    }

    @Override
    public CategoryIcon getSimpleIcon() {
        return icon;
    }
}
