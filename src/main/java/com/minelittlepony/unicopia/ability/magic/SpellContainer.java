package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.SpellPredicate;

public interface SpellContainer {
    SpellContainer EMPTY = new SpellContainer() {
        @Override
        public <T extends Spell> Optional<T> get(SpellPredicate<T> type, boolean update) {
            return Optional.empty();
        }

        @Override
        public void put(Spell effect) { }
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
}
