package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.GemstoneItem;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public class IngredientWithSpell implements Predicate<ItemStack> {
    private static final Predicate<Ingredient> INGREDIENT_IS_PRESENT = ((Predicate<Ingredient>)(Ingredient::isEmpty)).negate();

    private Optional<Ingredient> stack = Optional.empty();
    private Optional<SpellType<?>> spell = Optional.empty();

    @Nullable
    private ItemStack[] stacks;

    private IngredientWithSpell() {}

    @Override
    public boolean test(ItemStack t) {
        boolean stackMatch = stack.map(m -> m.test(t)).orElse(true);
        boolean spellMatch = spell.map(m -> GemstoneItem.getSpellKey(t).equals(m)).orElse(true);
        return stackMatch && spellMatch;
    }

    public ItemStack[] getMatchingStacks() {
        if (stacks == null) {
            stacks = stack.stream()
                    .map(Ingredient::getMatchingStacks)
                    .flatMap(Arrays::stream)
                    .map(stack -> spell.map(spell -> GemstoneItem.enchant(stack, spell)).orElse(stack))
                    .toArray(ItemStack[]::new);
        }
        return stacks;
    }

    public boolean isEmpty() {
        return stack.filter(INGREDIENT_IS_PRESENT).isEmpty() && spell.isEmpty();
    }

    public void write(PacketByteBuf buf) {
        buf.writeOptional(stack, (b, i) -> i.write(b));
        buf.writeOptional(spell.map(SpellType::getId), PacketByteBuf::writeIdentifier);
    }

    public static IngredientWithSpell fromPacket(PacketByteBuf buf) {
        IngredientWithSpell ingredient = new IngredientWithSpell();
        ingredient.stack = buf.readOptional(Ingredient::fromPacket);
        ingredient.spell = buf.readOptional(PacketByteBuf::readIdentifier).flatMap(SpellType.REGISTRY::getOrEmpty);
        return ingredient;
    }

    public static IngredientWithSpell fromJson(JsonElement json) {
        IngredientWithSpell ingredient = new IngredientWithSpell();
        ingredient.stack = Optional.ofNullable(Ingredient.fromJson(json));
        if (json.isJsonObject() && json.getAsJsonObject().has("spell")) {
            ingredient.spell = SpellType.REGISTRY.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json.getAsJsonObject(), "spell")));
        }

        return ingredient;
    }

    public static DefaultedList<IngredientWithSpell> fromJson(JsonArray json) {
        DefaultedList<IngredientWithSpell> ingredients = DefaultedList.of();
        for (int i = 0; i < json.size(); i++) {
            IngredientWithSpell ingredient = fromJson(json.get(i));
            if (ingredient.isEmpty()) continue;
            ingredients.add(ingredient);
        }
        return ingredients;
    }
}
