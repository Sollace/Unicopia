package com.minelittlepony.unicopia.util.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class NbtMap<K, V> implements NbtSerialisable {
    public static <K, V> NbtMap<K, V> of(Codec<K> keyCodec, Codec<V> valueCodec) {
        return new NbtMap<>(Codec.unboundedMap(keyCodec, valueCodec));
    }

    private final Codec<Map<K, V>> codec;
    private final Map<K, V> data = new HashMap<>();

    public NbtMap(Codec<Map<K, V>> codec) {
        this.codec = codec;
    }

    public Optional<V> getOrEmpty(K key) {
        return Optional.ofNullable(data.get(key));
    }

    public V computeIfAbsent(K key, Supplier<V> factory) {
        return data.computeIfAbsent(key, e -> factory.get());
    }

    public V put(K key, V value) {
        return data.put(key, value);
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> function) {
        return data.compute(key, function);
    }

    @Nullable
    public V remove(K key) {
        return data.remove(key);
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.put("data", NbtSerialisable.encode(codec, data));
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        data.clear();
        NbtSerialisable.decode(codec, compound.get("data")).ifPresent(data::putAll);
    }
}
