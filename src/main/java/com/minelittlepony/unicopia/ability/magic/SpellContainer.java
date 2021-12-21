package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
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
        public void removeIf(Predicate<Spell> effect, boolean update) { }
    };

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
     */
    void removeIf(Predicate<Spell> effect, boolean update);

    /**
     * Removes all effects currently active in this slot.
     */
    void clear();

    interface Delegate extends SpellContainer {

        SpellContainer delegate();

        @Override
        default <T extends Spell> Optional<T> get(@Nullable SpellPredicate<T> type, boolean update) {
            return delegate().get(type, update);
        }

        @Override
        default void put(@Nullable Spell effect) {
            delegate().put(effect);
        }

        @Override
        default void removeIf(Predicate<Spell> effect, boolean update) {
            delegate().removeIf(effect, update);
        }

        @Override
        default void clear() {
            delegate().clear();
        }
    }
}
