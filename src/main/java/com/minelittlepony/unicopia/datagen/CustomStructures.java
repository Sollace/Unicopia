package com.minelittlepony.unicopia.datagen;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.UEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.StructureSpawns.BoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings.SpawnEntry;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.heightprovider.ConstantHeightProvider;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;

final class CustomStructures {
    public static final RegistryKey<Structure> ALTAR = Unicopia.key(RegistryKeys.STRUCTURE, "altar");
    public static final RegistryKey<Structure> ABANDONED_CHANGELING_HIVE = Unicopia.key(RegistryKeys.STRUCTURE, "abandoned_changeling_hive");
    public static final RegistryKey<Structure> CLOUD_VILLAGE = Unicopia.key(RegistryKeys.STRUCTURE, "cloud_village");
    public static final RegistryKey<Structure> FLOATING_ISLAND = Unicopia.key(RegistryKeys.STRUCTURE, "floating_island");
    public static final RegistryKey<Structure> CLOUD_CONTINENT = Unicopia.key(RegistryKeys.STRUCTURE, "cloud_continent");

    public static final RegistryKey<StructureSet> ALTARS = Unicopia.key(RegistryKeys.STRUCTURE_SET, "altars");
    public static final RegistryKey<StructureSet> CHANGELING_HIVES = Unicopia.key(RegistryKeys.STRUCTURE_SET, "changeling_hives");
    public static final RegistryKey<StructureSet> CLOUDS = Unicopia.key(RegistryKeys.STRUCTURE_SET, "clouds");

    static void bootstrap(Registerable<Structure> registerable) {
        RegistryEntryLookup<Biome> biomes = registerable.getRegistryLookup(RegistryKeys.BIOME);
        RegistryEntryLookup<StructurePool> pools = registerable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);

        registerable.register(ALTAR, new JigsawStructure(
                new Structure.Config.Builder(biomes.getOrThrow(BiomeTags.VILLAGE_DESERT_HAS_STRUCTURE))
                    .terrainAdaptation(StructureTerrainAdaptation.BEARD_THIN)
                    .build(),
                pools.getOrThrow(CustomStructurePools.ALTAR),
                Optional.empty(),
                6,
                ConstantHeightProvider.ZERO,
                true,
                Optional.of(Heightmap.Type.WORLD_SURFACE_WG),
                80,
                List.of(),
                DimensionPadding.NONE,
                StructureLiquidSettings.APPLY_WATERLOGGING
        ));

        var isOverworld = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);
        registerable.register(FLOATING_ISLAND, new JigsawStructure(
                new Structure.Config.Builder(isOverworld).build(),
                pools.getOrThrow(CustomStructurePools.CLOUDS_START),
                Optional.empty(),
                6,
                ConstantHeightProvider.create(YOffset.fixed(180)),
                true,
                Optional.of(Heightmap.Type.WORLD_SURFACE_WG),
                80,
                List.of(),
                DimensionPadding.NONE,
                StructureLiquidSettings.APPLY_WATERLOGGING
        ));
        registerable.register(ABANDONED_CHANGELING_HIVE, new JigsawStructure(
                new Structure.Config.Builder(biomes.getOrThrow(BiomeTags.IS_FOREST)).step(Feature.UNDERGROUND_STRUCTURES)
                    .spawnOverrides(Map.of(
                            SpawnGroup.MONSTER, new StructureSpawns(BoundingBox.PIECE, Pool.<SpawnEntry>of(new Weighted<>(new SpawnEntry(EntityType.SLIME, 2, 8), 3)))
                    )).build(),
                pools.getOrThrow(CustomStructurePools.CHANGELING_HIVE_START),
                Optional.empty(),
                20,
                ConstantHeightProvider.create(YOffset.fixed(-6)),
                false,
                Optional.of(Heightmap.Type.WORLD_SURFACE_WG),
                112,
                List.of(),
                DimensionPadding.NONE,
                StructureLiquidSettings.APPLY_WATERLOGGING
        ));
        registerable.register(CLOUD_CONTINENT, new JigsawStructure(
                new Structure.Config.Builder(isOverworld)
                    .spawnOverrides(Map.of(
                            SpawnGroup.AMBIENT, new StructureSpawns(BoundingBox.STRUCTURE, Pool.<SpawnEntry>of(new Weighted<>(new SpawnEntry(UEntities.STORM_CLOUD, 2, 8), 3)))
                    )).build(),
                pools.getOrThrow(CustomStructurePools.CLOUD_ISLAND_LARGE_START),
                Optional.empty(),
                6,
                ConstantHeightProvider.create(YOffset.fixed(180)),
                true,
                Optional.of(Heightmap.Type.WORLD_SURFACE_WG),
                80,
                List.of(),
                DimensionPadding.NONE,
                StructureLiquidSettings.APPLY_WATERLOGGING
        ));
        registerable.register(CLOUD_VILLAGE, new JigsawStructure(
                new Structure.Config.Builder(isOverworld).build(),
                pools.getOrThrow(CustomStructurePools.CLOUDS_START),
                Optional.empty(),
                6,
                ConstantHeightProvider.create(YOffset.fixed(180)),
                true,
                Optional.of(Heightmap.Type.WORLD_SURFACE_WG),
                80,
                List.of(),
                DimensionPadding.NONE,
                StructureLiquidSettings.APPLY_WATERLOGGING
        ));
    }

    static void bootstrapSets(Registerable<StructureSet> registerable) {
        RegistryEntryLookup<Structure> structures = registerable.getRegistryLookup(RegistryKeys.STRUCTURE);
        registerable.register(ALTARS, new StructureSet(List.of(
                StructureSet.createEntry(structures.getOrThrow(ALTAR))
        ), new RandomSpreadStructurePlacement(64, 32, SpreadType.LINEAR, 54386171)));
        registerable.register(CHANGELING_HIVES, new StructureSet(List.of(
                StructureSet.createEntry(structures.getOrThrow(ABANDONED_CHANGELING_HIVE))
        ), new RandomSpreadStructurePlacement(84, 62, SpreadType.LINEAR, 54138171)));
        registerable.register(CLOUDS, new StructureSet(List.of(
                StructureSet.createEntry(structures.getOrThrow(CLOUD_VILLAGE), 90),
                StructureSet.createEntry(structures.getOrThrow(FLOATING_ISLAND)),
                StructureSet.createEntry(structures.getOrThrow(CLOUD_CONTINENT), 5)
        ), new RandomSpreadStructurePlacement(10, 2, SpreadType.LINEAR, 54138171)));
    }
}
