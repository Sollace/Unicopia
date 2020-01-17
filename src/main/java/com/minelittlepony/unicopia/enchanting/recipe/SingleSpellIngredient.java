package com.minelittlepony.unicopia.enchanting.recipe;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.enchanting.recipe.AffineIngredients.AffineIngredient;
import com.minelittlepony.unicopia.enchanting.recipe.SpellIngredient.Serializer;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

class SingleSpellIngredient implements SpellIngredient {
    static final Serializer<SpellIngredient> SERIALIZER = new Serializer<SpellIngredient>() {
        @Override
        public SpellIngredient read(JsonElement json) {
            JsonObject obj = json.getAsJsonObject();
            Item item = obj.has("item") ? Registry.ITEM.get(new Identifier(obj.get("item").getAsString())) : null;

            if (item != null) {
                int size = Math.max(1, obj.has("count") ? obj.get("count").getAsInt() : 1);
                String spell = obj.has("spell") ? obj.get("spell").getAsString() : null;

                ItemStack stack = new ItemStack(item, size);

                if (spell != null) {
                    stack = SpellRegistry.instance().enchantStack(stack, spell);
                }

                return new SingleSpellIngredient(stack, !(obj.has("spell") || obj.has("data")));
            }

            if (obj.has("id")) {
                return AffineIngredient.parse(obj);
            }

            throw new JsonParseException("Invalid spell ingredient (single)");
        }

        @Override
        public SingleSpellIngredient read(PacketByteBuf buff) {
            return null;
        }

        @Override
        public void write(PacketByteBuf buff, SpellIngredient recipe) {
        }
    };

    private final ItemStack contained;

    SingleSpellIngredient(ItemStack stack, boolean meta) {
        contained = stack;
    }

    @Override
    public Stream<ItemStack> getStacks() {
        if (!contained.isEmpty()) {
            DefaultedList<ItemStack> subItems = DefaultedList.of();
            contained.getItem().getSubItems(ItemGroup.SEARCH, subItems);

            return subItems.stream();
        }

        return Streams.stream(Optional.ofNullable(contained));
    }

    @Override
    public ItemStack getStack() {
        return contained;
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
                && ItemStack.areItemsEqual(contained, other)) {
            return other.getCount() >= (materialMult * contained.getCount());
        }

        return false;
    }

    @Override
    public Serializer<?> getSerializer() {
        return SERIALIZER;
    }
}