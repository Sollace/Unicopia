package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
abstract class MixinMouse {
    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;

    @Inject(method = "updateMouse()V", at = @At("HEAD"))
    private void onUpdateMouse(CallbackInfo info) {
        Pony player = Pony.of(MinecraftClient.getInstance().player);
        if (player != null && player.getPhysics().isGravityNegative()) {
            cursorDeltaX = -cursorDeltaX;
            cursorDeltaY = -cursorDeltaY;
        }
    }
}
