package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Cycler;
import com.minelittlepony.common.client.gui.element.EnumSlider;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Toggle;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.WorldTribeManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class SettingsScreen extends GameGui {

    private final Config config = Unicopia.getConfig();

    private final ScrollContainer content = new ScrollContainer();

    public SettingsScreen(Screen parent) {
        super(new TranslatableText("unicopia.options.title"), parent);

        content.margin.top = 30;
        content.margin.bottom = 30;
        content.padding.top = 10;
        content.padding.right = 10;
        content.padding.bottom = 20;
        content.padding.left = 10;
    }

    @Override
    public void init() {
        content.init(this::rebuildContent);
    }

    private void rebuildContent() {

        int LEFT = content.width / 2 - 210;
        int RIGHT = content.width / 2 + 10;

        IntegratedServer server = client.getServer();

        boolean canOpenLan = client.isIntegratedServerRunning() && !client.getServer().isRemote();
        boolean singleColumn = server == null || LEFT < 0 || canOpenLan;

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
                .onChange(config.ignoreMineLP::set)
                .getStyle().setText("unicopia.options.ignore_mine_lp");

        content.addButton(new EnumSlider<>(LEFT, row += 20, config.preferredRace.get()))
                .onChange(config.preferredRace::set)
                .setFormatter(v -> I18n.translate("unicopia.options.preferred_race", v));

        if (server != null) {
            row += 20;
            content.addButton(new Label(LEFT, row)).getStyle().setText("unicopia.options.world");

            WorldTribeManager tribes = WorldTribeManager.forWorld((ServerWorld)server.getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid()).world);

            content.addButton(new EnumSlider<>(LEFT, row += 20, tribes.getDefaultRace()))
                    .onChange(tribes::setDefaultRace)
                    .setFormatter(v -> I18n.translate("unicopia.options.world.default_race", v))
                    .setEnabled(client.isInSingleplayer());

            if (RIGHT != LEFT) {
                row = 0;
            } else {
                row += 20;
            }

            content.addButton(new Label(RIGHT, row)).getStyle().setText("unicopia.options.lan");
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
