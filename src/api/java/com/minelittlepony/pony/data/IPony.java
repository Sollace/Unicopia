package com.minelittlepony.pony.data;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Stub
@SideOnly(Side.CLIENT)
public interface IPony {

    static IPony forPlayer(AbstractClientPlayer player) {
        return null;
    }

    PonyRace getRace(boolean ignorePony);

}
