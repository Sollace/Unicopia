package com.minelittlepony.unicopia.equine.player;

import net.minecraft.util.math.Vec3d;

// X - forward
// Y - vertical
// Z - sideways
public class Aeronautics {

    public double rollAngle;
    public double pitchAngle;

    /**
     * Gets the normalized direction vector
     */
    private Vec3d getMomentVector() {
        Vec3d bankVector = new Vec3d(0, Math.sin(rollAngle), Math.cos(rollAngle));
        Vec3d climbVector = new Vec3d(Math.cos(pitchAngle), Math.sin(pitchAngle), 0);

        return bankVector.crossProduct(climbVector).normalize();
    }

    /**
     * Returns the acceleration vector due to gravity in the direction of
     * the incline described by the roll and pitch components.
     *
     * @param gravity The global gravitation constant C
     */
    public Vec3d calcGravitationalAccelleration(double gravity) {
        return getMomentVector().multiply(-gravity);
    }

    /**
     * Gets the added thrust vector for the given forwards motion
     * and velocity projected against the direction of incline.
     *
     * @param forwards The forwards thrust speed
     * @param velocity The current motion vector
     */
    public Vec3d calcThrustVelocity(double forwards, Vec3d velocity) {
        velocity = velocity.normalize().multiply(forwards);

        return getMomentVector().multiply(velocity.x, velocity.y, 0).multiply(getDrag());
    }

    /**
     * The drag due to air resistance.
     */
    public double getDrag() {
        return 0.0078; // magic number until I figure out what tf I'm doing
    }
}
