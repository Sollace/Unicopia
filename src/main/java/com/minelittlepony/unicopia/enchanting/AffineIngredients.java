package com.minelittlepony.unicopia.enchanting;

import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.util.AssetWalker;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class AffineIngredients {

    private static final AffineIngredients instance = new AffineIngredients();

    public static AffineIngredients instance() {
        return instance;
    }

    private final Map<ResourceLocation, SpellIngredient> storedIngredients = Maps.newHashMap();

    private final AssetWalker walker = new AssetWalker(new ResourceLocation(Unicopia.MODID, "enchanting/ingredients"), this::handleJson);

    public void load() {
        storedIngredients.clear();

        walker.walk();
    }

    public SpellIngredient getIngredient(ResourceLocation res) {
        return storedIngredients.get(res);
    }

    protected void handleJson(ResourceLocation id, JsonObject json) throws JsonParseException {
        SpellIngredient ingredient = SpellIngredient.parse(json.get("items"));

        if (ingredient != null) {
            storedIngredients.put(id, ingredient);
        }
    }

    static class AffineIngredient implements SpellIngredient {

        private final ResourceLocation res;

        AffineIngredient(ResourceLocation res) {
            this.res = res;
        }

        @Override
        public boolean matches(ItemStack other, int materialMult) {
            return instance().getIngredient(res).matches(other, materialMult);
        }

        @Nonnull
        static SpellIngredient parse(JsonObject json) {
            return new AffineIngredient(new ResourceLocation(json.get("id").getAsString()));
        }
    }
}
