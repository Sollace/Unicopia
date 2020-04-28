package com.minelittlepony.unicopia.recipe;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

/**
 * A predicate that tests for a specific enchantment on an input item.
 */
class EnchantmentPredicate implements Ingredient.Predicate {

    public static Ingredient.Predicate read(PacketByteBuf buf) {
        int level = buf.readInt();
        if (level == 0) {
            return EMPTY;
        }
        return new EnchantmentPredicate(Registry.ENCHANTMENT.get(buf.readIdentifier()), level);
    }

    static Ingredient.Predicate read(JsonObject json) {

        if (!json.has("enchantment")) {
            return EMPTY;
        }

        JsonElement e = json.get("enchantment");

        if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();
            Enchantment enchantment = Registry.ENCHANTMENT.get(new Identifier(o.get("id").getAsString()));
            int level = o.has("level") ? o.get("level").getAsInt() : 1;

            return new EnchantmentPredicate(enchantment, level);
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(new Identifier(e.getAsString()));
        return new EnchantmentPredicate(enchantment, 1);
    }

    private final int level;
    private final Enchantment enchantment;

    EnchantmentPredicate(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
        if (enchantment == null) {
            throw new JsonParseException("Invalid enchantment (null)");
        }
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        output.addEnchantment(enchantment, level);
        return output;
    }

    @Override
    public boolean matches(ItemStack stack, int materialMult) {
        return EnchantmentHelper.getLevel(enchantment, stack) >= level;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(level);
        buf.writeIdentifier(Registry.ENCHANTMENT.getId(enchantment));
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return Stream.empty();
    }
}
