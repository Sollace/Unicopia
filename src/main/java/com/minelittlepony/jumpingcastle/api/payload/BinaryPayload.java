package com.minelittlepony.jumpingcastle.api.payload;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public interface BinaryPayload {

    @Nullable
    static BinaryPayload of(Object buffer) {
        if (buffer instanceof ByteBuf) {
            return ByteBufBinaryPayload.of((ByteBuf)buffer);
        }
        return null;
    }

    static BinaryPayload create() {
        return of(Unpooled.buffer());
    }

    <T> T buff();

    byte[] bytes();

    byte[] readToEnd();

    long readLong();

    BinaryPayload writeLong(long l);

    int readInt();

    BinaryPayload writeInt(int b);

    byte readByte();

    BinaryPayload writeByte(byte b);

    String readString();

    BinaryPayload writeString(String s);

    byte[] readBytes(int len);

    BinaryPayload writeBytes(byte[] bytes);

    BinaryPayload reverse();

    @SuppressWarnings("unchecked")
    default <T extends Serializable<? super T>> T readBinary(Class<T> type) {
        try {
            return (T)type.newInstance().read(this);
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    default <T extends Serializable<T>> BinaryPayload writeBinary(T message) {
        message.write(this);
        return this;
    }
}
