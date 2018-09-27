package com.minelittlepony.pony.data;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Stub
@SideOnly(Side.CLIENT)
public enum PonyRace {
    HUMAN,
    EARTH,
    PEGASUS,
    UNICORN,
    ALICORN,
    CHANGELING,
    ZEBRA,
    REFORMED_CHANGELING,
    GRIFFIN,
    HIPPOGRIFF,
    BATPONY,
    SEAPONY;

    public boolean hasHorn() {
        return false;
    }

    public boolean hasWings() {
        return false;
    }

    public boolean isHuman() {
        return this == HUMAN;
    }
}
