package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.*;
import net.minecraft.text.StringVisitable.StyledVisitor;

class ParagraphWrappingVisitor implements StyledVisitor<Object> {
    private int line = 0;
    private int pageWidth;

    private final TextRenderer font = MinecraftClient.getInstance().textRenderer;
    private final TextHandler handler = font.getTextHandler();

    private float currentLineCollectedLength = 0;
    private MutableText currentLine = Text.empty();
    private boolean progressedNonEmpty;

    private final Int2IntFunction widthSupplier;
    private final Consumer<Text> lineConsumer;

    ParagraphWrappingVisitor(DynamicContent.Page page, Int2IntFunction widthSupplier, Consumer<Text> lineConsumer) {
        this.widthSupplier = widthSupplier;
        this.lineConsumer = lineConsumer;
        pageWidth = widthSupplier.applyAsInt((line) * font.fontHeight);
    }

    @Override
    public Optional<Object> accept(Style style, String s) {

        int remainingLength = (int)(pageWidth - currentLineCollectedLength);

        while (!s.isEmpty()) {
            int trimmedLength = handler.getTrimmedLength(s, remainingLength, style);
            int newline = s.indexOf('\n');

            if (newline >= 0 && newline < trimmedLength) {
                trimmedLength = newline;
            } else {
                newline = -1;
            }

            if (trimmedLength == 0) {
                trimmedLength = s.length();
            }

            String wrappedFragment = s.substring(0, trimmedLength);
            int lastSpace = wrappedFragment.lastIndexOf(' ');
            trimmedLength = lastSpace > 0 ? Math.min(lastSpace, trimmedLength) : trimmedLength;

            Text fragment = Text.literal(s.substring(0, trimmedLength).trim()).setStyle(style);
            float grabbedWidth = handler.getWidth(fragment);

            if (currentLineCollectedLength + grabbedWidth > pageWidth) {
                advance();
            }

            if (currentLineCollectedLength > 0) {
                currentLine.append(" ");
                currentLineCollectedLength += handler.getWidth(" ");
            }
            currentLine.append(fragment);
            currentLineCollectedLength += grabbedWidth;

            if (trimmedLength <= s.length()) {
                s = s.substring(trimmedLength, s.length());
            }

            remainingLength = pageWidth;
        }

        return Optional.empty();
    }

    public void forceAdvance() {
        if (currentLineCollectedLength > 0) {
            advance();
        }
        advance();
    }

    public void advance() {
        if (progressedNonEmpty || currentLineCollectedLength > 0) {
            progressedNonEmpty = true;
            lineConsumer.accept(currentLine);
        }
        pageWidth = widthSupplier.applyAsInt((++line) * font.fontHeight);
        currentLine = Text.empty();
        currentLineCollectedLength = 0;
    }

    record StyledString (String string, Style style) {}
}