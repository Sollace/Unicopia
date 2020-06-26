package com.minelittlepony.unicopia.ducks;

import com.minelittlepony.unicopia.equine.ItemImpl;

public interface IItemEntity extends PonyContainer<ItemImpl> {

    int getAge();

    int getPickupDelay();
}
