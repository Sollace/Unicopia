package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.*;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.*;
import com.minelittlepony.unicopia.client.gui.spellbook.element.DynamicContent;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.*;

public class ClientChapters {
    private static Map<Identifier, Chapter> CHAPTERS = new HashMap<>();

    public static Set<SpellbookChapterList.Chapter> getChapters() {
        return new HashSet<>(CHAPTERS.values());
    }

    public static void load(Map<Identifier, Chapter> chapters) {
        CHAPTERS = chapters;
    }

    public static Chapter loadChapter(PacketByteBuf buffer) {
        return new Chapter(
                buffer.readIdentifier(),
                buffer.readEnumConstant(TabSide.class),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readOptional(DynamicContent::new)
        );
    }
}
