package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UBlocks;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.UItems;
import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.entity.capabilities.ICamera;
import com.minelittlepony.unicopia.entity.capabilities.IPlayer;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;

class ClientHooks {
    // This fixes lighting errors on the armour slots.
    // #MahjongPls
    public static void postEntityRender() {
        GlStateManager.enableAlphaTest();

        UClient.instance().postRenderEntity(event.getEntity());
    }

    public static void preEntityRender() {
        if (UClient.instance().renderEntity(event.getEntity(), event.getPartialRenderTick())) {
            event.setCanceled(true);
        }
    }

    public static void onDisplayGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiOptions || event.getGui() instanceof GuiShareToLan) {
            UnicopiaClient.addUniButton(event.getButtonList());
        }
    }

    public static void onGameTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END) {
            UClient.instance().tick();
        }
    }

    public static void beforePreRenderHud(RenderGameOverlayEvent.Pre event) {
        GlStateManager.pushMatrix();

        if (event.getType() != ElementType.ALL) {
            IPlayer player = UClient.instance().getIPlayer();

            if (player != null && MinecraftClient.getInstance().world != null) {
                UHud.instance.repositionElements(player, event.getResolution(), event.getType(), true);
            }
        }
    }

    public static void afterPreRenderHud(RenderGameOverlayEvent.Pre event) {
        if (event.isCanceled()) {
            GlStateManager.popMatrix();
        }
    }

    public static void postRenderHud(RenderGameOverlayEvent.Post event) {

        if (event.getType() == ElementType.ALL) {
            IPlayer player = UClient.instance().getIPlayer();

            if (player != null && MinecraftClient.getInstance().world != null) {
                UHud.instance.renderHud(player, event.getResolution());
            }
        }

        GlStateManager.popMatrix();
    }

    public static void registerItemColours(ColorHandlerEvent.Item event) {
        UItems.registerColors(event.getItemColors());
        UBlocks.registerColors(event.getItemColors(), event.getBlockColors());
    }

    public static void modifyFOV(FOVUpdateEvent event) {
        event.setNewfov(SpeciesList.instance().getPlayer(event.getEntity()).getCamera().calculateFieldOfView(event.getFov()));
    }

    public static void setupPlayerCamera(EntityViewRenderEvent.CameraSetup event) {

        IPlayer player = UClient.instance().getIPlayer();

        if (player != null) {
            ICamera view = player.getCamera();

            event.setRoll(view.calculateRoll());
            event.setPitch(view.calculatePitch(event.getPitch()));
            event.setYaw(view.calculateYaw(event.getYaw()));
        }
    }
}
