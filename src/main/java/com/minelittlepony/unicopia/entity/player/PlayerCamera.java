package com.minelittlepony.unicopia.entity.player;

public interface PlayerCamera {
    PlayerCamera DEFAULT = new PlayerCamera() {};

    default float calculateRoll(boolean firstPerson, float fovEffectScale) {
        return 0;
    }

    default float calculatePitch(float pitch) {
        return pitch;
    }

    default float calculateYaw(float yaw) {
        return yaw;
    }

    default float calculateDistance(float distance) {
        return distance;
    }

    default float calculateFieldOfView(float fov, boolean firstPerson, float fovEffectScale) {
        return fov;
    }
}
