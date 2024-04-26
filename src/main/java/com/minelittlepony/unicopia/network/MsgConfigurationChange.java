package com.minelittlepony.unicopia.network;

import java.util.HashSet;

import com.minelittlepony.unicopia.SyncedConfig;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.network.PacketByteBuf;

public record MsgConfigurationChange(SyncedConfig config) implements Packet {
    public MsgConfigurationChange(PacketByteBuf buffer) {
        this(new SyncedConfig(
                buffer.readCollection(HashSet::new, PacketByteBuf::readString),
                buffer.readCollection(HashSet::new, PacketByteBuf::readString)
        ));
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeCollection(config.wantItNeedItExcludeList(), PacketByteBuf::writeString);
        buffer.writeCollection(config.dimensionsWithoutAtmosphere(), PacketByteBuf::writeString);
    }

}
