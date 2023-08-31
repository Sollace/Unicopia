package com.minelittlepony.unicopia.util;

import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class Lerp {
    private long duration;

    private long startTime;
    private float start;
    private float end;

    private boolean finished = true;
    private final boolean angle;

    public Lerp(float initial) {
        this(initial, false);
    }

    public Lerp(float initial, boolean angle) {
        end = initial;
        this.angle = angle;
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
        if (angle) {
            return MathHelper.lerpAngleDegrees(delta, start, end);
        }
        return MathHelper.lerp(delta, start, end);
    }

    public float getTarget() {
        return end;
    }

    public boolean isFinished() {
        return finished;
    }

    private float getDelta() {
        return MathHelper.clamp((float)(Util.getMeasuringTimeMs() - startTime) / (float)duration, 0, 1);
    }
}
