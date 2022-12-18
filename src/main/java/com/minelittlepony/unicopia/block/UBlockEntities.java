package com.minelittlepony.unicopia.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityType.Builder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public interface UBlockEntities {
    BlockEntityType<WeatherVaneBlock.WeatherVane> WEATHER_VANE = create("weather_vane", BlockEntityType.Builder.create(WeatherVaneBlock.WeatherVane::new, UBlocks.WEATHER_VANE));

    static <T extends BlockEntity> BlockEntityType<T> create(String id, Builder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, builder.build(null));
    }

    static void bootstrap() {}
}
