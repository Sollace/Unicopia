package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

/**
 * Requires only one of the sub-ingredients to match when matching.
 * Makes a random choice from a pool of alternatives when crafting.
 */
class ChoicePredicate implements Ingredient.Predicate {
    static Ingredient.Predicate read(JsonArray arr) {
        return new ChoicePredicate(Ingredient.many(arr));
    }

    private final List<Ingredient> options;

    ChoicePredicate(List<Ingredient> options) {
        this.options = options;
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        return options.get((int)(random.nextFloat() * options.size())).getStack(random);
    }

    @Override
    public boolean matches(ItemStack stack, int materialMult) {
        return options.stream().anyMatch(i -> i.matches(stack, materialMult));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(options.size());
        options.forEach(i -> i.write(buf));
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return options.stream().flatMap(Ingredient::getMatchingStacks).distinct();
    }
}
