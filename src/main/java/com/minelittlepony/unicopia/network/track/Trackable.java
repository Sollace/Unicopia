package com.minelittlepony.unicopia.network.track;

import net.minecraft.entity.Entity;

public interface Trackable {
    default DataTrackerManager getDataTrackers() {
        throw new RuntimeException("Implemented by mixin");
    }

    static Trackable of(Entity entity) {
        return (Trackable)entity;
    }
}
