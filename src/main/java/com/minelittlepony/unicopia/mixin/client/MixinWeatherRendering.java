package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.server.world.WeatherAccess;

import net.minecraft.client.render.WeatherRendering;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Precipitation;

@Mixin(value = WeatherRendering.class, priority = 1001)
abstract class MixinWeatherRendering {
    @ModifyReturnValue(method = "getPrecipitationAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome$Precipitation;", at = @At("RETURN"))
    private Precipitation modifyPrecipitation(Precipitation precipitation, World world, BlockPos pos) {
        return ((WeatherAccess)world).isBelowClientCloudLayer(pos) ? precipitation : Precipitation.NONE;
    }
}
