package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
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
    default boolean apply(Caster<?> caster) {
        caster.getSpellSlot().put(this);
        return true;
    }

    /**
     * Called to generate this spell's effects.
     * @param caster    The caster currently fueling this spell
     * @param situation The situation in which the spell is being applied.
     */
    boolean tick(Caster<?> caster, Situation situation);

    /**
     * Marks this effect as dirty.
     */
    void setDirty();

    /**
     * Called when a gem is destroyed.
     */
    void onDestroyed(Caster<?> caster);

    /**
     * Used by crafting to combine two spells into one.
     *
     * Returns a compound spell representing the union of this and the other spell.
     */
    default Spell combineWith(Spell other) {
        return SpellType.COMPOUND_SPELL.create(SpellTraits.EMPTY).combineWith(this).combineWith(other);
    }

    /**
     * Converts this spell into a placeable spell.
     */
    default PlaceableSpell toPlaceable() {
        return SpellType.PLACED_SPELL.create(SpellTraits.EMPTY).setSpell(this);
    }

    /**
     * Converts this spell into a throwable spell.
     * @return
     */
    default ThrowableSpell toThrowable() {
        return SpellType.THROWN_SPELL.create(SpellTraits.EMPTY).setSpell(this);
    }
}
