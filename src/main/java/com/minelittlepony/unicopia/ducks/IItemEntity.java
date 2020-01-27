package com.minelittlepony.unicopia.ducks;

import com.minelittlepony.unicopia.entity.capabilities.ItemEntityCapabilities;

public interface IItemEntity extends IRaceContainerHolder<ItemEntityCapabilities> {

    int getAge();

    int getPickupDelay();
}
