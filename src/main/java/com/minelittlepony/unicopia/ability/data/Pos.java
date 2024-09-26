package com.minelittlepony.unicopia.ability.data;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.*;

public record Pos (int x, int y, int z) implements Hit {
    public static final PacketCodec<PacketByteBuf, Pos> CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, Pos::x,
                PacketCodecs.INTEGER, Pos::y,
                PacketCodecs.INTEGER, Pos::z,
                Pos::new
    );

    public Pos(Vec3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos pos() {
        return new BlockPos(x, y, z);
    }

    public Vec3d vec() {
        return Vec3d.ofCenter(new Vec3i(x, y, z));
    }

    public double distanceTo(Caster<?> caster) {
        return Math.sqrt(caster.asEntity().squaredDistanceTo(x, y, z));
    }
}