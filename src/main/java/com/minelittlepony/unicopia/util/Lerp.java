package com.minelittlepony.unicopia.util;

import com.minelittlepony.unicopia.Unicopia;

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
        if (Float.isNaN(newTarget) || Float.isInfinite(newTarget)) {
            Unicopia.LOGGER.error("Invalid lerp target. Target cannot be NaN or Infinity");
        }
        if (MathHelper.approximatelyEquals(end, newTarget)) {
            return false;
        }
        if (changeDuration == 0) {
            start = newTarget;
            end = newTarget;
            finished = true;
            return false;
        }

        start = getValue();
        startTime = Util.getMeasuringTimeMs();
        end = newTarget;
        duration = changeDuration;
        finished = false;
        if (Float.isNaN(start) || Float.isInfinite(start)) {
            Unicopia.LOGGER.error("Invalid lerp start. Value cannot be NaN or Infinity");
        }
        if (Float.isNaN(end) || Float.isInfinite(end)) {
            Unicopia.LOGGER.error("Invalid lerp end. Value cannot be NaN or Infinity");
        }
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
        if (duration == 0) {
            return 1;
        }
        return MathHelper.clamp((float)(Util.getMeasuringTimeMs() - startTime) / (float)duration, 0, 1);
    }
}
