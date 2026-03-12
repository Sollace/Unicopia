package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.Registries;

public final class SpellTraits implements Iterable<Map.Entry<Trait, Float>> {
    public static final SpellTraits EMPTY = new SpellTraits(Map.of());
    private static final SpellTraits SPAWN_EGG_TRAITS = new SpellTraits(Map.of(Trait.LIFE, 20F));

    private static Map<Identifier, SpellTraits> REGISTRY = new HashMap<>();
    static final Map<Trait, List<Item>> ITEMS = new HashMap<>();

    public static final Codec<SpellTraits> CODEC = Codec.unboundedMap(Trait.CODEC, Codec.FLOAT).flatXmap(
            map -> DataResult.success(fromEntries(map.entrySet().stream())),
            traits -> DataResult.success(traits.traits)
    );
    public static final PacketCodec<ByteBuf, SpellTraits> PACKET_CODEC = PacketCodecs.map(i -> new EnumMap<>(Trait.class), Trait.PACKET_CODEC, PacketCodecs.FLOAT).xmap(
            entries -> fromEntries(entries.entrySet().stream()),
            traits -> traits.traits
    );

    public static void load(Map<Identifier, SpellTraits> newRegistry) {
        REGISTRY = newRegistry;
        ITEMS.clear();
        REGISTRY.forEach((itemId, traits) -> {
            Registries.ITEM.getOptionalValue(itemId).ifPresent(item -> {
                traits.forEach(entry -> {
                    List<Item> items = ITEMS.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                    if (!items.contains(item)) {
                        items.add(item);
                    }
                 });
            });
        });
    }

    public static SpellTraits empty() {
        return EMPTY;
    }

    public static Map<Identifier, SpellTraits> all() {
        return new HashMap<>(REGISTRY);
    }

    private final EnumMap<Trait, Float> traits;
    private final float corruption;

    private SpellTraits(Map<Trait, Float> traits) {
        this.traits = traits.isEmpty() ? new EnumMap<>(Trait.class) : new EnumMap<>(traits);
        this.corruption = (float)stream().filter(e -> e.getValue() != 0).mapToDouble(e -> e.getKey().getGroup().getCorruption()).sum();
    }

    private SpellTraits(SpellTraits from) {
        this.traits = new EnumMap<>(from.traits);
        this.corruption = from.corruption;
    }

    public float getCorruption() {
        return corruption;
    }

    public SpellTraits multiply(float factor) {
        return factor == 0 ? EMPTY : map(v -> v * factor);
    }

    public SpellTraits add(float amount) {
        return amount == 0 ? this : map(v -> v + amount);
    }

    public SpellTraits add(SpellTraits traits) {
        return union(this, traits);
    }

    public SpellTraits map(Function<Float, Float> function) {
        return map((k, v) -> function.apply(v));
    }

    public SpellTraits map(BiFunction<Trait, Float, Float> function) {
        if (isEmpty()) {
            return this;
        }

        Map<Trait, Float> newMap = new EnumMap<>(traits);
        newMap.entrySet().forEach(entry -> entry.setValue(function.apply(entry.getKey(), entry.getValue())));
        return fromEntries(newMap.entrySet().stream());
    }

