package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.recipe.Utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;

/**
 * Tests for whether a tag contains the input when matching.
 * Supplies a random item from the tag as the output when crafting.
 */
class TagPredicate implements Predicate {
    static Predicate read(JsonObject json) {
        if (!json.has("tag")) {
            return Predicate.EMPTY;
        }

        JsonElement e = json.get("tag");
        if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();
            return new TagPredicate(Utils.getIdentifier(o, "id"), Math.max(1, JsonHelper.getInt(o, "count", 1)));
        }

        return new TagPredicate(Utils.asIdentifier(e), 1);
    }

    private final Tag<Item> tag;
    private final int count;

    TagPredicate(Identifier res, int count) {
        tag = Utils.require(ItemTags.getContainer().get(res), "Unknown item tag '" + res + "'");
        this.count = count;
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

    @Override
    public PredicateSerializer<?> getSerializer() {
        return PredicateSerializer.TAG;
    }

    static final class Serializer implements PredicateSerializer<TagPredicate> {
        @Override
        public Predicate read(PacketByteBuf buf) {
            int count = buf.readInt();
            if (count == 0) {
                return Predicate.EMPTY;
            }
            return new TagPredicate(buf.readIdentifier(), count);
        }

        @Override
        public void write(PacketByteBuf buf, TagPredicate predicate) {
            buf.writeInt(predicate.count);
            buf.writeIdentifier(predicate.tag.getId());
        }
    }
}
