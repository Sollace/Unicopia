package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class UStatusEffectTagProvider extends FabricTagProvider<StatusEffect> {
    public UStatusEffectTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.STATUS_EFFECT, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup lookup) {
        getOrCreateTagBuilder(UTags.StatusEffects.PINEAPPLE_EFFECTS).add(StatusEffects.REGENERATION, StatusEffects.ABSORPTION, StatusEffects.LUCK, StatusEffects.HASTE);
    }
}
