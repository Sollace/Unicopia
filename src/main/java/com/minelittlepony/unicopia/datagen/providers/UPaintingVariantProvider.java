package com.minelittlepony.unicopia.datagen.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class UPaintingVariantProvider extends FabricDynamicRegistryProvider {

    private final List<RegistryKey<PaintingVariant>> keys = new ArrayList<>();

    public UPaintingVariantProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Painting Variants";
    }

    public List<RegistryKey<PaintingVariant>> getKeys() {
        return keys;
    }

    @Override
    protected void configure(WrapperLookup registries, Entries entries) {
        register(entries, "bloom", 2, 1);
        register(entries, "chicken", 2, 1);
        register(entries, "bells", 2, 1);

        register(entries, "crystal", 3, 3);
        register(entries, "harmony", 3, 3);

        register(entries, "equality", 2, 4);
        register(entries, "solar", 2, 4);
        register(entries, "lunar", 2, 4);
        register(entries, "platinum", 2, 4);
        register(entries, "hurricane", 2, 4);
        register(entries, "pudding", 2, 4);
        register(entries, "terra", 2, 4);
        register(entries, "equestria", 2, 4);

        register(entries, "blossom", 2, 3);
        register(entries, "shadow", 2, 3);
    }

    private void register(Entries entries, String name, int width, int height) {
        RegistryKey<PaintingVariant> key = RegistryKey.of(RegistryKeys.PAINTING_VARIANT, Unicopia.id(name));
        keys.add(key);
        entries.add(key, new PaintingVariant(width, height, key.getValue()));
    }
}
