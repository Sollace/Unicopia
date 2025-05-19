package com.minelittlepony.unicopia.container.spellbook;

import java.util.Locale;

import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

public enum TabSide implements StringIdentifiable {
    LEFT,
    RIGHT;

    @SuppressWarnings("deprecation")
    public static final EnumCodec<TabSide> CODEC = StringIdentifiable.createCodec(TabSide::values);
    public static final PacketCodec<ByteBuf, TabSide> PACKET_CODEC = PacketCodecUtils.ofEnum(TabSide.class);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String asString() {
        return name;
    }
}