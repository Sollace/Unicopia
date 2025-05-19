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
            .addOptional(Identifier.of("ad_astra", "earth_orbit"))
            .addOptional(Identifier.of("ad_astra", "glacio_orbit"))
            .addOptional(Identifier.of("ad_astra", "mars_orbit"))
            .addOptional(Identifier.of("ad_astra", "mercury_orbit"))
            .addOptional(Identifier.of("ad_astra", "moon")).addOptional(Identifier.of("adastra", "moon_orbit"))
            .addOptional(Identifier.of("ad_astra", "venus_orbit"))

            .addOptional(Identifier.of("adastra", "earth_orbit"))
            .addOptional(Identifier.of("adastra", "glacio_orbit"))
            .addOptional(Identifier.of("adastra", "mars_orbit"))
            .addOptional(Identifier.of("adastra", "mercury_orbit"))
            .addOptional(Identifier.of("adastra", "moon")).addOptional(Identifier.of("adastra", "moon_orbit"))
            .addOptional(Identifier.of("adastra", "venus_orbit"));
    }
}
