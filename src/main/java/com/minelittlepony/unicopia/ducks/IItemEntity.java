package com.minelittlepony.unicopia.ducks;

import com.minelittlepony.unicopia.entity.ItemEntityCapabilities;

public interface IItemEntity extends PonyContainer<ItemEntityCapabilities> {

    int getAge();

    int getPickupDelay();
}
