package com.minelittlepony.unicopia.entity;

public interface IItemEntity extends Equine.Container<ItemImpl> {
    int getAge();

    int getPickupDelay();
}
