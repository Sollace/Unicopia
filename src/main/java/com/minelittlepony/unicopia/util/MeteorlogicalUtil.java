package com.minelittlepony.unicopia.util;

import com.minelittlepony.unicopia.client.UnicopiaClient;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public interface MeteorlogicalUtil {

    static boolean isLookingIntoSun(World world, Entity entity) {

        // check first whether the world has a sun
        if (!world.getDimension().hasSkyLight()) {
            return false;
        }

        // check if sun is obscured by clouds
        if (world.hasRain(entity.getBlockPos()) || world.isThundering()) {
            return false;
        }

        final float skyAngle = getSkyAngle(entity.getWorld());
        float playerYaw = MathHelper.wrapDegrees(entity.getHeadYaw());
        float playerAngle = (-entity.getPitch(1) / 90F) / 2F;

        // player is facing the other way so flip the yaw to match sun angle
        if (playerYaw > 0) {
            playerAngle = 1 - playerAngle;
        }

        playerYaw += UnicopiaClient.getInstance().tangentalSkyAngle.getValue();
        playerYaw = Math.abs(playerYaw);

        // check if day,
        // and player is looking towards the sun, and that there isn't a block obstructing their view
        return skyAngle < 1
            && (playerYaw > 89 && playerYaw < 92 || (playerAngle > 0.45F && playerAngle < 0.55F))
            && playerAngle > (skyAngle - 0.04F) && playerAngle < (skyAngle + 0.04F)
            && entity.raycast(100, 1, true).getType() == Type.MISS;
    }

    static float getSunIntensity(World world) {
        float skyAngle = getSkyAngle(world);
        if (skyAngle > 1) {
            return 0;
        }

        // intensity (0-1) has a peak at 0.5 (midday)
        float intensity = MathHelper.cos((skyAngle - 0.5F) * MathHelper.PI);

        if (world.isRaining()) {
            intensity *= 0.5;
        }
        if (world.isThundering()) {
            intensity *= 0.5;
        }

        return intensity;
    }

    // we translate sun angle to a scale of 0-1 (0=sunrise, 1=sunset, >1 nighttime)
    static float getSkyAngle(World world) {
        return ((world.getSkyAngle(1) + 0.25F) % 1F) * 2;
    }

    static boolean isPositionExposedToSun(World world, BlockPos pos) {
        if (world.isClient) {
            world.calculateAmbientDarkness();
        }

        return world.getDimension().hasSkyLight()
                && world.getLightLevel(LightType.SKY, pos) >= 12
                && !world.isRaining()
                && !world.isThundering()
                && world.isDay();
    }
}
