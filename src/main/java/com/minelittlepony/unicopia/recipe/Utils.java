package com.minelittlepony.unicopia.recipe;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.recipe.ingredient.PredicatedIngredient;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;

public final class Utils {
    private Utils() {}

    public static final Random RANDOM = new Random();

    public static <T> DefaultedList<T> read(PacketByteBuf buf, T def, Function<PacketByteBuf, T> reader) {
        DefaultedList<T> list = DefaultedList.ofSize(buf.readInt(), def);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, reader.apply(buf));
        }
        return list;
    }

    public static <T> void write(PacketByteBuf buf, DefaultedList<T> list, BiConsumer<T, PacketByteBuf> writer) {
        buf.writeInt(list.size());
        list.forEach(i -> writer.accept(i, buf));
    }

    public static Identifier asIdentifier(JsonElement json) {
        return new Identifier(json.getAsString());
    }

    public static Identifier getIdentifier(JsonObject json, String property) {
        return new Identifier(JsonHelper.getString(json, property));
    }

    public static <T> T require(T reference, String errorMessage) {
        if (reference == null) {
            throw new JsonParseException(errorMessage);
        }
        return reference;
    }

    public static <C extends Inventory> boolean matchShapeless(DefaultedList<PredicatedIngredient> ingredients, C inv, int mult) {
        if (mult == 0) {
            return false;
        }

        List<PredicatedIngredient> toMatch = Lists.newArrayList(ingredients);

        for (int i = 0; i < inv.getInvSize(); i++) {
            ItemStack stack = inv.getInvStack(i);

            if (!stack.isEmpty()) {
                if (toMatch.isEmpty() || !removeMatch(toMatch, stack, mult)) {
                    return false;
                }
            }
        }

        return toMatch.isEmpty();
    }

    private static boolean removeMatch(List<PredicatedIngredient> toMatch, ItemStack stack, int materialMult) {
        return toMatch.stream()
                .filter(s -> s.matches(stack, materialMult))
                .findFirst()
                .filter(toMatch::remove)
                .isPresent();
    }

}
