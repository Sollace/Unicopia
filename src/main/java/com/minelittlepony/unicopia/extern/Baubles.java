package com.minelittlepony.unicopia.extern;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.forgebullshit.FUF;
import com.minelittlepony.unicopia.item.ItemAlicornAmulet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class Baubles {

    private static boolean checkComplete;
    private static boolean modIsActive;

    @FUF(reason = "Forge is so strict with their class loading it's a ff**8 joke")
    public static boolean isModActive() {
        if (!checkComplete) {
            checkComplete = true;

            try {
                modIsActive = baubles.api.BaubleType.AMULET.getValidSlots().length > 0;
            } catch (Throwable e) {
                Unicopia.log.error("Baubles are not present. Continuing without them.");

                modIsActive = false;
            }
        }

        if (modIsActive) {
            Unicopia.log.debug("Baubles detected. baubles.api.BaubleType.AMULET.getValidSlots().length > 0 is true.");
        }

        return modIsActive;
    }

    public static ItemAlicornAmulet alicornAmulet() {
        try {
            if (isModActive()) {
                Unicopia.log.debug("Constructing BaubleAlicornAmulet.");

                // FUCK YOU FORGE YOU PIECE OF SHIT
                // DON'T LOAD THIS GOD DAMN CLASS UNTIL I FUCKING TELL YOU TO

                Class<?> cls = ClassLoader.getSystemClassLoader().loadClass("com.minelittlepony.unicopia.extern.BaubleAlicornAmulet");

                return (ItemAlicornAmulet)cls.getConstructor(String.class, String.class).newInstance(Unicopia.MODID, "alicorn_amulet");
            }
        } catch (Throwable e) {
            Unicopia.log.error("Unicopia-Baubles support failed to load.", e);
        }

        return new ItemAlicornAmulet(Unicopia.MODID, "alicorn_amulet");
    }

    public static int isBaubleEquipped(EntityPlayer player, Item bauble) {
        if (isModActive()) {
            return baubles.api.BaublesApi.isBaubleEquipped(player, bauble);
        }

        return -1;
    }
}
