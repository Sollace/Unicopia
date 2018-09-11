package com.minelittlepony.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.entity.projectile.EntityThrowable;

public class ProjectileUtil {

	/**
	 * Checks if the given entity is a projectile.
	 */
	public static boolean isProjectile(Entity e) {
		return e instanceof IProjectile
		        || e instanceof EntityFireball;
	}

	/**
	 * Checks if an entity is a thrown projectile.
	 */
	public static boolean isThrowable(Entity e) {
	    return e instanceof EntityThrowable ||
                e instanceof EntityArrow ||
                e instanceof EntityFireball;
	}

	/**
	 * Checks if the given projectile was thrown by the given entity
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> boolean isProjectileThrownBy(Entity throwable, T e) {
		if (e == null || !isProjectile(throwable)) {
		    return false;
		}

		return e.equals(getThrowingEntity(throwable));
	}

	/**
	 * Gets the thrower for a projectile or null
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getThrowingEntity(Entity throwable) {

	    if (throwable instanceof EntityArrow) {
	        return (T)((EntityArrow) throwable).shootingEntity;
	    }

	    if (throwable instanceof EntityFireball) {
            return (T)((EntityFireball) throwable).shootingEntity;
        }

	    if (throwable instanceof EntityLlamaSpit) {
           return (T)((EntityLlamaSpit) throwable).owner;
	    }

        if (throwable instanceof EntityThrowable) {
            return (T)((EntityThrowable) throwable).getThrower();
        }

		return null;
	}

	/**
	 * Sets the velocity and heading for a projectile.
	 *
	 * @param throwable		The projectile
	 * @param x				X Direction component
	 * @param y				Y Direction component
	 * @param z				Z Direction component
	 * @param velocity		Velocity
	 * @param inaccuracy	Inaccuracy
	 * @return				True the projectile's heading was set, false otherwise
	 */
	public static void setThrowableHeading(Entity throwable, double x, double y, double z, float velocity, float inaccuracy) {
		if (throwable instanceof IProjectile) {
			((IProjectile)throwable).shoot(x, y, z, velocity, inaccuracy);
		} else {
		    ((Entity)throwable).setVelocity(x, y, z);
		}
	}
}
