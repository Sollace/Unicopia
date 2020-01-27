package com.minelittlepony.unicopia.core.client.gui;

import com.minelittlepony.unicopia.core.SpeciesList;
import com.minelittlepony.unicopia.core.client.gui.UHud;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;

class ClientHooks {
    public static void beforePreRenderHud() {
        GlStateManager.pushMatrix();

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.world != null) {
            UHud.instance.repositionElements(SpeciesList.instance().getPlayer(client.player), client.window, true);
        }
    }

    public static void postRenderHud() {

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.world != null) {
            UHud.instance.renderHud(SpeciesList.instance().getPlayer(client.player), client.window);
        }

        GlStateManager.popMatrix();
    }
}
