package com.minelittlepony.unicopia.network.track;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;

public class ObjectTracker<T extends TrackableObject> implements NbtSerialisable {
    private final Map<UUID, T> trackedObjects = new Object2ObjectOpenHashMap<>();
    private volatile Map<UUID, T> quickAccess = Map.of();

    private final int id;
    private final Supplier<T> constructor;

    public ObjectTracker(int id, Supplier<T> constructor) {
        this.id = id;
        this.constructor = constructor;
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

    public synchronized void add(T obj) {
        trackedObjects.put(obj.getUuid(), obj);
        quickAccess = Map.copyOf(trackedObjects);
    }

    synchronized void getInitialPairs(List<MsgTrackedValues.TrackerObjects> output) {
        if (!trackedObjects.isEmpty()) {
            Map<UUID, NbtCompound> trackableCompounds = new HashMap<>();
            quickAccess.entrySet().forEach(object -> {
                trackableCompounds.put(object.getKey(), object.getValue().toTrackedNbt());
            });

            output.add(new MsgTrackedValues.TrackerObjects(id, Set.of(), trackableCompounds));
        }
    }

    synchronized void getDirtyPairs(List<MsgTrackedValues.TrackerObjects> output) {
        if (!trackedObjects.isEmpty()) {
            Map<UUID, NbtCompound> trackableCompounds = new HashMap<>();
            Set<UUID> removedTrackableObjects = new HashSet<>();
            trackedObjects.entrySet().removeIf(object -> {
                TrackableObject.Status status = object.getValue().getStatus();
                if (status == TrackableObject.Status.REMOVED) {
                    removedTrackableObjects.add(object.getKey());
                } else if (status != TrackableObject.Status.DEFAULT) {
                    trackableCompounds.put(object.getKey(), object.getValue().toTrackedNbt());
                }
                return status == TrackableObject.Status.REMOVED;
            });
            quickAccess = Map.copyOf(trackedObjects);

            if (!trackableCompounds.isEmpty() || !removedTrackableObjects.isEmpty()) {
                output.add(new MsgTrackedValues.TrackerObjects(id, removedTrackableObjects, trackableCompounds));
            }
        }
    }

    synchronized void load(MsgTrackedValues.TrackerObjects objects) {
        objects.removedValues().forEach(removedId -> {
            T o = trackedObjects.remove(removedId);
            if (o != null) {
                o.discard(true);
            }
        });
        objects.values().forEach((id, nbt) -> {
            T o = trackedObjects.get(id);
            if (o == null) {
                o = constructor.get();
                trackedObjects.put(id, o);
            }
            o.readTrackedNbt(nbt);
        });
        quickAccess = Map.copyOf(trackedObjects);
    }

    @Override
    public synchronized void toNBT(NbtCompound compound) {
        quickAccess.forEach((id, value) -> {
            compound.put(id.toString(), value.toTrackedNbt());
        });
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        Map<UUID, T> values = new Object2ObjectOpenHashMap<>();
        compound.getKeys().forEach(key -> {
            try {
                UUID id = UUID.fromString(key);
                if (id != null && !Util.NIL_UUID.equals(id)) {
                    NbtCompound nbt = compound.getCompound(key);
                    T entry = constructor.get();
                    entry.readTrackedNbt(nbt);
                    values.put(id, entry);
                }
            } catch (Throwable t) {
                Unicopia.LOGGER.warn("Exception loading tracked object", t);
            }
        });
        synchronized (this) {
            trackedObjects.clear();
            trackedObjects.putAll(values);
            quickAccess = Map.copyOf(trackedObjects);
        }
    }
}
