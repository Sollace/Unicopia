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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;
import com.minelittlepony.unicopia.util.InventoryUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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

    public SpellTraits add(float amount) {
        return amount == 0 ? this : map(v -> v + amount);
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
        return fromEntries(newMap.entrySet().stream()).orElse(EMPTY);
    }

    public boolean isEmpty() {
        return traits.isEmpty();
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

    public static Optional<SpellTraits> getEmbeddedTraits(ItemStack stack) {
        if (!(stack.hasTag() && stack.getTag().contains("spell_traits", NbtElement.COMPOUND_TYPE))) {
            return Optional.empty();
        }
        return fromNbt(stack.getTag().getCompound("spell_traits"));
    }

    public ItemStack applyTo(ItemStack stack) {
        stack = stack.copy();
        if (isEmpty()) {
            stack.removeSubTag("spell_traits");
            return stack;
        }
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
            Identifier id = buf.readIdentifier();
            float value = buf.readFloat();
            if (value == 0) {
                continue;
            }

            Trait.fromId(id).ifPresent(trait -> {
                entries.compute(trait, (k, v) -> v == null ? value : (v + value));
            });
        }
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SpellTraits(entries));
    }

    public static Stream<Map.Entry<Trait, Float>> streamFromNbt(NbtCompound traits) {
        return traits.getKeys().stream().map(key -> {
            Trait trait = Trait.fromId(key).orElse(null);
            if (trait == null || !traits.contains(key, NbtElement.NUMBER_TYPE)) {
                return null;
            }
            return Map.entry(trait, traits.getFloat(key));
        });
    }

    public static Stream<Map.Entry<Trait, Float>> streamFromJson(JsonObject traits) {
        return traits.entrySet().stream().map(entry -> {
            Trait trait = Trait.fromName(entry.getKey()).orElse(null);
            if (trait == null || !entry.getValue().isJsonPrimitive() && !entry.getValue().getAsJsonPrimitive().isNumber()) {
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

    public static final class Builder {
        private final Map<Trait, Float> traits = new EnumMap<>(Trait.class);

        public Builder with(Trait trait, float amount) {
            traits.put(trait, amount);
            return this;
        }

        public SpellTraits build() {
            return fromEntries(traits.entrySet().stream()).orElse(SpellTraits.EMPTY);
        }
    }
}
