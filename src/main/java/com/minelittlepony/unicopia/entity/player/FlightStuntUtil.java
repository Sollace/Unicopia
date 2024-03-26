package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.entity.mob.StormCloudEntity;
import com.minelittlepony.unicopia.util.MutableVector;

import net.minecraft.util.math.BlockPos;

public class FlightStuntUtil {
    public static boolean isPerformingDive(Pony pony, MutableVector velocity) {
        double horizontalSpeed = velocity.horizontalLengthSquared();
        double verticalSpeed = velocity.y;
        return horizontalSpeed != 0 && verticalSpeed < -0.3F && (verticalSpeed / horizontalSpeed) < -0.3F;
    }

    public static boolean isFlyingLow(Pony pony, MutableVector velocity) {
        BlockPos pos = pony.asEntity().getBlockPos();
        return velocity.horizontalLengthSquared() > 0.005F && (pos.getY() - StormCloudEntity.findSurfaceBelow(pony.asWorld(), pos).getY()) < 6;
    }
}
