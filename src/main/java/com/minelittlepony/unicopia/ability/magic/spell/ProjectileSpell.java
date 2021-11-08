package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Magic effects that can be thrown.
 */
public interface ProjectileSpell extends Spell, ProjectileDelegate {

    @Override
    default void onImpact(MagicProjectileEntity projectile, BlockPos pos, BlockState state) {
        if (!projectile.isClient()) {
            tick(projectile, Situation.PROJECTILE);
        }
    }

    default void configureProjectile(MagicProjectileEntity projectile, Caster<?> caster) { }
}
