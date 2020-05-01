package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Lazy;
import net.minecraft.util.PacketByteBuf;

@Immutable
public class Ingredient {
    public static final Ingredient EMPTY = new Ingredient(Predicate.EMPTY, Predicate.EMPTY, Predicate.EMPTY, Predicate.EMPTY, Predicate.EMPTY);

    private final List<Predicate> predicates;

    private final Lazy<List<ItemStack>> matchingStacks;
    private final Lazy<net.minecraft.recipe.Ingredient> preview;

    Ingredient(Predicate... predicates) {
        this.predicates = Lists.newArrayList(predicates);
        this.matchingStacks = new Lazy<>(() -> {
            return this.predicates.stream()
                    .flatMap(Predicate::getMatchingStacks)
                    .filter(s -> matches(s, 1))
                    .collect(Collectors.toList());
        });
        this.preview = new Lazy<>(() -> {
            return net.minecraft.recipe.Ingredient.ofStacks(getMatchingStacks().toArray(ItemStack[]::new));
        });
    }

    public Stream<ItemStack> getMatchingStacks() {
        return matchingStacks.get().stream();
    }

    public net.minecraft.recipe.Ingredient getPreview() {
        return preview.get();
    }

    public ItemStack getStack(Random random) {
        ItemStack[] output = new ItemStack[] { ItemStack.EMPTY.copy() };

        predicates.forEach(p -> output[0] = p.applyModifiers(output[0], random));

        return output[0];
    }

    public boolean matches(ItemStack other,  int materialMult) {
        return predicates.stream().allMatch(p -> p.matches(other, materialMult));
    }

    public void write(PacketByteBuf buf) {
        predicates.forEach(p -> p.write(buf));
    }

    public static Ingredient read(PacketByteBuf buf) {
        return new Ingredient(
                StackPredicate.read(buf),
                TagPredicate.read(buf),
                SpellPredicate.read(buf),
                EnchantmentPredicate.read(buf),
                ToxicityPredicate.read(buf));
    }

    public static Ingredient one(JsonElement json) {

        if (json.isJsonArray()) {
            return new Ingredient(
                    ChoicePredicate.read(json.getAsJsonArray()),
                    Predicate.EMPTY,
                    Predicate.EMPTY,
                    Predicate.EMPTY,
                    Predicate.EMPTY);
        }

        JsonObject obj = json.getAsJsonObject();
        return new Ingredient(
                StackPredicate.read(obj),
                TagPredicate.read(obj),
                SpellPredicate.read(obj),
                EnchantmentPredicate.read(obj),
                ToxicityPredicate.read(obj));
    }

    public static DefaultedList<Ingredient> many(JsonArray arr) {
        DefaultedList<Ingredient> ingredients = DefaultedList.copyOf(EMPTY);

        arr.forEach(i -> ingredients.add(one(i)));

        if (ingredients.isEmpty()) {
            throw new JsonParseException("Recipe cannot have 0 ingredients");
        }

        return ingredients;
    }

    public static DefaultedList<net.minecraft.recipe.Ingredient> preview(DefaultedList<Ingredient> input) {
        return preview(input, DefaultedList.of());
    }

    public static DefaultedList<net.minecraft.recipe.Ingredient> preview(DefaultedList<Ingredient> input, DefaultedList<net.minecraft.recipe.Ingredient> output) {
        input.stream().map(Ingredient::getPreview).forEach(output::add);
        return output;
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
