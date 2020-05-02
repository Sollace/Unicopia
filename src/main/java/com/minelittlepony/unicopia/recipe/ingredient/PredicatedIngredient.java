package com.minelittlepony.unicopia.recipe.ingredient;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.recipe.Utils;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Lazy;
import net.minecraft.util.PacketByteBuf;

@Immutable
public class PredicatedIngredient {
    public static final PredicatedIngredient EMPTY = new PredicatedIngredient(DefaultedList.of());

    private final DefaultedList<Predicate> predicates;

    private final Lazy<List<ItemStack>> matchingStacks;
    private final Lazy<net.minecraft.recipe.Ingredient> preview;

    PredicatedIngredient(DefaultedList<Predicate> predicates) {
        this.predicates = predicates;
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
        Utils.write(buf, predicates, Predicate::write);
    }

    public static PredicatedIngredient read(PacketByteBuf buf) {
        return new PredicatedIngredient(Utils.read(buf, Predicate.EMPTY, Predicate::read));
    }

    public static PredicatedIngredient one(JsonElement json) {

        if (json.isJsonArray()) {
            return new PredicatedIngredient(DefaultedList.copyOf(Predicate.EMPTY, ChoicePredicate.read(json.getAsJsonArray())));
        }

        JsonObject obj = json.getAsJsonObject();

        Predicate primary = StackPredicate.read(obj);
        Predicate secondary = TagPredicate.read(obj);
        if (primary != Predicate.EMPTY && secondary != Predicate.EMPTY) {
            throw new JsonParseException("Invalid ingredient. Cannot have both an item and a tag requirement.");
        }
        if (primary == secondary) {
            throw new JsonParseException("Invalid ingredient. Must have either an item or tag requirement.");
        }

        DefaultedList<Predicate> predicates = DefaultedList.of();
        predicates.add(primary);
        predicates.add(secondary);
        PredicateSerializer.JSON_READERS.stream()
            .map(reader -> reader.read(obj))
            .filter(i -> i != Predicate.EMPTY)
            .forEach(predicates::add);

        return new PredicatedIngredient(predicates);
    }

    public static DefaultedList<PredicatedIngredient> many(JsonArray arr) {

        if (arr.size() == 0) {
            throw new JsonParseException("Recipe cannot have 0 ingredients");
        }

        DefaultedList<PredicatedIngredient> ingredients = DefaultedList.ofSize(arr.size(), EMPTY);

        for (int i = 0; i < arr.size(); i++) {
            ingredients.set(i, one(arr.get(i)));
        }

        return ingredients;
    }

    public static DefaultedList<Ingredient> preview(DefaultedList<PredicatedIngredient> input) {
        return preview(input, DefaultedList.of());
    }

    public static DefaultedList<Ingredient> preview(DefaultedList<PredicatedIngredient> input, DefaultedList<Ingredient> output) {
        input.stream().map(PredicatedIngredient::getPreview).forEach(output::add);
        return output;
    }
}
