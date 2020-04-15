package com.minelittlepony.unicopia.magic.spell;

import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.MagicEffect;

import net.minecraft.nbt.CompoundTag;

public abstract class AbstractSpell implements MagicEffect {

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

    public static abstract class RangedAreaSpell extends AbstractSpell {

        @Override
        public int getMaxLevelCutOff(Caster<?> source) {
            return 17;
        }

        @Override
        public float getMaxExhaustion(Caster<?> caster) {
            return 1000;
        }

        @Override
        public float getExhaustion(Caster<?> caster) {
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
