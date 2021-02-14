package com.minelittlepony.unicopia.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface ProjectileDelegate {
    /**
     * Called once the projectile lands either hitting the ground or an entity.
     */
    void onImpact(MagicProjectileEntity projectile, BlockPos pos, BlockState state);

    /**
     * Called once the projectile lands either hitting the ground or an entity.
     */
    void onImpact(MagicProjectileEntity projectile, Entity entity);
}
