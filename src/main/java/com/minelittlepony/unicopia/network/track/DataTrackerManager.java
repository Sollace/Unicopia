package com.minelittlepony.unicopia.network.track;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.network.Channel;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;

public class DataTrackerManager {
    private final EntityConvertable<?> entity;
    private final Supplier<DynamicRegistryManager> registries;

    private final List<DataTracker> trackers = new ObjectArrayList<>();
    private final List<ObjectTracker<?>> objectTrackers = new ObjectArrayList<>();
    private final List<PacketEmitter> packetEmitters = new ObjectArrayList<>();

    private DataTracker primaryTracker;

    public DataTrackerManager(EntityConvertable<?> entity) {
        this.entity = entity;
        this.registries = Suppliers.memoize(() -> entity.asWorld().getRegistryManager());
        this.primaryTracker = checkoutTracker();
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
            var update = initial ? tracker.getInitialPairs(registries.get()) : tracker.getDirtyPairs(registries.get());
            if (update.isPresent()) {
                sender.accept(Channel.SERVER_TRACKED_ENTITY_DATA.toPacket(new MsgTrackedValues(
                        entity.asEntity().getId(),
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
            var update = initial ? tracker.getInitialPairs(registries.get()) : tracker.getDirtyPairs(registries.get());
            if (update.isPresent()) {
                sender.accept(Channel.SERVER_TRACKED_ENTITY_DATA.toPacket(new MsgTrackedValues(
                        entity.asEntity().getId(),
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
                tracker.load(update, registries.get());
            }
        });
        packet.updatedObjects().ifPresent(update -> {
            ObjectTracker<?> tracker = objectTrackers.get(update.id());
            if (tracker != null) {
                tracker.load(update, registries.get());
            }
        });
    }

    public interface PacketEmitter {
        void sendPackets(Consumer<Packet<?>> consumer, boolean initial);
    }
}
