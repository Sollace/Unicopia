package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.SyncedConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record MsgConfigurationChange(SyncedConfig config) {
    public static final PacketCodec<ByteBuf, MsgConfigurationChange> PACKET_CODEC = SyncedConfig.PACKET_CODEC.xmap(MsgConfigurationChange::new, MsgConfigurationChange::config);
}
