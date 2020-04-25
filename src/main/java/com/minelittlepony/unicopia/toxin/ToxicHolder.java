package com.minelittlepony.unicopia.toxin;

import net.minecraft.item.FoodComponent;

public interface ToxicHolder {
    void setFood(FoodComponent food);
    void setToxic(Toxic toxic);
}
