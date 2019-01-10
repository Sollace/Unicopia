package com.minelittlepony.unicopia.util.crafting;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CraftingManager implements IResourceManagerReloadListener {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<ResourceLocation, IRecipe> REGISTRY = Maps.newHashMap();

    private final Map<String, Function<JsonObject, IRecipe>> JSON_PARSERS = Maps.newHashMap();

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    @Nonnull
    private final ResourceLocation crafting_id;

    public CraftingManager(String modid, String resourcename) {
        this(new ResourceLocation(modid, resourcename + "/recipes"));
    }

    public CraftingManager(@Nonnull ResourceLocation id) {
        crafting_id = id;

        load();
    }

    protected void registerRecipeTypes(Map<String, Function<JsonObject, IRecipe>> types) {
        types.put("crafting_shaped", ShapedRecipes::deserialize);
        types.put("crafting_shapeless", ShapelessRecipes::deserialize);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        load();
    }

    public void load() {
        JSON_PARSERS.clear();
        REGISTRY.clear();

        registerRecipeTypes(JSON_PARSERS);

        try {
            String loadLocation = "/assets/" + crafting_id.getNamespace() + "/" + crafting_id.getPath();

            URL url = CraftingManager.class.getResource(loadLocation);

            if (url == null) {
                LOGGER.error("Couldn't find .mcassetsroot");
                return;
            }

            URI uri = url.toURI();

            if ("file".equals(uri.getScheme())) {
                loadRecipesFrom(Paths.get(CraftingManager.class.getResource(loadLocation).toURI()));
            } else {
                if (!"jar".equals(uri.getScheme())) {
                    LOGGER.error("Unsupported scheme " + uri + " trying to list all recipes");

                    return;
                }

                try (FileSystem filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    loadRecipesFrom(filesystem.getPath(loadLocation));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Couldn't get a list of all recipe files", e);
        }
    }

    private void loadRecipesFrom(@Nullable Path path) throws IOException {
        if (path == null) {
            return;
        }

        Iterator<Path> iterator = Files.walk(path).iterator();

        while (iterator.hasNext()) {
            Path i = iterator.next();

            if ("json".equals(FilenameUtils.getExtension(i.toString()))) {
                ResourceLocation id = new ResourceLocation(FilenameUtils.removeExtension(path.relativize(i).toString()).replaceAll("\\\\", "/"));

                try(BufferedReader bufferedreader = Files.newBufferedReader(i)) {
                    REGISTRY.put(id, parseRecipeJson(JsonUtils.fromJson(gson, bufferedreader, JsonObject.class)));
                } catch (JsonParseException e) {
                    LOGGER.error("Parsing error loading recipe " + id, e);

                    return;
                } catch (IOException e) {
                    LOGGER.error("Couldn't read recipe " + id + " from " + i, e);

                    return;
                }
            }
        }
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
        load();

        for (IRecipe irecipe : REGISTRY.values()) {
            if (irecipe.matches(craftMatrix, worldIn)) {
                return irecipe;
            }
        }

        return null;
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
