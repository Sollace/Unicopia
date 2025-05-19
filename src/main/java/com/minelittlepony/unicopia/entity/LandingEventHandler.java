package com.minelittlepony.unicopia.entity;

import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.Tickable;

public class LandingEventHandler implements Tickable {

    private final Living<?> living;

    @Nullable
    private final AtomicReference<Callback> callback = new AtomicReference<>();
    private double prevY;
    private float prevFallDistance;

    public LandingEventHandler(Living<?> living) {
        this.living = living;
    }

    public void setCallback(LandingEventHandler.Callback callback) {
        if (living.asEntity().isOnGround()) {
            callback.dispatch(0F);
        } else {
            updateCallback(callback);
        }
    }

    public void beforeTick() {

    }

    @Override
    public void tick() {
        if (living.asEntity().getY() > prevY) {
            discard();
        }
        prevY = living.asEntity().getY();

        if (living.asEntity().isOnGround() && living.landedChanged()) {
            fire(prevFallDistance);
        }
        prevFallDistance = living.asEntity().fallDistance;
    }

    float fire(float fallDistance) {
        var event = callback.getAndSet(null);
        return event == null ? fallDistance : event.dispatch(fallDistance);
    }

    void discard() {
        updateCallback(null);
    }

    void updateCallback(@Nullable Callback callback) {
        var event = this.callback.getAndSet(callback);
        if (event != null) {
            event.onCancelled();
        }
    }

    public interface Callback {
        float dispatch(float fallDistance);

        void onCancelled();
    }
}
