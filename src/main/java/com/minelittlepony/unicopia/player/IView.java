package com.minelittlepony.unicopia.player;

import net.minecraft.entity.player.EntityPlayer;

public interface IView {
    double calculateRoll(EntityPlayer player);

    double getBaseRoll();

    void setBaseRoll(double roll);
}
