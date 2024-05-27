package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.PacketByteBuf;

public class DataTracker {
    private final List<Pair<?>> codecs = new ObjectArrayList<>();
    private IntSet dirtyIndices = new IntOpenHashSet();
    private List<TrackableObject<?>> persistentObjects = new ObjectArrayList<>();

    private boolean initial = true;

    final int id;

    public DataTracker(int id) {
        this.id = id;
    }

    public <T extends TrackableObject<T>> T startTracking(T value) {
        persistentObjects.add(value);
        return value;
    }

    public <T> Entry<T> startTracking(TrackableDataType<T> type, T initialValue) {
        Entry<T> entry = new Entry<>(this, codecs.size());
        codecs.add(new Pair<>(entry.id(), type, initialValue));
        return entry;
    }

    @SuppressWarnings("unchecked")
    private <T> Pair<T> getPair(Entry<T> entry) {
        return (Pair<T>)codecs.get(entry.id());
    }

    private <T> T get(Entry<T> entry) {
        return getPair(entry).value;
    }

    private <T> void set(Entry<T> entry, T value) {
        Pair<T> pair = getPair(entry);
        if (!Objects.equals(pair.value, value)) {
            synchronized (this) {
                pair.value = value;
                dirtyIndices.add(entry.id());
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    synchronized void copyTo(DataTracker destination) {
        for (int i = 0; i < codecs.size(); i++) {
            ((Pair<Object>)destination.codecs.get(i)).value = codecs.get(i).value;
            TrackableObject<?> a = persistentObjects.get(i);
            TrackableObject<?> b = destination.persistentObjects.get(i);
            if (a != null && b != null) {
                ((TrackableObject)a).copyTo(b);
            }
        }
    }

    synchronized Optional<MsgTrackedValues.TrackerEntries> getInitialPairs() {
        initial = false;
        dirtyIndices = new IntOpenHashSet();
        return Optional.of(new MsgTrackedValues.TrackerEntries(id, true, codecs, writePersistentObjects(true)));
    }

    public synchronized Optional<MsgTrackedValues.TrackerEntries> getDirtyPairs() {
        if (initial) {
            return getInitialPairs();
        }

        Map<Integer, PacketByteBuf> updates = writePersistentObjects(false);

        if (dirtyIndices.isEmpty() && updates.isEmpty()) {
            return Optional.empty();
        }

        IntSet toSend = dirtyIndices;
        dirtyIndices = new IntOpenHashSet();
        List<Pair<?>> pairs = new ArrayList<>();
        for (int i : toSend) {
            pairs.add(codecs.get(i));
        }
        return Optional.of(new MsgTrackedValues.TrackerEntries(id, false, pairs, updates));
    }

    private Map<Integer, PacketByteBuf> writePersistentObjects(boolean initial) {
        Map<Integer, PacketByteBuf> updates = new HashMap<>();
        for (int i = 0; i < persistentObjects.size(); i++) {
            TrackableObject<?> o = persistentObjects.get(i);
            TrackableObject.Status status = initial ? TrackableObject.Status.NEW : o.getStatus();
            int id = i;
            o.write(status).ifPresent(data -> updates.put(id, data));
        }
        return updates;
    }

    public synchronized void load(MsgTrackedValues.TrackerEntries values) {
        if (values.wipe()) {
            codecs.clear();
            codecs.addAll(values.values());
        } else {
            for (var value : values.values()) {
                if (value.id >= 0 && value.id < codecs.size()) {
                    if (codecs.get(value.id).type == value.type) {
                        codecs.set(value.id, value);
                    }
                }
            }
        }

        for (var entry : values.objects().entrySet()) {
            TrackableObject<?> o = persistentObjects.get(entry.getKey());
            if (o != null) {
                o.read(entry.getValue());
            }
        }
    }

    public record Entry<T>(DataTracker tracker, int id) {
        public T get() {
            return tracker.get(this);
        }

        public void set(T t) {
            tracker.set(this, t);
        }
    }

    static class Pair<T> {
        private final TrackableDataType<T> type;
        public final int id;
        public T value;

        public Pair(int id, TrackableDataType<T> type, T value) {
            this.id = id;
            this.type = type;
            this.value = value;
        }

        public Pair(PacketByteBuf buffer) {
            this.id = buffer.readInt();
            this.type = TrackableDataType.of(buffer);
            this.value = type.read(buffer);
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeInt(id);
            type.write(buffer, value);
        }
    }

    public interface Updater<T> {
        void update(T t);
    }
}
