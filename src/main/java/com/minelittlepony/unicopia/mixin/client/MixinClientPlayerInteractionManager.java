package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;

@Mixin(ClientPlayerInteractionManager.class)
abstract class MixinClientPlayerInteractionManager {
    @Inject(method = "getReachDistance()F", at = @At("RETURN"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> info) {
        Pony player = Pony.of(MinecraftClient.getInstance().player);

        if (player != null) {
            info.setReturnValue(player.getExtendedReach() + info.getReturnValueF());
        }
    }

    @Inject(method = "hasExtendedReach", at = @At("HEAD"), cancellable = true)
    private void onHasExtendedReach(CallbackInfoReturnable<Boolean> info) {
        if (!info.getReturnValueZ() && Pony.of(MinecraftClient.getInstance().player).getExtendedReach() > 0) {
            info.setReturnValue(true);
        }
    }
}
