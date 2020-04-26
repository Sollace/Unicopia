package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.util.MotionCompositor;

import net.minecraft.util.math.Vec3d;

public class PlayerCamera extends MotionCompositor {

    private final Pony player;

    private double baseRoll = 0;

    public PlayerCamera(Pony player) {
        this.player = player;
    }

    public float calculateRoll() {

        double roll = baseRoll;

        if (player.getFlight().isFlying()) {
            Vec3d vel = player.getOwner().getVelocity();

            roll -= super.calculateRoll(player.getOwner(), vel.x, vel.y, vel.z);
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

    public float calculatePitch(float pitch) {
        return pitch + getEnergyAddition();
    }

    public float calculateYaw(float yaw) {
        return yaw + getEnergyAddition();
    }

    public double calculateFieldOfView(double fov) {
        fov += player.getMagicalReserves().getExertion() / 5;
        fov += getEnergyAddition();

        return fov;
    }

    protected float getEnergyAddition() {
        int maxE = (int)Math.floor(player.getMagicalReserves().getEnergy() * 100);

        if (maxE <= 0) {
            return 0;
        }

        float energyAddition = (player.getWorld().random.nextInt(maxE) - maxE/2) / 100F;

        if (Math.abs(energyAddition) <= 0.001) {
            return 0;
        }

        return energyAddition;
    }

    public double getBaseRoll() {
        return baseRoll;
    }

    public void setBaseRoll(double roll) {
        baseRoll = roll;
    }
}
