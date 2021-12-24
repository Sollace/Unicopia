package com.minelittlepony.unicopia.client;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public interface FlowingText {
    static Stream<Text> wrap(Text text, int maxWidth) {
        return MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(text, maxWidth, Style.EMPTY).stream().map(line -> {
            LiteralText compiled = new LiteralText("");
            line.visit((s, t) -> {
                compiled.append(new LiteralText(t).setStyle(s));
                return Optional.empty();
            }, text.getStyle());
            return compiled;
        });
    }
}
