package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.mob.UEntities;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class UEntityTypeTagProvider extends FabricTagProvider<EntityType<?>> {
    public UEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ENTITY_TYPE, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup lookup) {
        // TODO: Separate these into classes
        getOrCreateTagBuilder(UTags.Entities.TRANSFORMABLE).add(
                EntityType.SKELETON, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK, EntityType.ZOMBIE_VILLAGER,
                EntityType.CREEPER,
                EntityType.VILLAGER, EntityType.WANDERING_TRADER, EntityType.PILLAGER, EntityType.ILLUSIONER, EntityType.EVOKER, EntityType.WITCH,
                EntityType.TURTLE,
                EntityType.BLAZE, //TODO: 1.20.5 EntityType.BREEZE,
                EntityType.SHEEP, EntityType.PIG, EntityType.GOAT,
                EntityType.RABBIT, EntityType.POLAR_BEAR, EntityType.PANDA,
                EntityType.COW, EntityType.MOOSHROOM,
                EntityType.HORSE, EntityType.LLAMA, EntityType.DONKEY, EntityType.MULE,
                EntityType.CHICKEN, EntityType.PARROT,
                EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BEE, UEntities.BUTTERFLY,
                EntityType.MAGMA_CUBE, EntityType.SLIME,
                EntityType.ENDERMITE, UEntities.LOOT_BUG,
                EntityType.SQUID, EntityType.GLOW_SQUID,
                EntityType.OCELOT, EntityType.CAT,
                EntityType.WOLF, EntityType.FOX,
                EntityType.SALMON, EntityType.COD, EntityType.PUFFERFISH,
                EntityType.FROG
        );
    }
}