    public boolean isEmpty() {
        return traits.isEmpty();
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public boolean includes(SpellTraits other) {
        return other.stream().allMatch(pair -> {
            return get(pair.getKey()) >= pair.getValue();
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

    public float getOrDefault(Trait trait, float def) {
        float i = traits.getOrDefault(trait, def);
        return i == 0 ? def : i;
    }

    public float get(Trait trait) {
        return getOrDefault(trait, 0F);
    }

    public float get(Trait trait, float min, float max) {
        return MathHelper.clamp(get(trait), min, max);
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltip(List<Text> tooltip) {
        if (isEmpty()) {
            return;
        }
        tooltip.add(1, new ItemTraitsTooltipRenderer(this));
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        traits.forEach((key, value) -> nbt.putFloat(key.getId().toString(), value));
        return nbt;
    }

    public ItemStack applyTo(ItemStack stack) {
        stack = stack.copy();
        if (isEmpty()) {
            stack.remove(UDataComponentTypes.SPELL_TRAITS);
            return stack;
        }
        stack.set(UDataComponentTypes.SPELL_TRAITS, this);
        return stack;
    }

    @Override
    public String toString() {
        return "SpellTraits[" + traits.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(",")) + "]";
    }

    @Override
    public int hashCode() {
        return traits.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof SpellTraits && Objects.equals(traits, ((SpellTraits) other).traits);
    }

    public static SpellTraits union(SpellTraits a, SpellTraits b) {
        if (a.isEmpty()) {
            return b;
        }
        if (b.isEmpty()) {
            return a;
        }
        Map<Trait, Float> traits = new HashMap<>();
        combine(traits, a.traits);
        combine(traits, b.traits);
        return traits.isEmpty() ? EMPTY : new SpellTraits(traits);
    }

    public static SpellTraits union(SpellTraits...many) {
        Map<Trait, Float> traits = new HashMap<>();
        for (SpellTraits i : many) {
            combine(traits, i.traits);
        }
        return traits.isEmpty() ? EMPTY : new SpellTraits(traits);
    }

    public static SpellTraits of(Inventory inventory) {
        return of(InventoryUtil.stream(inventory).toList());
    }

    public static SpellTraits of(Collection<ItemStack> stacks) {
        return fromEntries(stacks.stream().flatMap(a -> of(a).entries().stream()));
    }

    public static SpellTraits of(ItemStack stack) {
        return getEmbeddedTraits(stack).orElseGet(() -> of(stack.getItem()));
    }

    public static SpellTraits of(Item item) {
        if (item instanceof ItemWithTraits i) {
            return i.getDefaultTraits();
        }
        if (item instanceof SpawnEggItem) {
            return SPAWN_EGG_TRAITS;
        }
        return REGISTRY.getOrDefault(Registries.ITEM.getId(item), EMPTY);
    }

    public static SpellTraits of(Block block) {
        return of(block.asItem());
    }

    public static Stream<Item> getItems(Trait trait) {
        return ITEMS.getOrDefault(trait, List.of()).stream();
    }

    public static Optional<SpellTraits> getEmbeddedTraits(ItemStack stack) {
        return Optional.ofNullable(stack.get(UDataComponentTypes.SPELL_TRAITS));
    }

    public static Optional<SpellTraits> fromNbt(NbtCompound traits) {
        return CODEC.decode(NbtOps.INSTANCE, traits).result().map(Pair::getFirst);
    }

    public static Codec<SpellTraits> createStringCodec(String delimiter) {
        return Codec.STRING.xmap(traits -> {
            return fromEntries(Arrays.stream(traits.split(delimiter)).map(a -> a.split(":"))
                    .flatMap(pair -> Trait.of(pair[0]).stream().map(key -> Map.entry(key, Float.parseFloat(pair[1])))));
        }, spellTraits -> spellTraits.stream().map(entry -> entry.getKey().asString() + ":" + entry.getValue()).collect(Collectors.joining(delimiter)));
    }

    private static SpellTraits fromEntries(Stream<Map.Entry<Trait, Float>> entries) {
        var result = entries.filter(Objects::nonNull)
                .filter(e -> e.getValue() != 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a + b, () -> new EnumMap<>(Trait.class)));

        if (result.isEmpty()) {
            return EMPTY;
        }
        return new SpellTraits(result);
    }

    private static void combine(Map<Trait, Float> to, Map<Trait, Float> from) {
        if (from.isEmpty()) {
            return;
        }
        from.forEach((trait, value) -> {
            if (value != 0) {
                to.compute(trait, (k, v) -> v == null ? value : (v + value));
            }
        });
    }

    public static final class Builder {
        private final Map<Trait, Float> traits = new EnumMap<>(Trait.class);

        public Builder with(Trait trait, float amount) {
            traits.put(trait, amount);
            return this;
        }

        public SpellTraits build() {
            return fromEntries(traits.entrySet().stream());
        }
    }
}
