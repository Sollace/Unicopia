package com.minelittlepony.unicopia.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;

public interface ProjectileDelegate<T extends ProjectileEntity> {
    /**
     * Called once the projectile lands either hitting the ground or an entity.
     */
    default void onImpact(T projectile, BlockPos pos, BlockState state) {}

    /**
     * Called once the projectile lands either hitting the ground or an entity.
     */
    default void onImpact(T projectile, Entity entity) {}
}
