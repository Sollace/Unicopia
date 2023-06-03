package com.minelittlepony.unicopia.client.gui;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.Race;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.*;

public class TribeConfirmationScreen extends GameGui implements HidesHud {
    private final Race selection;

    private final BooleanConsumer callback;

    public TribeConfirmationScreen(BooleanConsumer callback, Race selection) {
        super(Text.translatable("gui.unicopia.tribe_selection"));
        this.callback = callback;
        this.selection = selection;
    }

    @Override
    protected void init() {

        final int columnHeight = 167;
        final int columnWidth = 310;
        final int padding = 15;

        int top = (height - columnHeight) / 2;

        addDrawableChild(new Button(width / 2 + 5, top + columnHeight + padding, 100, 20))
            .onClick(b -> callback.accept(true))
            .getStyle().setText("Join Tribe");
        addDrawableChild(new Button(width / 2 - 105, top + columnHeight + padding, 100, 20))
            .onClick(b -> callback.accept(false))
            .getStyle().setText("Go Back");

        addDrawable(new Label(width / 2, top - 30).setCentered()).getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.confirm", selection.getDisplayName().copy().formatted(Formatting.YELLOW)));

        addDrawable(new TribeButton((width - 70) / 2, top, 0, selection));

        top += 43;

        int left = (width - columnWidth) / 2 + padding;

        Text race = selection.getAltDisplayName().copy().formatted(Formatting.YELLOW);

        addDrawable(new Label(left - 3, top += 10)).getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.confirm.goods", race).formatted(Formatting.YELLOW));

        top += 15;

        int maxWidth = 280;

        Identifier id = Race.REGISTRY.getId(selection);

        for (int i = 0; i < 5; i++) {
            String key = String.format("gui.unicopia.tribe_selection.confirm.goods.%d.%s.%s", i, id.getNamespace(), id.getPath());
            if (Language.getInstance().hasTranslation(key)) {
                TextBlock block = addDrawable(new TextBlock(left, top, maxWidth));
                block.getStyle().setText(Text.translatable(key));
                top += block.getBounds().height;
            }
        }

        addDrawable(new Label(left - 3, top += 5)).getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.confirm.bads", race).formatted(Formatting.YELLOW));

        top += 15;

        for (int i = 0; i < 5; i++) {
            String key = String.format("gui.unicopia.tribe_selection.confirm.bads.%d.%s.%s", i, id.getNamespace(), id.getPath());
            if (Language.getInstance().hasTranslation(key)) {
                TextBlock block = addDrawable(new TextBlock(left, top, maxWidth));
                block.getStyle().setText(Text.translatable(key));
                top += block.getBounds().height;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        final int columnHeight = 180;
        final int columnWidth = 310;
        final int segmentWidth = 123;

        int top = (height - columnHeight) / 2;
        int left = (width - columnWidth) / 2;

        top += 25;

        final int zOffset = 0;

        context.drawTexture(TribeSelectionScreen.TEXTURE, left + zOffset, top, 0, 70, 123, columnHeight);

        context.drawTexture(TribeSelectionScreen.TEXTURE, left + segmentWidth + zOffset, top, 20, 70, 123, columnHeight);

        context.drawTexture(TribeSelectionScreen.TEXTURE, width - left - segmentWidth + zOffset, top, 10, 70, 123, columnHeight);

        top -= 31;

        left = width / 2;

        context.drawTexture(TribeSelectionScreen.TEXTURE, left - 55, top, 140, 70, 21, 50);

        context.drawTexture(TribeSelectionScreen.TEXTURE, left - 35, top, 10, 70, 69, 50);

        context.drawTexture(TribeSelectionScreen.TEXTURE, left + 35, top, 148, 70, 21, 50);

        super.render(context, mouseX, mouseY, delta);
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
