package com.minelittlepony.unicopia.client.gui;

import java.util.List;
import java.util.Set;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRequestSpeciesChange;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class TribeSelectionScreen extends GameGui implements HidesHud {
    static final Identifier TEXTURE = Unicopia.id("textures/gui/tribe_selection.png");

    final Set<Race> allowedRaces;

    final Text journeyText;
    final Text choiceText;

    private boolean finished;

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

        final int itemWidth = 70 + 10;

        List<Race> options = Race.REGISTRY.stream().filter(race -> race.availability().isSelectable()).toList();

        int columns = Math.min(width / itemWidth, options.size());

        int x = (width - (columns * itemWidth)) / 2;
        int y = top;

        int column = 0;
        int row = 0;

        for (Race race : options) {
            addOption(race, x + (column * itemWidth), y + (row * itemWidth));
            column++;
            if (column >= columns) {
                column = 0;
                row++;
            }
        }

        top = height - 20;
    }

    private void addOption(Race race, int x, int y) {
        addDrawableChild(new TribeButton(x, y, width, race)).onClick(b -> {
            finished = true;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void finish() {
        finished = true;
        close();
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
