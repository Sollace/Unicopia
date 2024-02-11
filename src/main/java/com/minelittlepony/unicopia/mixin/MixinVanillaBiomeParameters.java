package com.minelittlepony.unicopia.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.server.world.gen.BiomeSelectionInjector;
import com.mojang.datafixers.util.Pair;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;

@Mixin(VanillaBiomeParameters.class)
abstract class MixinVanillaBiomeParameters {
    @ModifyVariable(method = "writeOverworldBiomeParameters", at = @At("HEAD"))
    private Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> onWriteOverworldBiomeParameters(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parametersCollector) {
        return new BiomeSelectionInjector(parametersCollector);
    }
}
