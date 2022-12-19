package com.minelittlepony.unicopia.ability.data;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.*;

public class Pos extends Hit {

    public static final Serializer<Pos> SERIALIZER = Pos::new;

    public final int x;
    public final int y;
    public final int z;

    Pos(PacketByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBuffer(PacketByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public Pos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Pos(BlockPos pos) {
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
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