package com.minelittlepony.unicopia.projectile;

import net.minecraft.entity.projectile.ProjectileEntity;

public interface ProjectileImpactListener {
    boolean onProjectileImpact(ProjectileEntity projectile);
}
