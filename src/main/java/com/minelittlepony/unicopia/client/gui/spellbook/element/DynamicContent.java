package com.minelittlepony.unicopia.client.gui.spellbook.element;

import java.util.*;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.gui.MagicText;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Content;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Drawable;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.container.spellbook.Flow;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.*;

public class DynamicContent implements Content {
    private static final Text UNKNOWN = Text.of("???");

    private SpellbookState.PageState state = new SpellbookState.PageState();
    private final List<Page> pages;

    private Bounds bounds = Bounds.empty();

    private final Panel leftPanel = new Panel(this);
    private final Panel rightPanel = new Panel(this);

    public DynamicContent(PacketByteBuf buffer) {
        pages = buffer.readList(Page::new);
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {
        int pageIndex = state.getOffset() * 2;
        MinecraftClient client = MinecraftClient.getInstance();

        Text pageText = Text.translatable("%s/%s", (pageIndex / 2) + 1, (int)Math.ceil(pages.size() / 2F));
        context.drawText(client.textRenderer, pageText, (int)(337 - client.textRenderer.getWidth(pageText) / 2F), 190, MagicText.getColor(), false);
    }

    @Override
    public void copyStateFrom(Content old) {
        if (old instanceof DynamicContent o) {
            if (state.getOffset() == o.state.getOffset()) {
                leftPanel.verticalScrollbar.getScrubber().scrollTo(o.leftPanel.verticalScrollbar.getScrubber().getPosition(), false);
                rightPanel.verticalScrollbar.getScrubber().scrollTo(o.rightPanel.verticalScrollbar.getScrubber().getPosition(), false);
            }
            state = o.state;
            setBounds(o.bounds);
        }
    }

    Optional<Page> getPage(int index) {
        if (index < 0 || index >= pages.size()) {
            return Optional.empty();
        }
        return Optional.of(pages.get(index));
    }

    private void setBounds(Bounds bounds) {
        this.bounds = bounds;
        pages.forEach(page -> {
            page.reset();
            int oldHeight = page.bounds.height;
            page.bounds.copy(bounds);
            page.bounds.left = 0;
            page.bounds.top = 0;
            page.bounds.width /= 2;
            page.bounds.height = oldHeight;
        });

        leftPanel.setBounds(bounds);
    }

    @Override
    public void init(SpellbookScreen screen, Identifier pageId) {
        state = screen.getState().getState(pageId);
        setBounds(screen.getFrameBounds());
        screen.addPageButtons(187, 30, 350, incr -> {
            state.swap(incr, (int)Math.ceil(pages.size() / 2F));
        });

        int pageIndex = state.getOffset() * 2;
        leftPanel.init(screen, pageIndex);
        rightPanel.init(screen, pageIndex + 1);
    }

    public class Page implements Drawable {
        private final Text title;
        private final int level;
        private final int color;

        private final List<PageElement> elements;

        private boolean compiled;

        private Bounds bounds = Bounds.empty();

        public Page(PacketByteBuf buffer) {
            title = TextCodecs.PACKET_CODEC.decode(buffer);
            level = buffer.readInt();
            color = buffer.readInt();
            elements = buffer.readList(r -> PageElement.read(this, r));
        }

        protected int getLineLimitAt(int yPosition) {
            return (bounds.width - 10) - elements.stream()
                    .filter(PageElement::isFloating)
                    .map(PageElement::bounds)
                    .filter(b -> b.containsY(yPosition))
                    .mapToInt(b -> b.width)
                    .sum();
        }

        protected int getLeftMarginAt(int yPosition) {
            return elements.stream()
                    .filter(p -> p.flow() == Flow.LEFT)
                    .map(PageElement::bounds)
                    .filter(b -> b.containsY(yPosition))
                    .mapToInt(b -> b.width)
                    .sum();
        }

        protected int getLevel() {
            return level;
        }

        protected Bounds getBounds() {
            return bounds;
        }

        public int getColor() {
            return color == 0 ? MagicText.getColor() : color;
        }

        public void reset() {
            compiled = false;
        }

        public void drawHeader(DrawContext context, int mouseX, int mouseY) {

            if (elements.isEmpty()) {
                return;
            }
            boolean needsMoreXp = level < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < level;
            int x = bounds.left;
            int y = bounds.top - 16;

            DrawableUtil.drawScaledText(context, needsMoreXp ? UNKNOWN : title, x, y, 1.3F, MagicText.getColor());
            DrawableUtil.drawScaledText(context, Text.translatable("gui.unicopia.spellbook.page.level_requirement", level < 0 ? "???" : "" + (level + 1)).formatted(Formatting.DARK_GREEN), x, y + 12, 0.8F, MagicText.getColor());
        }

        @Override
        public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {

            if (elements.isEmpty()) {
                return;
            }

            if (!compiled) {
                compiled = true;
                int relativeY = 0;
                int textHeight = 0;
                for (PageElement element : elements.stream().filter(PageElement::isInline).toList()) {
                    element.compile(relativeY, container);
                    relativeY += element.bounds().height;
                    if (element instanceof TextBlock) {
                        textHeight += element.bounds().height;
                    }
                }
                bounds.height = textHeight == 0 ? 0 : relativeY;
            }

            MatrixStack matrices = context.getMatrices();

            matrices.push();
            matrices.translate(0, -8, 0);
            elements.stream().filter(PageElement::isFloating).forEach(element -> {
                matrices.push();
                element.bounds().translate(matrices);
                element.draw(context, mouseX, mouseY, container);
                matrices.pop();
            });

            matrices.push();
            elements.stream().filter(PageElement::isInline).forEach(element -> {
                element.draw(context, mouseX, mouseY, container);
                matrices.translate(0, element.bounds().height, 0);
            });
            matrices.pop();

            matrices.pop();
        }

        @Override
        public String toString() {
            return "DynamicContents$Page[title=" + title + ",elements=" + elements + "]";
        }
    }
}