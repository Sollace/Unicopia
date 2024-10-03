package com.minelittlepony.unicopia.ability.magic;

import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

/**
 * Container for a single spell
 *
 * @param <T> The owning entity
 */
class SingleSpellSlot implements SpellSlots, NbtSerialisable {
    private final Caster<?> owner;
    private final MultiSpellSlot.Entry<Spell> entry;

    public SingleSpellSlot(Caster<?> owner) {
        this.owner = owner;
        this.entry = new MultiSpellSlot.Entry<>(owner);
        Trackable.of(owner.asEntity()).getDataTrackers().getPrimaryTracker().startTracking(entry);
    }

    @Override
    public boolean contains(UUID id) {
        return entry.spell.equalsOrContains(id);
    }

    @Override
    public void put(@Nullable Spell effect) {
        entry.spell.set(effect, owner);
    }

    @Override
    public void remove(UUID id, boolean force) {
        if (contains(id)) {
            entry.discard(force);
        }
    }

    @Override
    public <T extends Spell> Stream<T> stream(@Nullable SpellPredicate<T> type) {
        return entry.spell.findMatches(type);
    }

    @Override
    public boolean clear(boolean force) {
        if (entry.spell.get() != null) {
            entry.discard(force);
            return true;
        }
        return false;
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.put("effect", entry.spell.toNBT(lookup));
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        entry.spell.fromNBT(compound.getCompound("effect"), lookup);
    }

    @Override
    public void copyFrom(SpellSlots other, boolean alive) {
        other.get().ifPresent(this::put);
    }
}
