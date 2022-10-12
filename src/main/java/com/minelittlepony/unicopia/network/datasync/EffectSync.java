package com.minelittlepony.unicopia.network.datasync;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;

/**
 * Synchronisation class for spells.
 * Since we can't have our own serializers, we have to intelligently
 * determine whether to update it from an nbt tag.
 *
 * @param <T> The owning entity
 */
public class EffectSync implements SpellContainer {

    private final NetworkedReferenceSet<Spell> spells;

    private final Caster<?> owner;

    private final TrackedData<NbtCompound> param;

    @Nullable
    private NbtCompound lastValue;

    public EffectSync(Caster<?> owner, TrackedData<NbtCompound> param) {
        spells = new NetworkedReferenceSet<>(Spell::getUuid, () -> new SpellNetworkedReference<>(owner));
        this.owner = owner;
        this.param = param;
    }

    @Override
    public boolean contains(UUID id) {
        return spells.containsReference(id) || spells.getReferences().anyMatch(s -> s.equalsOrContains(id));
    }

    @Override
    public boolean contains(@Nullable SpellPredicate<?> type) {
        return read(type, true, false).findFirst().isPresent();
    }

    @Override
    public <T extends Spell> Optional<T> get(@Nullable SpellPredicate<T> type, boolean update) {
        return read(type, update, true).findFirst();
    }

    @Override
    public void put(@Nullable Spell effect) {
        spells.addReference(effect);
        write();
        if (owner instanceof UpdateCallback) {
            ((UpdateCallback)owner).onSpellSet(effect);
        }
    }

    @Override
    public boolean removeWhere(Predicate<Spell> test, boolean update) {
        return reduce(update, (initial, effect) -> {
            if (!test.test(effect)) {
                return initial;
            }
            spells.removeReference(effect);
            return true;
        });
    }

    @Override
    public boolean forEach(Function<Spell, Operation> test, boolean update) {
        return reduce(update, (initial, effect) -> {
            Operation op = test.apply(effect);
            if (op == Operation.REMOVE) {
                spells.removeReference(effect);
            } else {
                initial |= op != Operation.SKIP;
            }
            return initial;
        });
    }

    @Override
    public Stream<Spell> stream(boolean update) {
        return stream(null, update);
    }

    @Override
    public <T extends Spell> Stream<T> stream(@Nullable SpellPredicate<T> type, boolean update) {
        return read(type, update, true);
    }

    @Override
    public boolean clear() {
        if (spells.clear()) {
            write();
            if (owner instanceof UpdateCallback) {
                ((UpdateCallback)owner).onSpellSet(null);
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T extends Spell> Stream<T> read(@Nullable SpellPredicate<T> type, boolean synchronize, boolean sendUpdate) {
        if (synchronize && spells.fromNbt(owner.getEntity().getDataTracker().get(param)) && sendUpdate) {
            owner.getEntity().getDataTracker().set(param, spells.toNbt());
        }

        if (type == null) {
            return (Stream<T>)spells.getReferences();
        }
        return (Stream<T>)spells.getReferences().flatMap(s -> s.findMatches(type));
    }

    private boolean reduce(boolean update, Alteration alteration) {
        boolean initial = false;
        for (Spell i : read(null, update, false).toList()) {
            initial = alteration.apply(initial, i);
        }

        write();
        return initial;
    }

    private void write() {
        if (spells.isDirty()) {
            owner.getEntity().getDataTracker().set(param, spells.toNbt());
        }
    }

    public interface UpdateCallback {
        void onSpellSet(@Nullable Spell spell);
    }

    private interface Alteration {
        boolean apply(boolean initial, Spell spell);
    }
}
