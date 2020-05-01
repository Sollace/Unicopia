package com.minelittlepony.unicopia.recipe;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.recipe.ingredient.Ingredient;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.PacketByteBuf;

public class Utils {

    static final Random RANDOM = new Random();

    static <T> DefaultedList<T> read(PacketByteBuf buf, T def, Function<PacketByteBuf, T> reader) {
        DefaultedList<T> list = DefaultedList.ofSize(buf.readInt(), def);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, reader.apply(buf));
        }
        return list;
    }

    static <T> void write(PacketByteBuf buf, DefaultedList<T> list, BiConsumer<T, PacketByteBuf> writer) {
        buf.writeInt(list.size());
        list.forEach(i -> writer.accept(i, buf));
    }


    public static <C extends Inventory> boolean matchShapeless(DefaultedList<Ingredient> ingredients, C inv, int mult) {
        if (mult == 0) {
            return false;
        }

        List<Ingredient> toMatch = Lists.newArrayList(ingredients);

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

    private static boolean removeMatch(List<Ingredient> toMatch, ItemStack stack, int materialMult) {
        return toMatch.stream()
                .filter(s -> s.matches(stack, materialMult))
                .findFirst()
                .filter(toMatch::remove)
                .isPresent();
    }

}
