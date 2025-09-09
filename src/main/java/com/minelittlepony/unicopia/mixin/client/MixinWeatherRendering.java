package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.server.world.WeatherAccess;

import net.minecraft.client.render.WeatherRendering;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Precipitation;

@Mixin(value = WeatherRendering.class, priority = 1001)
abstract class MixinWeatherRendering {
    @Inject(method = "getPrecipitationAt", at = @At("HEAD"))
    private void modifyPrecipitation(World world, BlockPos pos, int seaLevel, CallbackInfoReturnable<Precipitation> info) {
        if (!((WeatherAccess)world).isBelowClientCloudLayer(pos)) {
            info.setReturnValue(Precipitation.NONE);
        }
    }
}
