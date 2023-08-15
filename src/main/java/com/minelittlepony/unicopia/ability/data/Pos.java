package com.minelittlepony.unicopia.ability.data;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.util.math.*;

public record Pos (int x, int y, int z) implements Hit {
    public static final Serializer<Pos> SERIALIZER = new Serializer<>(
            buf -> new Pos(buf.readInt(), buf.readInt(), buf.readInt()),
            (buf, t) -> {
                buf.writeInt(t.x());
                buf.writeInt(t.y());
                buf.writeInt(t.z());
            });

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