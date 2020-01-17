package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.minelittlepony.unicopia.entity.CloudEntity;
import com.minelittlepony.unicopia.entity.AdvancedProjectileEntity;
import com.minelittlepony.unicopia.entity.ConstructionCloudEntity;
import com.minelittlepony.unicopia.entity.EntityCuccoon;
import com.minelittlepony.unicopia.entity.EntityRacingCloud;
import com.minelittlepony.unicopia.entity.RainbowEntity;
import com.minelittlepony.unicopia.entity.EntitySpear;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.util.collection.ListHelper;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
import com.minelittlepony.unicopia.entity.WildCloudEntity;

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

public class UEntities {
    public static final EntityType<SpellbookEntity> SPELLBOOK = register("spellbook", EntityType.Builder.create(SpellbookEntity::new, EntityCategory.MISC).setDimensions(0.6f, 0.6f));
    public static final EntityType<SpellcastEntity> MAGIC_SPELL = register("magic_spell", EntityType.Builder.create(SpellcastEntity::new, EntityCategory.MISC).setDimensions(0.6F, 0.25F));
    public static final EntityType<CloudEntity> CLOUD = register("cloud", EntityType.Builder.create(CloudEntity::new, EntityCategory.CREATURE));
    public static final EntityType<WildCloudEntity> WILD_CLOUD = register("wild_cloud", EntityType.Builder.create(WildCloudEntity::new, EntityCategory.CREATURE));
    public static final EntityType<EntityRacingCloud> RACING_CLOUD = register("racing_cloud", EntityType.Builder.create(EntityRacingCloud::new, EntityCategory.CREATURE));
    public static final EntityType<ConstructionCloudEntity> CONSTRUCTION_CLOUD = register("construction_cloud", EntityType.Builder.create(ConstructionCloudEntity::new, EntityCategory.CREATURE));

    public static final EntityType<RainbowEntity> RAINBOW = register("rainbow", EntityType.Builder.create(RainbowEntity::new, EntityCategory.AMBIENT));
    public static final EntityType<RainbowEntity.Spawner> RAINBOW_SPAWNER = register("rainbow_spawner", EntityType.Builder.create(RainbowEntity.Spawner::new, EntityCategory.MISC));

    public static final EntityType<EntityCuccoon> CUCCOON = register("cuccoon", EntityType.Builder.create(EntityCuccoon::new, EntityCategory.MISC).setDimensions(0.6f, 0.6f));

    public static final EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", EntityType.Builder.create(ButterflyEntity::new, EntityCategory.AMBIENT));

    public static final EntityType<AdvancedProjectileEntity> THROWN_ITEM = register("thrown_item", EntityType.Builder.create(AdvancedProjectileEntity::new, EntityCategory.MISC));
    public static final EntityType<EntitySpear> THROWN_SPEAR = register("thrown_spear", EntityType.Builder.create(EntitySpear::new, EntityCategory.MISC));

    //builder.creature(CloudEntity.class, "cloud").withEgg(0x4169e1, 0x7fff00),
    //builder.creature(ButterflyEntity.class, "butterfly").withEgg(0x222200, 0xaaeeff),
    // builder.projectile(AdvancedProjectileEntity.class, "thrown_item", 100, 10),
    // builder.projectile(EntitySpear.class, "spear", 100, 10)

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
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
