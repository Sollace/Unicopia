package com.minelittlepony.unicopia.entity;

import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Tickable;

public interface Equine<T extends Entity> extends NbtSerialisable, Tickable {
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

    /**
     * Called when an entity is harmed.
     */
    default Optional<Boolean> onDamage(DamageSource source, float amount) {
        return Optional.empty();
    }

    @Nullable
    static <T extends Equine<?>> T of(Entity entity) {
        return PonyContainer.<Entity, T>of(entity)
                .map(PonyContainer::get)
                .orElse(null);
    }
}
