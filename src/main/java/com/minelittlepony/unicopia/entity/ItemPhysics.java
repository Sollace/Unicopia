package com.minelittlepony.unicopia.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

class ItemPhysics extends EntityPhysics<ItemImpl> {
    public ItemPhysics(ItemImpl itemImpl) {
        super(itemImpl, ItemImpl.ITEM_GRAVITY, false);
    }

    @Override
    public void tick() {
        super.tick();

        ItemEntity owner = pony.getMaster();

        if (isGravityNegative() && !owner.getStack().isEmpty()) {
            owner.setNoGravity(true);
            owner.addVelocity(
                    0,
                    0.04
                    + calcGravity(-0.04D), // apply our own
                0
            );

            if (!owner.isOnGround()
                    || Entity.squaredHorizontalLength(owner.getVelocity()) > 9.999999747378752E-6D) {

                float above = 0.98f;
                if (owner.verticalCollision) {
                   above *= owner.world.getBlockState(owner.getBlockPos().up()).getBlock().getSlipperiness();
                   //above /= 9;
                }

                owner.setVelocity(owner.getVelocity().multiply(above, 1, above));
            }
        }
    }

    @Override
    protected void onGravitychanged() {
        if (!pony.getMaster().world.isClient) {
            float gravity = this.getBaseGravityModifier();
            setBaseGravityModifier(gravity == 0 ? 1 : gravity * 2);
            setBaseGravityModifier(gravity);
        }
    }
}