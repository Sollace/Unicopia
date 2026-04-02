package com.minelittlepony.unicopia.util;

import net.minecraft.entity.LimbAnimator;

public interface LimbAnimationUtil {

    static void resetToZero(LimbAnimator animator) {
        animator.reset();
    }

    static void copy(LimbAnimator from, LimbAnimator to) {
        float lastSpeed = from.getAmplitude(0);
        float speed = from.getSpeed();
        float animationProgress = from.getAnimationProgress(); // animationProgress * timeScale

        resetToZero(to);
        to.setSpeed(lastSpeed);                  // lastSpeed into speed
                                                 // speed -> lastSpeed
        to.updateLimbs(animationProgress, 1, 1); // animationProgress into animationProgress, timeScale -> 1
        to.setSpeed(speed);                      // speed into speed
    }
}
