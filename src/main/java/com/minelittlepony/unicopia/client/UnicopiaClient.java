package com.minelittlepony.unicopia.client;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.event.ScreenInitCallback;
import com.minelittlepony.common.event.ScreenInitCallback.ButtonList;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.gui.LanSettingsScreen;
import com.minelittlepony.unicopia.client.gui.ShapingBenchScreen;
import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.shader.ViewportShader;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.container.*;
import com.minelittlepony.unicopia.entity.player.PlayerCamera;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.handler.ClientNetworkHandlerImpl;
import com.minelittlepony.unicopia.server.world.WeatherConditions;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.minelittlepony.unicopia.util.Lerp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class UnicopiaClient implements ClientModInitializer {

    private static UnicopiaClient instance;

    public static UnicopiaClient getInstance() {
        return instance;
    }

    @Nullable
    private Float originalRainGradient;
    private final Lerp rainGradient = new Lerp(0);

    public final Lerp tangentalSkyAngle = new Lerp(0, true);
    public final Lerp skyAngle = new Lerp(0, true);

    private ZapAppleStageStore.Stage zapAppleStage = ZapAppleStageStore.Stage.HIBERNATING;

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
            Race race = MineLPDelegate.getInstance().getPlayerPonyRace();

            if (race.isEquine()) {
                return race;
            }
        }

        return Unicopia.getConfig().preferredRace.get();
    }

    public static float getWorldBrightness(float initial) {
        return 0.6F;
    }

    public UnicopiaClient() {
        instance = this;
    }

    public void setZapAppleStage(ZapAppleStageStore.Stage stage) {
        zapAppleStage = stage;
    }

    public ZapAppleStageStore.Stage getZapAppleStage() {
        return zapAppleStage;
    }

    public float getSkyAngleDelta(float tickDelta) {
        if (MinecraftClient.getInstance().world == null) {
            return 0;
        }
        float skyAngle = MinecraftClient.getInstance().world.getSkyAngle(tickDelta);
        this.skyAngle.update(skyAngle, 200);
        return this.skyAngle.getValue() - skyAngle;
    }

    @Override
    public void onInitializeClient() {
        InteractionManager.INSTANCE = new ClientInteractionManager();
        new ClientNetworkHandlerImpl();

        KeyBindingsHandler.bootstrap();
        URenderers.bootstrap();

        HandledScreens.register(UScreenHandlers.SPELL_BOOK, SpellbookScreen::new);
        HandledScreens.register(UScreenHandlers.SHAPING_BENCH, ShapingBenchScreen::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientTickEvents.END_WORLD_TICK.register(this::onWorldTick);
        ScreenInitCallback.EVENT.register(this::onScreenInit);
        ItemTooltipCallback.EVENT.register(new ModifierTooltipRenderer());

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(ViewportShader.INSTANCE);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(SpellEffectsRenderDispatcher.INSTANCE);

        Unicopia.SIDE = () -> Optional.ofNullable(MinecraftClient.getInstance().player).map(Pony::of);
    }

    private void onTick(MinecraftClient client) {
        KeyBindingsHandler.INSTANCE.tick(client);
        UHud.INSTANCE.tick();
    }

    private void onWorldTick(ClientWorld world) {
        BlockPos pos = MinecraftClient.getInstance().getCameraEntity().getBlockPos();
        float tickDelta = MinecraftClient.getInstance().getTickDelta();

        Float targetRainGradient = getTargetRainGradient(world, pos, tickDelta);

        if (targetRainGradient != null) {
            rainGradient.update(targetRainGradient, 2000);
        }

        float gradient = rainGradient.getValue();
        if (!rainGradient.isFinished()) {
            world.setRainGradient(gradient);
            world.setThunderGradient(gradient);
        }
    }

    private Float getTargetRainGradient(ClientWorld world, BlockPos pos, float tickDelta) {
        if (WeatherConditions.get(world).isInRangeOfStorm(pos)) {
            if (originalRainGradient == null) {
                originalRainGradient = world.getRainGradient(tickDelta);
            }

            return 1F;
        }

        if (originalRainGradient != null) {
            Float f = originalRainGradient;
            originalRainGradient = null;
            return f;
        }

        return null;
    }

    private void onScreenInit(Screen screen, ButtonList buttons) {
        if (screen instanceof OpenToLanScreen) {
            buttons.addButton(new Button(screen.width / 2 - 155, 130, 150, 20))
                    .onClick(b -> MinecraftClient.getInstance().setScreen(new LanSettingsScreen(screen)))
                    .getStyle().setText(Text.translatable("unicopia.options.title"));
        }
    }

}
