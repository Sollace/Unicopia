package com.minelittlepony.unicopia.core.util.projectile;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.Projectile;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.thrown.ThrownEntity;
import net.minecraft.util.math.Vec3d;

public class ProjectileUtil {

    /**
     * Checks if the given entity is a projectile.
     */
    public static boolean isProjectile(Entity e) {
        return e instanceof Projectile
            || e instanceof FireballEntity;
    }

    /**
     * Checks if an entity is a thrown projectile.
     */
    public static boolean isThrowable(Entity e) {
        return e instanceof ThrownEntity
            || e instanceof ArrowEntity
            || e instanceof FireballEntity;
    }

    /**
     * Checks if the given projectile was thrown by the given entity
     */
    public static <T extends Entity> boolean isProjectileThrownBy(Entity throwable, @Nullable T e) {
        if (e == null || !isThrowable(throwable)) {
            return false;
        }

        return e.equals(getThrowingEntity(throwable));
    }

    /**
     * Gets the thrower for a projectile or null
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T getThrowingEntity(Entity throwable) {

        if (throwable instanceof ProjectileEntity) {
            return (T)((ProjectileEntity) throwable).getOwner();
        }

        if (throwable instanceof FireballEntity) {
            return (T)((FireballEntity) throwable).owner;
        }

        if (throwable instanceof LlamaSpitEntity) {
           return (T)((LlamaSpitEntity) throwable).owner;
        }

        if (throwable instanceof ThrownEntity) {
            return (T)((ThrownEntity) throwable).getOwner();
        }

        return null;
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

        if (throwable instanceof Projectile) {
            ((Projectile)throwable).setVelocity(heading.x, heading.y, heading.z, velocity, inaccuracy);
        } else {
            heading = heading.normalize().multiply(velocity);

            Vec3d vel = throwable.getVelocity();

            throwable.addVelocity(heading.x - vel.x, heading.y - vel.y, heading.z - vel.z);
        }
    }
}
