package com.minelittlepony.unicopia.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface NbtSerialisable {
    @Deprecated
    Serializer<NbtElement, BlockPos> BLOCK_POS = Serializer.ofCodec(BlockPos.CODEC);
    @Deprecated
    Serializer<NbtElement, ItemStack> ITEM_STACK = Serializer.ofCodec(ItemStack.CODEC);

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

    static Vec3d readVector(NbtList list) {
        return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }

    static <T> Optional<T> decode(Codec<T> codec, NbtElement nbt) {
        return codec.decode(NbtOps.INSTANCE, nbt).result().map(Pair::getFirst);
    }

    static <T> NbtElement encode(Codec<T> codec, T value) {
        return codec.encodeStart(NbtOps.INSTANCE, value).result().get();
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

    @Deprecated
    interface Serializer<N extends NbtElement, T> {
        T read(N compound, WrapperLookup lookup);

        N write(T t, WrapperLookup lookup);

        @SuppressWarnings("unchecked")
        default Optional<T> readOptional(String name, NbtCompound compound, WrapperLookup lookup) {
            return compound.contains(name, NbtElement.COMPOUND_TYPE)
                    ? Optional.ofNullable(read((N)compound.get(name), lookup))
                    : Optional.empty();
        }

        default void writeOptional(String name, NbtCompound compound, Optional<T> t, WrapperLookup lookup) {
            t.map(l -> write(l, lookup)).ifPresent(tag -> compound.put(name, tag));
        }

        default NbtList writeAll(Collection<? extends T> ts, WrapperLookup lookup) {
            NbtList list = new NbtList();
            ts.stream().map(l -> write(l, lookup)).forEach(list::add);
            return list;
        }

        @SuppressWarnings("unchecked")
        default Stream<T> readAll(NbtList list, WrapperLookup lookup) {
            return list.stream().map(l -> read((N)l, lookup)).filter(Objects::nonNull);
        }

        static <T extends NbtSerialisable> Serializer<NbtCompound, T> of(Supplier<T> factory) {
            return of((nbt, lookup) -> {
                T value = factory.get();
                value.fromNBT(nbt, lookup);
                return value;
            }, (value, lookup) -> value.toNBT(lookup));
        }

        static <T> Serializer<NbtElement, T> ofCodec(Codec<T> codec) {
            return of((value, lookup) -> decode(codec, value).get(), (t, lookup) -> encode(codec, t));
        }

        static <N extends NbtElement, T> Serializer<N, T> of(BiFunction<N, WrapperLookup, T> read, BiFunction<T, WrapperLookup, N> write) {
            return new Serializer<>() {
                @Override
                public T read(N compound, WrapperLookup lookup) {
                    return read.apply(compound, lookup);
                }

                @Override
                public N write(T t, WrapperLookup lookup) {
                    return write.apply(t, lookup);
                }
            };
        }
    }
}
