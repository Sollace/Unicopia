package com.minelittlepony.unicopia.client.gui;

import java.util.Set;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Cycler;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Toggle;
import com.minelittlepony.common.client.gui.packing.GridPacker;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.common.client.gui.style.Style;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.RegistryIndexer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class LanSettingsScreen extends GameGui {

    private static final GridPacker WHITELIST_GRID_PACKER = new GridPacker()
            .setItemSpacing(25)
            .setListWidth(200)
            .setItemWidth(30)
            .setItemHeight(0);

    private final Config config = Unicopia.getConfig();

    private final ScrollContainer content = new ScrollContainer();

    private boolean forceShowWhitelist;
    private boolean forceHideWhitelist;

    public LanSettingsScreen(Screen parent) {
        super(new TranslatableText("unicopia.options.title"), parent);

        content.margin.setVertical(30);
        content.getContentPadding().setHorizontal(10);
        content.getContentPadding().top = 10;
        content.getContentPadding().bottom = 20;
    }

    @Override
    public void init() {
        content.init(this::rebuildContent);
    }

    private void rebuildContent() {

        int LEFT = content.width / 2 - 100;

        IntegratedServer server = client.getServer();

        // In game
        //      Singleplayer server != null && !server.isRemote();
        //      Lan (hosting) server != null && server.isRemote();
        //      Multiplayer server == null
        // Out of game
        //      server == null

        final boolean canEditWhitelist = client.world == null || server != null;

        getChildElements().add(content);

        addButton(new Label(width / 2, 5).setCentered()).getStyle().setText(getTitle().getString());
        addButton(new Button(width / 2 - 100, height - 25))
            .onClick(sender -> finish())
            .getStyle()
                .setText("gui.done");

        int row = 0;

        content.addButton(new Label(LEFT, row += 20)).getStyle().setText("unicopia.options.lan");

        Set<Race> whitelist = config.speciesWhiteList.get();
        boolean whitelistEnabled = (forceShowWhitelist || !whitelist.isEmpty()) && !forceHideWhitelist;

        if (whitelist.isEmpty() && forceShowWhitelist) {
            for (Race r : Race.REGISTRY) {
                if (!r.isDefault()) {
                    whitelist.add(r);
                }
            }
        }

        content.addButton(new Toggle(LEFT, row += 20, whitelistEnabled))
            .onChange(v -> {
                forceShowWhitelist = v;
                forceHideWhitelist = !v;
                init();
                return v;
            })
            .setEnabled(canEditWhitelist)
            .getStyle().setText("unicopia.options.whitelist");

        if (whitelistEnabled) {

            content.addButton(new Label(LEFT, row += 20)).getStyle().setText(new TranslatableText("unicopia.options.whitelist.details").formatted(Formatting.DARK_GREEN));

            row += 20;
            WHITELIST_GRID_PACKER.start();

            for (Race race : Race.REGISTRY) {
                if (!race.isDefault()) {
                    Bounds bound = WHITELIST_GRID_PACKER.next();

                    Button button = content.addButton(new Toggle(LEFT + bound.left + 10, row + bound.top, whitelist.contains(race)))
                        .onChange(v -> {
                            if (v) {
                                whitelist.add(race);
                            } else {
                                whitelist.remove(race);
                            }
                            return v;
                        })
                        .setEnabled(canEditWhitelist)
                        .setStyle(createStyle(race));

                    ((TextureSprite)button.getStyle().getIcon()).setPosition(-20, 0);
                }
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, tickDelta);
        content.render(matrices, mouseX, mouseY, tickDelta);
    }

    @Override
    public void removed() {
        if (forceHideWhitelist) {
            config.speciesWhiteList.get().clear();
        }
        config.save();
    }

    public static Style createStyle(Race race) {
        int ordinal = Race.REGISTRY.getRawId(race);
        return new Style()
                .setIcon(new TextureSprite()
                        .setPosition(2, 2)
                        .setSize(16, 16)
                        .setTexture(TribeSelectionScreen.ICONS)
                        .setTextureOffset((16 * ordinal) % 256, (ordinal / 256) * 16)
                )
                .setTooltip(race.getTranslationKey(), 0, 10);
    }

    public static Cycler createRaceSelector(Screen screen) {
        RegistryIndexer<Race> races = RegistryIndexer.of(Race.REGISTRY);
        return new Cycler(screen.width / 2 + 110, 60, 20, 20) {
            @Override
            protected void renderForground(MatrixStack matrices, MinecraftClient mc, int mouseX, int mouseY, int foreColor) {
                super.renderForground(matrices, mc, mouseX, mouseY, foreColor);
                if (isMouseOver(mouseX, mouseY)) {
                    renderToolTip(matrices, screen, mouseX, mouseY);
                }
            }
        }.setStyles(Race.REGISTRY.stream().map(LanSettingsScreen::createStyle).toArray(Style[]::new)).onChange(i -> {
            Unicopia.getConfig().preferredRace.set(races.valueOf(i));
            Unicopia.getConfig().save();

            return i;
        }).setValue(races.indexOf(Unicopia.getConfig().preferredRace.get()));
    }
}
