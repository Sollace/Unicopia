package com.minelittlepony.unicopia.client;

import com.minelittlepony.common.client.gui.element.Cycler;
import com.minelittlepony.common.event.ScreenInitCallback;
import com.minelittlepony.common.event.ScreenInitCallback.ButtonList;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class UnicopiaClient implements ClientModInitializer {

    public static Race getPreferredRace() {
        if (!Unicopia.getConfig().ignoresMineLittlePony()
                && MinecraftClient.getInstance().player != null) {
            Race race = MineLPConnector.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }

        return Unicopia.getConfig().getPrefferedRace();
    }

    public static float getWorldBrightness(float initial) {
        return Math.min(1, initial + 0.6F);
    }

    @Override
    public void onInitializeClient() {
        InteractionManager.INSTANCE = new ClientInteractionManager();

        KeyBindingsHandler.bootstrap();
        URenderers.bootstrap();

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ScreenInitCallback.EVENT.register(this::onScreenInit);
    }

    private void onTick(MinecraftClient client) {
        KeyBindingsHandler.INSTANCE.tick(client);
    }

    private void onScreenInit(Screen screen, ButtonList buttons) {
        if (screen instanceof CreateWorldScreen) {
            buttons.add(new Cycler(screen.width / 2 + 110, 60, 20, 20) {
                @Override
                protected void renderForground(MatrixStack matrices, MinecraftClient mc, int mouseX, int mouseY, int foreColor) {
                    super.renderForground(matrices, mc, mouseX, mouseY, foreColor);
                    if (isMouseOver(mouseX, mouseY)) {
                        renderToolTip(matrices, screen, mouseX, mouseY);
                    }
                }
            }).setStyles(
                    Race.EARTH.getStyle(),
                    Race.UNICORN.getStyle(),
                    Race.PEGASUS.getStyle(),
                    Race.BAT.getStyle(),
                    Race.ALICORN.getStyle(),
                    Race.CHANGELING.getStyle()
            ).onChange(i -> {
                Unicopia.getConfig().setPreferredRace(Race.fromId(i + 1));

                return i;
            }).setValue(MathHelper.clamp(Unicopia.getConfig().getPrefferedRace().ordinal() - 1, 0, 5));
        }
    }
}
