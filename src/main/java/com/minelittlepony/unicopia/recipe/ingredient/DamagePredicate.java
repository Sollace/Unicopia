package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Lazy;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.Util;

/**
 * Tests for whether a tag contains the input when matching.
 * Supplies a random item from the tag as the output when crafting.
 */
class DamagePredicate implements Ingredient.Predicate {
    private static final Map<String, BiPredicate<Integer, Integer>> OPERATIONS = Util.make(new HashMap<>(), map -> {
        map.put("==", (a, b) -> a == b);
        map.put("!=", (a, b) -> a != b);
        map.put("<", (a, b) -> a < b);
        map.put("<=", (a, b) -> a <= b);
        map.put(">", (a, b) -> a > b);
        map.put("<=", (a, b) -> a <= b);
    });

    static Ingredient.Predicate read(PacketByteBuf buf) {
        int count = buf.readInt();
        if (count == 0) {
            return EMPTY;
        }
        return new DamagePredicate(buf.readInt(), buf.readString(32767));
    }

    static Ingredient.Predicate read(JsonObject json) {
        if (!json.has("damage")) {
            return EMPTY;
        }

        JsonObject o = JsonHelper.getObject(json, "damage");
        return new DamagePredicate(
                JsonHelper.getInt(o, "damage"),
                JsonHelper.getString(o, "op")
        );
    }

    private final String op;
    private final int damage;

    private final Lazy<BiPredicate<Integer, Integer>> operation;

    DamagePredicate(int damage, String op) {
        this.op = op.trim();
        this.damage = damage;
        this.operation = new Lazy<>(() -> OPERATIONS.get(op));
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        output.setDamage(damage);
        return output;
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return Stream.empty();
    }

    @Override
    public boolean matches(ItemStack other, int materialMult) {
        return !other.isEmpty() && operation.get().test(other.getDamage(), damage);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(1);
        buf.writeInt(damage);
        buf.writeString(op);
    }
}
