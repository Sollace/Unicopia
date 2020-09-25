package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.util.MutableVector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class AeronauticalPlayerPhysics extends PlayerPhysics {

    private final Aeronautics auronautics = new Aeronautics();

    private double gravity;

    private int thrustCountdown;

    public AeronauticalPlayerPhysics(Pony pony) {
        super(pony);
    }

    @Override
    public double calcGravity(double worldConstant) {
        return gravity = super.calcGravity(worldConstant);
    }

    @Override
    protected void moveFlying(Entity player, MutableVector velocity) {

        PlayerEntity ply = (PlayerEntity)player;

        auronautics.pitchAngle = 0;
        auronautics.rollAngle = 0;

        float yaw = player.getYaw(1);

        Vec3d motion = auronautics.calcGravitationalAccelleration(gravity);

        if (ply.forwardSpeed != 0 && thrustCountdown-- <= 0) {
            thrustCountdown = 20;
            player.playSound(getWingSound(), 0.4F, 1);
            motion = motion.add(auronautics.calcThrustVelocity(-100));
        }

        motion = motion.rotateY(yaw).add(velocity.toImmutable()).multiply(1 - auronautics.getDrag());

        velocity.x = motion.x;
        velocity.y = motion.y;
        velocity.z = motion.z;
    }
}
