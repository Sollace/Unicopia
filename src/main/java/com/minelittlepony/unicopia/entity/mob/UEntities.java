package com.minelittlepony.unicopia.entity.mob;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.behaviour.EntityBehaviour;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.Heightmap.Type;

public interface UEntities {
    EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", FabricEntityType.Builder.createMob(ButterflyEntity::new, SpawnGroup.AMBIENT, builder -> builder
                .spawnRestriction(SpawnLocationTypes.UNRESTRICTED, Type.MOTION_BLOCKING_NO_LEAVES, ButterflyEntity::canSpawn)
                .defaultAttributes(ButterflyEntity::createButterflyAttributes))
            .spawnableFarFromPlayer()
            .dimensions(0.25F, 0.25F)
            .eyeHeight(0.125F));
    EntityType<MagicProjectileEntity> THROWN_ITEM = register("thrown_item", EntityType.Builder.<MagicProjectileEntity>create(MagicProjectileEntity::new, SpawnGroup.MISC)
            .maxTrackingRange(100)
            .disableSummon()
            .trackingTickInterval(2)
            .dimensions(0.25F, 0.25F));
    EntityType<PhysicsBodyProjectileEntity> MUFFIN = register("muffin", EntityType.Builder.<PhysicsBodyProjectileEntity>create((type, world) -> new PhysicsBodyProjectileEntity(type, world, UItems.MUFFIN.getDefaultStack()), SpawnGroup.MISC)
            .maxTrackingRange(100)
            .disableSummon()
            .trackingTickInterval(2)
            .dimensions(0.25F, 0.25F));
    EntityType<MagicBeamEntity> MAGIC_BEAM = register("magic_beam", EntityType.Builder.<MagicBeamEntity>create(MagicBeamEntity::new, SpawnGroup.MISC)
            .maxTrackingRange(100)
            .disableSummon()
            .trackingTickInterval(2)
            .alwaysUpdateVelocity(true)
            .dimensions(0.25F, 0.25F));
    EntityType<FloatingArtefactEntity> FLOATING_ARTEFACT = register("floating_artefact", EntityType.Builder.create(FloatingArtefactEntity::new, SpawnGroup.MISC)
            .maxTrackingRange(200)
            .disableSummon()
            .dimensions(1, 1));
    EntityType<CastSpellEntity> CAST_SPELL = register("cast_spell", EntityType.Builder.<CastSpellEntity>create(CastSpellEntity::new, SpawnGroup.MISC)
            .maxTrackingRange(200)
            .disableSummon()
            .dimensions(4, 4));
    EntityType<FairyEntity> TWITTERMITE = register("twittermite", FabricEntityType.Builder.createMob(FairyEntity::new, SpawnGroup.MISC, builder -> builder
                .defaultAttributes(FairyEntity::createMobAttributes))
            .maxTrackingRange(200)
            .dimensions(0.1F, 0.1F));
    EntityType<FriendlyCreeperEntity> FRIENDLY_CREEPER = register("friendly_creeper", FabricEntityType.Builder.createMob(FriendlyCreeperEntity::new, SpawnGroup.MISC, builder -> builder
                .defaultAttributes(FriendlyCreeperEntity::createCreeperAttributes))
            .maxTrackingRange(8)
            .disableSummon()
            .dimensions(0.6f, 1.7f));
    EntityType<SpellbookEntity> SPELLBOOK = register("spellbook", FabricEntityType.Builder.createMob(SpellbookEntity::new, SpawnGroup.MISC, builder -> builder
                .defaultAttributes(SpellbookEntity::createMobAttributes))
            .maxTrackingRange(200)
            .dimensions(0.9F, 0.5F));
    EntityType<SombraEntity> SOMBRA = register("sombra", FabricEntityType.Builder.<SombraEntity>createMob(SombraEntity::new, SpawnGroup.MONSTER, builder -> builder
                .defaultAttributes(SombraEntity::createMobAttributes))
            .maxTrackingRange(200)
            .dimensions(2F, 4F));
    EntityType<CrystalShardsEntity> CRYSTAL_SHARDS = register("crystal_shards", EntityType.Builder.create(CrystalShardsEntity::new, SpawnGroup.MISC)
            .maxTrackingRange(100)
            .dimensions(1F, 1F));
    EntityType<StormCloudEntity> STORM_CLOUD = register("storm_cloud", EntityType.Builder.create(StormCloudEntity::new, SpawnGroup.MISC)
            .maxTrackingRange(200)
            .dimensions(20F, 20F));
    EntityType<AirBalloonEntity> AIR_BALLOON = register("air_balloon", FabricEntityType.Builder.createMob(AirBalloonEntity::new, SpawnGroup.MISC, builder -> builder
                .defaultAttributes(FlyingEntity::createMobAttributes))
            .maxTrackingRange(1000)
            .dimensions(2.5F, 0.1F));
    EntityType<LootBugEntity> LOOT_BUG = register("loot_bug", FabricEntityType.Builder.createMob(LootBugEntity::new, SpawnGroup.MONSTER, builder -> builder
                .defaultAttributes(LootBugEntity::createSilverfishAttributes))
            .maxTrackingRange(8)
            .dimensions(0.8F, 0.6F));
    EntityType<TentacleEntity> TENTACLE = register("ignominious_vine", EntityType.Builder.<TentacleEntity>create(TentacleEntity::new, SpawnGroup.MISC)
            .maxTrackingRange(8)
            .dimensions(0.8F, 0.8F));
    EntityType<IgnominiousBulbEntity> IGNOMINIOUS_BULB = register("ignominious_bulb", FabricEntityType.Builder.<IgnominiousBulbEntity>createMob(IgnominiousBulbEntity::new, SpawnGroup.MISC, builder -> builder
                .defaultAttributes(IgnominiousBulbEntity::createMobAttributes))
            .maxTrackingRange(8)
            .dimensions(3, 2));
    EntityType<SpecterEntity> SPECTER = register("specter", FabricEntityType.Builder.createMob(SpecterEntity::new, SpawnGroup.MONSTER, builder -> builder
                .spawnRestriction(SpawnLocationTypes.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark)
                .defaultAttributes(SpecterEntity::createAttributes))
            .makeFireImmune()
            .spawnableFarFromPlayer()
            .dimensions(1, 2));
    EntityType<MimicEntity> MIMIC = register("mimic", FabricEntityType.Builder.createMob(MimicEntity::new, SpawnGroup.MONSTER, builder -> builder
                .defaultAttributes(MimicEntity::createMobAttributes))
            .makeFireImmune()
            .disableSummon()
            .dimensions(0.875F, 0.875F));

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> type = builder.build();
        return Registry.register(Registries.ENTITY_TYPE, Unicopia.id(name), type);
    }

    static void bootstrap() {
        if (!Unicopia.getConfig().disableButterflySpawning.get()) {
            final Predicate<BiomeSelectionContext> butterflySpawnable = BiomeSelectors.foundInOverworld()
                    .and(ctx -> ctx.getBiome().hasPrecipitation() && ctx.getBiome().getTemperature() > 0.15F);

            BiomeModifications.addSpawn(butterflySpawnable.and(
                        BiomeSelectors.tag(BiomeTags.IS_RIVER)
                    .or(BiomeSelectors.tag(BiomeTags.IS_FOREST))
                    .or(BiomeSelectors.tag(BiomeTags.IS_HILL))
            ), SpawnGroup.AMBIENT, BUTTERFLY, 3, 3, 12);
            BiomeModifications.addSpawn(butterflySpawnable.and(
                        BiomeSelectors.tag(BiomeTags.IS_JUNGLE)
                    .or(BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN))
            ), SpawnGroup.AMBIENT, BUTTERFLY, 7, 5, 19);
        }

        BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityType.ZOMBIE), SpawnGroup.MONSTER, SPECTER, 2, 1, 2);

        UTradeOffers.bootstrap();
        EntityBehaviour.bootstrap();
        UEntityAttributes.bootstrap();
    }
}
