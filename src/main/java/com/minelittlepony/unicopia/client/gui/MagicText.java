package com.minelittlepony.unicopia.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public interface MagicText {
    static int getColor() {
        MinecraftClient client = MinecraftClient.getInstance();
        float ticks = client.player.age + client.getRenderTickCounter().getTickDelta(false);

        float sin = (MathHelper.sin(ticks / 10F) + 1) * 155 * 0.25F;
        float cos = (MathHelper.cos((ticks + 10) / 10F) + 1) * 155 * 0.25F;

        return (int)(sin + cos);
    }
}
