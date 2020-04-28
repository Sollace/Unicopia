package com.minelittlepony.unicopia.recipe;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Lazy;
import net.minecraft.util.PacketByteBuf;

@Immutable
class Ingredient {
    public static final Ingredient EMPTY = new Ingredient(Predicate.EMPTY, Predicate.EMPTY, Predicate.EMPTY, Predicate.EMPTY);

    private final Predicate stack;
    private final Predicate tag;
    private final Predicate spell;
    private final Predicate enchantment;

    private final Lazy<List<ItemStack>> matchingStacks;

    Ingredient(Predicate stack, Predicate tag, Predicate spell, Predicate enchantment) {
        this.stack = stack;
        this.tag = tag;
        this.spell = spell;
        this.enchantment = enchantment;
        this.matchingStacks = new Lazy<>(() -> {
            return Streams.concat(
                    stack.getMatchingStacks(),
                    tag.getMatchingStacks(),
                    spell.getMatchingStacks(),
                    enchantment.getMatchingStacks()
                ).filter(s -> matches(s, 1))
            .collect(Collectors.toList());
        });
    }

    public Stream<ItemStack> getMatchingStacks() {
        return matchingStacks.get().stream();
    }

    public ItemStack getStack(Random random) {
        ItemStack output = ItemStack.EMPTY.copy();
        stack.applyModifiers(output, random);
        tag.applyModifiers(output, random);
        spell.applyModifiers(output, random);
        enchantment.applyModifiers(output, random);
        return output;
    }

    public boolean matches(ItemStack other,  int materialMult) {
        return stack.matches(other, materialMult)
            && tag.matches(other, materialMult)
            && spell.matches(other, materialMult)
            && enchantment.matches(other, materialMult);
    }


    public void write(PacketByteBuf buf) {
        stack.write(buf);
        tag.write(buf);
        spell.write(buf);
        enchantment.write(buf);
    }

    public static Ingredient read(PacketByteBuf buf) {
        return new Ingredient(
                StackPredicate.read(buf),
                TagPredicate.read(buf),
                SpellPredicate.read(buf),
                EnchantmentPredicate.read(buf));
    }

    public static Ingredient one(JsonElement json) {

        if (json.isJsonArray()) {
            return new Ingredient(
                    ChoicePredicate.read(json.getAsJsonArray()),
                    Predicate.EMPTY,
                    Predicate.EMPTY,
                    Predicate.EMPTY);
        }

        JsonObject obj = json.getAsJsonObject();
        return new Ingredient(
                StackPredicate.read(obj),
                TagPredicate.read(obj),
                SpellPredicate.read(obj),
                EnchantmentPredicate.read(obj));
    }

    public static DefaultedList<Ingredient> many(JsonArray arr) {
        DefaultedList<Ingredient> ingredients = DefaultedList.copyOf(EMPTY);

        arr.forEach(i -> ingredients.add(one(i)));

        if (ingredients.isEmpty()) {
            throw new JsonParseException("Recipe cannot have 0 ingredients");
        }

        return ingredients;
    }

    public interface Predicate {
        Predicate EMPTY = new Predicate() {
            @Override
            public Stream<ItemStack> getMatchingStacks() {
                return Stream.empty();
            }
            @Override
            public boolean matches(ItemStack stack, int materialMult) {
                return true;
            }
        };

        Stream<ItemStack> getMatchingStacks();

        boolean matches(ItemStack stack, int materialMult);

        default ItemStack applyModifiers(ItemStack output, Random random) {
            return output;
        }

        default void write(PacketByteBuf buf) {
            buf.writeInt(0);
        }
    }
}
