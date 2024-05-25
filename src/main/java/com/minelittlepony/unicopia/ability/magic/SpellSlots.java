package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.NbtSerialisable;

public interface SpellSlots extends NbtSerialisable, Copyable<SpellSlots> {
    static SpellInventory ofUnbounded(Caster<?> caster) {
        return new SpellInventory(caster, new MultiSpellSlot(caster));
    }

    static SpellInventory ofSingle(Caster<?> caster) {
        return new SpellInventory(caster, new SingleSpellSlot(caster));
    }

    /**
     * Gets all active effects for this caster that match the given type.
     */
    <T extends Spell> Stream<T> stream(@Nullable SpellPredicate<T> type);

    /**
     * Sets the active effect.
     */
    void put(@Nullable Spell effect);

    /**
     * Removes all effects currently active in this slot.
     */
    boolean clear(boolean force);

    /**
     * Cleanly removes a spell from this spell container.
     *
     * @param spellid ID of the spell to remove.
     */
    void remove(UUID spellid, boolean force);

    /**
     * Checks if a spell with the given uuid is present.
     */
    boolean contains(UUID id);

    /**
     * Gets the active effect for this caster
     */
    default <T extends Spell> Optional<T> get() {
        return get(null);
    }

    /**
     * Checks if any matching spells are active.
     */
    default boolean contains(@Nullable SpellPredicate<?> type) {
        return get(type).isPresent();
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    default <T extends Spell> Optional<T> get(@Nullable SpellPredicate<T> type) {
        return stream(type).findFirst();
    }

    /**
     * Removes all effects currently active in this slot.
     */
    default boolean clear() {
        return clear(false);
    }

    /**
     * Cleanly removes a spell from this spell container.
     *
     * @param spellid ID of the spell to remove.
     */
    default void remove(UUID spellid) {
        remove(spellid, false);
    }

    /**
     * Removes all active effects that match or contain a matching effect.
     *
     * @return True if the collection was changed
     */
    default boolean removeIf(SpellPredicate<?> test) {
        return removeWhere(spell -> spell.findMatches(test).findFirst().isPresent());
    }

    /**
     * Removes all matching top level active effects.
     *
     * @return True if the collection was changed
     */
    default boolean removeWhere(SpellPredicate<?> test) {
        return reduce((initial, spell) -> {
            if (test.test(spell)) {
                remove(spell.getUuid());
                return true;
            }
            return initial;
        });
    }

    /**
     * Gets all active effects for this caster updating it if needed.
     */
    default Stream<Spell> stream() {
        return stream(null);
    }

    default boolean reduce(BiFunction<Boolean, Spell, Boolean> alteration) {
        return stream().reduce(false, alteration, (a, b) -> b);
    }

    public interface UpdateCallback {
        void onSpellAdded(Spell spell);
    }
}
