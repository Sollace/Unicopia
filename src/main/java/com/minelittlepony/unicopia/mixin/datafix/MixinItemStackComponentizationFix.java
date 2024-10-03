package com.minelittlepony.unicopia.mixin.datafix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Dynamic;

import net.minecraft.datafixer.fix.ItemStackComponentizationFix;

@Mixin(ItemStackComponentizationFix.class)
abstract class MixinItemStackComponentizationFix {
    @Inject(method = "fixStack", at = @At("TAIL"))
    private static void unicopia_fixStack(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, CallbackInfo info) {
        com.minelittlepony.unicopia.datafix.Schemas.fixComponents(data, dynamic);
    }
}