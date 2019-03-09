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

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CraftingManager {

    private final Map<ResourceLocation, IRecipe> REGISTRY = Maps.newHashMap();

    private final Map<String, Function<JsonObject, IRecipe>> JSON_PARSERS = Maps.newHashMap();

    @Nonnull
    private final ResourceLocation crafting_id;

    private final AssetWalker assets;

    public CraftingManager(String modid, String resourcename) {
        this(new ResourceLocation(modid, resourcename + "/recipes"));
    }

    public CraftingManager(@Nonnull ResourceLocation id) {
        crafting_id = id;
        assets = new AssetWalker(id, this::handleJson);
    }

    protected void handleJson(ResourceLocation id, JsonObject json) throws JsonParseException {
        REGISTRY.put(id, parseRecipeJson(json));
    }

    protected void registerRecipeTypes(Map<String, Function<JsonObject, IRecipe>> types) {
        types.put("crafting_shaped", ShapedRecipes::deserialize);
        types.put("crafting_shapeless", ShapelessRecipes::deserialize);
    }

    public void load() {
        JSON_PARSERS.clear();
        REGISTRY.clear();

        registerRecipeTypes(JSON_PARSERS);

        assets.walk();
    }

    protected IRecipe parseRecipeJson(JsonObject json) {
        String s = JsonUtils.getString(json, "type");

        if (!JSON_PARSERS.containsKey(s)) {
            throw new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
        }

        return JSON_PARSERS.get(s).apply(json);
    }

    @Nonnull
    public ItemStack findMatchingResult(InventoryCrafting craftMatrix, World worldIn) {
        IRecipe recipe = findMatchingRecipe(craftMatrix, worldIn);

        if (recipe != null) {
            return recipe.getCraftingResult(craftMatrix);
        }

        return ItemStack.EMPTY;
    }

    @Nullable
    public IRecipe findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn) {
        for (IRecipe irecipe : getRecipes()) {
            if (irecipe.matches(craftMatrix, worldIn)) {
                return irecipe;
            }
        }

        return null;
    }

    public Collection<IRecipe> getRecipes() {
        if (REGISTRY.isEmpty()) {
            load();
        }
        return REGISTRY.values();
    }

    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting craftMatrix, World worldIn) {
        IRecipe recipe = findMatchingRecipe(craftMatrix, worldIn);

        if (recipe != null) {
            return recipe.getRemainingItems(craftMatrix);
        }

        return cloneInventoryContents(craftMatrix);
    }

    public static NonNullList<ItemStack> cloneInventoryContents(InventoryCrafting craftMatrix) {
        NonNullList<ItemStack> result = NonNullList.withSize(craftMatrix.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < result.size(); ++i) {
            result.set(i, craftMatrix.getStackInSlot(i));
        }

        return result;
    }
}
