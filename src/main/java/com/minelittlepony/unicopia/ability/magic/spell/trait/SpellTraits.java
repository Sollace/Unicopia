package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.util.InventoryUtil;

import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

public final class SpellTraits implements Iterable<Map.Entry<Trait, Float>> {
    public static final SpellTraits EMPTY = new SpellTraits(Map.of());

    private final Map<Trait, Float> traits;

    SpellTraits(Map<Trait, Float> traits) {
        this.traits = traits;
    }

    SpellTraits(SpellTraits from) {
        this(new EnumMap<>(from.traits));
    }

    public SpellTraits multiply(float factor) {
        return factor == 0 ? EMPTY : map(v -> v * factor);
    }

    public SpellTraits map(Function<Float, Float> function) {
        if (isEmpty()) {
            return this;
        }

        Map<Trait, Float> newMap = new EnumMap<>(traits);
        newMap.entrySet().forEach(entry -> entry.setValue(function.apply(entry.getValue())));
        return fromEntries(newMap.entrySet().stream()).orElse(EMPTY);
    }

    public boolean isEmpty() {
        return traits.isEmpty();
    }

    public boolean includes(SpellTraits other) {
        return other.stream().allMatch(pair -> {
            return getAmount(pair.getKey()) >= pair.getValue();
        });
    }

    @Override
    public Iterator<Entry<Trait, Float>> iterator() {
        return entries().iterator();
    }

    public Set<Map.Entry<Trait, Float>> entries() {
        return traits.entrySet();
    }

    public Stream<Map.Entry<Trait, Float>> stream() {
        return entries().stream();
    }

    public float getAmount(Trait trait) {
        return traits.getOrDefault(trait, 0F);
    }

    public void appendTooltip(List<Text> tooltip) {
        if (isEmpty()) {
            return;
        }
        tooltip.add(new LiteralText("Traits:"));
        traits.forEach((trait, amount) -> {
            tooltip.add(new LiteralText(trait.name().toLowerCase() + ": " + amount));
        });
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        traits.forEach((key, value) -> nbt.putFloat(key.name(), value));
        return nbt;
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(traits.size());
        traits.forEach((trait, value) -> {
            buf.writeIdentifier(trait.getId());
            buf.writeFloat(value);
        });
    }

    public static SpellTraits union(SpellTraits...many) {
        Map<Trait, Float> traits = new HashMap<>();
        for (SpellTraits i : many) {
            combine(traits, i.traits);
        }
        return traits.isEmpty() ? EMPTY : new SpellTraits(traits);
    }

    public static SpellTraits of(Inventory inventory) {
        List<ItemStack> stacks = new ArrayList<>();
        InventoryUtil.iterate(inventory).forEach(stacks::add);
        return of(stacks);
    }

    public static SpellTraits of(Collection<ItemStack> stacks) {
        return fromEntries(stacks.stream().flatMap(a -> of(a).entries().stream())).orElse(SpellTraits.EMPTY);
    }

    public static SpellTraits of(ItemStack stack) {
        return getEmbeddedTraits(stack).orElseGet(() -> of(stack.getItem()));
    }

    public static SpellTraits of(Item item) {
        return TraitLoader.INSTANCE.values.getOrDefault(Registry.ITEM.getId(item), EMPTY);
    }

    public static SpellTraits of(Block block) {
        return of(block.asItem());
    }

    private static Optional<SpellTraits> getEmbeddedTraits(ItemStack stack) {
        if (!(stack.hasTag() && stack.getTag().contains("spell_traits", NbtElement.COMPOUND_TYPE))) {
            return Optional.empty();
        }
        return fromNbt(stack.getTag().getCompound("spell_traits"));
    }

    public ItemStack applyTo(ItemStack stack) {
        stack = stack.copy();
        stack.getOrCreateTag().put("spell_traits", toNbt());
        return stack;
    }

    public static Optional<SpellTraits> fromNbt(NbtCompound traits) {
        return fromEntries(streamFromNbt(traits));
    }

    public static Optional<SpellTraits> fromJson(JsonObject traits) {
        return fromEntries(streamFromJson(traits));
    }

    public static Optional<SpellTraits> fromPacket(PacketByteBuf buf) {
        Map<Trait, Float> entries = new HashMap<>();
        int count = buf.readInt();
        if (count <= 0) {
            return Optional.empty();
        }

        for (int i = 0; i < count; i++) {
            Trait trait = Trait.REGISTRY.getOrDefault(buf.readIdentifier(), null);
            float value = buf.readFloat();
            if (trait != null) {
                entries.compute(trait, (k, v) -> v == null ? value : (v + value));
            }
        }
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SpellTraits(entries));
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

    public static Stream<Map.Entry<Trait, Float>> streamFromJson(JsonObject traits) {
        return traits.entrySet().stream().map(entry -> {
            Trait trait = Trait.REGISTRY.get(entry.getKey().toUpperCase());
            if (trait == null && !entry.getValue().isJsonPrimitive() && !entry.getValue().getAsJsonPrimitive().isNumber()) {
                return null;
            }
            return Map.entry(trait, entry.getValue().getAsJsonPrimitive().getAsFloat());
        });
    }

    public static Optional<SpellTraits> fromEntries(Stream<Map.Entry<Trait, Float>> entries) {
        var result = collect(entries);

        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SpellTraits(result));
    }

    static void combine(Map<Trait, Float> to, Map<Trait, Float> from) {
        from.forEach((trait, value) -> {
            if (value != 0) {
                to.compute(trait, (k, v) -> v == null ? value : (v + value));
            }
        });
    }

    static Map<Trait, Float> collect(Stream<Map.Entry<Trait, Float>> entries) {
        return entries.filter(Objects::nonNull)
                .filter(e -> e.getValue() != 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a + b, () -> new EnumMap<>(Trait.class)));
    }
}
