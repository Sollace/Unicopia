package com.minelittlepony.unicopia.client;

import com.minelittlepony.client.MineLittlePony;
import com.minelittlepony.unicopia.Race;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public final class MineLPConnector {
    public static Race getPlayerPonyRace() {
        if (!FabricLoader.getInstance().isModLoaded("minelp")) {
            return Race.HUMAN;
        }

        switch (MineLittlePony.getInstance().getManager().getPony(MinecraftClient.getInstance().player).getRace(false)) {
            case ALICORN:
                return Race.ALICORN;
            case CHANGELING:
            case CHANGEDLING:
                return Race.CHANGELING;
            case ZEBRA:
            case EARTH:
                return Race.EARTH;
            case GRYPHON:
            case HIPPOGRIFF:
            case PEGASUS:
            case BATPONY:
                return Race.PEGASUS;
            case SEAPONY:
            case UNICORN:
                return Race.UNICORN;
            default:
                return Race.EARTH;
        }
    }
}