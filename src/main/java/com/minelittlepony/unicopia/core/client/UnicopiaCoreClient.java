package com.minelittlepony.unicopia.core.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.common.event.ClientReadyCallback;
import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.core.Config;
import com.minelittlepony.unicopia.core.IKeyBindingHandler;
import com.minelittlepony.unicopia.core.InteractionManager;
import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.SpeciesList;
import com.minelittlepony.unicopia.core.UnicopiaCore;
import com.minelittlepony.unicopia.core.ability.PowersRegistry;
import com.minelittlepony.unicopia.core.entity.player.IPlayer;
import com.minelittlepony.unicopia.core.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.core.util.MineLPConnector;
import com.minelittlepony.unicopia.core.util.dummy.DummyClientPlayerEntity;
import com.minelittlepony.unicopia.redux.client.render.DisguiseRenderer;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class UnicopiaCoreClient extends InteractionManager implements ClientModInitializer {

    private final IKeyBindingHandler keyboard = new KeyBindingsHandler();

    /**
     * The race preferred by the client - as determined by mine little pony.
     * Human if minelp was not installed.
     *
     * This is not neccessarily the _actual_ race used for the player,
     * as the server may not allow certain race types, or the player may override
     * this option in-game themselves.
     */
    private static Race clientPlayerRace = getclientPlayerRace();

    private static Race getclientPlayerRace() {
        if (!Config.instance().ignoresMineLittlePony()
                && MinecraftClient.getInstance().player != null) {
            Race race = MineLPConnector.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }


        return Config.instance().getPrefferedRace();
    }

    @Override
    @Nonnull
    public PlayerEntity createPlayer(Entity observer, GameProfile profile) {
        if (observer.world instanceof ClientWorld) {
            return new DummyClientPlayerEntity((ClientWorld)observer.world, profile);
        }
        return super.createPlayer(observer, profile);
    }

    @Override
    public boolean isClientPlayer(@Nullable PlayerEntity player) {
        if (MinecraftClient.getInstance().player == player) {
            return true;
        }

        if (MinecraftClient.getInstance().player == null || player == null) {
            return false;
        }

        return IPlayer.equal(MinecraftClient.getInstance().player, player);
    }

    @Override
    public int getViewMode() {
        return MinecraftClient.getInstance().options.perspective;
    }

    public void postRenderEntity(Entity entity) {
        if (entity instanceof PlayerEntity) {
            IPlayer iplayer = SpeciesList.instance().getPlayer((PlayerEntity)entity);

            if (iplayer.getGravity().getGravitationConstant() < 0) {
                GlStateManager.translated(0, entity.getDimensions(entity.getPose()).height, 0);
                GlStateManager.scalef(1, -1, 1);
                entity.prevPitch *= -1;
                entity.pitch *= -1;
            }
        }
    }

    public boolean renderEntity(Entity entity, float renderPartialTicks) {

        if (DisguiseRenderer.getInstance().renderDisguise(entity, renderPartialTicks)) {
            return true;
        }

        if (entity instanceof PlayerEntity) {
            IPlayer iplayer = SpeciesList.instance().getPlayer((PlayerEntity)entity);

            if (iplayer.getGravity().getGravitationConstant() < 0) {
                GlStateManager.scalef(1, -1, 1);
                GlStateManager.translated(0, -entity.getDimensions(entity.getPose()).height, 0);
                entity.prevPitch *= -1;
                entity.pitch *= -1;
            }

            if (DisguiseRenderer.getInstance().renderDisguiseToGui(iplayer)) {
                return true;
            }

            if (iplayer.isInvisible()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInitializeClient() {
        clientPlayerRace = getclientPlayerRace();
        UnicopiaCore.interactionManager = this;

        ClientTickCallback.EVENT.register(this::tick);
        ClientReadyCallback.EVENT.register(client -> {
            PowersRegistry.instance().getValues().forEach(keyboard::addKeybind);
        });
    }

    private void tick(MinecraftClient client) {
        PlayerEntity player = client.player;

        if (player != null && !player.removed) {
            Race newRace = getclientPlayerRace();

            if (newRace != clientPlayerRace) {
                clientPlayerRace = newRace;

                UnicopiaCore.getConnection().send(new MsgRequestCapabilities(player, clientPlayerRace), Target.SERVER);
            }
        }

        keyboard.onKeyInput();

        if (player instanceof ClientPlayerEntity) {
            ClientPlayerEntity sp = (ClientPlayerEntity)player;

            Input movement = sp.input;

            if (!(movement instanceof InversionAwareKeyboardInput)) {
                sp.input = new InversionAwareKeyboardInput(client, movement);
            }
        }

        if (!(client.mouse instanceof MouseControl)) {
            client.mouse = new MouseControl(client);
        }
    }
}
