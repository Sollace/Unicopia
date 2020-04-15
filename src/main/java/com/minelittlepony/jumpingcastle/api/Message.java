package com.minelittlepony.jumpingcastle.api;

import com.minelittlepony.jumpingcastle.api.payload.Serializable;

/**
 * A message for communicating over a channel.
 * Fields marked with @Expose are automatically serialized to the output packet stream
 * and will be made available on the far end of the pipe.
 *
 * Override the read and write methods to customise this behaviour.
 *
 */
public interface Message extends Serializable<Message> {

    /**
     * Gets a unique identifier to represent a packet over channel communications.
     */
    static long identifier(Class<? extends Message> cls) {
        return cls.getCanonicalName().hashCode();
    }

    /**
     * Gets a unique identifier to represent this packet over channel communications.
     */
    default long identifier() {
        return identifier(getClass());
    }

    /**
     * Handler interface for incoming messages.
     *
     * @param <T> The type of message to handle.
     */
    @FunctionalInterface
    public interface Handler<T extends Message> {
        /**
         * Called when a new message is received.
         *
         * @param message The message received.
         *
         * @param channel The channel used to deliver the message.
         */
        void onPayload(T message, Channel channel);
    }
}
