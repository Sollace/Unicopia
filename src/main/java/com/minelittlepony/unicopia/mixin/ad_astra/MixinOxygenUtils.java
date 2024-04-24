package com.minelittlepony.unicopia.mixin.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.compat.ad_astra.OxygenApi;

@Pseudo
@Mixin(
        targets = { "earth.terrarium.adastra.api.systems.OxygenApi" },
        remap = false
)
interface MixinOxygenApi extends OxygenApi {
}

@Pseudo
@Mixin(
        targets = { "earth.terrarium.adastra.api.ApiHelper" },
        remap = false
)
abstract class MixinApiHelper {
    @Inject(method = "load", at = @At("RETURN"), require = 0)
    private static <T> void onLoad(Class<T> clazz, CallbackInfoReturnable<T> info) {
        if (info.getReturnValue() instanceof OxygenApi api) {
            OxygenApi.API.set(api);
        }
    }
}