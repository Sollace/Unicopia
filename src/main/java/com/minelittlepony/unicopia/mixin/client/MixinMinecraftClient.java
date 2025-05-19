package com.minelittlepony.unicopia.mixin.client;

import java.util.Queue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public interface MixinMinecraftClient {
    @Accessor("renderTaskQueue")
    Queue<Runnable> getRenderTaskQueue();
}
