package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.InteractionManager;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public interface SpellbookChapter {
    PacketCodec<RegistryByteBuf, SpellbookChapter> PACKET_CODEC = PacketCodec.ofStatic(
            (buffer, chapter) -> ((SpellbookChapterLoader.Chapter)chapter).write(buffer),
            buffer -> InteractionManager.getInstance().readChapter(buffer)
    );
}
