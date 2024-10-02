package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.item.ItemDuck;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;

@Mixin(BlockAttachedEntity.class)
abstract class MixinAbstractDecorationEntity {
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo info) {
        final Object _this = this;
        if (_this instanceof ItemFrameEntity self) {
            ItemDuck.of(self.getHeldItemStack()).inFrameTick(self);
        }
    }
}
