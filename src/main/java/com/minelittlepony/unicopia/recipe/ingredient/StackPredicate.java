package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

/**
 * Tests for a specific item, stack size, and damage value when matching.
 * Presents that item as the output when crafting.
 */
class StackPredicate implements Ingredient.Predicate {
    static Ingredient.Predicate read(PacketByteBuf buf) {
        int count = buf.readInt();
        if (count == 0) {
            return EMPTY;
        }

        if (count > 0) {
            return new StackPredicate(buf.readItemStack());
        }

        DefaultedList<Ingredient> items = DefaultedList.copyOf(Ingredient.EMPTY);
        while (items.size() < count) {
            items.add(Ingredient.read(buf));
        }

        return new ChoicePredicate(items);
    }

    static Ingredient.Predicate read(JsonObject json) {
        if (!json.has("item")) {
            return EMPTY;
        }

        JsonElement e = json.get("item");

        if (e.isJsonArray()) {
            return ChoicePredicate.read(e.getAsJsonArray());
        }

        if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();

            Item item = o.has("item") ? Registry.ITEM.get(new Identifier(o.get("item").getAsString())) : Items.AIR;
            int size = o.has("count") ? Math.max(1, o.get("count").getAsInt()) : 1;
            return new StackPredicate(new ItemStack(item, size));
        }

        return new StackPredicate(new ItemStack(Registry.ITEM.get(new Identifier(e.getAsString()))));
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
    public void write(PacketByteBuf buf) {
        buf.writeInt(-1);
        buf.writeItemStack(stack);
    }
}
