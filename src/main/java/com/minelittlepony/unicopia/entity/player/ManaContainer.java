package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public class ManaContainer implements MagicReserves, Tickable, NbtSerialisable {
    private final Pony pony;

    private final BarInst energy;
    private final BarInst exhaustion;
    private final BarInst exertion;
    private final BarInst mana;
    private final BarInst xp;

    public ManaContainer(Pony pony) {
        this.pony = pony;
        this.energy = new BarInst(Pony.ENERGY, 100F, 0);
        this.exhaustion = new BarInst(Pony.EXHAUSTION, 100F, 0);
        this.exertion = new BarInst(Pony.EXERTION, 10F, 0);
        this.xp = new BarInst(Pony.XP, 1, 0);
        this.mana = new XpCollectingBar(Pony.MANA, 100F, 100F);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.put("energy", energy.toNBT());
        compound.put("exhaustion", exhaustion.toNBT());
        compound.put("exertion", exertion.toNBT());
        compound.put("mana",  mana.toNBT());
        compound.put("xp",  xp.toNBT());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        energy.fromNBT(compound.getCompound("energy"));
        exhaustion.fromNBT(compound.getCompound("exhaustion"));
        exertion.fromNBT(compound.getCompound("exertion"));
        mana.fromNBT(compound.getCompound("mana"));
        xp.fromNBT(compound.getCompound("xp"));
    }

    @Override
    public Bar getExertion() {
        return exertion;
    }

    @Override
    public Bar getExhaustion() {
        return exhaustion;
    }

    @Override
    public Bar getEnergy() {
        return energy;
    }

    @Override
    public Bar getMana() {
        return mana;
    }

    @Override
    public Bar getXp() {
        return xp;
    }

    @Override
    public void tick() {
        exertion.tick();
        energy.tick();
        mana.tick();
        xp.tick();

        exertion.add(-10);

        if (energy.get() > 5) {
            energy.multiply(0.8F);
        } else {
            energy.add(-1);
        }

        if (pony.getSpecies().canFly() && !pony.getPhysics().isFlying()) {
            exhaustion.multiply(0.8F);
        } else {
            exhaustion.add(-1);
        }

        if (!pony.getSpecies().canFly() || !pony.getPhysics().isFlying()) {
            if (mana.getPercentFill() < 1 && mana.getShadowFill() == mana.getPercentFill()) {
                mana.add((mana.getMax() / 10F) * Math.max(1, pony.getLevel().get() * 4));
            }
        }

        if (xp.getPercentFill() >= 1) {
            xp.set(0);
            pony.getLevel().add(1);
        }
    }

    class XpCollectingBar extends BarInst {

        XpCollectingBar(TrackedData<Float> marker, float max, float initial) {
            super(marker, max, initial);
        }

        @Override
        public float getMax() {
            return super.getMax() + 50 * pony.getLevel().get();
        }

        @Override
        public void set(float value) {
            float diff = value - get();
            if (diff > 0) {
                if (pony.getLevel().canLevelUp()) {
                    xp.add(0.001F / pony.getLevel().get());
                    if (xp.getPercentFill() >= 1) {
                        xp.set(0);
                        pony.getLevel().add(1);
                    }
                }

                value = get() + diff / (1 + pony.getLevel().get());
            }

            super.set(value);
        }
    }

    class BarInst implements Bar, NbtSerialisable {

        private final TrackedData<Float> marker;
        private final float max;

        private float trailingValue;

        BarInst(TrackedData<Float> marker, float max, float initial) {
            this.marker = marker;
            this.max = max;
            pony.getMaster().getDataTracker().startTracking(marker, initial);
        }

        @Override
        public float get() {
            return pony.getMaster().getDataTracker().get(marker);
        }

        @Override
        public float getShadowFill() {
            return trailingValue;
        }

        @Override
        public void set(float value) {
            load(MathHelper.clamp(value, 0, getMax()));
        }

        private void load(float value) {
            pony.getMaster().getDataTracker().set(marker, value);
        }

        @Override
        public float getMax() {
            return max;
        }

        void tick() {
            float fill = getPercentFill();
            float trailingIncrement = 0.003F;

            if (trailingValue > (fill - trailingIncrement) && trailingValue < (fill + trailingIncrement)) {
                trailingValue = fill;
            }
            if (trailingValue < fill) {
                trailingValue += trailingIncrement;
            }
            if (trailingValue > fill) {
                trailingValue -= trailingIncrement;
            }
        }

        @Override
        public void toNBT(NbtCompound compound) {
            compound.putFloat("shadow", trailingValue);
            compound.putFloat("value", get());
        }

        @Override
        public void fromNBT(NbtCompound compound) {
            trailingValue = compound.getFloat("shadow");
            load(compound.getFloat("value"));
        }
    }
}
