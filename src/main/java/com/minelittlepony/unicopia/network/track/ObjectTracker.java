package com.minelittlepony.unicopia.network.track;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.network.track.TrackableObject.Status;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class ObjectTracker<T extends TrackableObject<T>> {
    private final Map<UUID, T> trackedObjects = new Object2ObjectOpenHashMap<>();
    private volatile Map<UUID, T> quickAccess = Map.of();

    private final int id;
    private final Supplier<T> constructor;

    public ObjectTracker(int id, Supplier<T> constructor) {
        this.id = id;
        this.constructor = constructor;
    }

    public Map<UUID, T> entries() {
        return quickAccess;
    }

    public Set<UUID> keySet() {
        return quickAccess.keySet();
    }

    public Collection<T> values() {
        return quickAccess.values();
    }

    @Nullable
    public T get(UUID id) {
        return quickAccess.get(id);
    }

    @Nullable
    public T remove(UUID id, boolean immediate) {
        T entry = quickAccess.get(id);
        if (entry != null) {
            entry.discard(immediate);
        }
        return entry;
    }

    public boolean contains(UUID id) {
        return quickAccess.containsKey(id);
    }

    public boolean isEmpty() {
        return quickAccess.isEmpty();
    }

    public boolean clear(boolean immediate) {
        if (isEmpty()) {
            return false;
        }
        values().forEach(value -> value.discard(immediate));
        return true;
    }

    public synchronized void add(UUID id, T obj) {
        trackedObjects.put(id, obj);
        quickAccess = Map.copyOf(trackedObjects);
    }

    synchronized void copyTo(ObjectTracker<T> destination) {
        for (var entry : trackedObjects.entrySet()) {
            T copy = destination.constructor.get();
            entry.getValue().copyTo(copy);
            destination.trackedObjects.put(entry.getKey(), copy);
        }
        destination.quickAccess = Map.copyOf(destination.trackedObjects);
    }

    synchronized Optional<MsgTrackedValues.TrackerObjects> getInitialPairs(WrapperLookup lookup) {
        if (trackedObjects.isEmpty()) {
            return Optional.empty();
        }

        Map<UUID, PacketByteBuf> updates = new HashMap<>();
        quickAccess.entrySet().forEach(object -> {
            object.getValue().write(Status.NEW, lookup).ifPresent(data -> {
                updates.put(object.getKey(), data);
            });
        });

        return Optional.of(new MsgTrackedValues.TrackerObjects(id, Set.of(), updates));
    }

    synchronized Optional<MsgTrackedValues.TrackerObjects> getDirtyPairs(WrapperLookup lookup) {
        if (!trackedObjects.isEmpty()) {
            Map<UUID, PacketByteBuf> updates = new HashMap<>();
            Set<UUID> removedTrackableObjects = new HashSet<>();
            trackedObjects.entrySet().removeIf(object -> {
                TrackableObject.Status status = object.getValue().getStatus();
                if (status == TrackableObject.Status.REMOVED) {
                    removedTrackableObjects.add(object.getKey());
                    return true;
                }
                object.getValue().write(status, lookup).ifPresent(data -> {
                    updates.put(object.getKey(), data);
                });
                return false;
            });
            quickAccess = Map.copyOf(trackedObjects);

            if (!updates.isEmpty() || !removedTrackableObjects.isEmpty()) {
                return Optional.of(new MsgTrackedValues.TrackerObjects(id, removedTrackableObjects, updates));
            }
        }

        return Optional.empty();
    }

    synchronized void load(MsgTrackedValues.TrackerObjects objects, WrapperLookup lookup) {
        objects.removedValues().forEach(removedId -> {
            T o = trackedObjects.remove(removedId);
            if (o != null) {
                o.discard(true);
            }
        });
        objects.values().forEach((id, data) -> {
            T o = trackedObjects.get(id);
            if (o == null) {
                o = constructor.get();
                trackedObjects.put(id, o);
            }
            o.read(data, lookup);
        });
        quickAccess = Map.copyOf(trackedObjects);
    }

    public void load(Map<UUID, T> values) {
        synchronized (this) {
            trackedObjects.clear();
            trackedObjects.putAll(values);
            quickAccess = Map.copyOf(trackedObjects);
        }
    }
}
