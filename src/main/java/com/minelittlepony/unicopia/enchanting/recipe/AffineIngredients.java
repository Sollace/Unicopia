package com.minelittlepony.unicopia.enchanting.recipe;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.util.AssetWalker;

import net.minecraft.util.Identifier;

public class AffineIngredients {

    private static final AffineIngredients instance = new AffineIngredients();

    public static AffineIngredients instance() {
        return instance;
    }

    private final Map<Identifier, SpellIngredient> storedIngredients = Maps.newHashMap();

    private final AssetWalker walker = new AssetWalker(new Identifier(Unicopia.MODID, "enchanting/ingredients"), this::handleJson);

    public void load() {
        storedIngredients.clear();

        walker.walk();
    }

    public SpellIngredient getIngredient(Identifier res) {
        SpellIngredient result = storedIngredients.get(res);

        if (result == null) {
            new RuntimeException("Ingredient `" + res + "` was not found.").printStackTrace();
            return SpellIngredient.EMPTY;
        }

        return result;
    }

    protected void handleJson(Identifier id, JsonObject json) throws JsonParseException {
        SpellIngredient ingredient = SpellIngredient.parse(json.get("items"));

        if (ingredient != null) {
            storedIngredients.put(id, ingredient);
        }
    }
}
