package com.minelittlepony.unicopia.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface NbtSerialisable {
    Serializer<BlockPos> BLOCK_POS = Serializer.of(NbtHelper::toBlockPos, NbtHelper::fromBlockPos);

    /**
     * Called to save this to nbt to persist state on file or to transmit over the network
     *
     * @param compound  Compound tag to write to.
     */
    void toNBT(NbtCompound compound);

    /**
     * Called to load this state from nbt
     *
     * @param compound  Compound tag to read from.
     */
    void fromNBT(NbtCompound compound);

    default NbtCompound toNBT() {
        NbtCompound compound = new NbtCompound();
        toNBT(compound);
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

    interface Serializer<T> {
        T read(NbtCompound compound);

        NbtCompound write(T t);

        default Optional<T> readOptional(String name, NbtCompound compound) {
            return compound.contains(name, NbtElement.COMPOUND_TYPE)
                    ? Optional.ofNullable(read(compound.getCompound(name)))
                    : Optional.empty();
        }

        default void writeOptional(String name, NbtCompound compound, Optional<T> t) {
            t.map(this::write).ifPresent(tag -> compound.put(name, tag));
        }

        default T read(NbtElement element) {
            return read((NbtCompound)element);
        }

        default NbtList writeAll(Collection<T> ts) {
            NbtList list = new NbtList();
            ts.stream().map(this::write).forEach(list::add);
            return list;
        }

        default Stream<T> readAll(NbtList list) {
            return list.stream().map(this::read).filter(Objects::nonNull);
        }

        static <T> Serializer<T> of(Function<NbtCompound, T> read, Function<T, NbtCompound> write) {
            return new Serializer<>() {
                @Override
                public T read(NbtCompound compound) {
                    return read.apply(compound);
                }

                @Override
                public NbtCompound write(T t) {
                    return write.apply(t);
                }
            };
        }
    }
}
