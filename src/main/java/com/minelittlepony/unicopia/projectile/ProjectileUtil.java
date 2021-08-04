package com.minelittlepony.unicopia.projectile;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.mixin.MixinPersistentProjectileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;

public interface ProjectileUtil {

    /**
     * Checks if the given entity is a projectile.
     */
    static boolean isProjectile(Entity e) {
        return e instanceof ProjectileEntity;
    }

    /**
     * Checks if the given entity is a projectile that is not stuck in the ground.
     */
    static boolean isFlyingProjectile(Entity e) {
        return isProjectile(e) && !(e instanceof MixinPersistentProjectileEntity && ((MixinPersistentProjectileEntity)e).isInGround());
    }

    /**
     * Checks if the given projectile was thrown by the given entity
     */
    static <T extends Entity> boolean isProjectileThrownBy(Entity throwable, @Nullable T e) {
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
    static void setThrowableHeading(Entity throwable, Vec3d heading, float velocity, float inaccuracy) {

        if (throwable instanceof ProjectileEntity) {
            ((ProjectileEntity)throwable).setVelocity(heading.x, heading.y, heading.z, velocity, inaccuracy);
        } else {
            heading = heading.normalize().multiply(velocity);

            Vec3d vel = throwable.getVelocity();

            throwable.addVelocity(heading.x - vel.x, heading.y - vel.y, heading.z - vel.z);
        }
    }

    /**
     * Reverses a projectile's direction to deflect it off a surface.
     */
    static void ricochet(Entity projectile, Vec3d pos, float absorbtionRate) {
        Vec3d position = projectile.getPos();
        Vec3d motion = projectile.getVelocity();

        Vec3d normal = position.subtract(pos).normalize();
        Vec3d approach = motion.subtract(normal);

        if (approach.length() < motion.length()) {
            normal = normal.multiply(-1);
        }

        setThrowableHeading(projectile, normal, (float)motion.length() * absorbtionRate, 0);
    }
}
