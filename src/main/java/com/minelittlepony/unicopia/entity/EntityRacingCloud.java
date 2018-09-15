package com.minelittlepony.unicopia.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityRacingCloud extends EntityCloud {

    public EntityRacingCloud(World world) {
        super(world);
        setCloudSize(1);
    }

    @Override
    public boolean canBeSteered() {
        return true;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return getPassengers().size() < getCloudSize();
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        List<Entity> list = getPassengers();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void onUpdate() {
        Entity riddenByEntity = getControllingPassenger();

        if (riddenByEntity != null && canPassengerSteer()) {
            EntityLivingBase rider = (EntityLivingBase)riddenByEntity;

            double speed = 1.5F * rider.moveForward / 5;
            double horizontalDriving = (riddenByEntity.rotationYaw - rider.moveStrafing * 90) * Math.PI / 180;

            motionX += -Math.sin(horizontalDriving) * speed;
            motionZ +=  Math.cos(horizontalDriving) * speed;

            double pitch = riddenByEntity.rotationPitch * Math.PI / 180;

            motionY += -Math.sin(pitch) * (speed / 20);
        } else {
            motionY = 0;
        }

        super.onUpdate();
    }
}
