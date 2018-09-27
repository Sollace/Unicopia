package com.minelittlepony.unicopia.player;

import com.minelittlepony.transform.MotionCompositor;

import net.minecraft.entity.player.EntityPlayer;

class PlayerView extends MotionCompositor implements IView {

    private final IPlayer player;

    private double baseRoll = 0;

    public PlayerView(IPlayer player) {
        this.player = player;
    }

    @Override
    public double calculateRoll(EntityPlayer entity) {

        double roll = baseRoll;

        if (player.getGravity().isFlying()) {
            roll -= super.calculateRoll(entity, entity.motionX, entity.motionY, entity.motionZ);
        }

        return player.getInterpolator().interpolate("roll", (float)roll, 100);
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
