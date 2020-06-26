package com.minelittlepony.unicopia.util.projectile;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;

public class ProjectileUtil {

    /**
     * Checks if the given entity is a projectile.
     */
    public static boolean isProjectile(Entity e) {
        return e instanceof ProjectileEntity;
    }

    /**
     * Checks if the given projectile was thrown by the given entity
     */
    public static <T extends Entity> boolean isProjectileThrownBy(Entity throwable, @Nullable T e) {
        return e != null && isProjectile(throwable) && e.equals(((ProjectileEntity) throwable).getOwner());
    }

    /**
     * Sets the velocity and heading for a projectile.
     *
     * @param throwable     The projectile
     * @param heading       The directional heaving vector
     * @param velocity      Velocity
     * @param inaccuracy    Inaccuracy
     * @return              True the projectile's heading was set, false otherwise
     */
    public static void setThrowableHeading(Entity throwable, Vec3d heading, float velocity, float inaccuracy) {

        if (throwable instanceof ProjectileEntity) {
            ((ProjectileEntity)throwable).setVelocity(heading.x, heading.y, heading.z, velocity, inaccuracy);
        } else {
            heading = heading.normalize().multiply(velocity);

            Vec3d vel = throwable.getVelocity();

            throwable.addVelocity(heading.x - vel.x, heading.y - vel.y, heading.z - vel.z);
        }
    }
}
