package com.minelittlepony.unicopia.entity;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;

public interface Equine<T extends Entity> extends NbtSerialisable, Tickable, ProjectileImpactListener, EntityConvertable<T> {
    Race getSpecies();

    Physics getPhysics();

    void setSpecies(Race race);

    /**
     * Gets the last magical entity to attack us.
     */
    Entity getAttacker();

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
    @Override
    default boolean onProjectileImpact(ProjectileEntity projectile) {
        return false;
    }

    /**
     * Called when this entity jumps
     */
    default void onJump() {

    }

    /**
     * Called when an entity is harmed.
     */
    default Optional<Boolean> onDamage(DamageSource source, float amount) {
        return Optional.empty();
    }

    static <E extends Entity, T extends Equine<E>> Optional<T> of(@Nullable E entity) {
        return PonyContainer.<E, T>of(entity).map(PonyContainer::get);
    }
}
