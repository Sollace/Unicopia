package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.util.serialisation.InbtSerialisable;

/**
 * Interface for a magic spells
 */
public interface IMagicEffect extends InbtSerialisable, IAligned {

    /**
     * Gets the name used to identify this effect.
     */
    String getName();

    /**
     * Gets the tint for this spell when applied to a gem.
     */
    int getTint();

    /**
     * Sets this effect as dead.
     */
    void setDead();

    /**
     * Returns true if this spell is dead, and must be cleaned up.
     */
    boolean getDead();

    /**
     * Returns true if this effect has changes that need to be sent to the client.
     */
    boolean isDirty();

    /**
     * Marks this effect as dirty.
     */
    void setDirty(boolean dirty);

    /**
     * Returns true if this effect can be crafted into a gem.
     */
    boolean isCraftable();


    /**
     * Gets the highest level this spell can be safely operated at.
     * Gems may go higher, however chance of explosion/exhaustion increases with every level.
     */
    int getMaxLevelCutOff(ICaster<?> caster);

    float getMaxExhaustion(ICaster<?> caster);

    /**
     * Gets the chances of this effect turning into an innert gem or exploding.
     */
    float getExhaustion(ICaster<?> caster);

    /**
     * Called when first attached to a gem.
     */
    default void onPlaced(ICaster<?> caster) {

    }

    /**
     * Called every tick when attached to a player.
     *
     * @param source    The entity we are currently attached to.
     * @return true to keep alive
     */
    default boolean updateOnPerson(ICaster<?> caster) {
        return update(caster);
    }

    /**
     * Called every tick when attached to an entity.
     * Called on both sides.
     *
     * @param source   The entity we are currently attached to.
     */
    boolean update(ICaster<?> source);

    /**
     * Called every tick when attached to a player. Used to apply particle effects.
     * Is only called on the client side.
     *
     * @param source    The entity we are currently attached to.
     */
    default void renderOnPerson(ICaster<?> source) {
        render(source);
    }

    /**
     * Called every tick when attached to an entity to produce particle effects.
     * Is only called on the client side.
     *
     * @param source    The entity we are attached to.
     */
    void render(ICaster<?> source);

    /**
     * Return true to allow the gem update and move.
     */
    default boolean allowAI() {
        return false;
    }

    default IMagicEffect copy() {
        return SpellRegistry.instance().copyInstance(this);
    }
}
