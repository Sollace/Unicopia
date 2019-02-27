package com.minelittlepony.unicopia.player;

import com.minelittlepony.transform.MotionCompositor;

class PlayerView extends MotionCompositor implements IView {

    private final IPlayer player;

    private double baseRoll = 0;

    public PlayerView(IPlayer player) {
        this.player = player;
    }

    @Override
    public float calculateRoll() {

        double roll = baseRoll;

        if (player.getGravity().isFlying()) {
            roll -= super.calculateRoll(player.getOwner(), player.getOwner().motionX, player.getOwner().motionY, player.getOwner().motionZ);
        }

        if (player.getGravity().getGravitationConstant() < 0) {
            roll += 180;
        }

        return (float)player.getInterpolator().interpolate("roll", (float)roll, 100);
    }

    @Override
    public float calculatePitch(float pitch) {
        return pitch + getEnergyAddition();
    }

    @Override
    public float calculateYaw(float yaw) {
        return yaw + getEnergyAddition();
    }

    @Override
    public float calculateFieldOfView(float fov) {
        fov += player.getExertion() / 5;
        fov += getEnergyAddition();

        return fov;
    }

    protected float getEnergyAddition() {
        int maxE = (int)Math.floor(player.getEnergy() * 100);

        if (maxE <= 0) {
            return 0;
        }

        float energyAddition = (player.getWorld().rand.nextInt(maxE) - maxE/2) / 100F;

        if (Math.abs(energyAddition) <= 0.001) {
            return 0;
        }

        return energyAddition;
    }

    @Override
    public double getBaseRoll() {
        return baseRoll;
    }

    @Override
    public void setBaseRoll(double roll) {
        baseRoll = roll;
    }
}
