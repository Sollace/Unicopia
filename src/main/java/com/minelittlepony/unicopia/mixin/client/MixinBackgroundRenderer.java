package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BackgroundRenderer.FogType;

@Mixin(BackgroundRenderer.class)
abstract class MixinBackgroundRenderer {
    @Inject(method = "applyFog", at = @At("RETURN"))
    private static void onApplyFog(Camera camera, FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info) {
        WorldRenderDelegate.INSTANCE.applyFog(camera, fogType, viewDistance, thickFog, tickDelta);
    }
}
