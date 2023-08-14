package com.minelittlepony.unicopia.client.gui.spellbook.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.client.gui.ParagraphWrappingVisitor;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader.Flow;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

class TextBlock implements PageElement {
    private final DynamicContent.Page page;

    private final List<TextBlock.Line> wrappedText = new ArrayList<>();
    private final Bounds bounds = Bounds.empty();
    private final List<Supplier<Text>> uncompiledLines;

    public TextBlock(DynamicContent.Page page, List<Supplier<Text>> uncompiledLines) {
        this.page = page;
        this.uncompiledLines = uncompiledLines;
    }

    @Override
    public void compile(int y, IViewRoot container) {
        wrappedText.clear();
        ParagraphWrappingVisitor visitor = new ParagraphWrappingVisitor(
                yPosition -> page.getLineLimitAt(y + yPosition),
                (line, yPosition) -> wrappedText.add(new Line(line, page.getLeftMarginAt(y + yPosition)))
        );
        uncompiledLines.forEach(line -> {
            line.get().visit(visitor, Style.EMPTY);
            visitor.advance();
        });
        visitor.forceAdvance();
        bounds.height = MinecraftClient.getInstance().textRenderer.fontHeight * (wrappedText.size());
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        boolean needsMoreXp = page.getLevel() < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < page.getLevel();
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        wrappedText.forEach(line -> {
            context.drawText(font, needsMoreXp ? line.text().copy().formatted(Formatting.OBFUSCATED) : line.text().copy(), line.x(), 0, 0, false);
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