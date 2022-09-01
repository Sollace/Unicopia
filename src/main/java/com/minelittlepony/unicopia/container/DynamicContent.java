package com.minelittlepony.unicopia.container;

import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.container.SpellbookChapterList.Content;
import com.minelittlepony.unicopia.container.SpellbookChapterList.Drawable;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;

public class DynamicContent implements Content {

    private int offset = 0;
    private final List<Page> pages = new ArrayList<>();

    Bounds bounds = Bounds.empty();

    public DynamicContent(JsonArray pages) {
        pages.forEach(page -> this.pages.add(new Page(page.getAsJsonObject())));
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY) {
        int pageIndex = offset * 2;

        getPage(pageIndex).ifPresent(page -> page.draw(matrices, mouseX, mouseY));

        matrices.push();
        matrices.translate(bounds.width / 2 + 20, 0, 0);
        getPage(pageIndex + 1).ifPresent(page -> page.draw(matrices, mouseX, mouseY));
        matrices.pop();
    }

    @Override
    public void copyStateFrom(Content old) {
        if (old instanceof DynamicContent o) {
            offset = o.offset;
            bounds = o.bounds;
        }
    }

    private Optional<Page> getPage(int index) {
        if (index < 0 || index >= pages.size()) {
            return Optional.empty();
        }
        return Optional.of(pages.get(index));
    }

    @Override
    public void init(SpellbookScreen screen) {
        bounds = screen.getFrameBounds();
        pages.forEach(Page::reset);
        screen.addPageButtons(187, 30, 350, incr -> {
            offset = MathHelper.clamp(offset + incr, 0, (int)Math.ceil(pages.size() / 2F) - 1);
        });
    }

    class Page implements Drawable {
        private final List<Page.Paragraph> paragraphs = new ArrayList<>();
        private final List<Page.Image> images = new ArrayList<>();

        private final List<PageElement> elements = new ArrayList<>();

        private final Text title;
        private final int level;

        private boolean compiled;

        public Page(JsonObject json) {
            title = Text.Serializer.fromJson(json.get("title"));
            level = JsonHelper.getInt(json, "level", 0);
            int[] lineNumber = new int[1];
            JsonHelper.getArray(json, "elements", new JsonArray()).forEach(element -> {
                if (element.isJsonPrimitive()) {
                    paragraphs.add(new Paragraph(lineNumber[0], Text.Serializer.fromJson(element)));
                } else {
                    JsonObject image = JsonHelper.asObject(element, "element");
                    if (image.has("texture")) {
                        images.add(new Image(
                            new Identifier(JsonHelper.getString(image, "texture")),
                            new Bounds(
                                JsonHelper.getInt(image, "y", 0),
                                JsonHelper.getInt(image, "x", 0),
                                JsonHelper.getInt(image, "width", 0),
                                JsonHelper.getInt(image, "height", 0)
                            ),
                            Flow.valueOf(JsonHelper.getString(image, "flow", "RIGHT"))
                        ));
                    } else {
                        paragraphs.add(new Paragraph(lineNumber[0], Text.Serializer.fromJson(element)));
                    }
                }

                lineNumber[0]++;
            });
        }

        public void reset() {
            compiled = false;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY) {
            if (!compiled) {
                compiled = true;
                int relativeY = 0;
                for (PageElement element : elements.stream().filter(PageElement::isInline).toList()) {
                    element.compile(relativeY);
                    relativeY += element.bounds().height;
                }
            }

            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            boolean needsMoreXp = level < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < level;

            matrices.push();
            matrices.translate(bounds.left, bounds.top - 10, mouseY);
            matrices.scale(1.3F, 1.3F, 1);
            font.draw(matrices, needsMoreXp ? Text.of("???") : title, 0, 0, mouseY);
            matrices.pop();

            matrices.push();
            matrices.translate(bounds.left, bounds.top - 10, mouseY);
            matrices.translate(0, 12, 0);
            matrices.scale(0.8F, 0.8F, 1);
            font.draw(matrices, Text.literal(level < 0 ? "Level: ???" : "Level: " + (level + 1)).formatted(Formatting.DARK_GREEN), 0, 0, mouseY);
            matrices.pop();

            matrices.push();
            matrices.translate(bounds.left, bounds.top + 16, 0);
            elements.stream().filter(PageElement::isFloating).forEach(element -> {
                Bounds bounds = element.bounds();
                matrices.push();
                matrices.translate(bounds.left, bounds.top, 0);
                element.draw(matrices, mouseX, mouseY);
                matrices.pop();
            });
            matrices.push();

            elements.stream().filter(PageElement::isInline).forEach(element -> {
                element.draw(matrices, mouseX, mouseY);
                matrices.translate(0, element.bounds().height, 0);
            });
            matrices.pop();

            images.forEach(image -> image.draw(matrices, mouseX, mouseY));
            matrices.pop();
        }

        public record Paragraph(int y, Text text) {}

        public record Image(
            Identifier texture,
            Bounds bounds,
            Flow flow) implements PageElement {
            @Override
            public void draw(MatrixStack matrices, int mouseX, int mouseY) {
                RenderSystem.setShaderTexture(0, texture);
                DrawableHelper.drawTexture(matrices, bounds().left, bounds().top, 0, 0, 0, bounds().width, bounds().height, bounds().width, bounds().height);
                RenderSystem.setShaderTexture(0, SpellbookScreen.TEXTURE);
            }
        }

        protected int getLineLimitAt(int yPosition) {
            return (bounds.width / 2 - 10) - elements.stream()
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

        public class TextBlock implements PageElement {
            private final Text unwrappedText;
            private final List<Line> wrappedText = new ArrayList<>();
            private final Bounds bounds = Bounds.empty();

            public TextBlock(Text text) {
                unwrappedText = text;
            }

            @Override
            public void compile(int y) {
                wrappedText.clear();
                ParagraphWrappingVisitor visitor = new ParagraphWrappingVisitor(yPosition -> {
                    return getLineLimitAt(y + yPosition);
                }, (line, yPosition) -> {
                    wrappedText.add(new Line(line, getLeftMarginAt(y + yPosition)));
                });
                unwrappedText.visit(visitor, Style.EMPTY);
                visitor.forceAdvance();
                bounds.height = MinecraftClient.getInstance().textRenderer.fontHeight * (wrappedText.size() + 1);
            }

            @Override
            public void draw(MatrixStack matrices, int mouseX, int mouseY) {
                TextRenderer font = MinecraftClient.getInstance().textRenderer;
                boolean needsMoreXp = level < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < level;
                matrices.push();
                wrappedText.forEach(line -> {
                    font.draw(matrices, needsMoreXp ? line.text().copy().formatted(Formatting.OBFUSCATED) : line.text(), line.x(), 0, 0);
                    matrices.translate(0, font.fontHeight, 0);
                });
                matrices.pop();
            }

            @Override
            public Bounds bounds() {
                return bounds;
            }

            @Override
            public Flow flow() {
                return Flow.NONE;
            }

            private record Line(Text text, int x) { }
        }

        private interface PageElement extends Drawable {
            Bounds bounds();

            Flow flow();

            default boolean isInline() {
                return flow() == Flow.NONE;
            }

            default boolean isFloating() {
                return !isInline();
            }

            default void compile(int y) {}
        }

        private enum Flow {
            NONE, LEFT, RIGHT
        }
    }
}