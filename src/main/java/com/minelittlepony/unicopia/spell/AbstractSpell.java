package com.minelittlepony.unicopia.spell;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractSpell implements IMagicEffect {

	protected boolean isDead;
	protected boolean isDirty;

	private int strength = 0;

    @Override
    public int getCurrentLevel() {
        return strength;
    }

    @Override
    public void setCurrentLevel(int level) {
        strength = level;
    }

    @Override
    public boolean isCraftable() {
        return true;
    }

	@Override
	public void setDead() {
		isDead = true;
	}

	@Override
	public boolean getDead() {
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
    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("dead", isDead);
        compound.setInteger("spell_strength", strength);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        setDirty(false);
        isDead = compound.getBoolean("dead");
        strength = compound.getInteger("spell_strength");
    }
}
