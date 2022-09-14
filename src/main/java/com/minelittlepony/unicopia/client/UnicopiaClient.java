package com.minelittlepony.unicopia.client;

import java.util.Optional;

import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.event.ScreenInitCallback;
import com.minelittlepony.common.event.ScreenInitCallback.ButtonList;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.gui.LanSettingsScreen;
import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPConnector;
import com.minelittlepony.unicopia.container.*;
import com.minelittlepony.unicopia.entity.player.PlayerCamera;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;

public class UnicopiaClient implements ClientModInitializer {

    public static Optional<PlayerCamera> getCamera() {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && MinecraftClient.getInstance().cameraEntity == player) {
            return Optional.of(Pony.of(player).getCamera());
        }

        return Optional.empty();
    }

    public static Race getPreferredRace() {
        if (!Unicopia.getConfig().ignoreMineLP.get()
                && MinecraftClient.getInstance().player != null) {
            Race race = MineLPConnector.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }

        return Unicopia.getConfig().preferredRace.get();
    }

    public static float getWorldBrightness(float initial) {
        return 0.6F;
    }

    @Override
    public void onInitializeClient() {
        InteractionManager.INSTANCE = new ClientInteractionManager();

        KeyBindingsHandler.bootstrap();
        URenderers.bootstrap();

        ScreenRegistry.register(UScreenHandlers.SPELL_BOOK, SpellbookScreen::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ScreenInitCallback.EVENT.register(this::onScreenInit);
        ItemTooltipCallback.EVENT.register(new ModifierTooltipRenderer());

        Unicopia.SIDE = () -> Optional.ofNullable(MinecraftClient.getInstance().player).map(Pony::of);
    }

    private void onTick(MinecraftClient client) {
        KeyBindingsHandler.INSTANCE.tick(client);
        UHud.INSTANCE.tick();
    }

    private void onScreenInit(Screen screen, ButtonList buttons) {
        if (screen instanceof CreateWorldScreen) {
            buttons.addButton(LanSettingsScreen.createRaceSelector(screen));
        }
        if (screen instanceof OpenToLanScreen) {
            buttons.addButton(new Button(screen.width / 2 - 155, 130, 150, 20))
                    .onClick(b -> MinecraftClient.getInstance().setScreen(new LanSettingsScreen(screen)))
                    .getStyle().setText(new TranslatableText("unicopia.options.title"));
        }
    }

}
