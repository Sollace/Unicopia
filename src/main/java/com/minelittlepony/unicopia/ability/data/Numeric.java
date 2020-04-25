package com.minelittlepony.unicopia.ability.data;

import com.google.gson.annotations.Expose;

import net.minecraft.util.PacketByteBuf;

public class Numeric extends Hit {
    public static final Serializer<Numeric> SERIALIZER = Numeric::new;

    @Expose
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