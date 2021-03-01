package com.minelittlepony.unicopia.network;

import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

/**
 * Synchronisation class for spell effects.
 * Since we can't have our own serializers, we have to intelligently
 * determine whether to update it from an nbt tag.
 *
 * @param <T> The owning entity
 */
public class EffectSync {

    @Nullable
    private Spell effect;

    private final Caster<?> owned;

    private final TrackedData<CompoundTag> param;

    @Nullable
    private CompoundTag lastValue;

    public EffectSync(Caster<?> owned, TrackedData<CompoundTag> param) {
        this.owned = owned;
        this.param = param;
    }

    public <T extends Spell> Optional<T> getOrEmpty(Class<T> type, boolean update) {
        T effect = get(type, update);

        if (effect == null || effect.isDead()) {
            return Optional.empty();
        }

        return Optional.of(effect);
    }

    @SuppressWarnings("unchecked")
    public <E extends Spell> E get(Class<E> type, boolean update) {
        if (update) {
            sync(true);
        }

        if (effect == null || type == null || type.isAssignableFrom(effect.getClass())) {
            return (E)effect;
        }

        return null;
    }

    public boolean has() {
        sync(false);
        return effect != null;
    }

    private void sync(boolean force) {
        CompoundTag comp = owned.getEntity().getDataTracker().get(param);

        if (comp == null || !comp.contains("effect_id")) {
            if (effect != null) {
                effect.setDead();
                effect = null;
            }
            return;
        } else {
            if (effect == null || !effect.getType().getId().equals(new Identifier(comp.getString("effect_id")))) {
                if (effect != null) {
                    effect.setDead();
                }
                effect = SpellType.fromNBT(comp);
            } else if (owned.getEntity().world.isClient()) {
                if (lastValue != comp || !(comp == null || comp.equals(lastValue))) {
                    lastValue = comp;
                    effect.fromNBT(comp);
                }
            } else if ((force || !owned.getEntity().world.isClient()) && effect.isDirty()) {
                set(effect);
            }
        }
    }

    public void set(@Nullable Spell effect) {
        if (this.effect != null && this.effect != effect) {
            this.effect.setDead();
        }
        this.effect = effect;

        if (effect == null) {
            owned.getEntity().getDataTracker().set(param, new CompoundTag());
        } else {
            owned.getEntity().getDataTracker().set(param, SpellType.toNBT(effect));
        }
    }
}
