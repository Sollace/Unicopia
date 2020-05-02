package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.minelittlepony.unicopia.recipe.Utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

/**
 * Tests for a specific item, stack size, and damage value when matching.
 * Presents that item as the output when crafting.
 */
class StackPredicate implements Predicate {
    static Predicate read(JsonObject json) {
        if (!json.has("item")) {
            return EMPTY;
        }

        JsonElement e = json.get("item");

        if (e.isJsonArray()) {
            return ChoicePredicate.read(e.getAsJsonArray());
        }

        int count = Math.max(1, JsonHelper.getInt(json, "count", 1));

        if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();

            Item item = o.has("item") ? Registry.ITEM.get(Utils.getIdentifier(o, "item")) : Items.AIR;
            return new StackPredicate(new ItemStack(item, count));
        }

        return new StackPredicate(new ItemStack(Registry.ITEM.get(Utils.asIdentifier(e)), count));
    }

    private final ItemStack stack;

    StackPredicate(ItemStack stack) {
        this.stack = stack;
        if (stack.isEmpty()) {
            throw new JsonSyntaxException("Unknown item tag");
        }
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        return output.isEmpty() ? stack.copy() : output;
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        if (!stack.isEmpty()) {
            DefaultedList<ItemStack> subItems = DefaultedList.of();
            stack.getItem().appendStacks(ItemGroup.SEARCH, subItems);

            return subItems.stream();
        }

        return Stream.of(stack);
    }

    @Override
    public boolean matches(ItemStack other, int materialMult) {
        if (other.isEmpty() != stack.isEmpty()) {
            return false;
        }

        if (other.isEmpty()) {
            return true;
        }

        return ItemStack.areItemsEqual(stack, other)
                && other.getCount() >= (materialMult * stack.getCount());
    }

    @Override
    public PredicateSerializer<?> getSerializer() {
        return PredicateSerializer.STACK;
    }

    static final class Serializer implements PredicateSerializer<StackPredicate> {
        @Override
        public Predicate read(PacketByteBuf buf) {
            return new StackPredicate(buf.readItemStack());
        }

        @Override
        public void write(PacketByteBuf buf, StackPredicate predicate) {
            buf.writeItemStack(predicate.stack);
        }

    }
}
