package com.minelittlepony.unicopia.util.serialization;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public interface NbtSerialisable {
    /**
     * Called to save this to nbt to persist state on file or to transmit over the network
     *
     * @param compound  Compound tag to write to.
     */
    void toNBT(NbtCompound compound, WrapperLookup lookup);

    /**
     * Called to load this state from nbt
     *
     * @param compound  Compound tag to read from.
     */
    void fromNBT(NbtCompound compound, WrapperLookup lookup);

    default NbtCompound toNBT(WrapperLookup lookup) {
        NbtCompound compound = new NbtCompound();
        toNBT(compound, lookup);
        return compound;
    }

    @Deprecated
    static NbtCompound subTag(String name, NbtCompound parent) {
        NbtCompound child = new NbtCompound();
        parent.put(name, child);
        return child;
    }

    @Deprecated
    static NbtCompound subTag(String name, NbtCompound parent, Consumer<NbtCompound> writer) {
        writer.accept(subTag(name, parent));
        return parent;
    }

    @Deprecated
    static <K, V> Map<K, V> readMap(NbtCompound nbt, Function<String, K> keyFunction, BiFunction<K, NbtElement, V> valueFunction) {
        return nbt.getKeys().stream().map(k -> {
            K key = keyFunction.apply(k);
            if (key == null) {
                return null;
            }
            V value = valueFunction.apply(key, nbt.get(k));
            if (value == null) {
                return null;
            }
            return Map.entry(key, value);
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Deprecated
    static <K, V> NbtCompound writeMap(Map<K, V> map, Function<K, String> keyFunction, Function<V, ? extends NbtElement> valueFunction) {
        return writeMap(new NbtCompound(), map, keyFunction, valueFunction);
    }

    @Deprecated
    private static <K, V> NbtCompound writeMap(NbtCompound nbt, Map<K, V> map, Function<K, String> keyFunction, Function<V, ? extends NbtElement> valueFunction) {
        map.forEach((k, v) -> nbt.put(keyFunction.apply(k), valueFunction.apply(v)));
        return nbt;
    }
}
