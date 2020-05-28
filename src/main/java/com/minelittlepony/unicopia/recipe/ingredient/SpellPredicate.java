package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.magic.Spell;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.recipe.Utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

/**
 * A predicate that tests for a specific spell on an input when matching.
 * Appends that spell to the output when crafting.
 */
class SpellPredicate implements Predicate {
    private final Spell spell;

    SpellPredicate(String spell) {
        this.spell = Utils.require(SpellRegistry.instance().getSpellFromName(spell), "Unknown spell tag '" + spell + "'");
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        if (output.isEmpty()) {
            output = new ItemStack(UItems.GEM);
        }
        return SpellRegistry.instance().enchantStack(output, spell.getName());
    }

    @Override
    public boolean matches(ItemStack stack, int materialMult) {
        return SpellRegistry.getKeyFromStack(stack).equals(spell.getName());
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return Stream.empty();
    }

    @Override
    public PredicateSerializer<?> getSerializer() {
        return PredicateSerializer.SPELL;
    }

    static final class Serializer implements PredicateSerializer<SpellPredicate>, PredicateSerializer.JsonReader {
        @Override
        public Predicate read(PacketByteBuf buf) {
            int level = buf.readInt();
            if (level == 0) {
                return SpellPredicate.EMPTY;
            }
            return new SpellPredicate(buf.readString());
        }

        @Override
        public void write(PacketByteBuf buf, SpellPredicate predicate) {
            buf.writeString(predicate.spell.getName());
        }

        @Override
        public Predicate read(JsonObject json) {
            if (!json.has("spell")) {
                return SpellPredicate.EMPTY;
            }
            return new SpellPredicate(json.get("spell").getAsString());
        }
    }
}
