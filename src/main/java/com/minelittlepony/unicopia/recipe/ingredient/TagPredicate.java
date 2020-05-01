package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

/**
 * Tests for whether a tag contains the input when matching.
 * Supplies a random item from the tag as the output when crafting.
 */
class TagPredicate implements Ingredient.Predicate {
    static Ingredient.Predicate read(PacketByteBuf buf) {
        int count = buf.readInt();
        if (count == 0) {
            return EMPTY;
        }
        return new TagPredicate(buf.readIdentifier(), count);
    }

    static Ingredient.Predicate read(JsonObject json) {
        if (!json.has("tag")) {
            return EMPTY;
        }

        JsonElement e = json.get("tag");
        if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();

            Identifier id = new Identifier(o.get("id").getAsString());
            int count = o.has("count") ? Math.max(1, o.get("count").getAsInt()) : 1;
            if (count == 0) {
                return EMPTY;
            }

            return new TagPredicate(id, count);
        }

        return new TagPredicate(new Identifier(json.getAsString()), 1);
    }

    private final Tag<Item> tag;
    private final int count;

    TagPredicate(Identifier res, int count) {
        tag = ItemTags.getContainer().get(res);
        this.count = count;
        if (tag == null) {
            throw new JsonSyntaxException("Unknown item tag '" + res + "'");
        }
    }

    @Override
    public ItemStack applyModifiers(ItemStack output, Random random) {
        return output.isEmpty() ? new ItemStack(tag.getRandom(random)) : output;
    }

    @Override
    public Stream<ItemStack> getMatchingStacks() {
        return tag.values().stream().map(ItemStack::new);
    }

    @Override
    public boolean matches(ItemStack other, int materialMult) {
        return !other.isEmpty()
                && tag.contains(other.getItem())
                && other.getCount() > (count * materialMult);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(count);
        buf.writeIdentifier(tag.getId());
    }
}
