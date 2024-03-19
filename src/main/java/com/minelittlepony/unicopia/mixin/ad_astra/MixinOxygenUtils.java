package com.minelittlepony.unicopia.mixin.ad_astra;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;

import com.minelittlepony.unicopia.compat.ad_astra.OxygenUtils;

@Pseudo
@Mixin(
        targets = { "earth.terrarium.adastra.api.systems.OxygenApi" },
        remap = false
)
public interface MixinOxygenUtils extends OxygenUtils.OxygenApi {
    @Accessor("API")
    @Coerce
    static Object getAPI() {
        throw new AbstractMethodError("stub");
    }

    @Dynamic("Compiler-generated class-init() method")
    @Inject(method = "<clinit>()V", at = @At("RETURN"), remap = false)
    private static void classInit() {
        OxygenUtils.API = (OxygenUtils.OxygenApi)getAPI();
    }
}
