package com.minelittlepony.unicopia;

import com.minelittlepony.MineLittlePony;

public final class MineLP {
    private static boolean checkComplete;
    private static boolean modIsActive;

    /**
     * Returns true if mine little pony is present. That's all we need.
     */
    public static boolean modIsActive() {
        if (!checkComplete) {
            try {
                MineLittlePony.getInstance();
                modIsActive = true;
            } catch (Exception e) {
                modIsActive = false;
            }
        }
        return modIsActive;
    }
}