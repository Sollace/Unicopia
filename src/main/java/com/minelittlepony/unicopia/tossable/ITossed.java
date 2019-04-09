package com.minelittlepony.unicopia.tossable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;

public interface ITossed extends IProjectile {

    void setItem(ItemStack stack);

    void setThrowDamage(float damage);

    float getThrowDamage();

    void setHydrophobic();

    boolean getHydrophobic();

    void launch(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy);

    default void launch(double x, double y, double z, float velocity, float inaccuracy) {
        shoot(x, y, z, velocity, inaccuracy);
    }
}
