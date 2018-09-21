package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;

public class EffectSync<T extends EntityLivingBase> {
    private IMagicEffect effect;

    private final ICaster<T> owned;

    private final DataParameter<NBTTagCompound> param;

    public EffectSync(ICaster<T> owned, DataParameter<NBTTagCompound> param) {
        this.owned = owned;
        this.param = param;
    }

    public boolean has() {
        return get() != null;
    }

    public IMagicEffect get() {
        NBTTagCompound comp = owned.getEntity().getDataManager().get(param);

        if (comp == null || !comp.hasKey("effect_id")) {
            effect = null;
        } else {
            String id = comp.getString("effect_id");
            if (effect == null || id != effect.getName()) {
                effect = SpellRegistry.instance().createEffectFromNBT(comp);
            } else {
                effect.readFromNBT(comp);
            }
        }

        return effect;
    }

    public void set(IMagicEffect effect) {
        this.effect = effect;

        if (effect == null) {
            owned.getEntity().getDataManager().set(param, new NBTTagCompound());
        } else {
            owned.getEntity().getDataManager().set(param, SpellRegistry.instance().serializeEffectToNBT(effect));
        }
    }
}
