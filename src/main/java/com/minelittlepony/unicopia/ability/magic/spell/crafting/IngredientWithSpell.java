package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

public class IngredientWithSpell implements Predicate<ItemStack> {
    private static final IngredientWithSpell EMPTY = new IngredientWithSpell();
    private static final Predicate<Ingredient> INGREDIENT_IS_PRESENT = ((Predicate<Ingredient>)(Ingredient::isEmpty)).negate();
    public static final Codec<IngredientWithSpell> CODEC = Codec.of(new Encoder<IngredientWithSpell>() {
        @Override
        public <T> DataResult<T> encode(IngredientWithSpell input, DynamicOps<T> ops, T prefix) {
            throw new JsonParseException("cannot serialize this type");
        }
    }, new Decoder<IngredientWithSpell>() {
        @Override
        public <T> DataResult<Pair<IngredientWithSpell, T>> decode(DynamicOps<T> ops, T input) {
            // TODO: Doing codecs properly is an exercise left to the readers
            DataResult<Pair<Ingredient, T>> stack = Ingredient.ALLOW_EMPTY_CODEC.decode(ops, input);
            IngredientWithSpell ingredient = new IngredientWithSpell();
            ingredient.stack = stack.result().map(Pair::getFirst);
            ingredient.spell = Optional.ofNullable(((com.mojang.serialization.MapLike)input).get("Spell")).flatMap(unserializedSpell -> {
                return SpellType.REGISTRY.getCodec().parse((DynamicOps)ops, unserializedSpell).result();
            });

            return DataResult.success(new Pair<>(ingredient, null));
        }
    });
    public static final Codec<DefaultedList<IngredientWithSpell>> LIST_CODEC = CODEC.listOf().xmap(list -> {
        return DefaultedList.<IngredientWithSpell>copyOf(EMPTY, list.toArray(IngredientWithSpell[]::new));
    }, a -> a);

    private Optional<Ingredient> stack = Optional.empty();
    private Optional<SpellType<?>> spell = Optional.empty();

    @Nullable
    private ItemStack[] stacks;

    private IngredientWithSpell() {}

    @Override
    public boolean test(ItemStack t) {
        boolean stackMatch = stack.map(m -> m.test(t)).orElse(true);
        boolean spellMatch = spell.map(m -> EnchantableItem.getSpellKey(t).equals(m)).orElse(true);
        return stackMatch && spellMatch;
    }

    public ItemStack[] getMatchingStacks() {
        if (stacks == null) {
            stacks = stack.stream()
                    .map(Ingredient::getMatchingStacks)
                    .flatMap(Arrays::stream)
                    .map(stack -> spell.map(spell -> EnchantableItem.enchant(stack, spell)).orElse(stack))
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
}
