package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.toxin.Toxicity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

/**
 * A predicate that tests for a specific spell on an input when matching.
 * Appends that spell to the output when crafting.
 */
class ToxicityPredicate implements Ingredient.Predicate {
    static Ingredient.Predicate read(PacketByteBuf buf) {
        int ordinal = buf.readInt();
        if (ordinal == 0) {
            return EMPTY;
        }
        return new ToxicityPredicate(Toxicity.values()[ordinal - 1]);
    }

    static Ingredient.Predicate read(JsonObject json) {
        if (!json.has("toxicity")) {
            return EMPTY;
        }

        return new ToxicityPredicate(Toxicity.byName(json.get("toxicity").getAsString()));
    }

    private final Toxicity toxicity;

    ToxicityPredicate(Toxicity toxicity) {
        this.toxicity = toxicity;
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        return toxicity.ontoStack(output);
    }

    @Override
    public boolean matches(ItemStack stack, int materialMult) {
        return Toxicity.fromStack(stack) == toxicity;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(toxicity.ordinal() + 1);
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return Stream.empty();
    }
}
