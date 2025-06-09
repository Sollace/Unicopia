package com.minelittlepony.unicopia.client.gui;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.*;
import com.minelittlepony.common.client.gui.style.Style;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.minelittlepony.unicopia.util.RegistryIndexer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class SettingsScreen extends GameGui {
    private final Config config = Unicopia.getConfig();

    private final ScrollContainer content = new ScrollContainer();

    @Nullable
    private Style mineLpStatus;

    private long drawingHudOutline;

    public SettingsScreen(Screen parent) {
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

        row += 10;

        mineLpStatus = content.addButton(new Label(LEFT, row += 10)).getStyle().setText(getMineLPStatus());

        content.addButton(new Toggle(LEFT, row += 20, config.toggleAbilityKeys.get()))
            .onChange(config.toggleAbilityKeys)
            .getStyle().setText("unicopia.options.toggle_ability_keys");

        content.addButton(new EnumSlider<>(LEFT, row += 20, config.hudPosition))
            .onChange(v -> {
                drawingHudOutline = System.currentTimeMillis() + 1000L;
                return config.hudPosition.set(v);
            })
            .setTextFormat(v -> Text.translatable("unicopia.options.hud_position", v.getValue().name()));

        RegistryIndexer<Race> races = RegistryIndexer.of(Race.REGISTRY);

        content.addButton(new Slider(LEFT, row += 25, 0, races.size(), races.indexOf(config.preferredRace.get())))
                .onChange(races.createSetter(config.preferredRace))
                .setTextFormat(v -> Text.translatable("unicopia.options.preferred_race", races.valueOf(v.getValue()).getDisplayName()));

        IntegratedServer server = client.getServer();
        if (server != null) {
            row += 20;
            content.addButton(new Label(LEFT, row)).getStyle().setText("unicopia.options.world");

            UnicopiaWorldProperties tribes = UnicopiaWorldProperties.forWorld((ServerWorld)server.getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid()).getWorld());

            content.addButton(new Slider(LEFT, row += 20, 0, races.size(), races.indexOf(tribes.getDefaultRace())))
                    .onChange(races.createSetter(tribes::setDefaultRace))
                    .setTextFormat(v -> Text.translatable("unicopia.options.world.default_race", races.valueOf(v.getValue()).getDisplayName()))
                    .setEnabled(client.isInSingleplayer());
        }
    }

    private Text getMineLPStatus() {
        final boolean hasMineLP = FabricLoader.getInstance().isModLoaded("minelp");

        if (hasMineLP) {
            if (config.ignoreMineLP.get()) {
                return Text.translatable("unicopia.options.ignore_mine_lp.undetected").formatted(Formatting.DARK_GREEN);
            }

            return Text.translatable("unicopia.options.ignore_mine_lp.detected", MineLPDelegate.getInstance().getPlayerPonyRace().getDisplayName()).formatted(Formatting.GREEN);
        }

        return Text.translatable("unicopia.options.ignore_mine_lp.missing").formatted(Formatting.RED);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);
        content.render(context, mouseX, mouseY, tickDelta);
        if (drawingHudOutline > System.currentTimeMillis()) {

            int scaledWidth = client.getWindow().getScaledWidth();
            int scaledHeight = client.getWindow().getScaledHeight();

            HudPosition selectedPos = config.hudPosition.get();

            for (var hudPos : HudPosition.values()) {
                HudPosition.Alignment armAlignment = client.options.getMainArm().getValue() == Arm.LEFT ? HudPosition.Alignment.START : HudPosition.Alignment.END;
                if (hudPos == HudPosition.OFF_HAND) {
                    armAlignment = armAlignment.opposite();
                }

                int left = hudPos.getHorizontal().pick(2, scaledWidth - 50, ((scaledWidth - 50) / 2) + (109 * armAlignment.getSignum()));
                int top = hudPos.getVertical().pick(12, scaledHeight - 50, scaledHeight - 50);
                if (hudPos == HudPosition.BOTTOM_CENTER) {
                    top -= 22;
                }

                context.getMatrices().push();
                context.getMatrices().translate(left + UHud.PRIMARY_SLOT_SIZE / 2, top + UHud.PRIMARY_SLOT_SIZE / 2, 0);
                DrawableUtil.drawArc(context.getMatrices(), 0, UHud.PRIMARY_SLOT_SIZE / 2, 0, MathHelper.TAU, hudPos == selectedPos ? 0xEEFF00A5 : 0xEEFFFF25);
                context.getMatrices().pop();
            }
        }
    }

    @Override
    public void removed() {
        config.save();
    }
}
