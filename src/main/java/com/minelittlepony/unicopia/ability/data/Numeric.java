package com.minelittlepony.unicopia.ability.data;

import net.minecraft.network.PacketByteBuf;

public class Numeric extends Hit {
    public static final Serializer<Numeric> SERIALIZER = Numeric::new;

    public int type;

    Numeric(PacketByteBuf buf) {
        type = buf.readInt();
    }

    @Override
    public void toBuffer(PacketByteBuf buf) {
        buf.writeInt(type);
    }

    public Numeric(int t) {
        type = t;
    }
}