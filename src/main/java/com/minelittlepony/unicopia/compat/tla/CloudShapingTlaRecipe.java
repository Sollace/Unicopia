package com.minelittlepony.unicopia.compat.tla;

import java.util.List;

import com.minelittlepony.unicopia.recipe.URecipes;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.Identifier;

public class CloudShapingTlaRecipe implements TlaRecipe {
    private final Identifier id;
    private final RecipeCategory category;
    private final TlaIngredient input;
    private final TlaStack output;

    static void generate(RecipeCategory category, PluginContext context) {
        context.addRecipeGenerator(URecipes.CLOUD_SHAPING, recipe -> new CloudShapingTlaRecipe(category, recipe));
    }

    public CloudShapingTlaRecipe(RecipeCategory category, RecipeEntry<StonecuttingRecipe> recipe) {
        this.id = recipe.id();
        this.category = category;
        input = TlaIngredient.ofIngredient(recipe.value().getIngredients().get(0));
        output = TlaStack.of(recipe.value().getResult(MinecraftClient.getInstance().world.getRegistryManager()));
    }

    @Override
    public RecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<TlaStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return List.of();
    }

    @Override
    public void buildGui(GuiBuilder builder) {
        builder.addArrow(26, 1, false);
        builder.addSlot(input, 0, 0).markInput();
        builder.addSlot(output, 58, 0).markOutput();
    }
}