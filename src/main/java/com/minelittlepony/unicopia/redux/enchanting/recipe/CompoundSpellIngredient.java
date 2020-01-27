package com.minelittlepony.unicopia.redux.enchanting.recipe;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

class CompoundSpellIngredient implements SpellIngredient {
    static final Serializer<CompoundSpellIngredient> SERIALIZER = new Serializer<CompoundSpellIngredient>() {
        @Override
        public CompoundSpellIngredient read(JsonElement json) {
            JsonArray arr = json.getAsJsonArray();

            if (arr.size() > 0) {
                List<SpellIngredient> items = Lists.newArrayList();

                for (JsonElement j : arr) {
                    SpellIngredient item = SpellIngredient.parse(j);

                    if (item != null) {
                        items.add(item);
                    }
                }

                if (!items.isEmpty()) {
                    return new CompoundSpellIngredient(items);
                }
            }

            throw new JsonParseException("Invalid spell ingredient (compound)");
        }

        @Override
        public CompoundSpellIngredient read(PacketByteBuf buff) {
            return null;
        }

        @Override
        public void write(PacketByteBuf buff, CompoundSpellIngredient recipe) {
        }
    };

    private final List<SpellIngredient> items;

    CompoundSpellIngredient(List<SpellIngredient> items) {
        this.items = items;
    }

    @Override
    public Stream<ItemStack> getStacks() {
        Stream<ItemStack> stacks = Lists.<ItemStack>newArrayList().stream();

        for (SpellIngredient i : items) {
            stacks = Streams.concat(stacks, i.getStacks());
        }

        return stacks.distinct();
    }

    @Override
    public ItemStack getStack() {
        return items.get((int)(Math.random() * items.size())).getStack();
    }

    @Override
    public boolean matches(ItemStack other,  int materialMult) {
        return items.stream().anyMatch(item -> item.matches(other, materialMult));
    }

    @Override
    public Serializer<?> getSerializer() {
        return SERIALIZER;
    }
}