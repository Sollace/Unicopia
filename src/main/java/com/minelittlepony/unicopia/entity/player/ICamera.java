package com.minelittlepony.unicopia.entity.player;

public interface ICamera {
    float calculateRoll();

    float calculatePitch(float pitch);

    float calculateYaw(float yaw);

    float calculateFieldOfView(float initialfow);

    double getBaseRoll();

    void setBaseRoll(double roll);
}
