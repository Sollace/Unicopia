package com.minelittlepony.unicopia.mixin.client;

import java.util.Queue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;

@Mixin(MinecraftClient.class)
public interface MixinMinecraftClient {
    @Accessor("renderTaskQueue")
    Queue<Runnable> getRenderTaskQueue();

    @Mutable
    @Accessor("worldRenderer")
    void setWorldRenderer(WorldRenderer worldRenderer);
}
