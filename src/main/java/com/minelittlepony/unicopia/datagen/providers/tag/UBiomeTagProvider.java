package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.server.world.gen.UBiomes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.Biome;

public class UBiomeTagProvider extends FabricTagProvider<Biome> {
    public UBiomeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BIOME, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup lookup) {
        getOrCreateTagBuilder(BiomeTags.IS_FOREST)
            .add(UBiomes.SWEET_APPLE_ORCHARD);
    }
}
