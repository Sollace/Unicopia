package com.minelittlepony.unicopia.block.cloud;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

public interface CloudLike {
    static Block.Settings applyCloudProperties(Block.Settings settings) {
        return settings.nonOpaque().dynamicBounds().allowsSpawning((state, world, pos, type) -> {
            return type == EntityType.PHANTOM || type == EntityType.PARROT || type.getSpawnGroup() == SpawnGroup.AMBIENT;
        });
    }
}
