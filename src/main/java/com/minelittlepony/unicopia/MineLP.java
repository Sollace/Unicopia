package com.minelittlepony.unicopia;

import com.minelittlepony.MineLittlePony;
import com.minelittlepony.pony.data.PonyRace;

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

                // always true, but this will throw if we don't have what we need.
                modIsActive = PonyRace.HUMAN.isHuman();
            } catch (Exception e) {
                modIsActive = false;
            }
        }
        return modIsActive;
    }
}