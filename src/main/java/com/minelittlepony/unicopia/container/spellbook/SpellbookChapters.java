package com.minelittlepony.unicopia.container.spellbook;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public interface SpellbookChapters {
    PacketCodec<RegistryByteBuf, Map<Identifier, Chapter>> SERVER_PACKET_CODEC = PacketCodecs
            .map(i -> Untyped.cast(new HashMap<>(i)), Identifier.PACKET_CODEC, Chapter.PACKET_CODEC);

    PacketCodec<RegistryByteBuf, Map<Identifier, SpellbookChapter>> PACKET_CODEC = PacketCodec.ofStatic(
            (buffer, chapters) -> SERVER_PACKET_CODEC.encode(buffer, Untyped.cast(chapters)),
            buffer -> InteractionManager.getInstance().readChapters(buffer)
    );
    Map<Identifier, ? extends SpellbookChapter> chapters();
}
