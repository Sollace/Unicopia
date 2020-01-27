package com.minelittlepony.unicopia.core.entity;

import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.util.InbtSerialisable;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;

public interface IEntity extends InbtSerialisable, Updatable {
    Race getSpecies();

    void setSpecies(Race race);

    void onDimensionalTravel(int destinationDimension);

    /**
     * Returns true if this player is fully invisible.
     * Held items and other effects will be hidden as well.
     */
    default boolean isInvisible() {
        return false;
    }

    /**
     * Sets whether this player should be invisible.
     */
    default void setInvisible(boolean invisible) {

    }

    /**
     * Called at the beginning of an update cycle.
     */
    default boolean beforeUpdate() {
        return false;
    }


    /**
     * Event triggered when this entity is hit by a projectile.
     */
    default boolean onProjectileImpact(ProjectileEntity projectile) {
        return false;
    }

    /**
     * Called when this player finishes eating food.
     */
    default void onUse(ItemStack stack) {

    }

    /**
     * Called when this entity jumps
     */
    default void onJump() {

    }
}
