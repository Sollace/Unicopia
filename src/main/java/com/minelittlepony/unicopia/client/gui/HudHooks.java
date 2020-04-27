package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.MinecraftClient;

@Deprecated
// XXX: hud render events
class HudHooks {
    public static void beforePreRenderHud() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.world != null) {
            UHud.instance.repositionElements(Pony.of(client.player), client.getWindow(), true);
        }
    }

    public static void postRenderHud() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.world != null) {
            UHud.instance.renderHud(Pony.of(client.player), client.getWindow());
        }
    }
}
