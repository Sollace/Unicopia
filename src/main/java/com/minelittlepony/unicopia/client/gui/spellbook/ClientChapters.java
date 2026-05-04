package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.*;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.*;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter;
import net.minecraft.util.*;

public class ClientChapters {
    private static Map<Identifier, Chapter> CHAPTERS = Map.of();

    public static Map<Identifier, Chapter> getChapters() {
        return CHAPTERS;
    }

    public static void load(Map<Identifier, SpellbookChapter> chapters) {
        CHAPTERS = chapters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new Chapter(entry.getKey(), entry.getValue())));
    }
}
