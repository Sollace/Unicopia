package com.minelittlepony.unicopia.client.mixin;

import org.spongepowered.asm.mixin.Final;
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
    private @Final MinecraftClient client;
    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;

    @Inject(method = "updateMouse()V", at = @At("HEAD"))
    private void onUpdateMouse(CallbackInfo info) {
        if (Pony.of(client.player).getGravity().getGravitationConstant() < 0) {
            cursorDeltaX = -cursorDeltaX;
            cursorDeltaY = -cursorDeltaY;
        }
    }
}
