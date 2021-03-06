package com.minelittlepony.unicopia.ability.data;

import net.minecraft.network.PacketByteBuf;

public class Hit {

    public static final Hit INSTANCE = new Hit();
    public static final Serializer<Hit> SERIALIZER = buf -> INSTANCE;

    public static Hit of(boolean value) {
        return value ? INSTANCE : null;
    }

    protected Hit() {

    }

    public void toBuffer(PacketByteBuf buf) {

    }

    public interface Serializer<T extends Hit> {
        T fromBuffer(PacketByteBuf buf);
    }
}