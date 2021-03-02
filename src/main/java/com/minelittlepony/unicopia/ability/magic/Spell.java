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
     * Gets the highest level this spell can be safely operated at.
     * Gems may go higher, however chance of explosion/exhaustion increases with every level.
     */
    int getMaxLevelCutOff(Caster<?> caster);

    float getMaxExhaustion(Caster<?> caster);

    /**
     * Gets the chances of this effect turning into an innert gem or exploding.
     */
    float getExhaustion(Caster<?> caster);

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

    /**
     * Called every tick when attached to an entity.
     * Called on both sides.
     *
     * @param source   The entity we are currently attached to.
     */
    boolean update(Caster<?> source);

    /**
     * Called every tick when attached to an entity to produce particle effects.
     * Is only called on the client side.
     *
     * @param source    The entity we are attached to.
     */
    void render(Caster<?> source);

    /**
     * Return true to allow the gem update and move.
     */
    default boolean allowAI() {
        return false;
    }

    /**
     * Returns a new, deep-copied instance of this spell.
     */
    default Spell copy() {
        return SpellType.copy(this);
    }
}
