package com.minelittlepony.unicopia.ability.magic;

import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellReference;
import com.minelittlepony.unicopia.network.track.ObjectTracker;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.network.track.TrackableObject;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import net.minecraft.nbt.NbtCompound;

/**
 * Container for multiple spells
 *
 * @param <T> The owning entity
 */
class MultiSpellSlot implements SpellSlots, NbtSerialisable {
    private final Caster<?> owner;
    private final ObjectTracker<Entry<?>> tracker;

    public MultiSpellSlot(Caster<?> owner) {
        this.owner = owner;
        this.tracker = Trackable.of(owner.asEntity()).getDataTrackers().checkoutTracker(() -> new Entry<>(owner));
    }

    public ObjectTracker<?> getTracker() {
        return tracker;
    }

    @Override
    public boolean contains(UUID id) {
        return tracker.contains(id)
            || tracker.values().stream().anyMatch(s -> s.spell.equalsOrContains(id));
    }

    @Override
    public void put(@Nullable Spell effect) {
        if (effect != null) {
            tracker.add(new Entry<>(owner, effect));
        }
    }

    @Override
    public void remove(UUID id, boolean force) {
        tracker.remove(id, force);
    }

    @Override
    public <T extends Spell> Stream<T> stream(@Nullable SpellPredicate<T> type) {
        return tracker.values().stream().flatMap(s -> s.spell.findMatches(type));
    }

    @Override
    public boolean clear(boolean force) {
        return tracker.clear(force);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.put("spells", tracker.toNBT());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        tracker.fromNBT(compound.getCompound("spells"));
    }

    static final class Entry<T extends Spell> implements TrackableObject {
        private final Caster<?> owner;
        final SpellReference<T> spell = new SpellReference<>();
        private Status status = Status.NEW;

        public Entry(Caster<?> owner) {
            this.owner = owner;
        }
        public Entry(Caster<?> owner, T spell) {
            this.owner = owner;
            this.spell.set(spell);
            if (owner instanceof UpdateCallback callback) {
                callback.onSpellAdded(spell);
            }
        }

        @Override
        public void discard(boolean immediate) {
            if (immediate) {
                spell.set(null, owner);
            } else {
                Spell s = spell.get();
                if (s != null) {
                    s.setDead();
                    s.tickDying(owner);
                }
            }
        }

        @Override
        public UUID getUuid() {
            return spell.get().getUuid();
        }

        @Override
        public Status getStatus() {
            try {
                if (spell.get() == null) {
                    return Status.REMOVED;
                }
                if (spell.hasDirtySpell()) {
                    return Status.UPDATED;
                }
                return status;
            } finally {
                status = Status.DEFAULT;
            }
        }

        @Override
        public NbtCompound toTrackedNbt() {
            return spell.toNBT();
        }

        @Override
        public void readTrackedNbt(NbtCompound compound) {
            spell.fromNBT(compound);
        }
    }
}
