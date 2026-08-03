package com.minelittlepony.unicopia.network.track;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.network.Channel;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;

public class DataTrackerManager {
    private final Entity entity;
    private DynamicRegistryManager lookup;
    private Boolean isClient;
    private final List<DataTracker> trackers = new ObjectArrayList<>();
    private final List<ObjectTracker<?>> objectTrackers = new ObjectArrayList<>();
    private final List<PacketEmitter> packetEmitters = new ObjectArrayList<>();

    private DataTracker primaryTracker;

    public DataTrackerManager(Entity entity) {
        this.entity = entity;
        // We don't call entity.getWorld() here anymore
        this.primaryTracker = checkoutTracker();
    }

    //Safe way to get registryManager which won't crash when entity.getWorld() won't be null.
    public DynamicRegistryManager getLookup() {
        if (this.lookup == null && entity.getWorld() != null) {
            this.lookup = entity.getWorld().getRegistryManager();
        }
        return this.lookup;
    }

    //And one for the isClient(), too.
    public boolean isClient() {
        if (this.isClient == null) {
            if (entity.getWorld() != null) {
                this.isClient = entity.getWorld().isClient;
            } else {
                return false;
            }
        }
        return this.isClient;
    }

    public synchronized void addPacketEmitter(PacketEmitter packetEmitter) {
        packetEmitters.add(packetEmitter);
    }

    public DataTracker getPrimaryTracker() {
        return primaryTracker;
    }

    public synchronized DataTracker checkoutTracker() {
        DataTracker tracker = new DataTracker(trackers.size());
        trackers.add(tracker);
        packetEmitters.add((sender, initial) -> {
            var update = initial ? tracker.getInitialPairs(getLookup()) : tracker.getDirtyPairs(getLookup());
            if (update.isPresent()) {
                sender.accept(Channel.SERVER_TRACKED_ENTITY_DATA.toPacket(new MsgTrackedValues(
                        entity.getId(),
                        Optional.empty(),
                        update
                )));
            }
        });
        return tracker;
    }

    public synchronized <T extends TrackableObject<T>> ObjectTracker<T> checkoutTracker(Supplier<T> objFunction) {
        ObjectTracker<T> tracker = new ObjectTracker<>(objectTrackers.size(), objFunction);
        objectTrackers.add(tracker);
        packetEmitters.add((sender, initial) -> {
            var update = initial ? tracker.getInitialPairs(getLookup()) : tracker.getDirtyPairs(getLookup());
            if (update.isPresent()) {
                sender.accept(Channel.SERVER_TRACKED_ENTITY_DATA.toPacket(new MsgTrackedValues(
                        entity.getId(),
                        update,
                        Optional.empty()
                )));
            }
        });
        return tracker;
    }

    public void tick(Consumer<Packet<?>> sender) {
        synchronized (this) {
            for (var emitter : packetEmitters) {
                emitter.sendPackets(sender, false);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized void copyTo(DataTrackerManager destination) {
        for (int i = 0; i < trackers.size(); i++) {
            trackers.get(i).copyTo(i >= destination.trackers.size() ? destination.checkoutTracker() : destination.trackers.get(i));
        }
        for (int i = 0; i < objectTrackers.size(); i++) {
            ((ObjectTracker)objectTrackers.get(i)).copyTo(destination.objectTrackers.get(i));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized void sendInitial(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> sender) {
        synchronized (this) {
            for (var emitter : packetEmitters) {
                emitter.sendPackets((Consumer)sender, true);
            }
        }
    }

    synchronized void load(MsgTrackedValues packet) {
        packet.updatedTrackers().ifPresent(update -> {
            DataTracker tracker = trackers.get(update.id());
            if (tracker != null) {
                tracker.load(update, getLookup());
            }
        });
        packet.updatedObjects().ifPresent(update -> {
            ObjectTracker<?> tracker = objectTrackers.get(update.id());
            if (tracker != null) {
                tracker.load(update, getLookup());
            }
        });
    }

    public interface PacketEmitter {
        void sendPackets(Consumer<Packet<?>> consumer, boolean initial);
    }
}
