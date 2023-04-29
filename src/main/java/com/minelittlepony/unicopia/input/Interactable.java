package com.minelittlepony.unicopia.input;

import java.util.function.BooleanSupplier;

import com.minelittlepony.unicopia.util.Tickable;

public class Interactable implements Tickable {

    private final BooleanSupplier stateGetter;

    private boolean prevState;
    private boolean currentState;

    private long prevActivationTime;
    private long activationTime;

    public Interactable(BooleanSupplier stateGetter) {
        this.stateGetter = stateGetter;
    }

    @Override
    public void tick() {
        prevState = currentState;
        currentState = stateGetter.getAsBoolean();
        if (prevState != currentState && currentState) {
            prevActivationTime = activationTime;
            activationTime = System.currentTimeMillis();
        }
    }

    public boolean getState() {
        return currentState;
    }

    public boolean hasChanged(Heuristic changeType) {
        if (changeType == Heuristic.ONCE) {
            return prevState != currentState;
        }

        final long stageDuration = 250;
        final long now = System.currentTimeMillis();
        return activationTime > now - stageDuration && prevActivationTime > activationTime - stageDuration;
    }
}
