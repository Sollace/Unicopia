package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter;
import com.minelittlepony.unicopia.container.spellbook.TabSide;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class SpellbookChapterList {
    private final SpellbookScreen screen;

    private final Chapter craftingChapter;

    private final Map<Identifier, Chapter> chapters = new HashMap<>();

    public SpellbookChapterList(SpellbookScreen screen, Chapter craftingChapter, Chapter... builtIn) {
        this.screen = screen;
        this.craftingChapter = craftingChapter;
        chapters.putAll(ClientChapters.getChapters());
        chapters.put(craftingChapter.id(), craftingChapter);
        for (Chapter i : builtIn) {
            chapters.put(i.id(), i);
        }
    }

    public Stream<Chapter> getTabs(TabSide side) {
        return chapters.values().stream().filter(chapter -> chapter.side() == side);
    }

    public Chapter getCurrentChapter() {
        if (Debug.SPELLBOOK_CHAPTERS) {
            ClientChapters.getChapters().forEach((id, chapter) -> {
                chapters.compute(id, (key, old) -> {
                    Optional.ofNullable(old).flatMap(Chapter::content).ifPresent(o -> {
                        chapter.content().ifPresent(neu -> neu.copyStateFrom(o));
                    });
                    return chapter;
                });
            });
        }

        return screen.getState().getCurrentPageId().map(chapters::get).orElse(craftingChapter);
    }

    public record Chapter (
        Identifier id,
        TabSide side,
        int tabY,
        int color,
        Optional<Content> content) implements SpellbookChapter {

        public static Identifier createIcon(Identifier id, String suffex) {
            return id.withPath(p -> "textures/gui/container/pages/" + p + suffex + ".png");
        }
    }

    public interface Content extends Drawable {
        void init(SpellbookScreen screen, Identifier pageId);

        default void copyStateFrom(Content old) {}

        default Identifier getIcon(Chapter chapter, Identifier icon) {
            return icon;
        }

        static Optional<Content> of(BiConsumer<SpellbookScreen, Identifier> init, Drawable obj) {
            return Optional.of(new Content() {
                @Override
                public void init(SpellbookScreen screen, Identifier pageId) {
                    init.accept(screen, pageId);
                }

                @Override
                public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {
                    obj.draw(context, mouseX, mouseY, container);
                }
            });
        }
    }

    public interface Drawable {
        void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container);
    }
}
