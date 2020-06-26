package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.world.block.gas.CloudInteractionContext;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;

@Mixin(EntityShapeContext.class)
abstract class MixinEntityContextImpl implements CloudInteractionContext.Holder {
    private CloudInteractionContext cloudContext;

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
    private void onInit(Entity e, CallbackInfo into) {
        cloudContext = CloudInteractionContext.of(e);
    }

    @Override
    public CloudInteractionContext getCloudInteractionContext() {
        return cloudContext == null ? CloudInteractionContext.empty() : cloudContext;
    }
}
