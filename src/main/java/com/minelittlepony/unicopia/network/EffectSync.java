package com.minelittlepony.unicopia.network;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.spell.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;

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

    private Optional<Spell> spell = Optional.empty();

    private final Caster<?> owner;

    private final TrackedData<NbtCompound> param;

    @Nullable
    private NbtCompound lastValue;

    public EffectSync(Caster<?> owner, TrackedData<NbtCompound> param) {
        this.owner = owner;
        this.param = param;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Spell> Optional<T> get(@Nullable SpellPredicate<T> type, boolean update) {
        if (update) {
            sync(true);
        }

        if (checkReference() && (type == null || type.test(spell.get()))) {
            return (Optional<T>)spell;
        }

        return Optional.empty();
    }

    @Override
    public boolean isPresent() {
        sync(false);
        return checkReference();
    }

    private boolean checkReference() {
        return spell.isPresent() && !spell.get().isDead();
    }

    private void sync(boolean force) {
        @Nullable
        NbtCompound comp = owner.getEntity().getDataTracker().get(param);

        @Nullable
        Spell effect = spell.orElse(null);

        if (comp == null || !comp.contains("effect_id") || !comp.contains("uuid")) {
            if (effect != null) {
                updateReference(null);
            }
        } else if (effect == null || !effect.getUuid().equals(comp.getUuid("uuid"))) {
            updateReference(SpellType.fromNBT(comp));
        } else if (owner.isClient()) {
            if (!Objects.equals(lastValue, comp)) {
                lastValue = comp;
                effect.fromNBT(comp);
            }
        } else if (force && effect.isDirty()) {
            put(effect);
        }
    }

    @Override
    public void put(@Nullable Spell effect) {
        effect = effect == null || effect.isDead() ? null : effect;
        updateReference(effect);
        owner.getEntity().getDataTracker().set(param, effect == null ? new NbtCompound() : SpellType.toNBT(effect));
    }

    private void updateReference(@Nullable Spell effect) {
        @Nullable
        Spell old = spell.orElse(null);
        if (old != effect) {
            spell = Optional.ofNullable(effect);

            if (old != null && (effect == null || !old.getUuid().equals(effect.getUuid()))) {
                old.setDead();
                old.onDestroyed(owner);
            }
        }
    }
}
