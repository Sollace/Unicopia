package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.registry.Registry;

public final class SpellTraits {
    public static final SpellTraits EMPTY = new SpellTraits(Map.of());

    private final Map<Trait, Float> traits;

    SpellTraits(Map<Trait, Float> traits) {
        this.traits = traits;
    }

    public boolean isEmpty() {
        return traits.isEmpty();
    }

    public Set<Map.Entry<Trait, Float>> entries() {
        return traits.entrySet();
    }

    public float getAmount(Trait trait) {
        return traits.getOrDefault(trait, 0F);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        traits.forEach((key, value) -> nbt.putFloat(key.name(), value));
        return nbt;
    }

    public static Optional<SpellTraits> of(Collection<ItemStack> stacks) {
        return fromEntries(stacks.stream().flatMap(a -> of(a).entries().stream()));
    }

    public static SpellTraits of(ItemStack stack) {
        return getEmbeddedTraits(stack).orElseGet(() -> of(stack.getItem()));
    }

    public static SpellTraits of(Item item) {
        return TraitLoader.INSTANCE.values.getOrDefault(Registry.ITEM.getId(item), null);
    }

    public static SpellTraits of(Block block) {
        return of(block.asItem());
    }

    private static Optional<SpellTraits> getEmbeddedTraits(ItemStack stack) {
        if (!(stack.hasTag() && stack.getTag().contains("spell_traits", NbtElement.COMPOUND_TYPE))) {
            return Optional.empty();
        }
        return readNbt(stack.getTag().getCompound("spell_traits"));
    }

    public static Optional<SpellTraits> readNbt(NbtCompound traits) {
        return fromEntries(streamFromNbt(traits));
    }

    public static Stream<Map.Entry<Trait, Float>> streamFromNbt(NbtCompound traits) {
        return traits.getKeys().stream().map(key -> {
            Trait trait = Trait.REGISTRY.get(key.toUpperCase());
            if (trait == null && !traits.contains(key, NbtElement.NUMBER_TYPE)) {
                return null;
            }
            return Map.entry(trait, traits.getFloat(key));
        });
    }

    public static Optional<SpellTraits> fromEntries(Stream<Map.Entry<Trait, Float>> entries) {
        var result = collect(entries);

        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SpellTraits(result));
    }

    static Map<Trait, Float> collect(Stream<Map.Entry<Trait, Float>> entries) {
        return entries.filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a + b, () -> new EnumMap<>(Trait.class)));
    }
}
