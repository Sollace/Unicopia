package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.util.Tickable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.Entity;

public class DataTrackerManager implements Tickable {
    private final Entity entity;
    final boolean isClient;
    private final Int2ObjectOpenHashMap<DataTracker> trackers = new Int2ObjectOpenHashMap<>();

    private IntSet discardedTrackers = new IntOpenHashSet();
    private int nextId = 0;

    private final DataTracker primaryTracker = checkoutTracker();

    public DataTrackerManager(Entity entity) {
        this.entity = entity;
        this.isClient = entity.getWorld().isClient;
    }

    public DataTracker getPrimaryTracker() {
        return primaryTracker;
    }

    public DataTracker checkoutTracker() {
        DataTracker tracker = new DataTracker(this, nextId++);
        trackers.put(tracker.id, tracker);
        return tracker;
    }

    void closeTracker(int id) {
        if (id <= 0) {
            return;
        }

        trackers.remove(id);
        if (!isClient) {
            discardedTrackers.add(id);
        }
    }

    @Override
    public void tick() {
        if (isClient) {
            return;
        }

        List<MsgTrackedValues.TrackerEntries> toTransmit = new ArrayList<>();

        for (var entry : trackers.int2ObjectEntrySet()) {
            MsgTrackedValues.TrackerEntries dirtyPairs = entry.getValue().getDirtyPairs();
            if (dirtyPairs != null) {
                toTransmit.add(dirtyPairs);
            }
        }

        if (!toTransmit.isEmpty() || !discardedTrackers.isEmpty()) {
            MsgTrackedValues packet = new MsgTrackedValues(entity.getId(), toTransmit, discardedTrackers.toIntArray());
            discardedTrackers = new IntOpenHashSet();
            Channel.SERVER_TRACKED_ENTITY_DATA.sendToSurroundingPlayers(packet, entity);
        }
    }

    void load(MsgTrackedValues packet) {
        for (int id : packet.removedTrackers()) {
            closeTracker(id);
        }
        for (var update : packet.updatedTrackers()) {
            DataTracker tracker = trackers.get(update.id());
            if (tracker != null) {
                tracker.load(update.wipe(), update.values());
            }
        }
    }
}
