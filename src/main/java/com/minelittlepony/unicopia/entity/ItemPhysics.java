package com.minelittlepony.unicopia.entity;

import net.minecraft.entity.ItemEntity;

class ItemPhysics extends EntityPhysics<ItemEntity> {
    public ItemPhysics(ItemEntity entity) {
        super(entity);
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