package com.minelittlepony.unicopia.util;

import net.minecraft.world.World;

public final class WorldHelper {


    /**
     * Gets the daylight brightness value on a scale of 0-1.
     * Midday = 1
     * Sunrise/Sunset = 0
     * Nighttime = 0
     */
    public static float getDaylightBrightness(World w, float partialTicks) {
        float celst = w.getSkyAngle(partialTicks);

        // ----------------------------
        // 0 |          0.5      |      1
        //   |          |        |    midday
        //   |          midnight |
        // sunset                |
        //                       sunrise
        //---|                   |-------
   //---//                    |-------|
        //  /                    \
        // /                      \
        //                         \
        //                          \
        // midnight = 0.5
        // sunrise = 0.7
        // midday = 1
        // sunset = 0.3

        if (celst >= 0.7F || celst <= 0.3F) {
            if (celst >= 0.7) {
                celst -= 0.7;
            } else {
                celst = -celst + 0.3F;
            }

            return celst * (3 + 1/3);
        } else {
            celst = 0;
        }

        return celst;
    }

    /**
     * Gets the brightness of the moon. Works as the inverse of getDaylightBrightness but for the moon.
     * Midnight = 1
     * Sunrise/Sunset = 0
     * Daytime = 0
     */
    public static float getLunarBrightness(World w, float partialTicks) {
        float celst = w.getSkyAngle(partialTicks);

        // ----------------------------
        // 0 |          0.5      |      1
        //   |          |        |    midday
        //   |          midnight |
        // sunset                |
        //                       sunrise
        //   |-------------------|
        //   \         |        /
        //    \        |       /
        //     \       |      /
        //      \      |     /
        //             |
        // midnight = 0.5
        // sunrise = 0.7
        // midday = 1
        // sunset = 0.3

        if (celst < 0.7F && celst > 0.3F) {
            return Math.abs(celst - 0.5F) / 0.2F;
        }

        return 0;
    }
}
