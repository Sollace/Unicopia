package com.minelittlepony.unicopia.ducks;

import com.minelittlepony.unicopia.entity.ItemEntityCapabilities;

public interface IItemEntity extends RaceContainerHolder<ItemEntityCapabilities> {

    int getAge();

    int getPickupDelay();
}
