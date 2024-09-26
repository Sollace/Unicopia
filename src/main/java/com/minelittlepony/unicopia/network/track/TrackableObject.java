package com.minelittlepony.unicopia.network.track;

import java.util.Optional;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public interface TrackableObject<T extends TrackableObject<T>> {
    Status getStatus();

    default void read(PacketByteBuf buffer, WrapperLookup lookup) {
        readTrackedNbt(PacketCodecs.NBT_COMPOUND.decode(buffer), lookup);
    }

    default Optional<PacketByteBuf> write(Status status, WrapperLookup lookup) {
        if (status == Status.NEW || status == Status.UPDATED) {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            PacketCodecs.NBT_COMPOUND.encode(buffer, writeTrackedNbt(lookup));
            return Optional.of(buffer);
        }
        return Optional.empty();
    }

    void readTrackedNbt(NbtCompound nbt, WrapperLookup lookup);

    NbtCompound writeTrackedNbt(WrapperLookup lookup);

    void discard(boolean immediate);

    void copyTo(T destination);

    public enum Status {
        DEFAULT,
        NEW,
        UPDATED,
        REMOVED
    }
}
