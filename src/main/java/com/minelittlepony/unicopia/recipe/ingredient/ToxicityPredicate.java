package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.toxin.Toxicity;
import com.minelittlepony.unicopia.recipe.Utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;

/**
 * A predicate that tests for a specific spell on an input when matching.
 * Appends that spell to the output when crafting.
 */
class ToxicityPredicate implements Predicate {
    private final Toxicity toxicity;

    ToxicityPredicate(String name) {
        this.toxicity = Utils.require(Toxicity.byName(name), "Unknown toxicity tag '" + name + "'");
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
    public Stream<ItemStack> getMatchingStacks() {
        return Stream.empty();
    }

    @Override
    public PredicateSerializer<?> getSerializer() {
        return PredicateSerializer.TOXICITY;
    }

    public static final class Serializer implements PredicateSerializer<ToxicityPredicate>, PredicateSerializer.JsonReader {

        @Override
        public Predicate read(PacketByteBuf buf) {
            return new ToxicityPredicate(Toxicity.values()[buf.readInt()].name());
        }

        @Override
        public void write(PacketByteBuf buf, ToxicityPredicate predicate) {
            buf.writeInt(predicate.toxicity.ordinal());
        }

        @Override
        public Predicate read(JsonObject json) {
            if (!json.has("toxicity")) {
                return Predicate.EMPTY;
            }

            return new ToxicityPredicate(JsonHelper.getString(json, "toxicity"));
        }

    }

}
