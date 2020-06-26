package com.minelittlepony.unicopia.world.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.world.recipe.Utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

/**
 * A predicate that tests for a specific enchantment on an input when matching.
 * Appends that enchantment to the output when crafting.
 */
class EnchantmentPredicate implements Predicate {
    private final int level;
    private final Enchantment enchantment;

    EnchantmentPredicate(Identifier id, int level) {
        this.enchantment = Utils.require(Registry.ENCHANTMENT.get(id), "Invalid enchantment tag '" + id + "'");
        this.level = level;
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        if (output.isEmpty()) {
            output = new ItemStack(Items.ENCHANTED_BOOK);
        }
        output.addEnchantment(enchantment, level);
        return output;
    }

    @Override
    public boolean matches(ItemStack stack, int materialMult) {
        return EnchantmentHelper.getLevel(enchantment, stack) >= level;
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return Stream.empty();
    }

    @Override
    public PredicateSerializer<?> getSerializer() {
        return PredicateSerializer.ENCHANTMENT;
    }

    static final class Serializer implements PredicateSerializer<EnchantmentPredicate>, PredicateSerializer.JsonReader {
        @Override
        public Predicate read(PacketByteBuf buf) {
            int level = buf.readInt();
            if (level == 0) {
                return Predicate.EMPTY;
            }
            return new EnchantmentPredicate(buf.readIdentifier(), level);
        }

        @Override
        public void write(PacketByteBuf buf, EnchantmentPredicate predicate) {
            buf.writeInt(predicate.level);
            buf.writeIdentifier(Registry.ENCHANTMENT.getId(predicate.enchantment));
        }

        @Override
        public Predicate read(JsonObject json) {
            if (!json.has("enchantment")) {
                return Predicate.EMPTY;
            }

            JsonElement e = json.get("enchantment");

            if (e.isJsonObject()) {
                JsonObject o = e.getAsJsonObject();
                return new EnchantmentPredicate(Utils.getIdentifier(o, "id"), Math.max(1, JsonHelper.getInt(o, "level", 1)));
            }

            return new EnchantmentPredicate(Utils.asIdentifier(e), 1);
        }
    }

}
