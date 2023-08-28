package com.minelittlepony.unicopia.util;

import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class Lerp {
    private long duration;

    private long startTime;
    private float start;
    private float end;

    private boolean finished = true;

    public Lerp(float initial) {
        end = initial;
    }

    public boolean update(float newTarget, long changeDuration) {
        if (MathHelper.approximatelyEquals(end, newTarget)) {
            return false;
        }
        start = getValue();
        startTime = Util.getMeasuringTimeMs();
        end = newTarget;
        duration = changeDuration;
        finished = false;
        return true;
    }

    public float getValue() {
        if (finished) {
            return end;
        }
        float delta = getDelta();
        finished = delta >= 1F;
        return MathHelper.lerp(delta, start, end);
    }

    public boolean isFinished() {
        return finished;
    }

    private float getDelta() {
        return MathHelper.clamp((float)(Util.getMeasuringTimeMs() - startTime) / (float)duration, 0, 1);
    }
}
