package com.minelittlepony.unicopia;

import java.util.Set;

import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record SyncedConfig (
    Set<String> wantItNeedItExcludeList,
    Set<String> dimensionsWithoutAtmosphere) {
    public static final PacketCodec<ByteBuf, SyncedConfig> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecUtils.STRING_SET, SyncedConfig::wantItNeedItExcludeList,
            PacketCodecUtils.STRING_SET, SyncedConfig::dimensionsWithoutAtmosphere,
            SyncedConfig::new
    );
}
