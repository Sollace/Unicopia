package com.minelittlepony.unicopia.client;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;

public interface TextHelper {
    static Stream<Text> wrap(Text text, int maxWidth) {
        return MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(text, maxWidth, Style.EMPTY).stream().map(line -> {
            MutableText compiled = Text.literal("");
            line.visit((s, t) -> {
                compiled.append(Text.literal(t).setStyle(s));
                return Optional.empty();
            }, text.getStyle());
            return compiled;
        });
    }

    static Text join(Text delimiter, Iterable<? extends MutableText> elements) {
        MutableText initial = Text.empty();
        return StreamSupport.stream(elements.spliterator(), false).collect(Collectors.reducing(initial, (a, b) -> {
            return a == initial ? b : a.append(delimiter).append(b);
        }));
    }
}
