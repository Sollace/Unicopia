package com.minelittlepony.unicopia.entity;

import java.util.List;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.EndBiome;
import net.minecraft.world.biome.ForestBiome;
import net.minecraft.world.biome.MountainsBiome;
import net.minecraft.world.biome.NetherBiome;
import net.minecraft.world.biome.OceanBiome;
import net.minecraft.world.biome.PlainsBiome;
import net.minecraft.world.biome.RiverBiome;

public interface UEntities {
    EntityType<SpellbookEntity> SPELLBOOK = register("spellbook", FabricEntityTypeBuilder.create(EntityCategory.MISC, SpellbookEntity::new).size(EntityDimensions.changing(0.6f, 0.6f)));
    EntityType<SpellcastEntity> MAGIC_SPELL = register("magic_spell", FabricEntityTypeBuilder.create(EntityCategory.MISC, SpellcastEntity::new).size(EntityDimensions.changing(0.6F, 0.25F)));
    EntityType<CloudEntity> CLOUD = register("cloud", FabricEntityTypeBuilder.create(EntityCategory.CREATURE, CloudEntity::new));
    EntityType<WildCloudEntity> WILD_CLOUD = register("wild_cloud", FabricEntityTypeBuilder.create(EntityCategory.CREATURE, WildCloudEntity::new));
    EntityType<RacingCloudEntity> RACING_CLOUD = register("racing_cloud", FabricEntityTypeBuilder.create(EntityCategory.CREATURE, RacingCloudEntity::new));
    EntityType<ConstructionCloudEntity> CONSTRUCTION_CLOUD = register("construction_cloud", FabricEntityTypeBuilder.create(EntityCategory.CREATURE, ConstructionCloudEntity::new));

    EntityType<RainbowEntity> RAINBOW = register("rainbow", FabricEntityTypeBuilder.create(EntityCategory.AMBIENT, RainbowEntity::new));
    EntityType<RainbowEntity.Spawner> RAINBOW_SPAWNER = register("rainbow_spawner", FabricEntityTypeBuilder.create(EntityCategory.MISC, RainbowEntity.Spawner::new));

    EntityType<CuccoonEntity> CUCCOON = register("cuccoon", FabricEntityTypeBuilder.create(EntityCategory.MISC, CuccoonEntity::new).size(EntityDimensions.changing(1.5F, 1.6F)));

    EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", FabricEntityTypeBuilder.create(EntityCategory.AMBIENT, ButterflyEntity::new));

    EntityType<ProjectileEntity> THROWN_ITEM = register("thrown_item", FabricEntityTypeBuilder.<ProjectileEntity>create(EntityCategory.MISC, ProjectileEntity::new).trackable(100, 10));
    EntityType<SpearEntity> THROWN_SPEAR = register("thrown_spear", FabricEntityTypeBuilder.<SpearEntity>create(EntityCategory.MISC, SpearEntity::new).trackable(100, 10));

    static <T extends Entity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> builder) {
        return Registry.register(Registry.ENTITY_TYPE, new Identifier(Unicopia.MODID, name), builder.build());
    }

    static void bootstrap() {
        Registry.BIOME.forEach(biome -> {
            if (!(biome instanceof NetherBiome || biome instanceof EndBiome)) {
                addifAbsent(biome.getEntitySpawnList(EntityCategory.AMBIENT), biome instanceof OceanBiome ? WildCloudEntity.SPAWN_ENTRY_OCEAN : WildCloudEntity.SPAWN_ENTRY_LAND);
                addifAbsent(biome.getEntitySpawnList(EntityCategory.CREATURE), RainbowEntity.SPAWN_ENTRY);
            }

            if (biome instanceof PlainsBiome
                || biome instanceof RiverBiome
                || biome instanceof MountainsBiome
                || biome instanceof ForestBiome) {
                addifAbsent(biome.getEntitySpawnList(EntityCategory.AMBIENT), ButterflyEntity.SPAWN_ENTRY);
            }
        });
    }

    static <T> void addifAbsent(List<T> entries, T entry) {
        if (!entries.contains(entry)) {
            entries.add(entry);
        }
    }
}
