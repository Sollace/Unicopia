package com.minelittlepony.unicopia.enchanting.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.SystemUtil;

public interface SpellIngredient {

    SpellIngredient EMPTY = new SingleSpellIngredient(ItemStack.EMPTY, false);
    Map<String, Serializer<? extends SpellIngredient>> SERIALIZERS = SystemUtil.consume(new HashMap<>(), map -> {
        map.put("compound", CompoundSpellIngredient.SERIALIZER);
        map.put("single", SingleSpellIngredient.SERIALIZER);
        map.put("affine", AffineIngredient.SERIALIZER);
    });

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
            String type = buff.readString();

            return SERIALIZERS.get(type).read(buff);
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
