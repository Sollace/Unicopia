package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import net.minecraft.client.render.Camera;

@Mixin(Camera.class)
abstract class MixinCamera {
    @ModifyVariable(method = "setRotation",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private float modifyYaw(float yaw) {
        return UnicopiaClient.getCamera().calculateYaw(yaw);
    }

    @ModifyVariable(method = "setRotation",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private float modifyPitch(float pitch) {
        return UnicopiaClient.getCamera().calculatePitch(pitch);
    }

    @ModifyReturnValue(method = "clipToSpace",
            at = @At("RETURN"))
    private float modifyCameraDistance(float initial) {
        return UnicopiaClient.getCamera().calculateDistance(initial);
    }
}
