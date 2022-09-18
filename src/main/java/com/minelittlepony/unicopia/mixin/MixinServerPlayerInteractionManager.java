package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;

@Mixin(ServerPlayerInteractionManager.class)
public interface MixinServerPlayerInteractionManager {
    @Accessor
    ServerPlayerEntity getPlayer();
}
