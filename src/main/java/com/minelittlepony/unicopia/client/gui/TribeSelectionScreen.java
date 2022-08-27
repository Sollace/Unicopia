package com.minelittlepony.unicopia.client.gui;

import java.util.List;
import java.util.Set;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRequestSpeciesChange;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class TribeSelectionScreen extends GameGui implements HidesHud {
    static final Identifier ICONS = new Identifier("unicopia", "textures/gui/icons.png");
    static final Identifier TEXTURE = new Identifier("unicopia", "textures/gui/tribe_selection.png");

    final Set<Race> allowedRaces;

    private boolean finished;

    public TribeSelectionScreen(Set<Race> allowedRaces) {
        super(Text.translatable("gui.unicopia.tribe_selection"));
        this.allowedRaces = allowedRaces;
    }

    @Override
    protected void init() {
        final int pageWidth = 300;
        final int left = (width - pageWidth) / 2;

        int top = 0;

        addDrawable(new Label(width / 2, top += 20).setCentered()).getStyle().setText(getTitle().copy().formatted(Formatting.YELLOW));

        top += height / 8;

        TextBlock block = addDrawable(new TextBlock(left, top += 10, pageWidth));
        block.getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.welcome.journey"));
        top += block.getBounds().height;

        block = addDrawable(new TextBlock(left, top += 7, pageWidth));
        block.getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.welcome.choice"));
        top += block.getBounds().height;

        Race preference = UnicopiaClient.getPreferredRace();

        top += 30;

        final int itemWidth = 70;

        List<Race> options = Race.REGISTRY.stream().filter(race -> !race.isDefault() && !race.isOp()).toList();

        int totalWidth = options.size() * (itemWidth + 10) - 10;

        int x = (width - totalWidth) / 2;

        for (Race race : options) {
            addOption(race, x, top);
            x += itemWidth + 10;
        }

        top = height - 20;

        if (!preference.isDefault()) {
            addDrawable(new Label(width / 2, top).setCentered()).getStyle().setText(Text.translatable("gui.unicopia.tribe_selection.preference", preference.getDisplayName().copy().formatted(Formatting.YELLOW)));
        }
    }

    private void addOption(Race race, int x, int top) {
        addDrawableChild(new TribeButton(x, top, width, race)).onClick(b -> {
            finished = true;
            client.setScreen(new TribeConfirmationScreen(result -> {
                finished = false;

                if (result) {
                    Channel.CLIENT_REQUEST_SPECIES_CHANGE.send(new MsgRequestSpeciesChange(race, true));
                    finish();
                } else {
                    client.setScreen(this);
                }
            }, race));
        }).setEnabled(allowedRaces.contains(race));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
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
