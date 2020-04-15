package com.minelittlepony.jumpingcastle.api.payload;

public interface Serializable<T extends Serializable<T>> {
    /**
     * Reads the contents of this message from a networking payload.
     */
    @SuppressWarnings("unchecked")
    default T read(BinaryPayload payload) {
        return (T)JsonSerializer.read(payload, getClass());
    }

    /**
     * Writes the contents of this message to a networking payload.
     */
    default void write(BinaryPayload payload) {
        JsonSerializer.write(payload, this);
    }

}
