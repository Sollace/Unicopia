package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class IngredientWithSpell implements CustomIngredient {
    private static final IngredientWithSpell EMPTY = new IngredientWithSpell(Optional.empty(), Optional.empty());

    public static final Codec<IngredientWithSpell> CODEC = CodecUtils.extend(Ingredient.CODEC, SpellType.REGISTRY.getCodec().fieldOf("spell")).xmap(
        pair -> new IngredientWithSpell(pair.getFirst(), pair.getSecond()),
        ingredient -> new Pair<>(ingredient.stack, ingredient.spell)
    );
    public static final PacketCodec<RegistryByteBuf, IngredientWithSpell> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(Ingredient.PACKET_CODEC), i -> i.stack,
            PacketCodecs.optional(Identifier.PACKET_CODEC.xmap(SpellType::getKey, SpellType::getId)), i -> i.spell,
            IngredientWithSpell::new
    );

    public static final Codec<DefaultedList<IngredientWithSpell>> LIST_CODEC = CODEC.listOf().xmap(
            list -> DefaultedList.<IngredientWithSpell>copyOf(EMPTY, list.toArray(IngredientWithSpell[]::new)),
            Function.identity()
    );

    private final Optional<Ingredient> stack;
    private final Optional<SpellType<?>> spell;

    private final Supplier<ItemStack[]> stacks;

    public static IngredientWithSpell mundane(ItemConvertible item) {
        return new IngredientWithSpell(Optional.of(Ingredient.ofItems(item)), Optional.empty());
    }

    public static IngredientWithSpell of(ItemConvertible base, SpellType<?> spell) {
        if (spell == SpellType.EMPTY_KEY) {
            return mundane(base);
        }
        return new IngredientWithSpell(Optional.of(Ingredient.ofItems(base)), Optional.of(spell));
    }

    private IngredientWithSpell(Optional<Ingredient> stack, Optional<SpellType<?>> spell) {
        this.stack = stack;
        this.spell = spell;
        stacks = Suppliers.memoize(() -> {
            return stack.stream()
                    .map(Ingredient::getMatchingItems)
                    .flatMap(List::stream)
                    .map(item -> item.value().getDefaultStack())
                    .map(s -> spell.map(p -> EnchantableItem.enchant(s, p)).orElse(s))
                    .toArray(ItemStack[]::new);
        });
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return UIngredients.ENCHANTED_ITEM;
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public boolean test(ItemStack t) {
        boolean stackMatch = stack.map(m -> m.test(t)).orElse(true);
        boolean spellMatch = spell.map(m -> EnchantableItem.getSpellKey(t).equals(m)).orElse(true);
        return stackMatch && spellMatch;
    }

    @Override
    public List<RegistryEntry<Item>> getMatchingItems() {
        return stack.map(i -> i.getMatchingItems()).orElseGet(List::of);
    }

    public ItemStack[] getMatchingStacks() {
        return stacks.get();
    }

    public boolean isEmpty() {
        return stack.isEmpty() && spell.isEmpty();
    }
}
