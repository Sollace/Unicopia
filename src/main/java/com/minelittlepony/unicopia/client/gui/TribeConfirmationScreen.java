package com.minelittlepony.unicopia.client.gui;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
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

    private final ScrollContainer textBody = new ScrollContainer();

    public TribeConfirmationScreen(BooleanConsumer callback, Race selection) {
        super(Text.translatable("gui.unicopia.tribe_selection"));
        this.callback = callback;
        this.selection = selection;
    }

    @Override
    protected void init() {
        if (parent != null) {
            parent.init(client, width, height);
        }
        final int columnHeight = 167;
        final int columnWidth = 310;
        final int padding = 15;

        int top = (height - columnHeight) / 2;
        int left = (width - columnWidth) / 2 + 8;
        int maxWidth = 295;

        textBody.verticalScrollbar.layoutToEnd = true;
        textBody.margin.top = top + 43;
        textBody.margin.left = left;
        textBody.margin.right = width - (left + maxWidth);
        textBody.margin.bottom = height - (textBody.margin.top + 130);
        textBody.getContentPadding().top = 10;
        textBody.getContentPadding().left = 8;
        textBody.getContentPadding().bottom = 100;
        textBody.getContentPadding().right = 0;
        textBody.init(this::buildTextBody);

        getChildElements().add(textBody);

        addDrawableChild(new Button(width / 2 + 5, top + columnHeight + padding, 100, 20))
            .onClick(b -> callback.accept(true))
            .getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.join"));
        addDrawableChild(new Button(width / 2 - 105, top + columnHeight + padding, 100, 20))
            .onClick(b -> callback.accept(false))
            .getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.cancel"));

        addDrawable(new Label(width / 2, top - 30).setCentered()).getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.confirm", selection.getDisplayName().copy().formatted(Formatting.YELLOW)));

        addDrawable(new TribeButton((width - 70) / 2, top, 0, selection));
    }

    private void buildTextBody() {
        int top = 0;

        int left = 0;

        Text race = selection.getAltDisplayName().copy().formatted(Formatting.YELLOW);

        textBody.addButton(new Label(left - 3, top += 10)).getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.confirm.goods", race).formatted(Formatting.YELLOW));

        top += 15;

        int maxWidth = 270;

        Identifier id = Race.REGISTRY.getId(selection);

        for (int i = 0; i < 10; i++) {
            String key = String.format("gui.unicopia.tribe_selection.confirm.goods.%d.%s.%s", i, id.getNamespace(), id.getPath());
            if (Language.getInstance().hasTranslation(key)) {
                TextBlock block = textBody.addButton(new TextBlock(left, top, maxWidth));
                block.getStyle().setText(Text.translatable(key));
                top += block.getBounds().height;
            }
        }

        textBody.addButton(new Label(left - 3, top += 5)).getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.confirm.bads", race).formatted(Formatting.YELLOW));

        top += 15;

        for (int i = 0; i < 10; i++) {
            String key = String.format("gui.unicopia.tribe_selection.confirm.bads.%d.%s.%s", i, id.getNamespace(), id.getPath());
            if (Language.getInstance().hasTranslation(key)) {
                TextBlock block = textBody.addButton(new TextBlock(left, top, maxWidth));
                block.getStyle().setText(Text.translatable(key));
                top += block.getBounds().height;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, -2);
        if (parent != null) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, -100);
            parent.render(context, -1, -1, delta);
            context.getMatrices().pop();
        }
        renderBackground(context);

        final int columnHeight = 180;
        final int columnWidth = 310;
        final int segmentWidth = 123;

        int top = (height - columnHeight) / 2;
        int left = (width - columnWidth) / 2;

        top += 40;
        final int zOffset = 0;

        context.drawTexture(TribeSelectionScreen.TEXTURE, left + zOffset, top, 0, 70, 123, columnHeight);
        context.drawTexture(TribeSelectionScreen.TEXTURE, left + segmentWidth + zOffset, top, 20, 70, 123, columnHeight);
        context.drawTexture(TribeSelectionScreen.TEXTURE, width - left - segmentWidth + zOffset + 1, top, 10, 70, 123, columnHeight);
        top -= 31;
        left = width / 2;
        context.drawTexture(TribeSelectionScreen.TEXTURE, left - 55, top, 140, 70, 21, 50);
        context.drawTexture(TribeSelectionScreen.TEXTURE, left + 35, top, 148, 70, 21, 50);
        textBody.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();
        context.drawTexture(TribeSelectionScreen.TEXTURE, left - 35, top - 5, 10, 70, 69, 50);
        context.drawTexture(TribeSelectionScreen.TEXTURE, left - 35, top - 15, 10, 70, 69, 50);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            callback.accept(false);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            callback.accept(true);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
