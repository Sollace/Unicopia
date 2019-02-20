package com.minelittlepony.unicopia;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.gui.Button;
import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.entity.EntityFakeClientPlayer;
import com.minelittlepony.unicopia.extern.MineLP;
import com.minelittlepony.unicopia.gui.GuiScreenSettings;
import com.minelittlepony.unicopia.init.UEntities;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.input.Keyboard;
import com.minelittlepony.unicopia.inventory.gui.GuiOfHolding;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.render.DisguiseRenderer;
import com.minelittlepony.util.gui.ButtonGridLayout;
import com.minelittlepony.util.lang.ClientLocale;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IInteractionObject;

import static com.minelittlepony.util.gui.ButtonGridLayout.*;

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
        if (!UConfig.instance().ignoresMineLittlePony()
                && Minecraft.getMinecraft().player != null) {
            Race race = MineLP.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }


        return UConfig.instance().getPrefferedRace();
    }

    static void addUniButton(List<GuiButton> buttons) {
        ButtonGridLayout layout = new ButtonGridLayout(buttons);

        GuiButton uni = new Button(0, 0, 150, 20, ClientLocale.format("gui.unicopia"), b -> {
            Minecraft mc = Minecraft.getMinecraft();

            mc.displayGuiScreen(new GuiScreenSettings(mc.currentScreen));
        });

        List<Integer> possibleXCandidates = list(layout.getColumns());
        List<Integer> possibleYCandidates = list(layout.getRows());

        uni.y = last(possibleYCandidates, 1);

        if (layout.getRows()
                .filter(y -> layout.getRow(y).size() == 1).count() < 2) {
            uni.y += 25;
            uni.x = first(possibleXCandidates, 0);

            layout.getRow(last(possibleYCandidates, 0)).forEach(button -> {
                button.y = Math.max(button.y, uni.y + uni.height + 13);
            });
        } else {
            uni.x = first(possibleXCandidates, 2);
        }

        layout.getElements().add(uni);
    }

    @Override
    public void displayGuiToPlayer(EntityPlayer player, IInteractionObject inventory) {
        if (player instanceof EntityPlayerSP) {
            if ("unicopia:itemofholding".equals(inventory.getGuiID())) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiOfHolding(inventory));
            }
        } else {
            super.displayGuiToPlayer(player, inventory);
        }
    }

    @Override
    @Nullable
    public EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    @Nullable
    public EntityPlayer getPlayerByUUID(UUID playerId) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.getUniqueID().equals(playerId)) {
            return mc.player;
        }

        return mc.world.getPlayerEntityByUUID(playerId);
    }

    @Nonnull
    public EntityPlayer createPlayer(Entity observer, GameProfile profile) {
        return new EntityFakeClientPlayer(observer.world, profile);
    }

    @Override
    public boolean isClientPlayer(@Nullable EntityPlayer player) {
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
        return Minecraft.getMinecraft().gameSettings.thirdPersonView;
    }

    @Override
    public boolean renderEntity(Entity entity, float renderPartialTicks) {

        if (DisguiseRenderer.instance().renderDisguise(entity, renderPartialTicks)) {
            return true;
        }

        if (entity instanceof EntityPlayer) {
            IPlayer iplayer = PlayerSpeciesList.instance().getPlayer((EntityPlayer)entity);

            if (DisguiseRenderer.instance().renderDisguiseToGui(iplayer)) {
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

    public void tick() {
        EntityPlayer player = UClient.instance().getPlayer();

        if (player != null && !player.isDead) {
            Race newRace = getclientPlayerRace();

            if (newRace != clientPlayerRace) {
                clientPlayerRace = newRace;

                Unicopia.getConnection().send(new MsgRequestCapabilities(player, clientPlayerRace), Target.SERVER);
            }
        }

        Keyboard.getKeyHandler().onKeyInput();
    }
}
