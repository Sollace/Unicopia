package com.minelittlepony.unicopia.util;

import com.minelittlepony.common.util.Color;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;

public interface ColorHelper {
    static int getRainbowColor(Entity entity, int speed, float tickDelta) {
        int n = entity.age / speed + entity.getId();
        int o = DyeColor.values().length;
        int p = n % o;
        int q = (n + 1) % o;
        float r = (entity.age % speed + tickDelta) / 25.0f;
        float[] fs = SheepEntity.getRgbColor(DyeColor.byId(p));
        float[] gs = SheepEntity.getRgbColor(DyeColor.byId(q));
        float s = fs[0] * (1.0f - r) + gs[0] * r;
        float t = fs[1] * (1.0f - r) + gs[1] * r;
        float u = fs[2] * (1.0f - r) + gs[2] * r;
        return Color.argbToHex(1, s, t, u);
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

    static int lerp(float delta, int fromColor, int toColor) {
        return Color.argbToHex(
                MathHelper.lerp(delta, Color.a(fromColor), Color.a(toColor)),
                MathHelper.lerp(delta, Color.r(fromColor), Color.r(toColor)),
                MathHelper.lerp(delta, Color.g(fromColor), Color.g(toColor)),
                MathHelper.lerp(delta, Color.b(fromColor), Color.b(toColor))
        );
    }
}
