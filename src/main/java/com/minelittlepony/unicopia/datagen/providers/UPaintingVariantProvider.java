package com.minelittlepony.unicopia.datagen.providers;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.datagen.DataGenRegistryProvider;

import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.PaintingVariantTags;

public class UPaintingVariantProvider extends DataGenRegistryProvider<PaintingVariant> {
    public UPaintingVariantProvider() {
        super(RegistryKeys.PAINTING_VARIANT);
    }

    @Override
    public void run(Registerable<PaintingVariant> registerable) {
        register(registerable, "bloom", 2, 1);
        register(registerable, "chicken", 2, 1);
        register(registerable, "bells", 2, 1);

        register(registerable, "crystal", 3, 3);
        register(registerable, "harmony", 3, 3);

        register(registerable, "equality", 2, 4);
        register(registerable, "solar", 2, 4);
        register(registerable, "lunar", 2, 4);
        register(registerable, "platinum", 2, 4);
        register(registerable, "hurricane", 2, 4);
        register(registerable, "pudding", 2, 4);
        register(registerable, "terra", 2, 4);
        register(registerable, "equestria", 2, 4);

        register(registerable, "blossom", 2, 3);
        register(registerable, "shadow", 2, 3);
    }

    private void register(Registerable<PaintingVariant> registerable, String name, int width, int height) {
        RegistryKey<PaintingVariant> key = RegistryKey.of(RegistryKeys.PAINTING_VARIANT, Unicopia.id(name));
        registerable.register(key, new PaintingVariant(width, height, key.getValue()));
    }

    @Override
    protected void configureTags(TagProvider tagProvider, WrapperLookup lookup) {
        tagProvider.getOrCreateTagBuilder(PaintingVariantTags.PLACEABLE).add(getKeys());
    }
}
