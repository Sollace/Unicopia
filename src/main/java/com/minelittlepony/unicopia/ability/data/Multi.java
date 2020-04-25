package com.minelittlepony.unicopia.ability.data;

import net.minecraft.util.PacketByteBuf;

public class Multi extends Pos {
    public static final Serializer<Multi> SERIALIZER = Multi::new;

    public int hitType;

    Multi(PacketByteBuf buf) {
        super(buf);
        hitType = buf.readInt();
    }

    @Override
    public void toBuffer(PacketByteBuf buf) {
        super.toBuffer(buf);
        buf.writeInt(hitType);
    }

    public Multi(int x, int y, int z, int hit) {
        super(x, y, z);
        hitType = hit;
    }
}