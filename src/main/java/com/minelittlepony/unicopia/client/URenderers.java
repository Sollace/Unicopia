package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.block.*;
import com.minelittlepony.unicopia.client.particle.ChangelingMagicParticle;
import com.minelittlepony.unicopia.client.particle.CloudsEscapingParticle;
import com.minelittlepony.unicopia.client.particle.DiskParticle;
import com.minelittlepony.unicopia.client.particle.DustCloudParticle;
import com.minelittlepony.unicopia.client.particle.FloatingBubbleParticle;
import com.minelittlepony.unicopia.client.particle.FootprintParticle;
import com.minelittlepony.unicopia.client.particle.GroundPoundParticle;
import com.minelittlepony.unicopia.client.particle.HealthDrainParticle;
import com.minelittlepony.unicopia.client.particle.LightningBoltParticle;
import com.minelittlepony.unicopia.client.particle.MagicParticle;
import com.minelittlepony.unicopia.client.particle.RainboomParticle;
import com.minelittlepony.unicopia.client.particle.RainbowTrailParticle;
import com.minelittlepony.unicopia.client.particle.RaindropsParticle;
import com.minelittlepony.unicopia.client.particle.ShockwaveParticle;
import com.minelittlepony.unicopia.client.particle.SphereParticle;
import com.minelittlepony.unicopia.client.particle.SpiralParticle;
import com.minelittlepony.unicopia.client.particle.WindParticle;
import com.minelittlepony.unicopia.client.render.*;
import com.minelittlepony.unicopia.client.render.entity.*;
import com.minelittlepony.unicopia.client.render.item.BlockTintSource;
import com.minelittlepony.unicopia.client.render.item.UItemPredicates;
import com.minelittlepony.unicopia.client.render.shader.URenderPipelines;
import com.minelittlepony.unicopia.client.render.spell.SpellRendererFactory;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.particle.UParticles;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.fabricmc.fabric.api.client.rendering.v1.*;

import net.minecraft.block.Block;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleEffect;

public interface URenderers {

    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(UParticles.UNICORN_MAGIC, createFactory(MagicParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.CHANGELING_MAGIC, createFactory(ChangelingMagicParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.BUBBLE, createFactory(FloatingBubbleParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.RAIN_DROPS, createFactory(RaindropsParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.HEALTH_DRAIN, createFactory(HealthDrainParticle::create));
        ParticleFactoryRegistry.getInstance().register(UParticles.FOOTPRINT, createFactory(FootprintParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.SPIRAL, createFactory(SpiralParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.RAINBOOM_RING, RainboomParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.RAINBOOM_TRAIL, RainbowTrailParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.WIND, WindParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.SHOCKWAVE, ShockwaveParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.SPHERE, SphereParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.DISK, DiskParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.GROUND_POUND, GroundPoundParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.CLOUDS_ESCAPING, CloudsEscapingParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.LIGHTNING_BOLT, LightningBoltParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.DUST_CLOUD, DustCloudParticle::new);

        AccessoryFeatureRenderer.register(
                BraceletFeatureRenderer::new, AmuletFeatureRenderer::new, GlassesFeatureRenderer::new,
                WingsFeatureRenderer::new, HornFeatureRenderer::new, IcarusWingsFeatureRenderer::new, BatWingsFeatureRenderer::new,
                HeldEntityFeatureRenderer::new, DisguisedArmsFeatureRenderer::new
        );

        EntityRendererRegistry.register(UEntities.THROWN_ITEM, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.MUFFIN, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.MAGIC_BEAM, MagicBeamEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.BUTTERFLY, ButterflyEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.FLOATING_ARTEFACT, FloatingArtefactEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.CAST_SPELL, CastSpellEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.TWITTERMITE, FairyEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.SPELLBOOK, SpellbookEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.SOMBRA, SombraEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.CRYSTAL_SHARDS, CrystalShardsEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.STORM_CLOUD, StormCloudEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.AIR_BALLOON, AirBalloonEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.FRIENDLY_CREEPER, FriendlyCreeperEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.LOOT_BUG, LootBugEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.TENTACLE, TentacleEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.IGNOMINIOUS_BULB, IgnominiousBulbEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.SPECTER, SpecterEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.MIMIC, MimicEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.PALM_BOAT, ctx -> new CustomBoatEntityRenderer<>(ctx, UEntities.PALM_BOAT, false));
        EntityRendererRegistry.register(UEntities.PALM_CHEST_BOAT, ctx -> new CustomBoatEntityRenderer<>(ctx, UEntities.PALM_BOAT, true));
        EntityRendererRegistry.register(UEntities.LEVITATING_ITEM, LevitatingItemEntityRenderer::new);

        BlockEntityRendererFactories.register(UBlockEntities.WEATHER_VANE, WeatherVaneBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(UBlockEntities.FANCY_BED, CloudBedBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(UBlockEntities.CLOUD_CHEST, CloudChestBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(UBlockEntities.ITEM_JAR, ItemJarBlockEntityRenderer::new);

        ColorProviderRegistry.BLOCK.register(BlockTintSource::getColor, TintedBlock.REGISTRY.stream().toArray(Block[]::new));
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), UBlocks.TRANSLUCENT_BLOCKS.stream().toArray(Block[]::new));
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), UBlocks.SEMI_TRANSPARENT_BLOCKS.stream().toArray(Block[]::new));
        // for lava boats
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), Fluids.LAVA, Fluids.FLOWING_LAVA);
        LeavesAdditionsModel.bootstrap();

        UItemPredicates.bootstrap();
        SpellRendererFactory.bootstrap();
        URenderPipelines.bootstrap();
    }

    static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    interface ParticleSupplier<T extends ParticleEffect> {
        Particle get(T effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz);
    }
}
