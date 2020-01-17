package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.client.render.entity.ButterflyEntityRenderer;
import com.minelittlepony.unicopia.client.render.entity.RenderCloud;
import com.minelittlepony.unicopia.client.render.entity.RenderCuccoon;
import com.minelittlepony.unicopia.client.render.entity.RenderRainbow;
import com.minelittlepony.unicopia.client.render.entity.RenderSpear;
import com.minelittlepony.unicopia.client.render.entity.RenderSpellbook;
import com.minelittlepony.unicopia.client.render.entity.SpellcastEntityRenderer;
import com.minelittlepony.unicopia.entity.AdvancedProjectileEntity;
import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.minelittlepony.unicopia.entity.CloudEntity;
import com.minelittlepony.unicopia.entity.EntityCuccoon;
import com.minelittlepony.unicopia.entity.EntitySpear;
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

public class UEntityRenderers {
    static void bootstrap() {
        EntityRendererRegistry.INSTANCE.register(CloudEntity.class, RenderCloud::new);
        EntityRendererRegistry.INSTANCE.register(SpellcastEntity.class, SpellcastEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(AdvancedProjectileEntity.class, (manager, context) -> new FlyingItemEntityRenderer<>(manager, MinecraftClient.getInstance().getItemRenderer()));
        EntityRendererRegistry.INSTANCE.register(SpellbookEntity.class, RenderSpellbook::new);
        EntityRendererRegistry.INSTANCE.register(RainbowEntity.class, RenderRainbow::new);
        EntityRendererRegistry.INSTANCE.register(ButterflyEntity.class, ButterflyEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(EntityCuccoon.class, RenderCuccoon::new);
        EntityRendererRegistry.INSTANCE.register(EntitySpear.class, RenderSpear::new);
    }
}
