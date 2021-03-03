package com.minelittlepony.unicopia.network;

import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

/**
 * Synchronisation class for spells.
 * Since we can't have our own serializers, we have to intelligently
 * determine whether to update it from an nbt tag.
 *
 * @param <T> The owning entity
 */
public class EffectSync {

    private Optional<Spell> spell = Optional.empty();

    private final Caster<?> owner;

    private final TrackedData<CompoundTag> param;

    @Nullable
    private CompoundTag lastValue;

    public EffectSync(Caster<?> owner, TrackedData<CompoundTag> param) {
        this.owner = owner;
        this.param = param;
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
    public <T extends Spell> Optional<T> get(boolean update) {
        return get(null, update);
    }

    /**
     * Gets the active effect for this caster updating it if needed.
     */
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

    /**
     * Returns true if this caster has an active effect attached to it.
     */
    public boolean isPresent() {
        sync(false);
        return checkReference();
    }

    private boolean checkReference() {
        return spell.isPresent() && !spell.get().isDead();
    }

    private void sync(boolean force) {
        CompoundTag comp = owner.getEntity().getDataTracker().get(param);

        Spell effect = spell.orElse(null);

        if (comp == null || !comp.contains("effect_id")) {
            updateReference(null);
        } else if (!checkReference() || !effect.getType().getId().equals(new Identifier(comp.getString("effect_id")))) {
            updateReference(SpellType.fromNBT(comp));
        } else if (owner.getEntity().world.isClient()) {
            if (lastValue != comp || !(comp == null || comp.equals(lastValue))) {
                lastValue = comp;
                effect.fromNBT(comp);
            }
        } else if ((force || !owner.isClient()) && effect.isDirty()) {
            put(effect);
        }
    }

    public void put(@Nullable Spell effect) {
        updateReference(effect);
        owner.getEntity().getDataTracker().set(param, effect == null ? new CompoundTag() : SpellType.toNBT(effect));
    }

    private void updateReference(@Nullable Spell effect) {
        if (spell.isPresent() && spell.get() != effect) {
            spell.get().setDead();
        }
        spell = Optional.ofNullable(effect);
    }
}
