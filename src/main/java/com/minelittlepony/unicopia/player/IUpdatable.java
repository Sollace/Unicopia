package com.minelittlepony.unicopia.player;

import net.minecraft.entity.Entity;

public interface IUpdatable<T extends Entity> {
    void onUpdate(T entity);
}
