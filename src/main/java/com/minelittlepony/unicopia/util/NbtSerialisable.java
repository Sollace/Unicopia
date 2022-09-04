package com.minelittlepony.unicopia.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface NbtSerialisable {
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

    static void writeBlockPos(String name, Optional<BlockPos> pos, NbtCompound nbt) {
        pos.map(NbtHelper::fromBlockPos).ifPresent(p -> nbt.put("hoveringPosition", p));
    }

    static Optional<BlockPos> readBlockPos(String name, NbtCompound nbt) {
        return nbt.contains(name) ? Optional.ofNullable(NbtHelper.toBlockPos(nbt.getCompound(name))) : Optional.empty();
    }

    interface Serializer<T> {
        T read(NbtCompound compound);

        NbtCompound write(T t);

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
