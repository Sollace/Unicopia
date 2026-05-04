package com.minelittlepony.unicopia.client.gui.spellbook.element;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.container.spellbook.Flow;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

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
            case ChapterPageElement.TextBlock text -> new TextBlock(List.of(text::text));
            case ChapterPageElement.Ingredients ingredients -> new TextBlock(ingredients.entries().stream().map(entry -> switch(entry.element()) {
                case Item item -> formatLine(() -> item.getDefaultStack().getName(), "item", entry.count());
                case Trait trait -> formatLine(trait::getShortName, "trait", entry.count());
                case SpellType<?> spell -> formatLine(spell::getName, "spell", entry.count());
                case Text text -> Suppliers.ofInstance(text);
                default -> throw new IllegalArgumentException("Unexpected value: " + entry);
            }).toList());
            default -> throw new IllegalArgumentException("Unexpected value: " + element);
        };
    }

    private static Supplier<Text> formatLine(Supplier<Text> line, String kind, int count) {
        return () -> Text.translatable("gui.unicopia.spellbook.page.requirements.entry." + kind, count, line.get());
    }
}