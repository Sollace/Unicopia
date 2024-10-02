package com.minelittlepony.unicopia.client;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.event.ScreenInitCallback;
import com.minelittlepony.common.event.ScreenInitCallback.ButtonList;
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
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class UnicopiaClient implements ClientModInitializer {

    private static UnicopiaClient instance;

    public static UnicopiaClient getInstance() {
        return instance;
    }

    @Nullable
    public static Pony getClientPony() {
        return Pony.of(MinecraftClient.getInstance().player);
    }

    public final Lerp tangentalSkyAngle = new Lerp(0, true);
    public final Lerp skyAngle = new Lerp(0, true);

    private ZapAppleStageStore.Stage zapAppleStage = ZapAppleStageStore.Stage.HIBERNATING;

    public static Optional<PlayerCamera> getCamera() {
        return Optional.ofNullable(getNullableCamera());
    }

    @Nullable
    private static PlayerCamera getNullableCamera() {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && MinecraftClient.getInstance().cameraEntity == player) {
            return Pony.of(player).getCamera();
        }

        return null;
    }


    public static Vec3d getAdjustedSoundPosition(Vec3d pos) {
        PlayerCamera cam = getNullableCamera();
        if (cam == null) {
            return pos;
        }
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        Vector3f rotated = pos.subtract(camera.getPos()).toVector3f();
        rotated = rotated.rotateAxis(cam.calculateRoll() * MathHelper.RADIANS_PER_DEGREE, 0, 1, 0);

        return new Vec3d(rotated).add(camera.getPos());
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
        new ClientInteractionManager();
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
    }

    private void onTick(MinecraftClient client) {
        KeyBindingsHandler.INSTANCE.tick(client);
        UHud.INSTANCE.tick();
    }

    private void onWorldTick(ClientWorld world) {
        /*BlockPos pos = MinecraftClient.getInstance().getCameraEntity().getBlockPos();
        float tickDelta = MinecraftClient.getInstance().getTickDelta();

        Float targetRainGradient = ((WeatherAccess)world).isInRangeOfStorm(pos) ? (Float)1F : ((WeatherAccess)world).isBelowCloudLayer(pos) ? null : (Float)0F;
        Float targetThunderGradient = ((WeatherAccess)world).isInRangeOfStorm(pos) ? (Float)1F : null;

        ((WeatherAccess)world).setWeatherOverride(null, null);
        rainGradient.update(targetRainGradient == null ? world.getRainGradient(tickDelta) : targetRainGradient, 2000);

        ((WeatherAccess)world).setWeatherOverride(1F, null);
        thunderGradient.update(targetThunderGradient == null ? world.getThunderGradient(tickDelta) : targetThunderGradient, 2000);

        ((WeatherAccess)world).setWeatherOverride(
                rainGradient.isFinished() ? targetRainGradient : (Float)rainGradient.getValue(),
                thunderGradient.isFinished() ? targetThunderGradient : (Float)thunderGradient.getValue()
        );*/
    }

    private void onScreenInit(Screen screen, ButtonList buttons) {
        if (screen instanceof OpenToLanScreen) {
            buttons.addButton(new Button(screen.width / 2 - 155, 130, 150, 20))
                    .onClick(b -> MinecraftClient.getInstance().setScreen(new LanSettingsScreen(screen)))
                    .getStyle().setText(Text.translatable("unicopia.options.title"));
        }
    }

}
