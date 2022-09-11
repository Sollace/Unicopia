package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SpellbookChapterList {
    public static final Identifier CRAFTING_ID = Unicopia.id("crafting");
    public static final Identifier PROFILE_ID = Unicopia.id("profile");
    public static final Identifier TRAIT_DEX_ID = Unicopia.id("traits");

    private final SpellbookScreen screen;

    private final Chapter craftingChapter;

    private final Map<Identifier, Chapter> chapters = new HashMap<>();

    public SpellbookChapterList(SpellbookScreen screen, Chapter craftingChapter, Chapter... builtIn) {
        this.screen = screen;
        this.craftingChapter = craftingChapter;
        SpellbookChapterLoader.INSTANCE.getChapters().forEach(chapter -> {
            chapters.put(chapter.id(), chapter);
        });
        chapters.put(craftingChapter.id(), craftingChapter);
        for (Chapter i : builtIn) {
            chapters.put(i.id(), i);
        }
    }

    public Stream<Chapter> getTabs(TabSide side) {
        return chapters.values().stream().filter(chapter -> chapter.side() == side);
    }

    public Chapter getCurrentChapter() {
        if (SpellbookChapterLoader.DEBUG) {
            SpellbookChapterLoader.INSTANCE.getChapters().forEach(chapter -> {
                Optional.ofNullable(chapters.get(chapter.id())).flatMap(Chapter::content).ifPresent(old -> {
                    chapter.content().ifPresent(neu -> neu.copyStateFrom(old));
                });
                chapters.put(chapter.id(), chapter);
            });
        }

        return screen.getState().getCurrentPageId().map(chapters::get).orElse(craftingChapter);
    }

    public record Chapter (
        Identifier id,
        TabSide side,
        int tabY,
        int color,
        Optional<Content> content) {

        public Identifier icon() {
            return new Identifier(id().getNamespace(), "textures/gui/container/pages/" + id().getPath() + ".png");
        }
    }

    public enum TabSide {
        LEFT,
        RIGHT
    }

    public interface Content extends Drawable {
        void init(SpellbookScreen screen, Identifier pageId);

        default void copyStateFrom(Content old) {}

        default boolean showInventory() {
            return false;
        }

        static Optional<Content> of(BiConsumer<SpellbookScreen, Identifier> init, Drawable obj) {
            return Optional.of(new Content() {
                @Override
                public void init(SpellbookScreen screen, Identifier pageId) {
                    init.accept(screen, pageId);
                }

                @Override
                public void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {
                    obj.draw(matrices, mouseX, mouseY, container);
                }
            });
        }
    }

    public interface Drawable {
        void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container);
    }
}
