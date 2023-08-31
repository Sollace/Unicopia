package com.minelittlepony.unicopia.entity.player;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

class ManaContainer implements MagicReserves, Tickable, NbtSerialisable, Copyable<ManaContainer> {
    private final Pony pony;

    private final Map<String, BarInst> bars = new HashMap<>();

    private final BarInst energy;
    private final BarInst exhaustion;
    private final BarInst exertion;
    private final BarInst mana;
    private final BarInst xp;
    private final BarInst charge;

    public ManaContainer(Pony pony) {
        this.pony = pony;
        this.energy = addBar("energy", new BarInst(Pony.ENERGY, 100F, 0));
        this.exhaustion = addBar("exhaustion", new BarInst(Pony.EXHAUSTION, 100F, 0));
        this.exertion = addBar("exertion", new BarInst(Pony.EXERTION, 10F, 0));
        this.xp = addBar("xp", new BarInst(Pony.XP, 1F, 0));
        this.mana = addBar("mana", new XpCollectingBar(Pony.MANA, 100F, 1));
        this.charge = addBar("charge", new BarInst(Pony.CHARGE, 10F, 0) {
            @Override
            protected float applyLimits(float value) {
                return Math.max(0, value);
            }
        });
    }

    protected BarInst addBar(String name, BarInst bar) {
        bars.put(name, bar);
        return bar;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        bars.forEach((key, bar) -> compound.put(key, bar.toNBT()));
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        bars.forEach((key, bar) -> bar.fromNBT(compound.getCompound(key)));
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
    public Bar getCharge() {
        return charge;
    }

    @Override
    public void tick() {
        bars.values().forEach(BarInst::tick);

        exertion.addPercent(-10);

        if (energy.get() > 5) {
            energy.multiply(0.8F);
        } else {
            energy.addPercent(-1);
        }

        if (pony.getSpecies().canFly() && !pony.getPhysics().isFlying()) {
            exhaustion.multiply(0.8F);
        } else {
            exhaustion.addPercent(-1);
        }

        if (!pony.getSpecies().canFly() || !pony.getPhysics().isFlying()) {
            if (mana.getPercentFill() < 1 && mana.getShadowFill(1) <= mana.getPercentFill(1)) {
                mana.addPercent(MathHelper.clamp(1 + pony.getLevel().get(), 1, 50) / 4F);
            }
        }

        if (xp.getPercentFill() >= 1) {
            xp.set(0);
            pony.getLevel().add(1);
        }
    }

    @Override
    public void copyFrom(ManaContainer other, boolean alive) {
        if (alive) {
            mana.resetTo(mana.getMax());
            xp.resetTo(other.xp.get());
        } else {
            energy.resetTo(0.6F);
            exhaustion.resetTo(0);
            exertion.resetTo(0);
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
        private float prevTrailingValue;
        private float prevValue;

        BarInst(TrackedData<Float> marker, float max, float initial) {
            this.marker = marker;
            this.max = max;
            this.trailingValue = initial;
            pony.asEntity().getDataTracker().startTracking(marker, getMax() * initial);
        }

        @Override
        public float get() {
            return applyLimits(pony.asEntity().getDataTracker().get(marker));
        }

        @Override
        public float get(float tickDelta) {
            return MathHelper.lerp(tickDelta, prevValue, get());
        }

        @Override
        public float getShadowFill(float tickDelta) {
            return MathHelper.lerp(tickDelta, prevTrailingValue, trailingValue);
        }

        @Override
        public void set(float value) {
            load(applyLimits(value));
        }

        private void load(float value) {
            pony.asEntity().getDataTracker().set(marker, value);
        }

        protected float getInitial(float initial) {
            return initial;
        }

        protected float applyLimits(float value) {
            return MathHelper.clamp(value, 0, getMax());
        }

        void resetTo(float value) {
            trailingValue = MathHelper.clamp(value / getMax(), 0, 1);
            load(value);
        }

        @Override
        public float getMax() {
            return max;
        }

        void tick() {
            prevValue = get();
            prevTrailingValue = trailingValue;

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
