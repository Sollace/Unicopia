package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;

public interface SpellContainer {
    SpellContainer EMPTY = new SpellContainer() {
        @Override
        public <T extends Spell> Optional<T> get(SpellPredicate<T> type, boolean update) {
            return Optional.empty();
        }

        @Override
        public void put(Spell effect) { }

        @Override
        public void clear() { }

        @Override
        public boolean removeIf(Predicate<Spell> effect, boolean update) {
            return false;
        }

        @Override
        public boolean forEach(Function<Spell, Operation> action, boolean update) {
            return false;
        }

        @Override
        public boolean contains(UUID id) {
            return false;
        }
    };

    /**
     * Checks if a spell with the given uuid is present.
     */
    boolean contains(UUID id);

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    default <T extends Spell> Optional<T> get(boolean update) {
        return get(null, update);
    }

    /**
     * Returns true if this caster has an active effect attached to it.
     */
    default boolean isPresent() {
        return get(true).isPresent();
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

    /**
     * Removes all effects currently active in this slot.
     */
    void clear();

    interface Delegate extends SpellContainer {

        SpellContainer delegate();

        @Override
        default boolean contains(UUID id) {
            return delegate().contains(id);
        }

        @Override
        default <T extends Spell> Optional<T> get(@Nullable SpellPredicate<T> type, boolean update) {
            return delegate().get(type, update);
        }

        @Override
        default void put(@Nullable Spell effect) {
            delegate().put(effect);
        }

        @Override
        default boolean removeIf(Predicate<Spell> effect, boolean update) {
            return delegate().removeIf(effect, update);
        }

        @Override
        default boolean forEach(Function<Spell, Operation> action, boolean update) {
            return delegate().forEach(action, update);
        }

        @Override
        default void clear() {
            delegate().clear();
        }
    }

    public enum Operation {
        SKIP,
        KEEP,
        REMOVE
    }
}
