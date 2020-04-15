package com.minelittlepony.jumpingcastle.api;

import java.util.UUID;

/**
 * A channel for sending and recieving messages.
 */
public interface Channel {
    /**
     * Registers a handler for a specific message type transmitted over this channel.
     *
     * @param messageType   The message type being recieved.
     * @param handler       A handler instance to handle the message.
     */
    <T extends Message> Channel listenFor(Class<T> messageType, Message.Handler<T> handler);

    /**
     * Registers a handler for a specific message type transmitted over this channel.
     *
     * @param messageType   The message type being recieved.
     */
    <T extends Message & Message.Handler<T>> Channel listenFor(Class<T> messageType);

    /**
     * Gets the minecraft server
     */
    <T> T getServer();

    /**
     * Sends a message over this channel. By default targets all other clients listening on this channel.
     *
     * @param message The message to send.
     */
    default Channel send(Message message) {
        return send(message, Target.CLIENTS);
    }

    /**
     * Sends a message over this channel.
     *
     * @param message The message to send.
     * @param target  Recipients that must handle this message (clients, server, or both)
     */
    Channel send(Message message, Target target);

    /**
     * Sends a message back. Use this if you're a server.
     *
     * @param message The message to send.
     * @param recipient  Recipient that must handle this message
     */
    Channel respond(Message message, UUID recipient);

    /**
     * Sends a message back to all clients. Use this if you're a server.
     *
     * @param message The message to send.
     */
    Channel broadcast(Message message);
}
