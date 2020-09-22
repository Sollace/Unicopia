package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;

import net.minecraft.nbt.CompoundTag;

public abstract class AbstractSpell implements Spell {

    protected boolean isDead;
    protected boolean isDirty;

    @Override
    public boolean isCraftable() {
        return true;
    }

    @Override
    public void setDead() {
        isDead = true;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    @Override
    public int getMaxLevelCutOff(Caster<?> source) {
        return 1;
    }

    @Override
    public float getMaxExhaustion(Caster<?> caster) {
        return 1;
    }

    @Override
    public float getExhaustion(Caster<?> caster) {
        return 0;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putBoolean("dead", isDead);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        setDirty(false);
        isDead = compound.getBoolean("dead");
    }
}
