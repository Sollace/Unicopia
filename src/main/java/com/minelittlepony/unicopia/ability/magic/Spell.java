package com.minelittlepony.unicopia.ability.magic;

import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.projectile.ProjectileEntity;

/**
 * Interface for a magic spells
 */
public interface Spell extends NbtSerialisable, Affine {

    /**
     * Returns the registered type of this spell.
     */
    SpellType<?> getType();

    /**
     * Sets this effect as dead.
     */
    void setDead();

    /**
     * Returns true if this spell is dead, and must be cleaned up.
     */
    boolean isDead();

    /**
     * Returns true if this effect has changes that need to be sent to the client.
     */
    boolean isDirty();

    /**
     * Marks this effect as dirty.
     */
    void setDirty(boolean dirty);

    /**
     * Called when first attached to a gem.
     */
    default void onPlaced(Caster<?> caster) {

    }

    /**
     * Called when a gem is destroyed.
     */
    default void onDestroyed(Caster<?> caster) {
        setDead();
    }

    default boolean handleProjectileImpact(ProjectileEntity projectile) {
        return false;
    }
}
