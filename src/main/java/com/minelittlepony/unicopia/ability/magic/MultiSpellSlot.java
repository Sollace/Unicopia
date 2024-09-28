package com.minelittlepony.unicopia.ability.magic;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellReference;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.network.track.MsgTrackedValues;
import com.minelittlepony.unicopia.network.track.ObjectTracker;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.network.track.TrackableObject;
import com.minelittlepony.unicopia.network.track.MsgTrackedValues.TrackerEntries;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

/**
 * Container for multiple spells
 *
 * @param <T> The owning entity
 */
class MultiSpellSlot implements SpellSlots, NbtSerialisable {
    private final Caster<?> owner;
    private final ObjectTracker<Entry<Spell>> tracker;

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
            tracker.add(effect.getUuid(), new Entry<>(owner, effect));
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
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.put("spells", NbtSerialisable.writeMap(tracker.entries(), UUID::toString, entry -> entry.spell.toNBT(lookup)));
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        tracker.load(NbtSerialisable.readMap(compound.getCompound("spells"), key -> {
            try {
                return UUID.fromString(key);
            } catch (Throwable ignore) {}
            return null;
        }, (key, nbt) -> {
            try {
                Entry<Spell> entry = new Entry<>(owner);
                entry.spell.fromNBT((NbtCompound)nbt, lookup);
                return entry;
            } catch (Throwable t) {
                Unicopia.LOGGER.warn("Exception loading tracked object: {}", t.getMessage());
            }
            return null;
        }));
    }

    static final class Entry<T extends Spell> implements TrackableObject<Entry<T>> {
        private final Caster<?> owner;
        final SpellReference<T> spell = new SpellReference<>();
        private boolean hasValue;

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
                    if (s.isDead()) {
                        s.destroy(owner);
                    }
                }
            }
        }

        @Override
        public Status getStatus() {
            boolean hasValue = spell.get() != null;
            if (hasValue != this.hasValue) {
                this.hasValue = hasValue;
                return hasValue ? Status.NEW : Status.REMOVED;
            }

            return Status.DEFAULT;
        }

        @Override
        public void readTrackedNbt(NbtCompound nbt, WrapperLookup lookup) {
            spell.fromNBT(nbt, lookup);
        }

        @Override
        public NbtCompound writeTrackedNbt(WrapperLookup lookup) {
            return spell.toNBT(lookup);
        }

        @Override
        public void read(PacketByteBuf buffer, WrapperLookup lookup) {
            byte contentType = buffer.readByte();
            if (contentType == 1) {
                readTrackedNbt(PacketCodecs.NBT_COMPOUND.decode(buffer), lookup);
            } else {
                T spell = this.spell.get();
                if (spell != null) {
                    spell.getDataTracker().load(MsgTrackedValues.TrackerEntries.PACKET_CODEC.decode(buffer), lookup);
                }
            }
        }

        @Override
        public Optional<PacketByteBuf> write(Status status, WrapperLookup lookup) {
            if (status != Status.DEFAULT) {
                PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
                buffer.writeByte(1);
                PacketCodecs.NBT_COMPOUND.encode(buffer, spell.toNBT(lookup));
                return Optional.of(buffer);
            }
            @Nullable T spell = this.spell.get();
            if (spell == null) {
                return Optional.empty();
            }
            return spell.getDataTracker().getDirtyPairs(lookup).map(entries -> {
                PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
                buffer.writeByte(0);
                TrackerEntries.PACKET_CODEC.encode(buffer, entries);
                return buffer;
            });
        }

        @Override
        public void copyTo(Entry<T> destination) {
            destination.spell.set(spell.get());
        }
    }

    @Override
    public void copyFrom(SpellSlots other, boolean alive) {
        if (alive) {
            other.stream().forEach(this::put);
        } else {
            other.stream().filter(SpellType.PLACE_CONTROL_SPELL).forEach(this::put);
        }
    }
}
