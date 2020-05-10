package com.minelittlepony.unicopia.ducks;

import com.minelittlepony.unicopia.entity.ItemImpl;

public interface IItemEntity extends PonyContainer<ItemImpl> {

    int getAge();

    int getPickupDelay();
}
