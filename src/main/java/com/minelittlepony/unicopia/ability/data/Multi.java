package com.minelittlepony.unicopia.ability.data;

import net.minecraft.util.math.Vec3i;

public record Multi (Pos pos, int hitType) implements Hit {
    public static final Serializer<Multi> SERIALIZER = new Serializer<>(
            buf -> new Multi(Pos.SERIALIZER.read().apply(buf), buf.readInt()),
            (buf, t) -> {
                Pos.SERIALIZER.write().accept(buf, t.pos());
                buf.writeInt(t.hitType());
            });

    public Multi(Vec3i pos, int hit) {
        this(new Pos(pos), hit);
    }
}