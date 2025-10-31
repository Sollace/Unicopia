package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

public class IngredientWithSpell implements CustomIngredient {
    public static final Codec<IngredientWithSpell> CODEC = RecordCodecBuilder.create(i -> i.group(
            Ingredient.CODEC.optionalFieldOf("item").forGetter(o -> o.stack),
            SpellType.REGISTRY.getCodec().optionalFieldOf("spell").forGetter(o -> o.spell)
    ).apply(i, IngredientWithSpell::new));
    public static final Codec<IngredientWithSpell> FLEXIBLE_CODEC = Codec.xor(Ingredient.CODEC, CODEC).xmap(
            either -> Either.unwrap(either.mapLeft(i -> new IngredientWithSpell(Optional.of(i), Optional.empty()))),
            ingredient -> ingredient.stack.isEmpty() || ingredient.spell.isPresent() ? Either.right(ingredient) : Either.left(ingredient.stack.get())
    );
    public static final PacketCodec<RegistryByteBuf, IngredientWithSpell> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(Ingredient.PACKET_CODEC), i -> i.stack,
            PacketCodecs.optional(Identifier.PACKET_CODEC.xmap(SpellType::getKey, SpellType::getId)), i -> i.spell,
            IngredientWithSpell::new
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
