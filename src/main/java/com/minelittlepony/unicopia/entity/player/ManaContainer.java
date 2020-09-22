package com.minelittlepony.unicopia.entity.player;

public class ManaContainer implements MagicReserves {
    private final Pony pony;

    public ManaContainer(Pony pony) {
        this.pony = pony;
        pony.getOwner().getDataTracker().startTracking(Pony.ENERGY, 0F);
        pony.getOwner().getDataTracker().startTracking(Pony.EXERTION, 0F);
    }

    @Override
    public float getExertion() {
        return pony.getOwner().getDataTracker().get(Pony.EXERTION);
    }

    @Override
    public void setExertion(float exertion) {
        pony.getOwner().getDataTracker().set(Pony.EXERTION, Math.max(0, exertion));
    }

    @Override
    public float getEnergy() {
        return pony.getOwner().getDataTracker().get(Pony.ENERGY);
    }

    @Override
    public void setEnergy(float energy) {
        pony.getOwner().getDataTracker().set(Pony.ENERGY, Math.max(0, energy));
    }

}
