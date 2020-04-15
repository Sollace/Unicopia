package com.minelittlepony.unicopia.redux.client;

import com.minelittlepony.unicopia.core.UParticles;
import com.minelittlepony.unicopia.core.client.particle.ChangelingMagicParticle;
import com.minelittlepony.unicopia.core.client.particle.DiskParticle;
import com.minelittlepony.unicopia.core.client.particle.RaindropsParticle;
import com.minelittlepony.unicopia.core.client.particle.SphereParticle;
import com.minelittlepony.unicopia.core.client.particle.MagicParticle;
import com.minelittlepony.unicopia.core.util.collection.ListHelper;
import com.minelittlepony.unicopia.redux.client.render.ButterflyEntityRenderer;
import com.minelittlepony.unicopia.redux.client.render.RenderCloud;
import com.minelittlepony.unicopia.redux.client.render.RenderCuccoon;
import com.minelittlepony.unicopia.redux.client.render.RenderRainbow;
import com.minelittlepony.unicopia.redux.client.render.RenderSpear;
import com.minelittlepony.unicopia.redux.client.render.RenderSpellbook;
import com.minelittlepony.unicopia.redux.client.render.SpellcastEntityRenderer;
import com.minelittlepony.unicopia.redux.entity.ButterflyEntity;
import com.minelittlepony.unicopia.redux.entity.CloudEntity;
import com.minelittlepony.unicopia.redux.entity.CuccoonEntity;
import com.minelittlepony.unicopia.redux.entity.ProjectileEntity;
import com.minelittlepony.unicopia.redux.entity.RainbowEntity;
import com.minelittlepony.unicopia.redux.entity.SpearEntity;
import com.minelittlepony.unicopia.redux.entity.SpellbookEntity;
import com.minelittlepony.unicopia.redux.entity.SpellcastEntity;
import com.minelittlepony.unicopia.redux.entity.WildCloudEntity;

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

public interface URenderers {
    static void bootstrap() {
        EntityRendererRegistry.INSTANCE.register(CloudEntity.class, RenderCloud::new);
        EntityRendererRegistry.INSTANCE.register(SpellcastEntity.class, SpellcastEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(ProjectileEntity.class, (manager, context) -> new FlyingItemEntityRenderer<>(manager, MinecraftClient.getInstance().getItemRenderer()));
        EntityRendererRegistry.INSTANCE.register(SpellbookEntity.class, RenderSpellbook::new);
        EntityRendererRegistry.INSTANCE.register(RainbowEntity.class, RenderRainbow::new);
        EntityRendererRegistry.INSTANCE.register(ButterflyEntity.class, ButterflyEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(CuccoonEntity.class, RenderCuccoon::new);
        EntityRendererRegistry.INSTANCE.register(SpearEntity.class, RenderSpear::new);

        ParticleFactoryRegistry.instance().register(UParticles.UNICORN_MAGIC, MagicParticle::new);
        ParticleFactoryRegistry.instance().register(UParticles.CHANGELING_MAGIC, ChangelingMagicParticle::new);
        ParticleFactoryRegistry.instance().register(UParticles.RAIN_DROPS, RaindropsParticle::new);
        ParticleFactoryRegistry.instance().register(UParticles.SPHERE, SphereParticle::new);
        ParticleFactoryRegistry.instance().register(UParticles.DISK, DiskParticle::new);
    }
}
