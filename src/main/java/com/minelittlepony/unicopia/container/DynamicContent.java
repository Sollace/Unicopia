package com.minelittlepony.unicopia.container;

import java.util.*;

import com.google.gson.*;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.container.SpellbookChapterList.Content;
import com.minelittlepony.unicopia.container.SpellbookChapterList.Drawable;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;

public class DynamicContent implements Content {
    private static final Text UNKNOWN = Text.of("???");
    private static final Text UNKNOWN_LEVEL = Text.literal("Level: ???").formatted(Formatting.DARK_GREEN);

    private int offset = 0;
    private final List<Page> pages = new ArrayList<>();

    private Bounds bounds = Bounds.empty();

    public DynamicContent(JsonArray json) {
        json.forEach(element -> pages.add(new Page(element.getAsJsonObject())));
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {
        int pageIndex = offset * 2;

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
            offset = o.offset;
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
    public void init(SpellbookScreen screen) {
        setBounds(screen.getFrameBounds());
        screen.addPageButtons(187, 30, 350, incr -> {
            offset = MathHelper.clamp(offset + incr, 0, (int)Math.ceil(pages.size() / 2F) - 1);
        });
    }

    class Page implements Drawable {
        private final List<PageElement> elements = new ArrayList<>();

        private final Text title;
        private final int level;

        private boolean compiled;

        private Bounds bounds = Bounds.empty();

        public Page(JsonObject json) {
            title = Text.Serializer.fromJson(json.get("title"));
            level = JsonHelper.getInt(json, "level", 0);
            JsonHelper.getArray(json, "elements", new JsonArray()).forEach(element -> {
                elements.add(PageElement.fromJson(this, element));
            });
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
                    .filter(p -> p.flow() == PageElement.Flow.LEFT)
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

            DrawableUtil.drawScaledText(matrices, needsMoreXp ? UNKNOWN : title, bounds.left, bounds.top - 10, 1.3F, mouseY);
            DrawableUtil.drawScaledText(matrices, level < 0 ? UNKNOWN_LEVEL : Text.literal("Level: " + (level + 1)).formatted(Formatting.DARK_GREEN), bounds.left, bounds.top - 10 + 12, 0.8F, mouseY);

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