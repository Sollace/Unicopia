package com.minelittlepony.unicopia.entity.capabilities;

public interface ICamera {
    float calculateRoll();

    float calculatePitch(float pitch);

    float calculateYaw(float yaw);

    float calculateFieldOfView(float initialfow);

    double getBaseRoll();

    void setBaseRoll(double roll);
}
