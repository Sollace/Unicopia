package com.minelittlepony.unicopia.client.gui;

import java.util.Set;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Toggle;
import com.minelittlepony.common.client.gui.packing.GridPacker;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.common.client.gui.style.Style;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
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
        super(Text.translatable("unicopia.options.title"), parent);

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

        content.addButton(new Toggle(LEFT, row += 20, config.enableCheats.get()))
            .onChange(v -> {
                config.enableCheats.set(v);
                return v;
            })
            .getStyle().setText("unicopia.options.cheats");

        Set<String> whitelist = config.speciesWhiteList.get();
        boolean whitelistEnabled = (forceShowWhitelist || !whitelist.isEmpty()) && !forceHideWhitelist;

        if (whitelist.isEmpty() && forceShowWhitelist) {
            for (Race r : Race.REGISTRY) {
                if (!r.isUnset()) {
                    whitelist.add(r.getId().toString());
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

            content.addButton(new Label(LEFT, row += 20)).getStyle().setText(Text.translatable("unicopia.options.whitelist.details").formatted(Formatting.DARK_GREEN));

            row += 20;
            WHITELIST_GRID_PACKER.start();

            for (Race race : Race.REGISTRY) {
                if (!race.isUnset() && race.availability().isGrantable()) {
                    Bounds bound = WHITELIST_GRID_PACKER.next();

                    Button button = content.addButton(new Toggle(LEFT + bound.left + 10, row + bound.top, whitelist.contains(race.getId().toString())))
                        .onChange(v -> {
                            if (v) {
                                whitelist.add(race.getId().toString());
                            } else {
                                whitelist.remove(race.getId().toString());
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
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);
        content.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public void removed() {
        if (forceHideWhitelist) {
            config.speciesWhiteList.get().clear();
        }
        config.save();
    }

    public static Style createStyle(Race race) {
        return new Style().setIcon(TribeButton.createSprite(race, 2, 2, 15))
                .setTooltip(race.getTranslationKey(), 0, 10);
    }
}
