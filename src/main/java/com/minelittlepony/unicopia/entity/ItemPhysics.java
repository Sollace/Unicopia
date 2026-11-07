package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.network.track.DataTracker;
import net.minecraft.entity.ItemEntity;

class ItemPhysics extends EntityPhysics<ItemEntity> {
    public ItemPhysics(DataTracker tracker, EntityConvertable<ItemEntity> entity) {
        super(tracker, entity);
    }

    @Override
    protected void onGravitychanged() {
        if (!entity.getWorld().isClient) {
            float gravity = getBaseGravityModifier();
            setBaseGravityModifier(gravity == 0 ? 1 : gravity * 2);
            setBaseGravityModifier(gravity);
        }
    }
}