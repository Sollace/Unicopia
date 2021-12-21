package com.minelittlepony.unicopia.network.datasync;

import java.util.Optional;
import java.util.function.Consumer;
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
    @SuppressWarnings("unchecked")
    public <T extends Spell> Optional<T> get(@Nullable SpellPredicate<T> type, boolean update) {
        return (Optional<T>)(type == null ? read(update, true).findFirst() : read(update, true).filter(type).findFirst());
    }

    @Override
    public boolean isPresent() {
        return read(true, false).findFirst().isPresent();
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
    public void removeIf(Predicate<Spell> test, boolean update) {
        read(effect -> {
            if (test.test(effect)) {
                spells.removeReference(effect);
            }
        });
    }

    @Override
    public void clear() {
        put(null);
    }

    private Stream<Spell> read(boolean synchronize, boolean sendUpdate) {
        if (synchronize && spells.fromNbt(owner.getEntity().getDataTracker().get(param)) && sendUpdate) {
            write();
        }

        return spells.getReferences();
    }

    private void read(Consumer<Spell> consumer) {
        spells.fromNbt(owner.getEntity().getDataTracker().get(param));
        spells.getReferences().toList().forEach(consumer);
        write();
    }

    private void write() {
        if (spells.isDirty()) {
            owner.getEntity().getDataTracker().set(param, spells.toNbt());
        }
    }

    public interface UpdateCallback {
        void onSpellSet(@Nullable Spell spell);
    }
}
