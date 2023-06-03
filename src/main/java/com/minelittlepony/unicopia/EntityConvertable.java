package com.minelittlepony.unicopia;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Interface for things that can be owned by an entity.
 * <p>
 * Ownership is retained so long as the owner is still active. If the owner leaves or dies, the link is broken.
 *
 * @param <E> The type of object that owns us.
 */
public interface EntityConvertable<E extends Entity> extends WorldConvertable {
    E asEntity();

    /**
     * Gets the center position where this caster is located.
     */
    default BlockPos getOrigin() {
        return asEntity().getBlockPos();
    }
    /**
     * Gets the center position where this caster is located.
     */
    default Vec3d getOriginVector() {
        return asEntity().getPos();
    }

    @Override
    default World asWorld() {
        return asEntity().getWorld();
    }
}
