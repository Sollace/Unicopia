package com.minelittlepony.unicopia.world.client;

import com.minelittlepony.unicopia.world.block.UBlocks;
import com.minelittlepony.unicopia.world.client.gui.UScreens;
import com.minelittlepony.unicopia.world.client.render.ButterflyEntityRenderer;
import com.minelittlepony.unicopia.world.client.render.CloudEntityRenderer;
import com.minelittlepony.unicopia.world.client.render.CucoonEntityRenderer;
import com.minelittlepony.unicopia.world.client.render.RainbowEntityRenderer;
import com.minelittlepony.unicopia.world.client.render.SpearEntityRenderer;
import com.minelittlepony.unicopia.world.client.render.SpellbookEntityRender;
import com.minelittlepony.unicopia.world.client.render.SpellcastEntityRenderer;
import com.minelittlepony.unicopia.world.entity.UEntities;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public interface UWorldClient {
    static void bootstrap() {
        EntityRendererRegistry.INSTANCE.register(UEntities.CLOUD, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.WILD_CLOUD, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.CONSTRUCTION_CLOUD, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.RACING_CLOUD, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.MAGIC_SPELL, SpellcastEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.THROWN_ITEM, (manager, context) -> new FlyingItemEntityRenderer<>(manager, context.getItemRenderer()));
        EntityRendererRegistry.INSTANCE.register(UEntities.SPELLBOOK, SpellbookEntityRender::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.RAINBOW, RainbowEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.BUTTERFLY, ButterflyEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.CUCOON, CucoonEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.THROWN_SPEAR, SpearEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
                UBlocks.ENCHANTED_TORCH, UBlocks.ENCHANTED_WALL_TORCH,
                UBlocks.BAKERY_DOOR, UBlocks.LIBRARY_DOOR, UBlocks.MISTED_GLASS_DOOR, UBlocks.DIAMOND_DOOR,
                UBlocks.APPLE_SAPLING, UBlocks.ALFALFA_CROPS,
                UBlocks.TOMATO_PLANT, UBlocks.CLOUDSDALE_TOMATO_PLANT
        );
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
                UBlocks.CLOUD_ANVIL, UBlocks.CLOUD_FARMLAND,
                UBlocks.CLOUD_BLOCK, UBlocks.CLOUD_SLAB, UBlocks.CLOUD_STAIRS,
                UBlocks.ENCHANTED_CLOUD_BLOCK, UBlocks.ENCHANTED_CLOUD_SLAB, UBlocks.ENCHANTED_CLOUD_STAIRS,

                UBlocks.SLIME_DRIP, UBlocks.SLIME_LAYER
        );
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                UBlocks.APPLE_LEAVES
        );

        UScreens.bootstrap();
    }
}
