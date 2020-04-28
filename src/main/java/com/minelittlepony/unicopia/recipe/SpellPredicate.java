package com.minelittlepony.unicopia.recipe;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

class SpellPredicate implements Ingredient.Predicate {
    static Ingredient.Predicate read(PacketByteBuf buf) {
        int level = buf.readInt();
        if (level == 0) {
            return EMPTY;
        }
        return new SpellPredicate(buf.readString());
    }

    static Ingredient.Predicate read(JsonObject json) {
        if (!json.has("spell")) {
            return EMPTY;
        }

        return new SpellPredicate(json.get("spell").getAsString());
    }

    private final String spell;

    SpellPredicate(String spell) {
        this.spell = spell;
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        return SpellRegistry.instance().enchantStack(output, spell);
    }

    @Override
    public boolean matches(ItemStack stack, int materialMult) {
        return SpellRegistry.getKeyFromStack(stack).equals(spell);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(1);
        buf.writeString(spell);
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return Stream.empty();
    }
}
