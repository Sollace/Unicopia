package com.minelittlepony.unicopia.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;

import static net.minecraft.util.math.ColorHelper.*;

public interface ColorHelper {
    static int getRainbowColor(Entity entity, int speed, float tickDelta) {
        int n = entity.age / speed + entity.getId();
        int o = DyeColor.values().length;
        int p = n % o;
        int q = (n + 1) % o;
        float r = (entity.age % speed + tickDelta) / 25.0f;
        int fs = SheepEntity.getRgbColor(DyeColor.byId(p));
        int gs = SheepEntity.getRgbColor(DyeColor.byId(q));
        return lerp(r, fs, gs);
    }

    static float[] changeSaturation(float red, float green, float blue, float intensity) {
        float avg = (red + green + blue) / 3F;
        float r = avg + (red - avg) * intensity;
        float g = avg + (green - avg) * intensity;
        float b = avg + (blue - avg) * intensity;

        if (r > 1) {
            g -= r - 1;
            b -= r - 1;
            r = 1;
        }
        if (g > 1) {
            r -= g - 1;
            b -= g - 1;
            g = 1;
        }
        if (b > 1) {
            r -= b - 1;
            g -= b - 1;
            b = 1;
        }

        return new float[] {r, g, b};
    }

    static int saturate(int color, float intensity) {
        float a = getAlpha(color) / 255F,
                red = getRed(color) / 255F,
                green = getGreen(color) / 255F,
                blue = getBlue(color) / 255F;
        float avg = (red + green + blue) / 3F;
        float r = avg + (red - avg) * intensity,
                g = avg + (green - avg) * intensity,
                b = avg + (blue - avg) * intensity;

        if (r > 1) {
            g -= r - 1;
            b -= r - 1;
            r = 1;
        }
        if (g > 1) {
            r -= g - 1;
            b -= g - 1;
            g = 1;
        }
        if (b > 1) {
            r -= b - 1;
            g -= b - 1;
            b = 1;
        }

        return fromFloats(a, r, g, b);
    }
}
