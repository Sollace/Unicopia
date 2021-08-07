package com.minelittlepony.unicopia.client.gui;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.EnumSlider;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Toggle;
import com.minelittlepony.common.client.gui.style.Style;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.WorldTribeManager;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPConnector;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class SettingsScreen extends GameGui {
    private final Config config = Unicopia.getConfig();

    private final ScrollContainer content = new ScrollContainer();

    @Nullable
    private Style mineLpStatus;

    public SettingsScreen(Screen parent) {
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

        getChildElements().add(content);

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
                .setTextFormat(v -> new TranslatableText("unicopia.options.preferred_race", v.getValue().getDisplayName()));

        IntegratedServer server = client.getServer();
        if (server != null) {
            row += 20;
            content.addButton(new Label(LEFT, row)).getStyle().setText("unicopia.options.world");

            WorldTribeManager tribes = WorldTribeManager.forWorld((ServerWorld)server.getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid()).world);

            content.addButton(new EnumSlider<>(LEFT, row += 20, tribes.getDefaultRace()))
                    .onChange(tribes::setDefaultRace)
                    .setTextFormat(v -> new TranslatableText("unicopia.options.world.default_race", v.getValue().getDisplayName()))
                    .setEnabled(client.isInSingleplayer());
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
        config.save();
    }
}
