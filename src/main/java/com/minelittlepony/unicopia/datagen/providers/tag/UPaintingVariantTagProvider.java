package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.datagen.providers.UPaintingVariantProvider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.PaintingVariantTags;

public class UPaintingVariantTagProvider extends FabricTagProvider<PaintingVariant> {

    private final UPaintingVariantProvider provider;

    public UPaintingVariantTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture, UPaintingVariantProvider provider) {
        super(output, RegistryKeys.PAINTING_VARIANT, registriesFuture);
        this.provider = provider;
    }

    @Override
    protected void configure(WrapperLookup lookup) {
        getOrCreateTagBuilder(PaintingVariantTags.PLACEABLE).add(provider.getKeys());
    }
}
