package com.minelittlepony.unicopia.extern;

import com.minelittlepony.unicopia.item.ItemAlicornAmulet;

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

    public static ItemAlicornAmulet alicornAmulet(String domain, String name) {
        if (isModActive()) {
            return new BaubleAlicornAmulet(domain, name);
        }
        return new ItemAlicornAmulet(domain, name);
    }

    public static int isBaubleEquipped(EntityPlayer player, Item bauble) {
        if (isModActive()) {
            return BaublesApi.isBaubleEquipped(player, bauble);
        }

        return -1;
    }
}
