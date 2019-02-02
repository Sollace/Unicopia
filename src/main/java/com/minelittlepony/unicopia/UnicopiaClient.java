package com.minelittlepony.unicopia;

import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.hud.UHud;
import com.minelittlepony.unicopia.input.Keyboard;
import com.minelittlepony.unicopia.inventory.gui.GuiOfHolding;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.IView;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(Side.CLIENT)
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
        if (!UConfig.getInstance().ignoresMineLittlePony()
                && Minecraft.getMinecraft().player != null) {
            Race race = MineLP.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }

        return UConfig.getInstance().getPrefferedRace();
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

    @Override
    public boolean isClientPlayer(@Nullable EntityPlayer player) {
        if (getPlayer() == player) {
            return true;
        }

        if (getPlayer() == null || player == null) {
            return false;
        }

        return getPlayer().getGameProfile().getId().equals(player.getGameProfile().getId());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        UEntities.preInit();
        UParticles.init();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent event) {
        clientPlayerRace = getclientPlayerRace();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerItemColours(ColorHandlerEvent.Item event) {
        UItems.registerColors(event.getItemColors());
        UBlocks.registerColors(event.getItemColors(), event.getBlockColors());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRenderHud(RenderGameOverlayEvent.Post event) {
        if (event.getType() != ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && mc.world != null) {
            IPlayer player = PlayerSpeciesList.instance().getPlayer(mc.player);

            UHud.instance.renderHud(player, event.getResolution());
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void modifyFOV(FOVUpdateEvent event) {
        event.setNewfov(PlayerSpeciesList.instance().getPlayer(event.getEntity()).getCamera().calculateFieldOfView(event.getFov()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onGameTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END) {
            EntityPlayer player = UClient.instance().getPlayer();

            if (player != null) {
                Race newRace = getclientPlayerRace();

                if (newRace != clientPlayerRace) {
                    clientPlayerRace = newRace;

                    Unicopia.channel.send(new MsgRequestCapabilities(player, clientPlayerRace), Target.SERVER);
                }
            }

            Keyboard.getKeyHandler().onKeyInput();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void setupPlayerCamera(EntityViewRenderEvent.CameraSetup event) {

        EntityPlayer player = UClient.instance().getPlayer();

        if (player != null) {
            IView view = PlayerSpeciesList.instance().getPlayer(player).getCamera();

            event.setRoll(view.calculateRoll());
            event.setPitch(view.calculatePitch(event.getPitch()));
            event.setYaw(view.calculateYaw(event.getYaw()));
        }
    }
}
