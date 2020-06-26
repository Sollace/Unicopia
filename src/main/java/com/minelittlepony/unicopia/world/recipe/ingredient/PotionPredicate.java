package com.minelittlepony.unicopia.world.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.world.recipe.Utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

/**
 * A predicate that tests for a specific enchantment on an input when matching.
 * Appends that enchantment to the output when crafting.
 */
class PotionPredicate implements Predicate {
    private final Potion potion;

    PotionPredicate(Identifier id, int level) {
        this.potion = Registry.POTION.get(id);
        if (potion == Potions.EMPTY) {
            throw new JsonParseException("Invalid potion tag '" + id + "'");
        }
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        if (output.isEmpty()) {
            output = new ItemStack(Items.POTION);
        }
        PotionUtil.setPotion(output, potion);

        return output;
    }

    @Override
    public boolean matches(ItemStack stack, int materialMult) {
        return PotionUtil.getPotion(stack) == potion;
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return Stream.empty();
    }

    @Override
    public PredicateSerializer<?> getSerializer() {
        return PredicateSerializer.ENCHANTMENT;
    }

    static final class Serializer implements PredicateSerializer<PotionPredicate>, PredicateSerializer.JsonReader {
        @Override
        public Predicate read(PacketByteBuf buf) {
            return new PotionPredicate(buf.readIdentifier(), 1);
        }

        @Override
        public void write(PacketByteBuf buf, PotionPredicate predicate) {
            buf.writeIdentifier(Registry.POTION.getId(predicate.potion));
        }

        @Override
        public Predicate read(JsonObject json) {
            if (!json.has("potion")) {
                return Predicate.EMPTY;
            }

            JsonElement e = json.get("potion");

            if (e.isJsonObject()) {
                JsonObject o = e.getAsJsonObject();
                return new PotionPredicate(Utils.getIdentifier(o, "id"), Math.max(1, JsonHelper.getInt(o, "level", 1)));
            }

            return new PotionPredicate(Utils.asIdentifier(e), 1);
        }
    }

}
