package com.minelittlepony.unicopia.container.spellbook;

import java.util.Map;

import com.minelittlepony.unicopia.InteractionManager;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public interface SpellbookChapters {
    PacketCodec<RegistryByteBuf, SpellbookChapters> PACKET_CODEC = PacketCodec.ofStatic(
            (buffer, chapter) -> ((SpellbookChapterLoader.Payload)chapter).write(buffer),
            buffer -> InteractionManager.getInstance().readChapters(buffer)
    );

    Map<Identifier, ? extends SpellbookChapter> chapters();

    record Impl(Map<Identifier, ? extends SpellbookChapter> chapters) implements SpellbookChapters {}
}
