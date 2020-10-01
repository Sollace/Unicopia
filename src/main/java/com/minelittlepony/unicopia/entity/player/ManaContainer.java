package com.minelittlepony.unicopia.entity.player;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.MathHelper;

public class ManaContainer implements MagicReserves {
    private final Pony pony;

    private final Bar energy;
    private final Bar exertion;
    private final Bar mana;

    public ManaContainer(Pony pony) {
        this.pony = pony;
        this.energy = new BarInst(Pony.ENERGY, 100F);
        this.exertion = new BarInst(Pony.EXERTION, 10F);
        this.mana = new BarInst(Pony.MANA, 100F);
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

    class BarInst implements Bar {

        private final TrackedData<Float> marker;
        private final float max;

        BarInst(TrackedData<Float> marker, float max) {
            this.marker = marker;
            this.max = max;
            pony.getOwner().getDataTracker().startTracking(marker, 0F);
        }

        @Override
        public float get() {
            return pony.getOwner().getDataTracker().get(marker);
        }

        @Override
        public void set(float value) {
            pony.getOwner().getDataTracker().set(marker, MathHelper.clamp(value, 0, getMax()));
        }

        @Override
        public float getMax() {
            return max;
        }
    }
}
