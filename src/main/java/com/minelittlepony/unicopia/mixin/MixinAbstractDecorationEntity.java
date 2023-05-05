package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.ItemImpl.ClingyItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;

@Mixin(AbstractDecorationEntity.class)
abstract class MixinAbstractDecorationEntity extends Entity {
    MixinAbstractDecorationEntity() { super(null, null); }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(CallbackInfo info) {
        final Object _this = this;
        if (_this instanceof ItemFrameEntity self) {
            ItemStack stack = self.getHeldItemStack();
            if (stack.getItem() instanceof ClingyItem item) {
                item.inFrameTick(self);
            }
        }
    }
}
