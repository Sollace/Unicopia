package com.minelittlepony.unicopia.client;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.MineLP;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.UEntities;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.ability.render.DisguiseRenderer;
import com.minelittlepony.unicopia.client.gui.SettingsScreen;
import com.minelittlepony.unicopia.client.input.Keyboard;
import com.minelittlepony.unicopia.client.input.MouseControl;
import com.minelittlepony.unicopia.entity.capabilities.IPlayer;
import com.minelittlepony.unicopia.client.input.InversionAwareKeyboardInput;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class UnicopiaClient extends UClient {

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
            Race race = MineLP.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }


        return Config.instance().getPrefferedRace();
    }

    @Override
    public void displayGuiToPlayer(PlayerEntity player, IInteractionObject inventory) {
        if (player instanceof PlayerEntitySP) {
            if ("unicopia:itemofholding".equals(inventory.getGuiID())) {
                MinecraftClient.getInstance().displayGuiScreen(new GuiOfHolding(inventory));
            }
        } else {
            super.displayGuiToPlayer(player, inventory);
        }
    }

    @Override
    @Nullable
    public PlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    @Override
    @Nullable
    public PlayerEntity getPlayerByUUID(UUID playerId) {
        Minecraft mc = MinecraftClient.getInstance();

        if (mc.player.getUniqueID().equals(playerId)) {
            return mc.player;
        }

        return mc.world.getPlayerEntityByUUID(playerId);
    }

    @Override
    @Nonnull
    public PlayerEntity createPlayer(Entity observer, GameProfile profile) {
        return new EntityFakeClientPlayer(observer.world, profile);
    }

    @Override
    public boolean isClientPlayer(@Nullable PlayerEntity player) {
        if (getPlayer() == player) {
            return true;
        }

        if (getPlayer() == null || player == null) {
            return false;
        }

        return IPlayer.equal(getPlayer(), player);
    }

    @Override
    public int getViewMode() {
        return MinecraftClient.getInstance().gameSettings.thirdPersonView;
    }

    @Override
    public void postRenderEntity(Entity entity) {
        if (entity instanceof PlayerEntity) {
            IPlayer iplayer = SpeciesList.instance().getPlayer((PlayerEntity)entity);

            if (iplayer.getGravity().getGravitationConstant() < 0) {
                GlStateManager.translate(0, entity.height, 0);
                GlStateManager.scale(1, -1, 1);
                entity.prevRotationPitch *= -1;
                entity.rotationPitch *= -1;
            }
        }
    }

    @Override
    public boolean renderEntity(Entity entity, float renderPartialTicks) {

        if (DisguiseRenderer.getInstance().renderDisguise(entity, renderPartialTicks)) {
            return true;
        }

        if (entity instanceof PlayerEntity) {
            IPlayer iplayer = SpeciesList.instance().getPlayer((PlayerEntity)entity);

            if (iplayer.getGravity().getGravitationConstant() < 0) {
                GlStateManager.scale(1, -1, 1);
                GlStateManager.translate(0, -entity.height, 0);
                entity.prevRotationPitch *= -1;
                entity.rotationPitch *= -1;
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
    public void preInit() {
        UEntities.preInit();
        UParticles.init();
    }

    @Override
    public void init() {
        clientPlayerRace = getclientPlayerRace();
    }

    @Override
    public void tick() {
        PlayerEntity player = UClient.instance().getPlayer();

        if (player != null && !player.removed) {
            Race newRace = getclientPlayerRace();

            if (newRace != clientPlayerRace) {
                clientPlayerRace = newRace;

                Unicopia.getConnection().send(new MsgRequestCapabilities(player, clientPlayerRace), Target.SERVER);
            }
        }

        Keyboard.getKeyHandler().onKeyInput();

        MinecraftClient client = MinecraftClient.getInstance();

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
