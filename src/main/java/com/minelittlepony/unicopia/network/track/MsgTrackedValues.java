package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sollace.fabwork.api.packets.Handled;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

public record MsgTrackedValues(
        int owner,
        Optional<TrackerObjects> updatedObjects,
        Optional<TrackerEntries> updatedTrackers
) implements Handled<PlayerEntity> {
    public static final PacketCodec<RegistryByteBuf, MsgTrackedValues> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, MsgTrackedValues::owner,
            PacketCodecs.optional(TrackerObjects.PACKET_CODEC), MsgTrackedValues::updatedObjects,
            PacketCodecs.optional(TrackerEntries.PACKET_CODEC), MsgTrackedValues::updatedTrackers,
            MsgTrackedValues::new
    );

    @Override
    public void handle(PlayerEntity sender) {
        Entity entity = sender.getWorld().getEntityById(owner);
        if (entity instanceof Trackable trackable) {
            trackable.getDataTrackers().load(this);
        }
    }

    public record TrackerObjects(int id, Set<UUID> removedValues, Map<UUID, byte[]> values) {
        public static final PacketCodec<RegistryByteBuf, TrackerObjects> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, TrackerObjects::id,
                Uuids.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), TrackerObjects::removedValues,
                PacketCodecs.map(HashMap::new, Uuids.PACKET_CODEC, PacketCodecs.BYTE_ARRAY), TrackerObjects::values,
                TrackerObjects::new
        );
    }

    public record TrackerEntries(int id, boolean wipe, List<DataTracker.Pair<?>> values, Map<Integer, byte[]> objects) {
        public static final PacketCodec<RegistryByteBuf, TrackerEntries> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, TrackerEntries::id,
                PacketCodecs.BOOL, TrackerEntries::wipe,
                DataTracker.Pair.PACKET_CODEC.collect(PacketCodecs.toCollection(ArrayList::new)), TrackerEntries::values,
                PacketCodecs.map(HashMap::new, PacketCodecs.INTEGER, PacketCodecs.BYTE_ARRAY), TrackerEntries::objects,
                TrackerEntries::new
        );
    }
}
