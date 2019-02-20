package com.minelittlepony.unicopia.enchanting;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface SpellIngredient {

    @Nullable
    static SpellIngredient parse(JsonElement json) {
        if (json.isJsonArray()) {
            return Compound.parse(json.getAsJsonArray());
        }

        return Single.parse(json.getAsJsonObject());
    }

    boolean matches(ItemStack other,  int materialMult);

    class Compound implements SpellIngredient {
        private final List<SpellIngredient> items;

        Compound(List<SpellIngredient> items) {
            this.items = items;
        }

        @Override
        public boolean matches(ItemStack other,  int materialMult) {
            return items.stream().anyMatch(item -> item.matches(other, materialMult));
        }

        @Nullable
        static SpellIngredient parse(JsonArray json) {
            if (json.size() > 0) {
                List<SpellIngredient> items = Lists.newArrayList();

                for (JsonElement j : json) {
                    SpellIngredient item = SpellIngredient.parse(j);

                    if (item != null) {
                        items.add(item);
                    }
                }

                if (!items.isEmpty()) {
                    return new Compound(items);
                }
            }

            return null;
        }
    }

    class Single implements SpellIngredient {

        private final ItemStack contained;
        private final boolean ignoreMeta;

        Single(ItemStack stack, boolean meta) {
            contained = stack;
            ignoreMeta = meta;
        }

        @Override
        public boolean matches(ItemStack other,  int materialMult) {
            if (other.isEmpty() != contained.isEmpty()) {
                return false;
            } else if (other.isEmpty()) {
                return true;
            }

            if (other.isEmpty()) {
                return false;
            }

            if (contained.getItem() == other.getItem()
                    && (ignoreMeta || other.getMetadata() == contained.getMetadata())
                    && ItemStack.areItemStackTagsEqual(contained, other)) {
                return other.getCount() >= (materialMult * contained.getCount());
            }

            return false;
        }

        @Nullable
        public static Single parse(JsonObject json) {
            Item item = json.has("item") ? Item.getByNameOrId(json.get("item").getAsString()) : null;

            if (item != null) {
                int metadata = Math.max(0, json.has("data") ? json.get("data").getAsInt() : 0);
                int size = Math.max(1, json.has("count") ? json.get("count").getAsInt() : 1);
                String spell = json.has("spell") ? json.get("spell").getAsString() : null;

                ItemStack stack = new ItemStack(item, size, metadata);

                if (spell != null) {
                    stack = SpellRegistry.instance().enchantStack(stack, spell);
                }

                return new Single(stack, !json.has("data"));
            }

            return null;
        }
    }
}
