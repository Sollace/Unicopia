package com.minelittlepony.unicopia.ability.data;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3i;

public class Multi extends Pos {
    public static final Serializer<Multi> SERIALIZER = Multi::new;

    public final int hitType;

    Multi(PacketByteBuf buf) {
        super(buf);
        hitType = buf.readInt();
    }

    @Override
    public void toBuffer(PacketByteBuf buf) {
        super.toBuffer(buf);
        buf.writeInt(hitType);
    }

    public Multi(Vec3i pos, int hit) {
        super(pos.getX(), pos.getY(), pos.getZ());
        hitType = hit;
    }
}