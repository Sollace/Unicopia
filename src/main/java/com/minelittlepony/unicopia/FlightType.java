package com.minelittlepony.unicopia;

public enum FlightType {
    NONE,
    CREATIVE,
    AVIAN,
    INSECTOID;

    public boolean isGrounded() {
        return this == NONE;
    }

    public boolean canFly() {
        return !isGrounded();
    }

    public boolean canFlyCreative() {
        return this == CREATIVE || this == INSECTOID;
    }

    public boolean canFlySurvival() {
        return canFly() && !canFlyCreative();
    }
}
