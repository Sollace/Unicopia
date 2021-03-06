package com.minelittlepony.unicopia.ability.magic;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.util.NbtSerialisable;

/**
 * Interface for a magic spells
 */
public interface Spell extends NbtSerialisable, Affine {

    /**
     * Returns the registered type of this spell.
     */
    SpellType<?> getType();

    /**
     * The unique id of this particular spell instance.
     */
    UUID getUuid();

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
     * Applies this spell to the supplied caster.
     */
    boolean apply(Caster<?> caster);

    /**
     * Marks this effect as dirty.
     */
    void setDirty();

    /**
     * Called when a gem is destroyed.
     */
    void onDestroyed(Caster<?> caster);
}
