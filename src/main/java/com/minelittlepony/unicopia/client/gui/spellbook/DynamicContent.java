package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.*;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Content;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Drawable;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader.Flow;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;

public class DynamicContent implements Content {
    private static final Text UNKNOWN = Text.of("???");
    private static final Text UNKNOWN_LEVEL = new LiteralText("Level: ???").formatted(Formatting.DARK_GREEN);

    private SpellbookState.PageState state = new SpellbookState.PageState();
    private final List<Page> pages;

    private Bounds bounds = Bounds.empty();

    public DynamicContent(PacketByteBuf buffer) {
        pages = buffer.readList(Page::new);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {
        int pageIndex = state.getOffset() * 2;

        getPage(pageIndex).ifPresent(page -> page.draw(matrices, mouseX, mouseY, container));

        matrices.push();
        getPage(pageIndex + 1).ifPresent(page -> {
            page.bounds.left = bounds.left + bounds.width / 2 + 20;
            page.draw(matrices, mouseX, mouseY, container);
        });
        matrices.pop();
    }

    @Override
    public void copyStateFrom(Content old) {
        if (old instanceof DynamicContent o) {
            state = o.state;
            setBounds(o.bounds);
        }
    }

    private Optional<Page> getPage(int index) {
        if (index < 0 || index >= pages.size()) {
            return Optional.empty();
        }
        return Optional.of(pages.get(index));
    }

    private void setBounds(Bounds bounds) {
        this.bounds = bounds;
        pages.forEach(page -> {
            page.reset();
            page.bounds.copy(bounds);
            page.bounds.width /= 2;
        });
    }

    @Override
    public void init(SpellbookScreen screen, Identifier pageId) {
        state = screen.getState().getState(pageId);
        setBounds(screen.getFrameBounds());
        screen.addPageButtons(187, 30, 350, incr -> {
            state.swap(incr, (int)Math.ceil(pages.size() / 2F));
        });
    }

    class Page implements Drawable {
        private final Text title;
        private final int level;

        private final List<PageElement> elements;

        private boolean compiled;

        private Bounds bounds = Bounds.empty();

        public Page(PacketByteBuf buffer) {
            title = buffer.readText();
            level = buffer.readInt();
            elements = buffer.readList(r -> PageElement.read(this, r));
        }

        protected int getLineLimitAt(int yPosition) {
            return (bounds.width - 10) - elements.stream()
                    .filter(PageElement::isFloating)
                    .map(PageElement::bounds)
                    .filter(b -> b.contains(b.left + b.width / 2, yPosition))
                    .mapToInt(b -> b.width)
                    .sum();
        }

        protected int getLeftMarginAt(int yPosition) {
            return elements.stream()
                    .filter(p -> p.flow() == Flow.LEFT)
                    .map(PageElement::bounds)
                    .filter(b -> b.contains(b.left + b.width / 2, yPosition))
                    .mapToInt(b -> b.width)
                    .sum();
        }

        protected int getLevel() {
            return level;
        }

        protected Bounds getBounds() {
            return bounds;
        }

        public void reset() {
            compiled = false;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {

            if (elements.isEmpty()) {
                return;
            }

            if (!compiled) {
                compiled = true;
                int relativeY = 0;
                for (PageElement element : elements.stream().filter(PageElement::isInline).toList()) {
                    element.compile(relativeY, container);
                    relativeY += element.bounds().height;
                }
            }

            boolean needsMoreXp = level < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < level;

            int headerColor = mouseY % 255;

            DrawableUtil.drawScaledText(matrices, needsMoreXp ? UNKNOWN : title, bounds.left, bounds.top - 10, 1.3F, headerColor);
            DrawableUtil.drawScaledText(matrices, level < 0 ? UNKNOWN_LEVEL : new LiteralText("Level: " + (level + 1)).formatted(Formatting.DARK_GREEN), bounds.left, bounds.top - 10 + 12, 0.8F, headerColor);

            matrices.push();
            matrices.translate(bounds.left, bounds.top + 16, 0);
            elements.stream().filter(PageElement::isFloating).forEach(element -> {
                Bounds bounds = element.bounds();
                matrices.push();
                matrices.translate(bounds.left, bounds.top, 0);
                element.draw(matrices, mouseX, mouseY, container);
                matrices.pop();
            });

            matrices.push();
            elements.stream().filter(PageElement::isInline).forEach(element -> {
                element.draw(matrices, mouseX, mouseY, container);
                matrices.translate(0, element.bounds().height, 0);
            });
            matrices.pop();

            matrices.pop();
        }
    }
}