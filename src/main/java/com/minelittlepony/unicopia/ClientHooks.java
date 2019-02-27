package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.forgebullshit.FUF;
import com.minelittlepony.unicopia.hud.UHud;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.IView;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
class ClientHooks {
    // This fixes lighting errors on the armour slots.
    // #MahjongPls
    @FUF(reason = "Forge should fix this. Cancelling their event skips neccessary state resetting at the end of the render method")
    @SubscribeEvent
    public static void postEntityRender(RenderLivingEvent.Post<?> event) {
        GlStateManager.enableAlpha();

        UClient.instance().postRenderEntity(event.getEntity());
    }

    @SubscribeEvent
    public static void preEntityRender(RenderLivingEvent.Pre<?> event) {
        if (UClient.instance().renderEntity(event.getEntity(), event.getPartialRenderTick())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDisplayGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiOptions || event.getGui() instanceof GuiShareToLan) {
            UnicopiaClient.addUniButton(event.getButtonList());
        }
    }

    @SubscribeEvent
    public static void onGameTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END) {
            UClient.instance().tick();
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGameOverlayEvent.Post event) {
        if (event.getType() != ElementType.ALL) {
            return;
        }

        IPlayer player = UClient.instance().getIPlayer();
        if (player != null && Minecraft.getMinecraft().world != null) {
            UHud.instance.renderHud(player, event.getResolution());
        }
    }

    @SubscribeEvent
    public static void modifyFOV(FOVUpdateEvent event) {
        event.setNewfov(PlayerSpeciesList.instance().getPlayer(event.getEntity()).getCamera().calculateFieldOfView(event.getFov()));
    }

    @SubscribeEvent
    public static void setupPlayerCamera(EntityViewRenderEvent.CameraSetup event) {

        IPlayer player = UClient.instance().getIPlayer();

        if (player != null) {
            IView view = player.getCamera();

            event.setRoll(view.calculateRoll());
            event.setPitch(view.calculatePitch(event.getPitch()));
            event.setYaw(view.calculateYaw(event.getYaw()));
        }
    }
}
