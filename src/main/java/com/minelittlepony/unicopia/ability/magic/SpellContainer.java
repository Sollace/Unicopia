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
        return get(type, true).isPresent();
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    default <T extends Spell> Optional<T> get(boolean update) {
        return get(null, update);
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    <T extends Spell> Optional<T> get(@Nullable SpellPredicate<T> type, boolean update);

    /**
     * Sets the active effect.
     */
    void put(@Nullable Spell effect);

    /**
     * Removes all matching active effects.
     *
     * @return True if the collection was changed
     */
    boolean removeIf(Predicate<Spell> test, boolean update);

    /**
     * Iterates active spells and optionally removes matching ones.
     *
     * @return True if any matching spells remain active
     */
    boolean forEach(Function<Spell, Operation> action, boolean update);

    Stream<Spell> stream(boolean update);

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
