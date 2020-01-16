package com.minelittlepony.unicopia.util.crafting;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.minelittlepony.util.AssetWalker;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class CraftingManager {

    private final Map<Identifier, Recipe<? super CraftingInventory>> REGISTRY = Maps.newHashMap();

    private final Map<String, Function<JsonObject, Recipe<? super CraftingInventory>>> JSON_PARSERS = Maps.newHashMap();

    @Nonnull
    private final Identifier crafting_id;

    private final AssetWalker assets;

    public CraftingManager(String modid, String resourcename) {
        this(new Identifier(modid, resourcename + "/recipes"));
    }

    public CraftingManager(@Nonnull Identifier id) {
        crafting_id = id;
        assets = new AssetWalker(id, this::handleJson);
    }

    protected void handleJson(Identifier id, JsonObject json) throws JsonParseException {
        REGISTRY.put(id, parseRecipeJson(json));
    }

    protected void registerRecipeTypes(Map<String, Function<JsonObject, Recipe<? super CraftingInventory>>> types) {
        types.put("crafting_shaped", ShapedRecipes::deserialize);
        types.put("crafting_shapeless", ShapelessRecipes::deserialize);
    }

    public void load() {
        JSON_PARSERS.clear();
        REGISTRY.clear();

        registerRecipeTypes(JSON_PARSERS);

        assets.walk();
    }

    protected Recipe<? super CraftingInventory> parseRecipeJson(JsonObject json) {
        String s = JsonHelper.getString(json, "type");

        if (!JSON_PARSERS.containsKey(s)) {
            throw new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
        }

        return JSON_PARSERS.get(s).apply(json);
    }

    @Deprecated
    @Nonnull
    public ItemStack findMatchingResult(CraftingInventory craftMatrix, World worldIn) {
        Recipe<? super CraftingInventory> recipe = findMatchingRecipe(craftMatrix, worldIn);

        if (recipe != null) {
            return recipe.getCraftingResult(craftMatrix);
        }

        return ItemStack.EMPTY;
    }

    @Deprecated
    @Nullable
    public Recipe<? super CraftingInventory> findMatchingRecipe(CraftingInventory craftMatrix, World world) {
        for (Recipe<? super CraftingInventory> irecipe : getRecipes()) {
            if (irecipe.matches(craftMatrix, world)) {
                return irecipe;
            }
        }

        return null;
    }

    @Deprecated
    public Collection<Recipe<? super CraftingInventory>> getRecipes() {
        if (REGISTRY.isEmpty()) {
            load();
        }
        return REGISTRY.values();
    }

    @Deprecated
    public DefaultedList<ItemStack> getRemainingItems(CraftingInventory craftMatrix, World worldIn) {
        Recipe<? super CraftingInventory> recipe = findMatchingRecipe(craftMatrix, worldIn);

        if (recipe != null) {
            return recipe.getRemainingItems(craftMatrix);
        }

        return cloneInventoryContents(craftMatrix);
    }

    public static DefaultedList<ItemStack> cloneInventoryContents(CraftingInventory craftMatrix) {
        DefaultedList<ItemStack> result = DefaultedList.ofSize(craftMatrix.getInvSize(), ItemStack.EMPTY);

        for (int i = 0; i < result.size(); ++i) {
            result.set(i, craftMatrix.getInvStack(i));
        }

        return result;
    }
}
