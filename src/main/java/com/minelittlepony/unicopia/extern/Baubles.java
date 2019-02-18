package com.minelittlepony.unicopia.extern;

import java.util.function.Supplier;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class Baubles {

    private static boolean checkComplete;
    private static boolean modIsActive;

    static boolean isModActive() {
        if (!checkComplete) {
            checkComplete = true;

            try {
                modIsActive = BaubleType.AMULET.getValidSlots().length > 0;
            } catch (Exception e) {
                modIsActive = false;
            }
        }

        return modIsActive;
    }

    public static <T> T ifActiveElseGet(Supplier<T> yes, Supplier<T> no) {
        return (isModActive() ? yes : no).get();
    }

    public static int isBaubleEquipped(EntityPlayer player, Item bauble) {
        if (isModActive()) {
            return BaublesApi.isBaubleEquipped(player, bauble);
        }

        return -1;
    }
}
