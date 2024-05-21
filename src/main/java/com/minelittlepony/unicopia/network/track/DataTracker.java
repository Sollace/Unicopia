package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.serialization.PacketCodec;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class DataTracker {
    private final List<Pair<?>> codecs = new ObjectArrayList<>();
    private IntSet dirtyIndices = new IntOpenHashSet();
    private Int2ObjectMap<TrackableObject> persistentObjects = new Int2ObjectOpenHashMap<>();

    private final DataTrackerManager manager;
    private boolean initial = true;

    final int id;

    public DataTracker(DataTrackerManager manager, int id) {
        this.manager = manager;
        this.id = id;
    }

    public <T extends TrackableObject> Entry<NbtCompound> startTracking(T value) {
        Entry<NbtCompound> entry = startTracking(TrackableDataType.of(PacketCodec.NBT), value.toTrackedNbt());
        persistentObjects.put(entry.id(), value);
        return entry;
    }

    public <T> Entry<T> startTracking(TrackableDataType<T> type, T initialValue) {
        Entry<T> entry = new Entry<>(codecs.size());
        codecs.add(new Pair<>(entry.id(), type, initialValue));
        return entry;
    }

    @SuppressWarnings("unchecked")
    private <T> Pair<T> getPair(Entry<T> entry) {
        return (Pair<T>)codecs.get(entry.id());
    }

    public <T> T get(Entry<T> entry) {
        return getPair(entry).value;
    }

    public <T> void set(Entry<T> entry, T value) {
        if (manager.isClient) {
            return;
        }

        Pair<T> pair = getPair(entry);
        if (!Objects.equals(pair.value, value)) {
            synchronized (this) {
                pair.value = value;
                dirtyIndices.add(entry.id());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateTrackables() {
        for (var entry : persistentObjects.int2ObjectEntrySet()) {
            int key = entry.getIntKey();
            TrackableObject.Status status = entry.getValue().getStatus();
            if (status == TrackableObject.Status.NEW || status == TrackableObject.Status.UPDATED) {
                ((Pair<Object>)codecs.get(key)).value = entry.getValue().toTrackedNbt();
                dirtyIndices.add(key);
            }
        }
    }

    synchronized void getInitialPairs(List<MsgTrackedValues.TrackerEntries> output) {
        updateTrackables();
        output.add(new MsgTrackedValues.TrackerEntries(id, true, codecs));
    }

    @Nullable
    synchronized void getDirtyPairs(List<MsgTrackedValues.TrackerEntries> output) {
        if (initial) {
            initial = false;
            dirtyIndices = new IntOpenHashSet();
            getInitialPairs(output);
        } else {
            updateTrackables();

            if (!dirtyIndices.isEmpty()) {
                IntSet toSend = dirtyIndices;
                dirtyIndices = new IntOpenHashSet();
                List<Pair<?>> pairs = new ArrayList<>();
                for (int i : toSend) {
                    pairs.add(codecs.get(i));
                }
                output.add(new MsgTrackedValues.TrackerEntries(id, false, pairs));
            }
        }
    }

    synchronized void load(MsgTrackedValues.TrackerEntries values) {
        if (values.wipe()) {
            codecs.clear();
            codecs.addAll(values.values());
            for (var entry : persistentObjects.int2ObjectEntrySet()) {
                Pair<?> pair = codecs.get(entry.getIntKey());
                if (pair != null) {
                    entry.getValue().readTrackedNbt((NbtCompound)pair.value);
                }
            }
        } else {
            for (var value : values.values()) {
                if (value.id >= 0 && value.id < codecs.size()) {
                    if (codecs.get(value.id).type == value.type) {
                        codecs.set(value.id, value);
                        TrackableObject o = persistentObjects.get(value.id);
                        if (o != null) {
                            o.readTrackedNbt((NbtCompound)value.value);
                        }
                    }
                }
            }
        }
    }

    public record Entry<T>(int id) {}
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
            this.type = TrackableDataType.of(buffer.readInt());
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
