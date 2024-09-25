package com.minelittlepony.unicopia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.InteractionManager;

import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.chunk.ChunkCache;

@Mixin(PathNodeMaker.class)
abstract class MixinPathNodeMaker {
    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(ChunkCache cachedWorld, MobEntity entity, CallbackInfo info) {
        InteractionManager.getInstance().setEquineContext(EquineContext.of(entity));
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void onClear(CallbackInfo info) {
        InteractionManager.getInstance().clearEquineContext();
    }
}
