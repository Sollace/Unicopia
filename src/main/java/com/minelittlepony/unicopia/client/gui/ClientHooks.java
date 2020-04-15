package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;

@Deprecated
// TODO: forge events
class ClientHooks {
    public static void beforePreRenderHud() {
        GlStateManager.pushMatrix();

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.world != null) {
            UHud.instance.repositionElements(Pony.of(client.player), client.window, true);
        }
    }

    public static void postRenderHud() {

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.world != null) {
            UHud.instance.renderHud(Pony.of(client.player), client.window);
        }

        GlStateManager.popMatrix();
    }
}
