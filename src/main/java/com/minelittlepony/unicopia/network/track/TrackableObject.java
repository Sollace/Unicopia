package com.minelittlepony.unicopia.network.track;

import java.util.Optional;

import com.minelittlepony.unicopia.util.serialization.PacketCodec;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public interface TrackableObject<T extends TrackableObject<T>> {
    Status getStatus();

    default void read(PacketByteBuf buffer) {
        readTrackedNbt(PacketCodec.COMPRESSED_NBT.read(buffer));
    }

    default Optional<PacketByteBuf> write(Status status) {
        if (status == Status.NEW || status == Status.UPDATED) {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            PacketCodec.COMPRESSED_NBT.write(buffer, writeTrackedNbt());
            return Optional.of(buffer);
        }
        return Optional.empty();
    }

    void readTrackedNbt(NbtCompound nbt);

    NbtCompound writeTrackedNbt();

    void discard(boolean immediate);

    void copyTo(T destination);

    public enum Status {
        DEFAULT,
        NEW,
        UPDATED,
        REMOVED
    }
}
