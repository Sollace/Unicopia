package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.client.gui.ParagraphWrappingVisitor;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Drawable;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader.Flow;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;

interface PageElement extends Drawable {
    @Override
    default void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {

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

    default void compile(int y, IViewRoot Container) {}

    static PageElement read(DynamicContent.Page page, PacketByteBuf buffer) {
        byte type = buffer.readByte();
        return (switch (type) {
            case 0 -> new Image(buffer.readIdentifier(), boundsFromBuffer(buffer), buffer.readEnumConstant(Flow.class));
            case 1 -> new Recipe(page, buffer.readIdentifier(), Bounds.empty());
            case 2 -> new Stack(page, IngredientWithSpell.fromPacket(buffer), boundsFromBuffer(buffer));
            case 3 -> new TextBlock(page, buffer.readText());
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        });
    }

    private static Bounds boundsFromBuffer(PacketByteBuf buffer) {
        return new Bounds(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    record Image(
        Identifier texture,
        Bounds bounds,
        Flow flow) implements PageElement {
        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {
            RenderSystem.setShaderTexture(0, texture);
            DrawableHelper.drawTexture(matrices, 0, 0, 0, 0, 0, bounds().width, bounds().height, bounds().width, bounds().height);
            RenderSystem.setShaderTexture(0, SpellbookScreen.TEXTURE);
        }
    }

    class TextBlock implements PageElement {
        private final DynamicContent.Page page;

        private final Text unwrappedText;
        private final List<Line> wrappedText = new ArrayList<>();
        private final Bounds bounds = Bounds.empty();

        public TextBlock(DynamicContent.Page page,Text text) {
            this.page = page;
            unwrappedText = text;
        }

        @Override
        public void compile(int y, IViewRoot container) {
            wrappedText.clear();
            ParagraphWrappingVisitor visitor = new ParagraphWrappingVisitor(
                    yPosition -> page.getLineLimitAt(y + yPosition),
                    (line, yPosition) -> wrappedText.add(new Line(line, page.getLeftMarginAt(y + yPosition)))
            );
            unwrappedText.visit(visitor, Style.EMPTY);
            visitor.forceAdvance();
            bounds.height = MinecraftClient.getInstance().textRenderer.fontHeight * (wrappedText.size());
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            boolean needsMoreXp = page.getLevel() < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < page.getLevel();
            matrices.push();
            wrappedText.forEach(line -> {
                font.draw(matrices, needsMoreXp ? line.text().copy().formatted(Formatting.OBFUSCATED) : line.text().copy(), line.x(), 0, 0);
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

    record Recipe (DynamicContent.Page page, Identifier id, Bounds bounds) implements PageElement {
        @Override
        public void compile(int y, IViewRoot container) {
            if (container instanceof SpellbookScreen book) {
                bounds().left = book.getX();
                bounds().top = book.getY();
            }
            MinecraftClient.getInstance().world.getRecipeManager().get(id).ifPresent(recipe -> {
                if (recipe instanceof SpellbookRecipe spellRecipe) {

                    boolean needsMoreXp = page.getLevel() < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < page.getLevel();

                    IngredientTree tree = new IngredientTree(
                            bounds().left + page().getBounds().left,
                            bounds().top + page().getBounds().top + y + 10,
                            page().getBounds().width - 20
                    ).obfuscateResult(needsMoreXp);
                    spellRecipe.buildCraftingTree(tree);
                    bounds.height = tree.build(container) - 10;
                }
            });
        }
    }

    record Stack (DynamicContent.Page page, IngredientWithSpell ingredient, Bounds bounds) implements PageElement {
        @Override
        public void compile(int y, IViewRoot container) {
            if (container instanceof SpellbookScreen book) {
                bounds().left = book.getX();
                bounds().top = book.getY();
            }
            IngredientTree tree = new IngredientTree(
                    bounds().left + page().getBounds().left,
                    bounds().top + page().getBounds().top + y + 10,
                    30
            );
            tree.input(ingredient.getMatchingStacks());
            bounds.height = tree.build(container) - 10;
        }
    }
}