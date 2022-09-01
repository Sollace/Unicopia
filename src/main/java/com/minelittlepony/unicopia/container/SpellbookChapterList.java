package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SpellbookChapterList {
    public static final Identifier CRAFTING_ID = Unicopia.id("crafting");
    public static final Identifier PROFILE_ID = Unicopia.id("profile");

    private final Chapter craftingChapter;

    private Optional<Identifier> currentChapter = Optional.empty();

    private final Map<Identifier, Chapter> chapters = new HashMap<>();

    public SpellbookChapterList(Chapter craftingChapter, Chapter profileChapter) {
        this.craftingChapter = craftingChapter;
        SpellbookChapterLoader.INSTANCE.getChapters().forEach(chapter -> {
            chapters.put(chapter.id(), chapter);
        });
        chapters.put(craftingChapter.id(), craftingChapter);
        chapters.put(profileChapter.id(), profileChapter);
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
        return currentChapter.map(chapters::get).orElse(craftingChapter);
    }

    public void setCurrentChapter(Chapter chapter) {
        currentChapter = Optional.of(chapter.id());
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
        void init(SpellbookScreen screen);

        default void copyStateFrom(Content old) {}

        static Optional<Content> of(Consumer<SpellbookScreen> init, Drawable obj) {
            return Optional.of(new Content() {
                @Override
                public void init(SpellbookScreen screen) {
                    init.accept(screen);
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
