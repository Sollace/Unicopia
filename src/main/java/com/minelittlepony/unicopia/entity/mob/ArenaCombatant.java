package com.minelittlepony.unicopia.entity.mob;

import java.util.Optional;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface ArenaCombatant {
    Optional<BlockPos> getHomePos();

    default float getAreaRadius() {
        return 30;
    }

    boolean teleportTo(Vec3d destination);
}
