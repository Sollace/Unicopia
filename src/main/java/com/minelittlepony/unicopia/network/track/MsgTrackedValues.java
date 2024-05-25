package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.minelittlepony.unicopia.util.serialization.PacketCodec;
import com.sollace.fabwork.api.packets.HandledPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public record MsgTrackedValues(
        int owner,
        Optional<TrackerObjects> updatedObjects,
        Optional<TrackerEntries> updatedTrackers
) implements HandledPacket<PlayerEntity> {
    public MsgTrackedValues(PacketByteBuf buffer) {
        this(
            buffer.readInt(),
            buffer.readOptional(TrackerObjects::new),
            buffer.readOptional(TrackerEntries::new)
        );
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(owner);
        buffer.writeOptional(updatedObjects, (buf, obj) -> obj.write(buf));
        buffer.writeOptional(updatedTrackers, (buf, tracker) -> tracker.write(buf));
    }

    @Override
    public void handle(PlayerEntity sender) {
        Entity entity = sender.getWorld().getEntityById(owner);
        if (entity instanceof Trackable trackable) {
            trackable.getDataTrackers().load(this);
        }
    }

    public record TrackerObjects(int id, Set<UUID> removedValues, Map<UUID, NbtCompound> values) {
        public TrackerObjects(PacketByteBuf buffer) {
            this(
                buffer.readInt(),
                buffer.readCollection(HashSet::new, PacketByteBuf::readUuid),
                buffer.readMap(HashMap::new, PacketByteBuf::readUuid, PacketCodec.NBT::read)
            );

        }

        public void write(PacketByteBuf buffer) {
            buffer.writeInt(id);
            buffer.writeCollection(removedValues, PacketByteBuf::writeUuid);
            buffer.writeMap(values, PacketByteBuf::writeUuid, PacketCodec.NBT::write);
        }
    }

    public record TrackerEntries(int id, boolean wipe, List<DataTracker.Pair<?>> values) {
        public TrackerEntries(PacketByteBuf buffer) {
            this(buffer.readInt(), buffer.readBoolean(), buffer.readCollection(ArrayList::new, DataTracker.Pair::new));
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeInt(id);
            buffer.writeBoolean(wipe);
            buffer.writeCollection(values, (buf, pair) -> pair.write(buf));
        }
    }
}
