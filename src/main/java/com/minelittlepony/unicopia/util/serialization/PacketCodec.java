package com.minelittlepony.unicopia.util.serialization;

import java.util.Optional;

import net.minecraft.network.PacketByteBuf;

public record PacketCodec<T>(PacketByteBuf.PacketReader<T> reader, PacketByteBuf.PacketWriter<T> writer) {

    public T read(PacketByteBuf buf) {
        return reader().apply(buf);
    }

    public void write(PacketByteBuf buf, T value) {
        writer().accept(buf, value);
    }

    public PacketCodec<Optional<T>> asOptional() {
        return new PacketCodec<>(buf -> buf.readOptional(reader), (buf, v) -> buf.writeOptional(v, writer));
    }
}
