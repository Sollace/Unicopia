package com.minelittlepony.unicopia.entity.player;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
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

    public ManaContainer(Pony pony, DataTracker tracker) {
        this.pony = pony;
        this.energy = addBar("energy", new BarInst(tracker, 100F, 0));
        this.exhaustion = addBar("exhaustion", new BarInst(tracker, 100F, 0));
        this.exertion = addBar("exertion", new BarInst(tracker, 10F, 0));
        this.xp = addBar("xp", new BarInst(tracker, 1F, 0));
        this.mana = addBar("mana", new XpCollectingBar(tracker, 100F, 1));
        this.charge = addBar("charge", new BarInst(tracker, 10F, 0) {
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
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        bars.forEach((key, bar) -> compound.put(key, bar.toNBT(lookup)));
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        bars.forEach((key, bar) -> bar.fromNBT(compound.getCompound(key), lookup));
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

        if (pony.getCompositeRace().canFly() && !pony.getPhysics().isFlying() && pony.asEntity().isOnGround()) {
            exhaustion.multiply(0.99F);
        } else {
            exhaustion.addPercent(-1);
        }

        if (!pony.getCompositeRace().canFly() || (!pony.getPhysics().isFlying() && pony.asEntity().isOnGround())) {
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
        XpCollectingBar(DataTracker tracker, float max, float initial) {
            super(tracker, max, initial);
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
        private final DataTracker.Entry<Float> marker;
        private final float max;

        private float trailingValue;
        private float prevTrailingValue;
        private float prevValue;

        BarInst(DataTracker tracker, float max, float initial) {
            this.max = max;
            this.trailingValue = initial;
            this.prevTrailingValue = initial;
            this.prevValue = initial;
            this.marker = tracker.startTracking(TrackableDataType.FLOAT, max * trailingValue);
        }

        @Override
        public float get() {
            return applyLimits(marker.get());
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
            marker.set(value);
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
        public void toNBT(NbtCompound compound, WrapperLookup lookup) {
            compound.putFloat("shadow", trailingValue);
            compound.putFloat("value", get());
        }

        @Override
        public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
            trailingValue = compound.getFloat("shadow");
            load(compound.getFloat("value"));
        }
    }
}
