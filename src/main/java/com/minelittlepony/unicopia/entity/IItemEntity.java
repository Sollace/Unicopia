package com.minelittlepony.unicopia.entity;

public interface IItemEntity extends PonyContainer<ItemImpl> {

    int getAge();

    int getPickupDelay();
}
