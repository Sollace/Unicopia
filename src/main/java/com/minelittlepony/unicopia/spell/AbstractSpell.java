package com.minelittlepony.unicopia.spell;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractSpell implements IMagicEffect {

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
    public int getMaxLevelCutOff(ICaster<?> source) {
        return 1;
    }

    public float getMaxExhaustion(ICaster<?> caster) {
        return 1;
    }

    public float getExhaustion(ICaster<?> caster) {
        return 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("dead", isDead);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        setDirty(false);
        isDead = compound.getBoolean("dead");
    }

    public static abstract class RangedAreaSpell extends AbstractSpell {

        @Override
        public int getMaxLevelCutOff(ICaster<?> source) {
            return 17;
        }

        @Override
        public float getMaxExhaustion(ICaster<?> caster) {
            return 1000;
        }

        @Override
        public float getExhaustion(ICaster<?> caster) {
            float max = getMaxLevelCutOff(caster);
            float current = caster.getCurrentLevel();

            if (current > max) {
                float maxEc = getMaxExhaustion(caster);

                current -= max;
                current /= max;
                current /= maxEc;

                return maxEc - current;
            }

            return super.getExhaustion(caster);
        }
    }
}
