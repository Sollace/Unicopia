package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.util.Tickable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public class DataTrackerManager implements Tickable {
    private final Entity entity;
    final boolean isClient;
    private final List<DataTracker> trackers = new ObjectArrayList<>();
    private final List<ObjectTracker<?>> objectTrackers = new ObjectArrayList<>();

    private final DataTracker primaryTracker = checkoutTracker();

    public DataTrackerManager(Entity entity) {
        this.entity = entity;
        this.isClient = entity.getWorld().isClient;
    }

    public DataTracker getPrimaryTracker() {
        return primaryTracker;
    }

    public synchronized DataTracker checkoutTracker() {
        DataTracker tracker = new DataTracker(this, trackers.size());
        trackers.add(tracker);
        return tracker;
    }

    public synchronized <T extends TrackableObject> ObjectTracker<T> checkoutTracker(Supplier<T> objFunction) {
        ObjectTracker<T> tracker = new ObjectTracker<>(objectTrackers.size(), objFunction);
        objectTrackers.add(tracker);
        return tracker;
    }

    @Override
    public void tick() {
        if (isClient) {
            return;
        }

        synchronized (this) {
            List<MsgTrackedValues.TrackerEntries> toTransmit = new ArrayList<>();
            List<MsgTrackedValues.TrackerObjects> objToTransmit = new ArrayList<>();

            for (var entry : trackers) entry.getDirtyPairs(toTransmit);
            for (var entry : objectTrackers) entry.getDirtyPairs(objToTransmit);

            if (!toTransmit.isEmpty() || !objToTransmit.isEmpty()) {
                MsgTrackedValues packet = new MsgTrackedValues(
                        entity.getId(),
                        objToTransmit,
                        toTransmit
                );
                Channel.SERVER_TRACKED_ENTITY_DATA.sendToSurroundingPlayers(packet, entity);
            }
        }
    }

    public synchronized void sendInitial(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> sender) {
        synchronized (this) {
            List<MsgTrackedValues.TrackerEntries> toTransmit = new ArrayList<>();
            List<MsgTrackedValues.TrackerObjects> objToTransmit = new ArrayList<>();

            for (var entry : trackers) entry.getInitialPairs(toTransmit);
            for (var entry : objectTrackers) entry.getInitialPairs(objToTransmit);

            if (!toTransmit.isEmpty() || !objToTransmit.isEmpty()) {
                MsgTrackedValues packet = new MsgTrackedValues(
                        entity.getId(),
                        objToTransmit,
                        toTransmit
                );
                sender.accept(Channel.SERVER_TRACKED_ENTITY_DATA.toPacket(packet));
            }
        }
    }

    synchronized void load(MsgTrackedValues packet) {
        for (var update : packet.updatedTrackers()) {
            DataTracker tracker = trackers.get(update.id());
            if (tracker != null) {
                tracker.load(update);
            }
        }
        for (var update : packet.updatedObjects()) {
            ObjectTracker<?> tracker = objectTrackers.get(update.id());
            if (tracker != null) {
                tracker.load(update);
            }
        }
    }
}
