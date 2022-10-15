package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;

@Mixin(Entity.class)
abstract class MixinEntity implements EntityDuck {
    @Override
    @Accessor
    public abstract void setRemovalReason(RemovalReason reason);

    @Override
    public boolean isLavaAffine() {
        Entity self = (Entity)(Object)this;
        return self.hasVehicle() && ((LavaAffine)self.getVehicle()).isLavaAffine();
    }

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void onIsFireImmune(CallbackInfoReturnable<Boolean> info) {
        if (isLavaAffine()) {
            info.setReturnValue(true);
        }
    }
}
