package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;
import com.minelittlepony.unicopia.block.cloud.CloudChestBlock;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public interface UBlockEntities {
    BlockEntityType<WeatherVaneBlock.WeatherVane> WEATHER_VANE = create("weather_vane", FabricBlockEntityTypeBuilder.create(WeatherVaneBlock.WeatherVane::new, UBlocks.WEATHER_VANE));
    BlockEntityType<CloudBedBlock.Tile> FANCY_BED = create("fancy_bed", FabricBlockEntityTypeBuilder.create(CloudBedBlock.Tile::new, UBlocks.CLOTH_BED, UBlocks.CLOUD_BED));
    BlockEntityType<ChestBlockEntity> CLOUD_CHEST = create("cloud_chest", FabricBlockEntityTypeBuilder.create(CloudChestBlock.TileData::new, UBlocks.CLOUD_CHEST));
    BlockEntityType<HiveBlock.TileData> HIVE_STORAGE = create("hive_storage", FabricBlockEntityTypeBuilder.create(HiveBlock.TileData::new, UBlocks.HIVE));
    BlockEntityType<ItemJarBlock.TileData> ITEM_JAR = create("item_jar", FabricBlockEntityTypeBuilder.create(ItemJarBlock.TileData::new, UBlocks.JAR));
    BlockEntityType<CrystalDoorBlock.TileData> CRYSTAL_DOOR = create("crystal_door", FabricBlockEntityTypeBuilder.create(CrystalDoorBlock.TileData::new, UBlocks.CRYSTAL_DOOR));

    static <T extends BlockEntity> BlockEntityType<T> create(String id, FabricBlockEntityTypeBuilder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, builder.build());
    }

    static void bootstrap() {}
}
