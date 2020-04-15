package com.minelittlepony.unicopia.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Calculates roll and incline for a player based on their motion vectors.
 *
 * @author Polyakovich - Big thank you to this dude and his meta-physics friend for working this all out.
 */
public abstract class MotionCompositor {

    static double clampLimit(double num, double limit) {
        return MathHelper.clamp(num, -limit, limit);
    }

    static float sensibleAngle(float angle) {
        angle %= 360;

        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;

        return angle;
    }
    /**
     * Gets the angle of horizontal roll in degrees based on the player's vertical and horizontal motion.
     */
    protected double calculateRoll(PlayerEntity player, double motionX, double motionY, double motionZ) {

        // since model roll should probably be calculated from model rotation rather than entity rotation...
        double roll = sensibleAngle(player.field_6220 - player.field_6220);
        double horMotion = Math.sqrt(motionX * motionX + motionZ * motionZ);
        float modelYaw = sensibleAngle(player.field_6220);

        // detecting that we're flying backwards and roll must be inverted
        if (Math.abs(sensibleAngle((float) Math.toDegrees(Math.atan2(motionX, motionZ)) + modelYaw)) > 90) {
            roll *= -1;
        }

        // ayyy magic numbers (after 5 - an approximation of nice looking coefficients calculated by hand)

        // roll might be zero, in which case Math.pow produces +Infinity. Anything x Infinity = NaN.
        double pow = roll != 0 ? Math.pow(Math.abs(roll), -0.191) : 0;

        roll *= horMotion * 5 * (3.6884f * pow);

        assert !Float.isNaN((float)roll);

        return MathHelper.clamp(roll, -54, 54);
    }

    /**
     * Gets the angle of inclination in degrees based on the player's vertical and horizontal motion.
     */
    protected double calculateIncline(PlayerEntity player, double motionX, double motionY, double motionZ) {
        double dist = Math.sqrt(motionX * motionX + motionZ * motionZ);
        double angle = Math.atan2(motionY, dist);

        if (!player.abilities.allowFlying) {
            angle /= 2;
        }

        angle = clampLimit(angle, Math.PI / 3);

        return Math.toDegrees(angle);
    }

}
