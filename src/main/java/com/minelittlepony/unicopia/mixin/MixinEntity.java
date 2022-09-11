package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.LavaAffine;
import com.minelittlepony.unicopia.entity.Removeable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;

@Mixin(Entity.class)
abstract class MixinEntity implements Removeable {
    @Override
    @Accessor
    public abstract void setRemovalReason(RemovalReason reason);

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void onIsFireImmune(CallbackInfoReturnable<Boolean> info) {
        if (this instanceof LavaAffine e && e.isLavaAffine()) {
            info.setReturnValue(true);
        }
    }
}
