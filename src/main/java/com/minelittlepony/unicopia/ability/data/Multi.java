package com.minelittlepony.unicopia.ability.data;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3i;

public record Multi (Pos pos, int hitType) implements Hit {
    public static final PacketCodec<PacketByteBuf, Multi> CODEC = PacketCodec.tuple(
            Pos.CODEC, Multi::pos,
            PacketCodecs.INTEGER, Multi::hitType,
            Multi::new
    );

    public Multi(Vec3i pos, int hit) {
        this(new Pos(pos), hit);
    }
}