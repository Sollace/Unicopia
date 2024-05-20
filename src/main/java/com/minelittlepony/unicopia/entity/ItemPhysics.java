package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.network.track.DataTracker;

import net.minecraft.entity.ItemEntity;

class ItemPhysics extends EntityPhysics<ItemEntity> {
    public ItemPhysics(ItemEntity entity, DataTracker tracker) {
        super(entity, tracker);
    }

    @Override
    public void tick() {
        super.tick();

        if (isGravityNegative() && !entity.getStack().isEmpty()) {
            entity.setNoGravity(true);
            entity.addVelocity(
                    0,
                    0.04
                    + calcGravity(-0.04D), // apply our own
                0
            );

            if (!entity.isOnGround()
                    || entity.getVelocity().horizontalLengthSquared() > 9.999999747378752E-6D) {

                float above = 0.98f;
                if (entity.verticalCollision) {
                   above *= entity.getWorld().getBlockState(entity.getBlockPos().up()).getBlock().getSlipperiness();
                   //above /= 9;
                }

                entity.setVelocity(entity.getVelocity().multiply(above, 1, above));
            }
        }
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