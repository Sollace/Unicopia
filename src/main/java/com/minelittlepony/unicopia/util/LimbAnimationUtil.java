package com.minelittlepony.unicopia.util;

import net.minecraft.entity.LimbAnimator;

public interface LimbAnimationUtil {

    static void resetToZero(LimbAnimator animator) {
        animator.setSpeed(0);
        animator.updateLimbs(-animator.getPos(), 1, 1);
        animator.setSpeed(0);
    }

    static void copy(LimbAnimator from, LimbAnimator to) {
        float prevSpeed = from.getSpeed(0);
        float speed = from.getSpeed();
        float pos = from.getPos();

        resetToZero(to);
        to.setSpeed(prevSpeed);
        to.updateLimbs(pos, 1, 1);
        to.setSpeed(speed);
    }
}
