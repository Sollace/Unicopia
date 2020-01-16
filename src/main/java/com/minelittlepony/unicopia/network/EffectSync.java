package com.minelittlepony.unicopia.network;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IMagicEffect;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.CompoundTag;

/**
 * Synchronisation class for spell effects.
 * Since we can't have our own serializers, we have to intelligently
 * determine whether to update it from an nbt tag.
 *
 * @param <T> The owning entity
 */
public class EffectSync {

    @Nullable
    private IMagicEffect effect;

    private final ICaster<?> owned;

    private final TrackedData<CompoundTag> param;

    public EffectSync(ICaster<?> owned, TrackedData<CompoundTag> param) {
        this.owned = owned;
        this.param = param;
    }

    public boolean has() {
        CompoundTag comp = owned.getEntity().getDataTracker().get(param);

        if (comp == null || !comp.containsKey("effect_id")) {
            if (effect != null) {
                effect.setDead();
                effect = null;
            }
        } else {
            String id = comp.getString("effect_id");

            if (effect == null || !effect.getName().contentEquals(id)) {
                if (effect != null) {
                    effect.setDead();
                }
                effect = SpellRegistry.instance().createEffectFromNBT(comp);
            } else if (!owned.getEntity().world.isClient() && effect.isDirty()) {
                set(effect);
            }
        }

        return effect != null;
    }

    @SuppressWarnings("unchecked")
    public <E extends IMagicEffect> E get(Class<E> type, boolean update) {
        if (!update) {
            if (effect == null || type == null || type.isAssignableFrom(effect.getClass())) {
                return (E)effect;
            }

            return null;
        }

        CompoundTag comp = owned.getEntity().getDataTracker().get(param);

        if (comp == null || !comp.containsKey("effect_id")) {
            if (effect != null) {
                effect.setDead();
                effect = null;
            }
        } else {
            String id = comp.getString("effect_id");

            if (effect == null || !effect.getName().contentEquals(id)) {
                if (effect != null) {
                    effect.setDead();
                }
                effect = SpellRegistry.instance().createEffectFromNBT(comp);
            } else if (owned.getEntity().world.isClient()) {
                effect.fromNBT(comp);
            } else if (effect.isDirty()) {
                set(effect);
            }
        }

        if (effect == null || type == null || type.isAssignableFrom(effect.getClass())) {
            return (E)effect;
        }

        return null;
    }

    public void set(@Nullable IMagicEffect effect) {
        if (this.effect != null && this.effect != effect) {
            this.effect.setDead();
        }
        this.effect = effect;

        if (effect == null) {
            owned.getEntity().getDataTracker().set(param, new CompoundTag());
        } else {
            owned.getEntity().getDataTracker().set(param, SpellRegistry.instance().serializeEffectToNBT(effect));
        }
    }
}
