package com.minelittlepony.unicopia.entity.player;

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
        // sine(angle) = y/h
        // cos(angle) = x/h

        Vec3d climbVector = new Vec3d(Math.cos(pitchAngle), Math.sin(pitchAngle), 0);
        Vec3d bankVector =  new Vec3d(0,                    Math.sin(rollAngle),  Math.cos(rollAngle));

        return bankVector.add(climbVector).normalize();
    }

    /**
     * Gets the normalized perpendicular vector.
     */
    private Vec3d getNormal() {
        // sine(angle) = y/h
        // cos(angle) = x/h

        Vec3d climbVector = new Vec3d(Math.cos(pitchAngle), Math.sin(pitchAngle), 0);
        Vec3d bankVector =  new Vec3d(0,                    Math.sin(rollAngle),  Math.cos(rollAngle));

        return bankVector.crossProduct(climbVector).normalize();
    }

    /**
     * Returns the acceleration vector due to gravity
     * parallel to the slope of the incline described
     * by the roll and pitch components.
     *
     * @param gravity The global gravitation constant C
     */
    public Vec3d calcGravitationalAccelleration(double gravity) {
        return getMomentVector().multiply(
               -gravity * Math.signum(pitchAngle),
               -gravity,
               -gravity * Math.signum(rollAngle)
        );
    }

    /**
     * Gets the added thrust vector for the given forwards motion
     * and velocity projected against the direction of incline.
     *
     * @param forwards The forwards thrust speed
     * @param velocity The current motion vector
     */
    public Vec3d calcThrustVelocity(double forwards) {
        return getNormal().add(getMomentVector())
                .normalize()
                .multiply(forwards * (1 - getDrag()));
    }

    /**
     * The drag due to air resistance.
     */
    public double getDrag() {
        return 0.0078; // magic number until I figure out what tf I'm doing
    }
}
