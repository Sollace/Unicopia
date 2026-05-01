package com.minelittlepony.unicopia.network.track;

import java.util.Optional;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;

public interface TrackableObject<T extends TrackableObject<T>> {
    Status getStatus();

    void read(RegistryByteBuf buffer);

    void write(RegistryByteBuf buffer);

    default Optional<? extends ByteBuf> write(Status status, DynamicRegistryManager lookup) {
        if (status == Status.NEW || status == Status.UPDATED) {
            RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), lookup);
            write(buffer);
            return Optional.of(buffer);
        }
        return Optional.empty();
    }

    void discard(boolean immediate);

    void copyTo(T destination);

    public enum Status {
        DEFAULT,
        NEW,
        UPDATED,
        REMOVED
    }
}
