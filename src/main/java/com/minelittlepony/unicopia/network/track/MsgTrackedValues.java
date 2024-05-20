package com.minelittlepony.unicopia.network.track;

import java.util.ArrayList;
import java.util.List;
import com.sollace.fabwork.api.packets.HandledPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public record MsgTrackedValues(
        int owner,
        List<TrackerEntries> updatedTrackers,
        int[] removedTrackers
) implements HandledPacket<PlayerEntity> {
    public MsgTrackedValues(PacketByteBuf buffer) {
        this(buffer.readInt(), buffer.readCollection(ArrayList::new, TrackerEntries::new), buffer.readIntArray());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(owner);
        buffer.writeCollection(updatedTrackers, (buf, tracker) -> tracker.write(buf));
        buffer.writeIntArray(removedTrackers);
    }

    @Override
    public void handle(PlayerEntity sender) {
        Entity entity = sender.getWorld().getEntityById(owner);
        if (entity instanceof Trackable trackable) {
            trackable.getDataTrackers().load(this);
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
