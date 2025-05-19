package com.minelittlepony.unicopia.ability.data;

import com.minelittlepony.unicopia.EntityConvertable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;

public record Rot (float pitch, float yaw) implements Hit {
    public static final PacketCodec<PacketByteBuf, Rot> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, Rot::pitch,
            PacketCodecs.FLOAT, Rot::yaw,
            Rot::new
    );

    public Rot applyTo(EntityConvertable<?> target) {
        Vec3d pos = target.getOriginVector();
        target.asEntity().updatePositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
        return this;
    }

    public static Rot of(EntityConvertable<?> source) {
        return new Rot(source.asEntity().getPitch(1), source.asEntity().getHeadYaw());
    }
}