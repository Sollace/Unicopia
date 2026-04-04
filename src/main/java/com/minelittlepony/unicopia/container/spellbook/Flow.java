package com.minelittlepony.unicopia.container.spellbook;

import java.util.Locale;

import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

public enum Flow implements StringIdentifiable {
    NONE, LEFT, RIGHT;

    public static final Codec<Flow> CODEC = StringIdentifiable.createCodec(Flow::values);
    public static final PacketCodec<ByteBuf, Flow> PACKET_CODEC = PacketCodecUtils.ofEnum(Flow.class);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String asString() {
        return name;
    }
}