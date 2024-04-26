package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public class UDimensionTypeTagProvider extends FabricTagProvider<DimensionType> {
    public UDimensionTypeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DIMENSION_TYPE, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup lookup) {
        getOrCreateTagBuilder(UTags.DimensionTypes.HAS_NO_ATMOSPHERE)
            .addOptional(new Identifier("ad_astra", "earth_orbit"))
            .addOptional(new Identifier("ad_astra", "glacio_orbit"))
            .addOptional(new Identifier("ad_astra", "mars_orbit"))
            .addOptional(new Identifier("ad_astra", "mercury_orbit"))
            .addOptional(new Identifier("ad_astra", "moon")).addOptional(new Identifier("adastra", "moon_orbit"))
            .addOptional(new Identifier("ad_astra", "venus_orbit"))

            .addOptional(new Identifier("adastra", "earth_orbit"))
            .addOptional(new Identifier("adastra", "glacio_orbit"))
            .addOptional(new Identifier("adastra", "mars_orbit"))
            .addOptional(new Identifier("adastra", "mercury_orbit"))
            .addOptional(new Identifier("adastra", "moon")).addOptional(new Identifier("adastra", "moon_orbit"))
            .addOptional(new Identifier("adastra", "venus_orbit"));
    }
}
