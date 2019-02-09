package com.minelittlepony.unicopia.network;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;

/**
 * Synchronisation class for spell effects.
 * Since we can't have our own serializers, we have to intelligently
 * determine whether to update it from an nbt tag.
 *
 * @param <T> The owning entity
 */
public class EffectSync<T extends EntityLivingBase> {

    @Nullable
    private IMagicEffect effect;

    private final ICaster<T> owned;

    private final DataParameter<NBTTagCompound> param;

    public EffectSync(ICaster<T> owned, DataParameter<NBTTagCompound> param) {
        this.owned = owned;
        this.param = param;
    }

    public boolean has() {
        NBTTagCompound comp = owned.getEntity().getDataManager().get(param);

        if (comp == null || !comp.hasKey("effect_id")) {
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
            } else if (!owned.getEntity().world.isRemote && effect.isDirty()) {
                set(effect);
            }
        }

        return effect != null;
    }

    public IMagicEffect get(boolean update) {
        if (!update) {
            return effect;
        }

        NBTTagCompound comp = owned.getEntity().getDataManager().get(param);

        if (comp == null || !comp.hasKey("effect_id")) {
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
            } else if (owned.getEntity().world.isRemote) {
                effect.readFromNBT(comp);
            } else if (effect.isDirty()) {
                set(effect);
            }
        }

        return effect;
    }

    public void set(@Nullable IMagicEffect effect) {
        if (this.effect != null && this.effect != effect) {
            this.effect.setDead();
        }
        this.effect = effect;

        if (effect == null) {
            owned.getEntity().getDataManager().set(param, new NBTTagCompound());
        } else {
            owned.getEntity().getDataManager().set(param, SpellRegistry.instance().serializeEffectToNBT(effect));
        }
    }
}
