package com.minelittlepony.unicopia.redux;

import com.minelittlepony.unicopia.core.util.collection.ListHelper;
import com.minelittlepony.unicopia.redux.entity.ButterflyEntity;
import com.minelittlepony.unicopia.redux.entity.CloudEntity;
import com.minelittlepony.unicopia.redux.entity.ConstructionCloudEntity;
import com.minelittlepony.unicopia.redux.entity.CuccoonEntity;
import com.minelittlepony.unicopia.redux.entity.ProjectileEntity;
import com.minelittlepony.unicopia.redux.entity.RacingCloudEntity;
import com.minelittlepony.unicopia.redux.entity.RainbowEntity;
import com.minelittlepony.unicopia.redux.entity.SpearEntity;
import com.minelittlepony.unicopia.redux.entity.SpellbookEntity;
import com.minelittlepony.unicopia.redux.entity.SpellcastEntity;
import com.minelittlepony.unicopia.redux.entity.WildCloudEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.EndBiome;
import net.minecraft.world.biome.ForestBiome;
import net.minecraft.world.biome.MountainsBiome;
import net.minecraft.world.biome.NetherBiome;
import net.minecraft.world.biome.OceanBiome;
import net.minecraft.world.biome.PlainsBiome;
import net.minecraft.world.biome.RiverBiome;

public interface UEntities {
    EntityType<SpellbookEntity> SPELLBOOK = register("spellbook", EntityType.Builder.create(SpellbookEntity::new, EntityCategory.MISC).setDimensions(0.6f, 0.6f));
    EntityType<SpellcastEntity> MAGIC_SPELL = register("magic_spell", EntityType.Builder.create(SpellcastEntity::new, EntityCategory.MISC).setDimensions(0.6F, 0.25F));
    EntityType<CloudEntity> CLOUD = register("cloud", EntityType.Builder.create(CloudEntity::new, EntityCategory.CREATURE));
    EntityType<WildCloudEntity> WILD_CLOUD = register("wild_cloud", EntityType.Builder.create(WildCloudEntity::new, EntityCategory.CREATURE));
    EntityType<RacingCloudEntity> RACING_CLOUD = register("racing_cloud", EntityType.Builder.create(RacingCloudEntity::new, EntityCategory.CREATURE));
    EntityType<ConstructionCloudEntity> CONSTRUCTION_CLOUD = register("construction_cloud", EntityType.Builder.create(ConstructionCloudEntity::new, EntityCategory.CREATURE));

    EntityType<RainbowEntity> RAINBOW = register("rainbow", EntityType.Builder.create(RainbowEntity::new, EntityCategory.AMBIENT));
    EntityType<RainbowEntity.Spawner> RAINBOW_SPAWNER = register("rainbow_spawner", EntityType.Builder.create(RainbowEntity.Spawner::new, EntityCategory.MISC));

    EntityType<CuccoonEntity> CUCCOON = register("cuccoon", EntityType.Builder.create(CuccoonEntity::new, EntityCategory.MISC).setDimensions(1.5F, 1.6F));

    EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", EntityType.Builder.create(ButterflyEntity::new, EntityCategory.AMBIENT));

    EntityType<ProjectileEntity> THROWN_ITEM = register("thrown_item", EntityType.Builder.create(ProjectileEntity::new, EntityCategory.MISC));
    EntityType<SpearEntity> THROWN_SPEAR = register("thrown_spear", EntityType.Builder.create(SpearEntity::new, EntityCategory.MISC));

    //builder.creature(CloudEntity.class, "cloud").withEgg(0x4169e1, 0x7fff00),
    //builder.creature(ButterflyEntity.class, "butterfly").withEgg(0x222200, 0xaaeeff),
    // builder.projectile(ProjectileEntity.class, "thrown_item", 100, 10),
    // builder.projectile(SpearEntity.class, "spear", 100, 10)

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registry.ENTITY_TYPE, name, builder.build(name));
    }

    static void bootstrap() {
        Registry.BIOME.forEach(biome -> {
            if (!(biome instanceof NetherBiome || biome instanceof EndBiome)) {
                ListHelper.addifAbsent(biome.getEntitySpawnList(EntityCategory.AMBIENT), biome instanceof OceanBiome ? WildCloudEntity.SPAWN_ENTRY_OCEAN : WildCloudEntity.SPAWN_ENTRY_LAND);
                ListHelper.addifAbsent(biome.getEntitySpawnList(EntityCategory.CREATURE), RainbowEntity.SPAWN_ENTRY);
            }

            if (biome instanceof PlainsBiome
                || biome instanceof RiverBiome
                || biome instanceof MountainsBiome
                || biome instanceof ForestBiome) {
                ListHelper.addifAbsent(biome.getEntitySpawnList(EntityCategory.AMBIENT), ButterflyEntity.SPAWN_ENTRY);
            }
        });
    }
}
