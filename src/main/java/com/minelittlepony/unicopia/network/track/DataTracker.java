package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.PacketByteBuf;

public class DataTracker {
    private final List<Pair<?>> codecs = new ObjectArrayList<>();
    private final Int2ObjectOpenHashMap<Consumer<?>> loadCallbacks = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Runnable> writethroughCallback = new Int2ObjectOpenHashMap<>();
    private IntSet dirtyIndices = new IntOpenHashSet();

    private final DataTrackerManager manager;
    private boolean initial = true;

    final int id;

    public DataTracker(DataTrackerManager manager, int id) {
        this.manager = manager;
        this.id = id;
    }

    public <T> Entry<T> startTracking(TrackableDataType<T> type, T initialValue) {
        Entry<T> entry = new Entry<>(codecs.size());
        codecs.add(new Pair<>(entry.id(), type, initialValue));
        return entry;
    }

    public <T> void onReceive(Entry<T> entry, Consumer<T> loadCallback) {
        loadCallbacks.put(entry.id(), loadCallback);
    }

    public <T> void onBeforeSend(Entry<T> entry, Runnable action) {
        writethroughCallback.put(entry.id(), action);
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
            pair.value = value;
            dirtyIndices.add(entry.id());
        }
    }

    @Nullable
    MsgTrackedValues.TrackerEntries getDirtyPairs() {
        writethroughCallback.values().forEach(Runnable::run);

        if (initial) {
            initial = false;
            dirtyIndices = new IntOpenHashSet();
            return new MsgTrackedValues.TrackerEntries(id, true, codecs);
        }

        if (dirtyIndices.isEmpty()) {
            return null;
        }

        IntSet toSend = dirtyIndices;
        dirtyIndices = new IntOpenHashSet();
        List<Pair<?>> pairs = new ArrayList<>();
        for (int i : toSend) {
            pairs.add(codecs.get(i));
        }
        return new MsgTrackedValues.TrackerEntries(id, false, pairs);
    }

    @SuppressWarnings("unchecked")
    void load(boolean wipe, List<Pair<?>> values) {
        if (wipe) {
            codecs.clear();
            codecs.addAll(values);
            for (var value : values) {
                Consumer<?> callback = loadCallbacks.get(value.id);
                if (callback != null) {
                    ((Consumer<Object>)callback).accept(value.value);
                }
            }
        } else {
            values.forEach(value -> {
                if (value.id >= 0 && value.id < codecs.size()) {
                    if (codecs.get(value.id).type == value.type) {
                        codecs.set(value.id, value);
                        Consumer<?> callback = loadCallbacks.get(value.id);
                        if (callback != null) {
                            ((Consumer<Object>)callback).accept(value.value);
                        }
                    }
                }
            });
        }
    }

    public void close() {
        manager.closeTracker(id);
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
}
