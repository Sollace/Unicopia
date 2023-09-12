package com.minelittlepony.unicopia.item.toxin;

import com.minelittlepony.unicopia.entity.ItemImpl;

import net.minecraft.item.*;

public interface ItemDuck extends ItemConvertible, ToxicHolder, ItemImpl.TickableItem {
    void setFoodComponent(FoodComponent food);
}
