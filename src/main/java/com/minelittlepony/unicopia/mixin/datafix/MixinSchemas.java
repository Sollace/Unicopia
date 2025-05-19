package com.minelittlepony.unicopia.mixin.datafix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.DataFixerBuilder;

import net.minecraft.datafixer.Schemas;

@Mixin(Schemas.class)
abstract class MixinSchemas {
    @Inject(method = "build", at = @At("TAIL"))
    private static void unicopia_build(DataFixerBuilder builder, CallbackInfo info) {
        com.minelittlepony.unicopia.datafix.Schemas.build(builder);
    }
}
