package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.gas.CloudInteractionContext;
import com.minelittlepony.unicopia.gas.CloudType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContextImpl;

@Mixin(EntityContextImpl.class)
abstract class MixinEntityContextImpl implements CloudInteractionContext {
    private CloudInteractionContext cloudContext;

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
    private void onInit(Entity e, CallbackInfo into) {
        cloudContext = CloudInteractionContext.of(e);
    }

    @Override
    public boolean isPlayer() {
        return cloudContext != null && cloudContext.isPlayer();
    }

    @Override
    public boolean isPegasis() {
        return cloudContext != null && cloudContext.isPegasis();
    }

    @Override
    public boolean canTouch(CloudType type) {
        return cloudContext != null && cloudContext.canTouch(type);
    }
}
