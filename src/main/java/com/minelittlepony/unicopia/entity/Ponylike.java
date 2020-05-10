package com.minelittlepony.unicopia.entity;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ducks.PonyContainer;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Tickable;

public interface Ponylike<T extends Entity> extends NbtSerialisable, Tickable {
    Race getSpecies();

    Physics getPhysics();

    void setSpecies(Race race);

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
     * Called when this entity jumps
     */
    default void onJump() {

    }

    @Nullable
    static <T extends Ponylike<?>> T of(Entity entity) {
        return PonyContainer.<Entity, T>of(entity)
                .map(PonyContainer::get)
                .orElse(null);
    }
}
