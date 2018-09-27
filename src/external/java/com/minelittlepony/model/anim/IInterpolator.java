package com.minelittlepony.model.anim;

// #MineLittlePony#
@FunctionalInterface
public interface IInterpolator {
    float interpolate(String key, float to, float scalingFactor);
}
