package com.minelittlepony.unicopia.client.gui;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.Race;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

public class TribeConfirmationScreen extends GameGui implements HidesHud {
    private final Race selection;

    private final BooleanConsumer callback;

    public TribeConfirmationScreen(BooleanConsumer callback, Race selection) {
        super(new TranslatableText("gui.unicopia.tribe_selection"));
        this.callback = callback;
        this.selection = selection;
    }

    @Override
    protected void init() {

        final int columnHeight = 167;
        final int columnWidth = 310;
        final int padding = 15;

        int top = (height - columnHeight) / 2;

        addDrawableChild(new ButtonWidget(width / 2 + 5, top + columnHeight + padding, 100, 20, new TranslatableText("Join Tribe"), b -> callback.accept(true)));
        addDrawableChild(new ButtonWidget(width / 2 - 105, top + columnHeight + padding, 100, 20, new TranslatableText("Go Back"), b -> callback.accept(false)));

        addDrawable(new Label(width / 2, top - 30).setCentered()).getStyle().setText(new TranslatableText("gui.unicopia.tribe_selection.confirm", selection.getDisplayName().shallowCopy().formatted(Formatting.YELLOW)));

        addDrawable(new TribeButton((width - 70) / 2, top, 0, selection));

        top += 43;

        int left = (width - columnWidth) / 2 + padding;

        Text race = selection.getAltDisplayName().shallowCopy().formatted(Formatting.YELLOW);

        addDrawable(new Label(left - 3, top += 10)).getStyle().setText(new TranslatableText("gui.unicopia.tribe_selection.confirm.goods", race).formatted(Formatting.YELLOW));

        top += 15;

        int maxWidth = 280;

        for (int i = 0; i < 5; i++) {
            String key = String.format("gui.unicopia.tribe_selection.confirm.goods.%d.%s", i, selection.name().toLowerCase());
            if (Language.getInstance().hasTranslation(key)) {
                TextBlock block = addDrawable(new TextBlock(left, top, maxWidth));
                block.getStyle().setText(new TranslatableText(key));
                top += block.getBounds().height;
            }
        }

        addDrawable(new Label(left - 3, top += 5)).getStyle().setText(new TranslatableText("gui.unicopia.tribe_selection.confirm.bads", race).formatted(Formatting.YELLOW));

        top += 15;

        for (int i = 0; i < 5; i++) {
            String key = String.format("gui.unicopia.tribe_selection.confirm.bads.%d.%s", i, selection.name().toLowerCase());
            if (Language.getInstance().hasTranslation(key)) {
                TextBlock block = addDrawable(new TextBlock(left, top, maxWidth));
                block.getStyle().setText(new TranslatableText(key));
                top += block.getBounds().height;
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        RenderSystem.setShaderTexture(0, TribeSelectionScreen.TEXTURE);

        final int columnHeight = 180;
        final int columnWidth = 310;
        final int segmentWidth = 123;

        int top = (height - columnHeight) / 2;
        int left = (width - columnWidth) / 2;

        top += 25;

        final int zOffset = 0;

        drawTexture(matrices, left + zOffset, top, 0, 70, 123, columnHeight);

        drawTexture(matrices, left + segmentWidth + zOffset, top, 20, 70, 123, columnHeight);

        drawTexture(matrices, width - left - segmentWidth + zOffset, top, 10, 70, 123, columnHeight);

        top -= 31;

        left = width / 2;

        drawTexture(matrices, left - 55, top, 140, 70, 21, 50);

        drawTexture(matrices, left - 35, top, 10, 70, 69, 50);

        drawTexture(matrices, left + 35, top, 148, 70, 21, 50);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {

            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
