package com.minelittlepony.unicopia.world.entity;

import java.util.List;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.EndBiome;
import net.minecraft.world.biome.ForestBiome;
import net.minecraft.world.biome.MountainsBiome;
import net.minecraft.world.biome.NetherWastesBiome;
import net.minecraft.world.biome.OceanBiome;
import net.minecraft.world.biome.PlainsBiome;
import net.minecraft.world.biome.RiverBiome;

public interface UEntities {
    EntityType<SpellbookEntity> SPELLBOOK = register("spellbook", FabricEntityTypeBuilder.create(SpawnGroup.MISC, SpellbookEntity::new)
            .dimensions(EntityDimensions.changing(0.6F, 0.6F)),
            LivingEntity.createLivingAttributes());
    EntityType<SpellcastEntity> MAGIC_SPELL = register("magic_spell", FabricEntityTypeBuilder.create(SpawnGroup.MISC, SpellcastEntity::new)
            .dimensions(EntityDimensions.changing(0.6F, 0.25F)),
            LivingEntity.createLivingAttributes());
    EntityType<CloudEntity> CLOUD = register("cloud", FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CloudEntity::new)
            .dimensions(CloudEntity.BASE_DIMENSIONS),
            LivingEntity.createLivingAttributes());
    EntityType<WildCloudEntity> WILD_CLOUD = register("wild_cloud", FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WildCloudEntity::new)
            .dimensions(CloudEntity.BASE_DIMENSIONS),
            LivingEntity.createLivingAttributes());
    EntityType<RacingCloudEntity> RACING_CLOUD = register("racing_cloud", FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RacingCloudEntity::new)
            .dimensions(CloudEntity.BASE_DIMENSIONS),
            LivingEntity.createLivingAttributes());
    EntityType<ConstructionCloudEntity> CONSTRUCTION_CLOUD = register("construction_cloud", FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ConstructionCloudEntity::new)
            .dimensions(CloudEntity.BASE_DIMENSIONS),
            LivingEntity.createLivingAttributes());

    EntityType<RainbowEntity> RAINBOW = register("rainbow", FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, RainbowEntity::new)
            .dimensions(EntityDimensions.fixed(1, 1)),
            LivingEntity.createLivingAttributes());

    EntityType<CucoonEntity> CUCOON = register("cucoon", FabricEntityTypeBuilder.create(SpawnGroup.MISC, CucoonEntity::new)
            .dimensions(EntityDimensions.changing(1.5F, 1.6F)),
            LivingEntity.createLivingAttributes());

    EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, ButterflyEntity::new)
            .dimensions(EntityDimensions.fixed(1, 1)),
            ButterflyEntity.createButterflyAttributes());

    EntityType<MagicProjectileEntity> THROWN_ITEM = register("thrown_item", FabricEntityTypeBuilder.<MagicProjectileEntity>create(SpawnGroup.MISC, MagicProjectileEntity::new)
            .trackable(100, 2)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));
    EntityType<SpearEntity> THROWN_SPEAR = register("thrown_spear", FabricEntityTypeBuilder.<SpearEntity>create(SpawnGroup.MISC, SpearEntity::new)
            .trackable(100, 2)
            .dimensions(EntityDimensions.fixed(0.6F, 0.6F)));

    static <T extends LivingEntity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> builder, DefaultAttributeContainer.Builder attributes) {
        EntityType<T> type = builder.build();
        FabricDefaultAttributeRegistry.register(type, attributes);
        return Registry.register(Registry.ENTITY_TYPE, new Identifier("unicopia", name), type);
    }

    static <T extends Entity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> builder) {
        EntityType<T> type = builder.build();
        return Registry.register(Registry.ENTITY_TYPE, new Identifier("unicopia", name), type);
    }

    static void bootstrap() {
        Registry.BIOME.forEach(biome -> {
            if (!(biome instanceof NetherWastesBiome || biome instanceof EndBiome)) {
                addifAbsent(biome.getEntitySpawnList(SpawnGroup.AMBIENT), biome instanceof OceanBiome ? WildCloudEntity.SPAWN_ENTRY_OCEAN : WildCloudEntity.SPAWN_ENTRY_LAND);
                addifAbsent(biome.getEntitySpawnList(SpawnGroup.CREATURE), RainbowEntity.SPAWN_ENTRY);
            }

            if (biome instanceof PlainsBiome
                || biome instanceof RiverBiome
                || biome instanceof MountainsBiome
                || biome instanceof ForestBiome) {
                addifAbsent(biome.getEntitySpawnList(SpawnGroup.AMBIENT), ButterflyEntity.SPAWN_ENTRY);
            }
        });
    }

    static <T> void addifAbsent(List<T> entries, T entry) {
        if (!entries.contains(entry)) {
            entries.add(entry);
        }
    }
}
