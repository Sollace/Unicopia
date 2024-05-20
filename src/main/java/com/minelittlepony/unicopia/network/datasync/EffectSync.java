package com.minelittlepony.unicopia.network.datasync;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.serialization.PacketCodec;

import net.minecraft.nbt.NbtCompound;

/**
 * Synchronisation class for spells.
 * Since we can't have our own serializers, we have to intelligently
 * determine whether to update it from an nbt tag.
 *
 * @param <T> The owning entity
 */
public class EffectSync implements SpellContainer, NbtSerialisable {

    private final NetworkedReferenceSet<Spell> spells;

    private final Caster<?> owner;

    private final DataTracker tracker;
    private final DataTracker.Entry<NbtCompound> param;

    public EffectSync(Caster<?> owner, DataTracker tracker) {
        this.owner = owner;
        this.tracker = tracker;
        this.param = tracker.startTracking(TrackableDataType.of(PacketCodec.NBT), new NbtCompound());
        spells = new NetworkedReferenceSet<>(Spell::getUuid, () -> new SpellNetworkedReference<>(owner));

        tracker.onBeforeSend(param, () -> {
            if (spells.isDirty()) {
                tracker.set(param, spells.toNbt());
            }
        });
        tracker.onReceive(param, nbt -> spells.fromNbt(nbt));
    }

    public boolean tick(Situation situation) {
        return tick(spell -> {
            if (spell.isDying()) {
                spell.tickDying(owner);
                return Operation.ofBoolean(!spell.isDead());
            }
            return Operation.ofBoolean(spell.tick(owner, situation));
        });
    }

    public boolean tick(Function<Spell, Operation> tickAction) {
        try {
            return forEach(spell -> {
                try {
                    return tickAction.apply(spell);
                } catch (Throwable t) {
                    Unicopia.LOGGER.error("Error whilst ticking spell on entity {}", owner, t);
                }
                return Operation.REMOVE;
            });
        } catch (Exception e) {
            Unicopia.LOGGER.error("Error whilst ticking spell on entity {}", owner.asEntity(), e);
        }
        return false;
    }

    @Override
    public boolean contains(UUID id) {
        return spells.containsReference(id) || spells.getReferences().anyMatch(s -> s.equalsOrContains(id));
    }

    @Override
    public void put(@Nullable Spell effect) {
        spells.addReference(effect);
        if (owner instanceof UpdateCallback callback) {
            callback.onSpellSet(effect);
        }
    }

    @Override
    public void remove(UUID id) {
        discard(spells.getReference(id));
    }

    @Override
    public boolean removeWhere(Predicate<Spell> test) {
        return reduce((initial, spell) -> {
            if (!test.test(spell)) {
                return initial;
            }
            discard(spell);
            return true;
        });
    }

    private void discard(Spell spell) {
        if (spell != null) {
            spell.setDead();
            spell.tickDying(owner);
            if (spell.isDead()) {
                spells.removeReference(spell);
            }
        }
    }

    @Override
    public boolean forEach(Function<Spell, Operation> test) {
        return reduce((initial, effect) -> {
            Operation op = test.apply(effect);
            if (op == Operation.REMOVE) {
                spells.removeReference(effect);
            } else {
                initial |= op != Operation.SKIP;
            }
            return initial;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Spell> Stream<T> stream(@Nullable SpellPredicate<T> type) {
        if (type == null) {
            return (Stream<T>)spells.getReferences();
        }
        return (Stream<T>)spells.getReferences().flatMap(s -> s.findMatches(type));
    }

    @Override
    public boolean clear() {
        if (spells.clear()) {
            if (owner instanceof UpdateCallback c) {
                c.onSpellSet(null);
            }
            return true;
        }
        return false;
    }

    private boolean reduce(BiFunction<Boolean, Spell, Boolean> alteration) {
        return stream().toList().stream().reduce(false, alteration, (a, b) -> b);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.put("spells", spells.toNbt());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        spells.fromNbt(compound.getCompound("spells"));
        tracker.set(param, spells.toNbt());
    }

    public interface UpdateCallback {
        void onSpellSet(@Nullable Spell spell);
    }
}
