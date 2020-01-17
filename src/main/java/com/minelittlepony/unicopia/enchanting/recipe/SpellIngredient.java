package com.minelittlepony.unicopia.enchanting.recipe;

import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.PacketByteBuf;

public interface SpellIngredient {

    SpellIngredient EMPTY = new SingleSpellIngredient(ItemStack.EMPTY, false);

    Serializer<SpellIngredient> SERIALIZER = new Serializer<SpellIngredient>() {
        @Override
        public SpellIngredient read(JsonElement json) {
            if (json.isJsonArray()) {
                return CompoundSpellIngredient.SERIALIZER.read(json);
            }

            return SingleSpellIngredient.SERIALIZER.read(json);
        }

        @Override
        public SpellIngredient read(PacketByteBuf buff) {
            byte type = buff.readByte();

            if (type == 0) {
                return SingleSpellIngredient.SERIALIZER.read(buff);
            }
            return CompoundSpellIngredient.SERIALIZER.read(buff);
        }

        @Override
        public void write(PacketByteBuf buff, SpellIngredient recipe) {
            buff.writeByte(recipe instanceof SingleSpellIngredient ? 0 : 1);
            recipe.write(buff);
        }
    };

    static SpellIngredient single(JsonObject json) {
        return parse(json.get("item"));
    }

    static DefaultedList<SpellIngredient> multiple(JsonObject json) {
        DefaultedList<SpellIngredient> ingredients = DefaultedList.of();

        json.get("ingredients").getAsJsonArray().forEach(i -> ingredients.add(parse(i)));

        if (ingredients.isEmpty()) {
            throw new JsonParseException("Recipe cannot have 0 ingredients");
        }

        return ingredients;
    }

    static SpellIngredient parse(JsonElement json) {
        return SERIALIZER.read(json);
    }

    boolean matches(ItemStack other,  int materialMult);

    ItemStack getStack();

    Stream<ItemStack> getStacks();

    Serializer<?> getSerializer();

    @SuppressWarnings("unchecked")
    default void write(PacketByteBuf buff) {
        ((Serializer<SpellIngredient>)getSerializer()).write(buff, this);
    }

    interface Serializer<T> {
        T read(JsonElement json);

        T read(PacketByteBuf buff);

        void write(PacketByteBuf buff, T ingredient);
    }
}
