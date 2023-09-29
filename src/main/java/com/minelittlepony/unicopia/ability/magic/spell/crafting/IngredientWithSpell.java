package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
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
    private static final IngredientWithSpell EMPTY = new IngredientWithSpell(Optional.empty(), Optional.empty());
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
            return DataResult.success(new Pair<>(
                new IngredientWithSpell(
                    Ingredient.ALLOW_EMPTY_CODEC.decode(ops, input).map(Pair::getFirst).result(),
                    ops.getMap(input).flatMap(maplike -> SpellType.REGISTRY.getCodec().parse(ops, maplike.get("spell"))).result()
                ), input)
            );
        }
    });
    public static final Codec<DefaultedList<IngredientWithSpell>> LIST_CODEC = CODEC.listOf().xmap(
            list -> DefaultedList.<IngredientWithSpell>copyOf(EMPTY, list.toArray(IngredientWithSpell[]::new)),
            Function.identity()
    );

    private final Optional<Ingredient> stack;
    private final Optional<SpellType<?>> spell;

    private final Supplier<ItemStack[]> stacks;

    private IngredientWithSpell(Optional<Ingredient> stack, Optional<SpellType<?>> spell) {
        this.stack = stack;
        this.spell = spell;
        stacks = Suppliers.memoize(() -> {
            return stack.stream()
                    .map(Ingredient::getMatchingStacks)
                    .flatMap(Arrays::stream)
                    .map(s -> spell.map(p -> EnchantableItem.enchant(s, p)).orElse(s))
                    .toArray(ItemStack[]::new);
        });
    }

    @Override
    public boolean test(ItemStack t) {
        boolean stackMatch = stack.map(m -> m.test(t)).orElse(true);
        boolean spellMatch = spell.map(m -> EnchantableItem.getSpellKey(t).equals(m)).orElse(true);
        return stackMatch && spellMatch;
    }

    public ItemStack[] getMatchingStacks() {
        return stacks.get();
    }

    public boolean isEmpty() {
        return stack.filter(INGREDIENT_IS_PRESENT).isEmpty() && spell.isEmpty();
    }

    public void write(PacketByteBuf buf) {
        buf.writeOptional(stack, (b, i) -> i.write(b));
        buf.writeOptional(spell.map(SpellType::getId), PacketByteBuf::writeIdentifier);
    }

    public static IngredientWithSpell fromPacket(PacketByteBuf buf) {
        return new IngredientWithSpell(
            buf.readOptional(Ingredient::fromPacket),
            buf.readOptional(PacketByteBuf::readIdentifier).flatMap(SpellType.REGISTRY::getOrEmpty)
        );
    }
}
