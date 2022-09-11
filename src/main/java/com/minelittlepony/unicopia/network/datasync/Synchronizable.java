package com.minelittlepony.unicopia.network.datasync;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class Synchronizable<T extends Synchronizable<T>> {

    private Optional<Consumer<T>> synchronizer = Optional.empty();

    @SuppressWarnings("unchecked")
    public void synchronize() {
        synchronizer.ifPresent(s -> s.accept((T)this));
    }

    public void setSynchronizer(Consumer<T> synchronizer) {
        this.synchronizer = Optional.of(synchronizer);
    }

    public abstract void copyFrom(T state);
}
