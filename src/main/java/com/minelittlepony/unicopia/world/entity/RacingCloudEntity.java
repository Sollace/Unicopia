package com.minelittlepony.unicopia.world.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RacingCloudEntity extends CloudEntity {

    public RacingCloudEntity(EntityType<RacingCloudEntity> type, World world) {
        super(type, world);
        setCloudSize(1);
    }

    @Override
    public boolean canBeControlledByRider() {
        return true;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengerList().size() < getCloudSize();
    }

    @Override
    @Nullable
    public Entity getPrimaryPassenger() {
        List<Entity> list = getPassengerList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        if (!(hasPassengers() || isConnectedThroughVehicle(player)) && hand == Hand.MAIN_HAND) {
            if (EquinePredicates.PLAYER_PEGASUS.test(player)) {
                if (!getStationary()) {
                    player.startRiding(this);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public void tick() {
        Entity jockey = getPrimaryPassenger();

        Vec3d vel = getVelocity();
        double motionX = vel.x;
        double motionY = vel.y;
        double motionZ = vel.z;

        if (jockey != null && canBeControlledByRider()) {
            LivingEntity rider = (LivingEntity)jockey;

            double speed = 1.5F * rider.forwardSpeed / 5;
            double horizontalDriving = (jockey.yaw - rider.sidewaysSpeed * 90) * Math.PI / 180;

            motionX += -Math.sin(horizontalDriving) * speed;
            motionZ +=  Math.cos(horizontalDriving) * speed;

            double pitch = jockey.pitch * Math.PI / 180;

            motionY += -Math.sin(pitch) * (speed / 20);
        } else {
            motionY = 0;
        }
        setVelocity(motionX, motionY, motionZ);

        super.tick();
    }
}
