package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRequestSpeciesChange;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TribeSelectionScreen extends GameGui implements HidesHud {
    static final Identifier TEXTURE = Unicopia.id("textures/gui/tribe_selection.png");

    final Set<Race> allowedRaces;

    final Text journeyText;
    final Text choiceText;

    private boolean finished;

    private final List<TribeButton> options = new ArrayList<>();
    private static int SELECTION = -1;

    private int prevScrollPosition;
    private int scrollPosition;
    private int targetScroll;

    public TribeSelectionScreen(Set<Race> allowedRaces, String baseString) {
        super(Text.translatable(baseString));
        this.allowedRaces = allowedRaces;
        this.journeyText = Text.translatable(baseString + ".journey");
        this.choiceText = Text.translatable(baseString + ".choice");
    }

    @Override
    protected void init() {
        final int pageWidth = 300;
        final int left = (width - pageWidth) / 2;

        int top = 0;

        addDrawable(new Label(width / 2, top += 20).setCentered()).getStyle().setText(getTitle().copy().formatted(Formatting.YELLOW));

        top += height / 8;

        TextBlock block = addDrawable(new TextBlock(left, top += 10, pageWidth));
        block.getStyle().setText(journeyText);
        top += block.getBounds().height;

        block = addDrawable(new TextBlock(left, top += 7, pageWidth));
        block.getStyle().setText(choiceText);
        top += block.getBounds().height;
        top += 30;

        List<Race> options = Race.REGISTRY.stream().filter(race -> race.availability().isSelectable()).toList();
        this.options.clear();

        for (Race race : options) {
            addOption(race, top);
        }

        scroll(SELECTION == -1 ? options.size() / 2 : SELECTION, false);
    }

    private void addOption(Race race, int y) {
        var option = new TribeButton(0, y, width, race);
        int index = options.size();
        options.add(addDrawableChild(option));
        option.onClick(b -> {
            finished = true;
            scroll(index, false);
            client.setScreen(new TribeConfirmationScreen(result -> {
                finished = false;

                if (result) {
                    Channel.CLIENT_REQUEST_SPECIES_CHANGE.sendToServer(new MsgRequestSpeciesChange(true, race));
                    finish();
                } else {
                    client.setScreen(this);
                }
            }, race));
        }).setEnabled(allowedRaces.contains(race));
    }

    @Override
    public void tick() {
        prevScrollPosition = scrollPosition;
        if (scrollPosition < targetScroll) {
            scrollPosition++;
        }
        if (scrollPosition > targetScroll) {
            scrollPosition--;
        }
    }

    private void updateScolling() {
        final int itemWidth = 70 + 10;
        int x = (width - itemWidth) / 2;
        float diff = MathHelper.lerp(client.getTickDelta(), prevScrollPosition, scrollPosition) / 4F;

        for (int i = 0; i < options.size(); i++) {
            var option = options.get(i);
            option.setX((int)(x + (i - diff) * itemWidth));
            option.setFocused(i == SELECTION);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateScolling();
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        if (!options.isEmpty()) {
            var element = options.get(0);

            float diff = (targetScroll - MathHelper.lerp(client.getTickDelta(), prevScrollPosition, scrollPosition)) * 7;
            context.drawTexture(TEXTURE, (width / 2) + 40 + (scrollPosition < targetScroll ? (int)diff : 0), element.getY() - 20, 10, 165, 153, 30, 85, 312, 312);
            context.drawTexture(TEXTURE, (width / 2) - 80 + (scrollPosition > targetScroll ? (int)diff : 0), element.getY() - 20, 10, 195, 153, 30, 85, 312, 312);
            if (element.getBounds().left < 0) {
                context.drawTexture(TEXTURE, 20, element.getY() - 10, 10, 188, 235, 24, 60, 312, 312);
            }
            element = options.get(options.size() - 1);
            if (element.getBounds().right() > width) {
                context.drawTexture(TEXTURE, width - 50, element.getY() - 10, 10, 164, 235, 24, 60, 312, 312);
            }
        }
    }

    @Override
    public void finish() {
        finished = true;
        close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            scroll(Math.max(SELECTION - 1, 0), true);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            scroll(Math.min(SELECTION + 1, options.size() - 1), true);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (options.get(SELECTION).active) {
                options.get(SELECTION).onPress();
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void scroll(int target, boolean animate) {
        SELECTION = target;
        target *= 4;
        if (targetScroll == target) {
            return;
        }
        targetScroll = target;
        if (!animate) {
            scrollPosition = targetScroll;
            prevScrollPosition = scrollPosition;
        } else {
            playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void removed() {
        if (!finished && client != null) {
            client.execute(() -> {
                finished = true;
                client.setScreen(this);
                finished = false;
            });
        }
    }
}
