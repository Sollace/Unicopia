package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.container.spellbook.Flow;

import net.minecraft.client.gui.DrawContext;

public interface PageElement {
    default void draw(DynamicContent.GuiPage page, DrawContext context, int mouseX, int mouseY, IViewRoot container) {

    }

    Bounds bounds();

    default Flow flow() {
        return Flow.NONE;
    }

    default boolean isInline() {
        return flow() == Flow.NONE;
    }

    default boolean isFloating() {
        return !isInline();
    }

    default void compile(DynamicContent.GuiPage page, int y, IViewRoot Container) {}

    static PageElement of(ChapterPageElement element) {
        return switch(element) {
            case ChapterPageElement.Image image -> new Image(image);
            case ChapterPageElement.Recipe recipe -> new Recipe(recipe);
            case ChapterPageElement.Stack stack -> new Stack(stack);
            case ChapterPageElement.TextElement text -> new TextBlock(text);
            case ChapterPageElement.Structure structure -> new Structure(Bounds.empty(), structure.toSchematic());
            default -> throw new IllegalArgumentException("Unexpected value: " + element);
        };
    }
}