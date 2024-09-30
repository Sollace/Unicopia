package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.*;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.*;
import com.minelittlepony.unicopia.client.gui.spellbook.element.DynamicContent;
import com.minelittlepony.unicopia.container.SpellbookChapter;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.*;

public class ClientChapters {
    private static Map<Identifier, Chapter> CHAPTERS = Map.of();

    public static Map<Identifier, Chapter> getChapters() {
        return CHAPTERS;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void load(Map<Identifier, SpellbookChapter> chapters) {
        CHAPTERS = (Map)chapters;
    }

    public static SpellbookChapter loadChapter(PacketByteBuf buffer) {
        return new Chapter(
                buffer.readIdentifier(),
                buffer.readEnumConstant(TabSide.class),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readOptional(DynamicContent::new)
        );
    }
}
