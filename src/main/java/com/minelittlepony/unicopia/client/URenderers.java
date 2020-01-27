package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.client.particle.ParticleChangelingMagic;
import com.minelittlepony.unicopia.client.particle.ParticleDisk;
import com.minelittlepony.unicopia.client.particle.ParticleRaindrops;
import com.minelittlepony.unicopia.client.particle.ParticleSphere;
import com.minelittlepony.unicopia.client.particle.ParticleUnicornMagic;
import com.minelittlepony.unicopia.client.render.entity.ButterflyEntityRenderer;
import com.minelittlepony.unicopia.client.render.entity.RenderCloud;
import com.minelittlepony.unicopia.client.render.entity.RenderCuccoon;
import com.minelittlepony.unicopia.client.render.entity.RenderRainbow;
import com.minelittlepony.unicopia.client.render.entity.RenderSpear;
import com.minelittlepony.unicopia.client.render.entity.RenderSpellbook;
import com.minelittlepony.unicopia.client.render.entity.SpellcastEntityRenderer;
import com.minelittlepony.unicopia.entity.ProjectileEntity;
import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.minelittlepony.unicopia.entity.CloudEntity;
import com.minelittlepony.unicopia.entity.CuccoonEntity;
import com.minelittlepony.unicopia.entity.SpearEntity;
import com.minelittlepony.unicopia.entity.RainbowEntity;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.entity.WildCloudEntity;
import com.minelittlepony.util.collection.ListHelper;

import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.entity.EntityCategory;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.EndBiome;
import net.minecraft.world.biome.ForestBiome;
import net.minecraft.world.biome.MountainsBiome;
import net.minecraft.world.biome.NetherBiome;
import net.minecraft.world.biome.OceanBiome;
import net.minecraft.world.biome.PlainsBiome;
import net.minecraft.world.biome.RiverBiome;

public class URenderers {
    static void bootstrap() {
        EntityRendererRegistry.INSTANCE.register(CloudEntity.class, RenderCloud::new);
        EntityRendererRegistry.INSTANCE.register(SpellcastEntity.class, SpellcastEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(ProjectileEntity.class, (manager, context) -> new FlyingItemEntityRenderer<>(manager, MinecraftClient.getInstance().getItemRenderer()));
        EntityRendererRegistry.INSTANCE.register(SpellbookEntity.class, RenderSpellbook::new);
        EntityRendererRegistry.INSTANCE.register(RainbowEntity.class, RenderRainbow::new);
        EntityRendererRegistry.INSTANCE.register(ButterflyEntity.class, ButterflyEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(CuccoonEntity.class, RenderCuccoon::new);
        EntityRendererRegistry.INSTANCE.register(SpearEntity.class, RenderSpear::new);

        ParticleFactoryRegistry.instance().register(UParticles.UNICORN_MAGIC, ParticleUnicornMagic::new);
        ParticleFactoryRegistry.instance().register(UParticles.CHANGELING_MAGIC, ParticleChangelingMagic::new);
        ParticleFactoryRegistry.instance().register(UParticles.RAIN_DROPS, ParticleRaindrops::new);
        ParticleFactoryRegistry.instance().register(UParticles.SPHERE, ParticleSphere::new);
        ParticleFactoryRegistry.instance().register(UParticles.DISK, ParticleDisk::new);
    }
}
