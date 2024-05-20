package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.minelittlepony.unicopia.util.serialization.PacketCodec;

import net.minecraft.network.PacketByteBuf;

public record TrackableDataType<T>(int id, PacketCodec<T> codec) {
    private static final List<TrackableDataType<?>> REGISTRY = new ArrayList<>();
    private static final Interner<TrackableDataType<?>> INTERNER = Interners.newStrongInterner();

    @SuppressWarnings("unchecked")
    public static <T> TrackableDataType<T> of(int id) {
        return (TrackableDataType<T>)REGISTRY.get(id);
    }

    public static <T> TrackableDataType<T> of(PacketCodec<T> codec) {
        @SuppressWarnings("unchecked")
        TrackableDataType<T> type = (TrackableDataType<T>) INTERNER.intern(new TrackableDataType<>(REGISTRY.size(), codec));
        if (type.id() == REGISTRY.size()) {
            REGISTRY.add(type);
        }
        return type;
    }

    public T read(PacketByteBuf buffer) {
        return codec().read(buffer);
    }

    public void write(PacketByteBuf buffer, T value) {
        buffer.writeInt(id());
        codec().write(buffer, value);
    }
}
