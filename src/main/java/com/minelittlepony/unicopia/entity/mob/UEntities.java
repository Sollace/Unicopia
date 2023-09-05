package com.minelittlepony.unicopia.entity.mob;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.behaviour.EntityBehaviour;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;

public interface UEntities {
    EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, ButterflyEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));
    EntityType<MagicProjectileEntity> THROWN_ITEM = register("thrown_item", FabricEntityTypeBuilder.<MagicProjectileEntity>create(SpawnGroup.MISC, MagicProjectileEntity::new)
            .trackRangeBlocks(100)
            .trackedUpdateRate(2)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));
    EntityType<PhysicsBodyProjectileEntity> MUFFIN = register("muffin", FabricEntityTypeBuilder.<PhysicsBodyProjectileEntity>create(SpawnGroup.MISC, PhysicsBodyProjectileEntity::new)
            .trackRangeBlocks(100)
            .trackedUpdateRate(2)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));
    EntityType<MagicProjectileEntity> MAGIC_BEAM = register("magic_beam", FabricEntityTypeBuilder.<MagicProjectileEntity>create(SpawnGroup.MISC, MagicProjectileEntity::new)
            .trackRangeBlocks(100)
            .trackedUpdateRate(2)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));
    EntityType<FloatingArtefactEntity> FLOATING_ARTEFACT = register("floating_artefact", FabricEntityTypeBuilder.create(SpawnGroup.MISC, FloatingArtefactEntity::new)
            .trackRangeBlocks(200)
            .dimensions(EntityDimensions.fixed(1, 1)));
    EntityType<CastSpellEntity> CAST_SPELL = register("cast_spell", FabricEntityTypeBuilder.create(SpawnGroup.MISC, CastSpellEntity::new)
            .trackRangeBlocks(200)
            .dimensions(EntityDimensions.fixed(1, 0.4F)));
    EntityType<FairyEntity> TWITTERMITE = register("twittermite", FabricEntityTypeBuilder.create(SpawnGroup.MISC, FairyEntity::new)
            .trackRangeBlocks(200)
            .dimensions(EntityDimensions.fixed(0.1F, 0.1F)));
    EntityType<SpellbookEntity> SPELLBOOK = register("spellbook", FabricEntityTypeBuilder.create(SpawnGroup.MISC, SpellbookEntity::new)
            .trackRangeBlocks(200)
            .dimensions(EntityDimensions.fixed(0.9F, 0.5F)));
    EntityType<SombraEntity> SOMBRA = register("sombra", FabricEntityTypeBuilder.<SombraEntity>create(SpawnGroup.MONSTER, SombraEntity::new)
            .trackRangeBlocks(200)
            .dimensions(EntityDimensions.changing(2F, 4F)));
    EntityType<CrystalShardsEntity> CRYSTAL_SHARDS = register("crystal_shards", FabricEntityTypeBuilder.create(SpawnGroup.MISC, CrystalShardsEntity::new)
            .trackRangeBlocks(100)
            .dimensions(EntityDimensions.changing(1F, 1F)));
    EntityType<StormCloudEntity> STORM_CLOUD = register("storm_cloud", FabricEntityTypeBuilder.create(SpawnGroup.MISC, StormCloudEntity::new)
            .trackRangeBlocks(200)
            .dimensions(EntityDimensions.changing(20F, 20F)));
    EntityType<AirBalloonEntity> AIR_BALLOON = register("air_balloon", FabricEntityTypeBuilder.create(SpawnGroup.MISC, AirBalloonEntity::new)
            .trackRangeBlocks(1000)
            .dimensions(EntityDimensions.changing(2.5F, 0.1F)));

    static <T extends Entity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> builder) {
        EntityType<T> type = builder.build();
        return Registry.register(Registries.ENTITY_TYPE, Unicopia.id(name), type);
    }

    static void bootstrap() {
        FabricDefaultAttributeRegistry.register(BUTTERFLY, ButterflyEntity.createButterflyAttributes());
        FabricDefaultAttributeRegistry.register(SPELLBOOK, SpellbookEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(TWITTERMITE, FairyEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(AIR_BALLOON, FlyingEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(SOMBRA, SombraEntity.createMobAttributes());

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

        UTradeOffers.bootstrap();
        EntityBehaviour.bootstrap();
        UEntityAttributes.bootstrap();
        Paintings.bootstrap();
    }

    interface Paintings {
        private static void register(String id, int width, int height) {
            Registry.register(Registries.PAINTING_VARIANT, RegistryKey.of(RegistryKeys.PAINTING_VARIANT, Unicopia.id(id)), new PaintingVariant(16 * width, 16 * height));
        }

        static void bootstrap() {
            register("bloom", 2, 1);
            register("chicken", 2, 1);
            register("bells", 2, 1);

            register("crystal", 3, 3);
            register("harmony", 3, 3);

            register("equality", 2, 4);
            register("solar", 2, 4);
            register("lunar", 2, 4);
            register("platinum", 2, 4);
            register("hurricane", 2, 4);
            register("pudding", 2, 4);
            register("terra", 2, 4);
            register("equestria", 2, 4);

            register("blossom", 2, 3);
            register("shadow", 2, 3);
        }
    }
}
