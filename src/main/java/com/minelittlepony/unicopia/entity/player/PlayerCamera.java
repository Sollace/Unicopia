package com.minelittlepony.unicopia.entity.player;

class PlayerCamera extends MotionCompositor implements ICamera {

    private final IPlayer player;

    private double baseRoll = 0;

    public PlayerCamera(IPlayer player) {
        this.player = player;
    }

    @Override
    public float calculateRoll() {

        double roll = baseRoll;

        if (player.getFlight().isFlying()) {
            roll -= super.calculateRoll(player.getOwner(), player.getOwner().getVelocity().motionX, player.getOwner().motionY, player.getOwner().motionZ);
        }

        if (player.getGravity().getGravitationConstant() < 0) {
            roll = -roll;
            roll += 180;
        }

        if (player.getEntity().age > 10) {
            roll = player.getInterpolator().interpolate("roll", (float)roll, 250);
        }

        return (float)roll;
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

        float energyAddition = (player.getWorld().random.nextInt(maxE) - maxE/2) / 100F;

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
