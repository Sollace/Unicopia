package com.minelittlepony.jumpingcastle.api.payload;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

interface ByteBufBinaryPayload extends BinaryPayload {

    static BinaryPayload of(ByteBuf buff) {
        return (ByteBufBinaryPayload)(() -> buff);
    }

    @SuppressWarnings("unchecked")
    ByteBuf buff();

    @Override
    default String readString() {
        return buff().readCharSequence(readInt(), StandardCharsets.UTF_8).toString();
    }

    @Override
    default int readInt() {
        return buff().readInt();
    }

    @Override
    default BinaryPayload writeInt(int b) {
        buff().writeInt(b);
        return this;
    }

    @Override
    default byte readByte() {
        return buff().readByte();
    }

    @Override
    default BinaryPayload writeByte(byte b) {
        buff().writeByte(b);
        return this;
    }

    default byte[] bytes() {
        return buff().array();
    }

    default byte[] readToEnd() {
        byte[] bytes = new byte[buff().writerIndex() - buff().readerIndex()];
        buff().readBytes(bytes);
        return bytes;
    }

    @Override
    default long readLong() {
        return buff().readLong();
    }

    @Override
    default BinaryPayload writeLong(long l) {
        buff().writeLong(l);
        return this;
    }

    @Override
    default BinaryPayload reverse() {
        buff().readerIndex(0);
        return this;
    }

    @Override
    default byte[] readBytes(int len) {
        byte[] data = new byte[len];
        buff().readBytes(data);
        return data;
    }

    @Override
    default BinaryPayload writeBytes(byte[] bytes) {
        buff().writeBytes(bytes);
        return this;
    }

    @Override
    default BinaryPayload writeString(String s) {
        buff().writeInt(s.getBytes(StandardCharsets.UTF_8).length);
        buff().writeCharSequence(s, StandardCharsets.UTF_8);
        return this;
    }
}
