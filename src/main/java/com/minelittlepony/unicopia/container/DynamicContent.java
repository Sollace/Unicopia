package com.minelittlepony.unicopia.container;

import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.container.SpellbookChapterList.Content;
import com.minelittlepony.unicopia.container.SpellbookChapterList.Draw;
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

    class Page implements Draw {
        private final List<Page.Paragraph> paragraphs = new ArrayList<>();
        private final List<Page.Image> images = new ArrayList<>();

        private boolean compiled = false;
        private final List<Text> wrappedText = new ArrayList<>();

        private final Text title;
        private final int level;

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
                            )
                        ));
                    } else {
                        paragraphs.add(new Paragraph(lineNumber[0], Text.Serializer.fromJson(element)));
                    }
                }

                lineNumber[0]++;
            });
        }

        public void compile() {
            if (!compiled) {
                compiled = true;
                wrappedText.clear();
                ParagraphWrappingVisitor visitor = new ParagraphWrappingVisitor(this, yPosition -> {
                    return (bounds.width / 2 - 10) - images.stream()
                        .map(Image::bounds)
                        .filter(b -> b.contains(b.left + b.width / 2, yPosition))
                        .mapToInt(b -> b.width)
                        .max()
                        .orElse(0);
                }, wrappedText::add);
                paragraphs.forEach(paragraph -> {
                    paragraph.text().visit(visitor, Style.EMPTY);
                    visitor.forceAdvance();
                });
            }
        }

        public void reset() {
            compiled = false;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY) {

            compile();
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
            matrices.translate(0, 16, 0);

            for (int y = 0; y < wrappedText.size(); y++) {
                Text line = wrappedText.get(y);
                if (needsMoreXp) {
                    line = line.copy().formatted(Formatting.OBFUSCATED);
                }
                font.draw(matrices, line,
                        bounds.left,
                        bounds.top + (y * font.fontHeight),
                        0
                );
            }

            matrices.translate(bounds.left, bounds.top, 0);
            images.forEach(image -> image.draw(matrices, mouseX, mouseY));
            matrices.pop();
        }

        public record Paragraph(int y, Text text) {}

        public record Image(
            Identifier texture,
            Bounds bounds) implements Draw {
            @Override
            public void draw(MatrixStack matrices, int mouseX, int mouseY) {
                RenderSystem.setShaderTexture(0, texture);
                DrawableHelper.drawTexture(matrices, bounds().left, bounds().top, 0, 0, 0, bounds().width, bounds().height, bounds().width, bounds().height);
                RenderSystem.setShaderTexture(0, SpellbookScreen.TEXTURE);
            }
        }
    }
}