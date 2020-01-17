package com.minelittlepony.unicopia.enchanting.recipe;

import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.util.AssetWalker;

import net.minecraft.item.ItemStack;
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

    static class AffineIngredient implements SpellIngredient {

        private final Identifier res;

        AffineIngredient(Identifier res) {
            this.res = res;
        }

        @Override
        public ItemStack getStack() {
            return instance().getIngredient(res).getStack();
        }

        @Override
        public Stream<ItemStack> getStacks() {
            return instance().getIngredient(res).getStacks();
        }

        @Override
        public boolean matches(ItemStack other, int materialMult) {
            return instance().getIngredient(res).matches(other, materialMult);
        }

        @Nonnull
        static SpellIngredient parse(JsonObject json) {
            return new AffineIngredient(new Identifier(json.get("id").getAsString()));
        }
    }
}
