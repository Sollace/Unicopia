package com.minelittlepony.unicopia.util.serialization;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.Vec3d;

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

    static NbtList writeVector(Vec3d vector) {
        NbtList list = new NbtList();
        list.add(NbtDouble.of(vector.getX()));
        list.add(NbtDouble.of(vector.getY()));
        list.add(NbtDouble.of(vector.getZ()));
        return list;
    }

    static <T> Optional<T> decode(Codec<T> codec, NbtElement nbt, WrapperLookup lookup) {
        return codec.decode(lookup.getOps(NbtOps.INSTANCE), nbt).result().map(Pair::getFirst);
    }

    static <T> NbtElement encode(Codec<T> codec, T value, WrapperLookup lookup) {
        return codec.encodeStart(lookup.getOps(NbtOps.INSTANCE), value).result().get();
    }

    static Vec3d readVector(NbtList list) {
        return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }

    static NbtCompound subTag(String name, NbtCompound parent) {
        NbtCompound child = new NbtCompound();
        parent.put(name, child);
        return child;
    }

    static NbtCompound subTag(String name, NbtCompound parent, Consumer<NbtCompound> writer) {
        writer.accept(subTag(name, parent));
        return parent;
    }

    static <K, V> Map<K, V> readMap(NbtCompound nbt, Function<String, K> keyFunction, Function<NbtElement, V> valueFunction) {
        return readMap(nbt, keyFunction, (k, v) -> valueFunction.apply(v));
    }

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

    static <K, V> NbtCompound writeMap(Map<K, V> map, Function<K, String> keyFunction, Function<V, ? extends NbtElement> valueFunction) {
        return writeMap(new NbtCompound(), map, keyFunction, valueFunction);
    }

    static <K, V> NbtCompound writeMap(NbtCompound nbt, Map<K, V> map, Function<K, String> keyFunction, Function<V, ? extends NbtElement> valueFunction) {
        map.forEach((k, v) -> nbt.put(keyFunction.apply(k), valueFunction.apply(v)));
        return nbt;
    }
}
