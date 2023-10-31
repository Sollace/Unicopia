package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityType.Builder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public interface UBlockEntities {
    BlockEntityType<WeatherVaneBlock.WeatherVane> WEATHER_VANE = create("weather_vane", BlockEntityType.Builder.create(WeatherVaneBlock.WeatherVane::new, UBlocks.WEATHER_VANE));
    BlockEntityType<CloudBedBlock.Tile> CLOUD_BED = create("cloud_bed", BlockEntityType.Builder.create(CloudBedBlock.Tile::new, UBlocks.WHITE_CLOUD_BED, UBlocks.ORANGE_CLOUD_BED));

    static <T extends BlockEntity> BlockEntityType<T> create(String id, Builder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, builder.build(null));
    }

    static void bootstrap() {}
}
