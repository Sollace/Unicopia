package com.minelittlepony.unicopia.ability.data;

import com.minelittlepony.unicopia.EntityConvertable;

import net.minecraft.util.math.Vec3d;

public record Rot (float pitch, float yaw) implements Hit {
    public static final Serializer<Rot> SERIALIZER = new Serializer<>(
            buf -> new Rot(buf.readFloat(), buf.readFloat()),
            (buf, t) -> {
                buf.writeFloat(t.pitch());
                buf.writeFloat(t.yaw());
            });

    public Rot applyTo(EntityConvertable<?> target) {
        Vec3d pos = target.getOriginVector();
        target.asEntity().updatePositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
        return this;
    }

    public static Rot of(EntityConvertable<?> source) {
        return new Rot(source.asEntity().getPitch(1), source.asEntity().getHeadYaw());
    }
}