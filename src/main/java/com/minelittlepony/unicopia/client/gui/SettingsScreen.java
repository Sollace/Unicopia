package com.minelittlepony.unicopia.client.gui;

import java.util.Set;

import javax.annotation.Nullable;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Cycler;
import com.minelittlepony.common.client.gui.element.EnumSlider;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Toggle;
import com.minelittlepony.common.client.gui.packing.GridPacker;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.common.client.gui.style.Style;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.WorldTribeManager;
import com.minelittlepony.unicopia.client.MineLPConnector;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class SettingsScreen extends GameGui {

    private static final GridPacker WHITELIST_GRID_PACKER = new GridPacker()
            .setItemSpacing(25)
            .setListWidth(200)
            .setItemWidth(30)
            .setItemHeight(0);

    private final Config config = Unicopia.getConfig();

    private final ScrollContainer content = new ScrollContainer();

    @Nullable
    private Style mineLpStatus;

    private boolean forceShowWhitelist;
    private boolean forceHideWhitelist;

    public SettingsScreen(Screen parent) {
        super(new TranslatableText("unicopia.options.title"), parent);

        content.margin.setVertical(30);
        content.padding.setHorizontal(10);
        content.padding.top = 10;
        content.padding.bottom = 20;
    }

    @Override
    public void init() {
        content.init(this::rebuildContent);
    }

    private void rebuildContent() {

        int LEFT = content.width / 2 - 210;
        int RIGHT = content.width / 2 + 10;

        IntegratedServer server = client.getServer();

        // In game
        //      Singleplayer server != null && !server.isRemote();
        //      Lan (hosting) server != null && server.isRemote();
        //      Multiplayer server == null
        // Out of game
        //      server == null

        final boolean showLanOptions = server == null || server.isRemote();

        final boolean canEditWhitelist = client.world == null || server != null && server.isRemote();

        boolean singleColumn = LEFT < 0 || !showLanOptions;

        if (singleColumn) {
            LEFT = content.width / 2 - 100;
            RIGHT = LEFT;
        }

        children().add(content);

        int row = 0;

        addButton(new Label(width / 2, 5).setCentered()).getStyle().setText(getTitle().getString());
        addButton(new Button(width / 2 - 100, height - 25))
            .onClick(sender -> finish())
            .getStyle()
                .setText("gui.done");

        content.addButton(new Label(LEFT, row)).getStyle().setText("unicopia.options.client");

        content.addButton(new Toggle(LEFT, row += 20, config.ignoreMineLP.get()))
                .onChange(v -> {
                    config.ignoreMineLP.set(v);
                    if (mineLpStatus != null) {
                        mineLpStatus.setText(getMineLPStatus());
                    }
                    return v;
                })
                .getStyle().setText("unicopia.options.ignore_mine_lp");

        mineLpStatus = content.addButton(new Label(LEFT, row += 10)).getStyle().setText(getMineLPStatus());

        content.addButton(new EnumSlider<>(LEFT, row += 25, config.preferredRace.get()))
                .onChange(config.preferredRace::set)
                .setFormatter(v -> new TranslatableText("unicopia.options.preferred_race", v.getDisplayName()).getString());

        if (server != null) {
            row += 20;
            content.addButton(new Label(LEFT, row)).getStyle().setText("unicopia.options.world");

            WorldTribeManager tribes = WorldTribeManager.forWorld((ServerWorld)server.getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid()).world);

            content.addButton(new EnumSlider<>(LEFT, row += 20, tribes.getDefaultRace()))
                    .onChange(tribes::setDefaultRace)
                    .setFormatter(v -> new TranslatableText("unicopia.options.world.default_race", v.getDisplayName()).getString())
                    .setEnabled(client.isInSingleplayer());
        }

        if (showLanOptions) {
            if (RIGHT != LEFT) {
                row = 0;
            } else {
                row += 20;
            }

            content.addButton(new Label(RIGHT, row)).getStyle().setText("unicopia.options.lan");

            Set<Race> whitelist = config.speciesWhiteList.get();
            boolean whitelistEnabled = (forceShowWhitelist || !whitelist.isEmpty()) && !forceHideWhitelist;

            content.addButton(new Toggle(RIGHT, row += 20, whitelistEnabled))
                .onChange(v -> {
                    forceShowWhitelist = v;
                    forceHideWhitelist = !v;
                    init();
                    return v;
                })
                .setEnabled(canEditWhitelist)
                .getStyle().setText("unicopia.options.whitelist");

            if (whitelistEnabled) {

                content.addButton(new Label(RIGHT, row += 20)).getStyle().setText(new TranslatableText("unicopia.options.whitelist.details").formatted(Formatting.DARK_GREEN));

                row += 20;
                WHITELIST_GRID_PACKER.start();

                for (Race race : Race.values()) {
                    if (!race.isDefault()) {
                        Bounds bound = WHITELIST_GRID_PACKER.next();

                        Button button = content.addButton(new Toggle(RIGHT + bound.left + 10, row + bound.top, whitelist.contains(race)))
                            .onChange(v -> {
                                if (v) {
                                    whitelist.add(race);
                                } else {
                                    whitelist.remove(race);
                                }
                                return v;
                            })
                            .setEnabled(canEditWhitelist)
                            .setStyle(race.getStyle());

                        ((TextureSprite)button.getStyle().getIcon()).setPosition(-20, 0);
                    }
                }
            }
        }
    }

    private Text getMineLPStatus() {
        final boolean hasMineLP = FabricLoader.getInstance().isModLoaded("minelp");

        if (hasMineLP) {
            if (config.ignoreMineLP.get()) {
                return new TranslatableText("unicopia.options.ignore_mine_lp.undetected").formatted(Formatting.DARK_GREEN);
            }

            return new TranslatableText("unicopia.options.ignore_mine_lp.detected", MineLPConnector.getPlayerPonyRace().getDisplayName()).formatted(Formatting.GREEN);
        }

        return new TranslatableText("unicopia.options.ignore_mine_lp.missing").formatted(Formatting.RED);
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

    public static Cycler createRaceSelector(Screen screen) {
        return new Cycler(screen.width / 2 + 110, 60, 20, 20) {
            @Override
            protected void renderForground(MatrixStack matrices, MinecraftClient mc, int mouseX, int mouseY, int foreColor) {
                super.renderForground(matrices, mc, mouseX, mouseY, foreColor);
                if (isMouseOver(mouseX, mouseY)) {
                    renderToolTip(matrices, screen, mouseX, mouseY);
                }
            }
        }.setStyles(
                Race.EARTH.getStyle(),
                Race.UNICORN.getStyle(),
                Race.PEGASUS.getStyle(),
                Race.BAT.getStyle(),
                Race.ALICORN.getStyle(),
                Race.CHANGELING.getStyle()
        ).onChange(i -> {
            Unicopia.getConfig().preferredRace.set(Race.fromId(i + 1));
            Unicopia.getConfig().save();

            return i;
        }).setValue(MathHelper.clamp(Unicopia.getConfig().preferredRace.get().ordinal() - 1, 0, 5));
    }
}
