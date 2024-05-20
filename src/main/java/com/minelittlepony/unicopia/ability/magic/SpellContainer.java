package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;

public interface SpellContainer {
    /**
     * Checks if a spell with the given uuid is present.
     */
    boolean contains(UUID id);

    /**
     * Checks if any matching spells are active.
     */
    default boolean contains(@Nullable SpellPredicate<?> type) {
        return get(type).isPresent();
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    default <T extends Spell> Optional<T> get() {
        return get(null);
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    default <T extends Spell> Optional<T> get(@Nullable SpellPredicate<T> type) {
        return stream(type).findFirst();
    }

    /**
     * Sets the active effect.
     */
    void put(@Nullable Spell effect);

    /**
     * Cleanly removes a spell from this spell container.
     *
     * @param spellid ID of the spell to remove.
     */
    void remove(UUID spellid);

    /**
     * Removes all active effects that match or contain a matching effect.
     *
     * @return True if the collection was changed
     */
    default boolean removeIf(Predicate<Spell> test) {
        return removeWhere(spell -> spell.findMatches(test).findFirst().isPresent());
    }

    /**
     * Removes all matching top level active effects.
     *
     * @return True if the collection was changed
     */
    boolean removeWhere(Predicate<Spell> test);

    /**
     * Iterates active spells and optionally removes matching ones.
     *
     * @return True if any matching spells remain active
     */
    boolean forEach(Function<Spell, Operation> action);


    /**
     * Gets all active effects for this caster updating it if needed.
     */
    default Stream<Spell> stream() {
        return stream(null);
    }

    /**
     * Gets all active effects for this caster that match the given type.
     */
    <T extends Spell> Stream<T> stream(@Nullable SpellPredicate<T> type);

    /**
     * Removes all effects currently active in this slot.
     */
    boolean clear();

    public enum Operation {
        SKIP,
        KEEP,
        REMOVE;

        public static Operation ofBoolean(boolean result) {
            return result ? KEEP : REMOVE;
        }
    }
}
