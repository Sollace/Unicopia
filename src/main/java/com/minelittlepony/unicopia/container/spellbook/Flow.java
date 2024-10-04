package com.minelittlepony.unicopia.container.spellbook;

import java.util.Locale;

import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

public enum Flow implements StringIdentifiable {
    NONE, LEFT, RIGHT;

    @SuppressWarnings("deprecation")
    public static final EnumCodec<Flow> CODEC = StringIdentifiable.createCodec(Flow::values);
    public static final PacketCodec<ByteBuf, Flow> PACKET_CODEC = PacketCodecUtils.ofEnum(Flow.class);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String asString() {
        return name;
    }
}