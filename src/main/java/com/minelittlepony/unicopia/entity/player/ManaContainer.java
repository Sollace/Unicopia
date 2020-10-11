package com.minelittlepony.unicopia.entity.player;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;

public class ManaContainer implements MagicReserves, Tickable {
    private final Pony pony;

    private final BarInst energy;
    private final BarInst exertion;
    private final BarInst mana;
    private final BarInst xp;

    public ManaContainer(Pony pony) {
        this.pony = pony;
        this.energy = new BarInst(Pony.ENERGY, 100F, 0);
        this.exertion = new BarInst(Pony.EXERTION, 10F, 0);
        this.xp = new BarInst(Pony.XP, 1, 0);
        this.mana = new XpCollectingBar(Pony.MANA, 100F, 100F);
    }

    @Override
    public Bar getExertion() {
        return exertion;
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

        if (!pony.getSpecies().canFly() || !pony.getPhysics().isFlying()) {
            if (mana.getShadowFill() <= mana.getPercentFill()) {
                mana.add(18);
            }
        }
    }

    class XpCollectingBar extends BarInst {

        XpCollectingBar(TrackedData<Float> marker, float max, float initial) {
            super(marker, max, initial);
        }

        @Override
        public void set(float value) {
            float diff = value - get();
            if (diff < 0) {
                if (pony.getLevel().canLevelUp()) {
                    xp.add(-diff / (float)Math.pow(1000, 1 + pony.getLevel().get()));
                    if (xp.getPercentFill() >= 1) {
                        pony.getLevel().add(1);
                        xp.set(0);
                    }
                }

                value = get() + diff / (1 + pony.getLevel().get());
            }

            super.set(value);
        }
    }

    class BarInst implements Bar {

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
            pony.getMaster().getDataTracker().set(marker, MathHelper.clamp(value, 0, getMax()));
        }

        @Override
        public float getMax() {
            return max;
        }

        void tick() {
            float fill = getPercentFill();
            float tralingIncrement = 0.003F;

            if (trailingValue > (fill - tralingIncrement) && trailingValue < (fill + tralingIncrement)) {
                trailingValue = fill;
            }
            if (trailingValue < fill) {
                trailingValue += tralingIncrement;
            }
            if (trailingValue > fill) {
                trailingValue -= tralingIncrement;
            }
        }
    }
}
