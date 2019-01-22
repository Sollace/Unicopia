package com.minelittlepony.unicopia.player;

public interface IView {
    float calculateRoll();

    float calculatePitch(float pitch);

    float calculateYaw(float yaw);

    float calculateFieldOfView(float initialfow);

    double getBaseRoll();

    void setBaseRoll(double roll);
}
