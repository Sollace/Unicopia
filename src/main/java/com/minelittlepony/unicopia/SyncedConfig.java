package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record SyncedConfig (
    Set<String> wantItNeedItExcludeList,
    Set<String> dimensionsWithoutAtmosphere,
    Set<Identifier> speciesWhiteList) {
    public static final PacketCodec<ByteBuf, SyncedConfig> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecUtils.STRING_SET, SyncedConfig::wantItNeedItExcludeList,
            PacketCodecUtils.STRING_SET, SyncedConfig::dimensionsWithoutAtmosphere,
            Identifier.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), SyncedConfig::speciesWhiteList,
            SyncedConfig::new
    );
}
