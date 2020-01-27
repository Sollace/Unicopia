package com.minelittlepony.unicopia.core.ducks;

import com.minelittlepony.unicopia.core.entity.ItemEntityCapabilities;

public interface IItemEntity extends IRaceContainerHolder<ItemEntityCapabilities> {

    int getAge();

    int getPickupDelay();
}
