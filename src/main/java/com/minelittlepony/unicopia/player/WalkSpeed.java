package com.minelittlepony.unicopia.player;

import java.lang.reflect.Field;

import com.minelittlepony.unicopia.forgebullshit.FUF;

import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.nbt.NBTTagCompound;;

/**
 * Workaround for setting the player's walking speed.
 */
@FUF(reason = "`setPlayerWalkSpeed(v)` is marked as client-only. Waiting on mixins...")
class WalkSpeed {

    private static boolean regularMethodCheck = true;

    private static boolean reflexiveMethodCheck = true;
    private static Field walkSpeed;

    public void setPlayerWalkSpeed(PlayerCapabilities capabilities, float speed) {
        if (capabilities.getWalkSpeed() == speed) {
            return;
        }

        if (!setNormally(capabilities, speed)) {
            if (!setReflexively(capabilities, speed)) {
                setTheMostSlow(capabilities, speed);
            }
        }
    }

    private boolean setNormally(PlayerCapabilities capabilities, float speed) {
        if (regularMethodCheck) {
            try {
                capabilities.setPlayerWalkSpeed(speed);
            } catch (Throwable t) {
                regularMethodCheck = false;
            }
        }

        return regularMethodCheck;
    }

    private boolean setReflexively(PlayerCapabilities capabilities, float speed) {
        if (reflexiveMethodCheck) {
            try {
                if (walkSpeed == null) {
                    Field[] f = PlayerCapabilities.class.getDeclaredFields();
                    walkSpeed = f[f.length - 1];
                    walkSpeed.setAccessible(true);
                }

                walkSpeed.set(capabilities, speed);
            } catch (Throwable t) {
                reflexiveMethodCheck = false;
            }
        }

        return reflexiveMethodCheck;
    }

    private void setTheMostSlow(PlayerCapabilities capabilities, float speed) {
        NBTTagCompound comp = new NBTTagCompound();

        capabilities.writeCapabilitiesToNBT(comp);

        comp.getCompoundTag("capabilities").setFloat("walkSpeed", speed);

        capabilities.readCapabilitiesFromNBT(comp);
    }
}
