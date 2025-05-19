package com.minelittlepony.unicopia.entity.player;

public interface PlayerCamera {
    PlayerCamera DEFAULT = new PlayerCamera() {};

    default float calculateRoll() {
        return 0;
    }

    default float calculateFirstPersonRoll() {
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

    default double calculateFieldOfView(double fov) {
        return fov;
    }
}
