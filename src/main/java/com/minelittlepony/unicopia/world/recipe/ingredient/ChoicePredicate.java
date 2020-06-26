package com.minelittlepony.unicopia.world.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.minelittlepony.unicopia.world.recipe.Utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.network.PacketByteBuf;

/**
 * Requires only one of the sub-ingredients to match when matching.
 * Makes a random choice from a pool of alternatives when crafting.
 */
class ChoicePredicate implements Predicate {
    static Predicate read(JsonArray arr) {
        return new ChoicePredicate(PredicatedIngredient.many(arr));
    }

    private final DefaultedList<PredicatedIngredient> options;

    ChoicePredicate(DefaultedList<PredicatedIngredient> options) {
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
    public Stream<ItemStack> getMatchingStacks() {
        return options.stream().flatMap(PredicatedIngredient::getMatchingStacks).distinct();
    }

    @Override
    public PredicateSerializer<?> getSerializer() {
        return PredicateSerializer.CHOICE;
    }

    static final class Serializer implements PredicateSerializer<ChoicePredicate> {
        @Override
        public Predicate read(PacketByteBuf buf) {
            return new ChoicePredicate(Utils.read(buf, PredicatedIngredient.EMPTY, PredicatedIngredient::read));
        }

        @Override
        public void write(PacketByteBuf buf, ChoicePredicate predicate) {
            Utils.write(buf, predicate.options, PredicatedIngredient::write);
        }
    }
}
