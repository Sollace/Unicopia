package com.minelittlepony.unicopia.projectile;

import java.util.function.Function;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public interface ProjectileDelegate {
    interface ConfigurationListener extends ProjectileDelegate {
        Function<Object, ConfigurationListener> PREDICATE = a -> a instanceof ConfigurationListener ? (ConfigurationListener)a : null;

        void configureProjectile(MagicProjectileEntity projectile, Caster<?> caster);
    }

    interface HitListener extends BlockHitListener, EntityHitListener {
        @Override
        default void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
            onImpact(projectile);
        }

        @Override
        default void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
            onImpact(projectile);
        }

        void onImpact(MagicProjectileEntity projectile);
    }

    interface BlockHitListener extends ProjectileDelegate {
        Function<Object, BlockHitListener> PREDICATE = a -> a instanceof BlockHitListener ? (BlockHitListener)a : null;
        /**
         * Called once the projectile lands either hitting the ground or an entity.
         */
        void onImpact(MagicProjectileEntity projectile, BlockHitResult hit);
    }

    interface EntityHitListener extends ProjectileDelegate {
        Function<Object, EntityHitListener> PREDICATE = a -> a instanceof EntityHitListener ? (EntityHitListener)a : null;
        /**
         * Called once the projectile lands either hitting the ground or an entity.
         */
        void onImpact(MagicProjectileEntity projectile, EntityHitResult hit);
    }
}
