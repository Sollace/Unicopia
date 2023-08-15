package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.*;
import com.minelittlepony.unicopia.client.particle.ChangelingMagicParticle;
import com.minelittlepony.unicopia.client.particle.CloudsEscapingParticle;
import com.minelittlepony.unicopia.client.particle.DiskParticle;
import com.minelittlepony.unicopia.client.particle.GroundPoundParticle;
import com.minelittlepony.unicopia.client.particle.HealthDrainParticle;
import com.minelittlepony.unicopia.client.particle.LightningBoltParticle;
import com.minelittlepony.unicopia.client.particle.MagicParticle;
import com.minelittlepony.unicopia.client.particle.RainboomParticle;
import com.minelittlepony.unicopia.client.particle.RainbowTrailParticle;
import com.minelittlepony.unicopia.client.particle.RaindropsParticle;
import com.minelittlepony.unicopia.client.particle.RunesParticle;
import com.minelittlepony.unicopia.client.particle.SphereParticle;
import com.minelittlepony.unicopia.client.render.*;
import com.minelittlepony.unicopia.client.render.entity.*;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.item.ChameleonItem;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.particle.UParticles;
import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Identifier;

public interface URenderers {
    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(UParticles.UNICORN_MAGIC, createFactory(MagicParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.CHANGELING_MAGIC, createFactory(ChangelingMagicParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.RAIN_DROPS, createFactory(RaindropsParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.HEALTH_DRAIN, createFactory(HealthDrainParticle::create));
        ParticleFactoryRegistry.getInstance().register(UParticles.RAINBOOM_RING, RainboomParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.RAINBOOM_TRAIL, RainbowTrailParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.MAGIC_RUNES, RunesParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.SPHERE, SphereParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.DISK, DiskParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.GROUND_POUND, GroundPoundParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.CLOUDS_ESCAPING, CloudsEscapingParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.LIGHTNING_BOLT, LightningBoltParticle::new);

        AccessoryFeatureRenderer.register(BraceletFeatureRenderer::new);
        AccessoryFeatureRenderer.register(AmuletFeatureRenderer::new);
        AccessoryFeatureRenderer.register(WingsFeatureRenderer::new);
        AccessoryFeatureRenderer.register(HornFeatureRenderer::new);
        AccessoryFeatureRenderer.register(IcarusWingsFeatureRenderer::new);
        AccessoryFeatureRenderer.register(BatWingsFeatureRenderer::new);
        AccessoryFeatureRenderer.register(GlassesFeatureRenderer::new);
        AccessoryFeatureRenderer.register(HeldEntityFeatureRenderer::new);

        EntityRendererRegistry.register(UEntities.THROWN_ITEM, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.MUFFIN, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.MAGIC_BEAM, MagicBeamEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.BUTTERFLY, ButterflyEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.FLOATING_ARTEFACT, FloatingArtefactEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.CAST_SPELL, CastSpellEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.TWITTERMITE, FairyEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.SPELLBOOK, SpellbookEntityRenderer::new);
        EntityRendererRegistry.register(UEntities.AIR_BALLOON, AirBalloonEntityRenderer::new);

        BlockEntityRendererFactories.register(UBlockEntities.WEATHER_VANE, WeatherVaneBlockEntityRenderer::new);

        ColorProviderRegistry.ITEM.register((stack, i) -> i > 0 ? -1 : ((DyeableItem)stack.getItem()).getColor(stack), UItems.FRIENDSHIP_BRACELET);
        BuiltinItemRendererRegistry.INSTANCE.register(UItems.FILLED_JAR, (stack, mode, matrices, vertexConsumers, light, overlay) -> {

            ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();

            ChameleonItem item = (ChameleonItem)stack.getItem();

            // Reset stuff done in the beforelands
            matrices.pop();

            if (mode == ModelTransformationMode.GUI) {
                DiffuseLighting.disableGuiDepthLighting();
            }

            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            ClientWorld world = MinecraftClient.getInstance().world;

            if (item.hasAppearance(stack)) {
                matrices.push();
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.translate(0.0125, 0.1, 0);
                renderer.renderItem(item.getAppearanceStack(stack), mode, light, overlay, matrices, immediate, world, 0);
                matrices.pop();
            }
            renderer.renderItem(item.createAppearanceStack(stack, UItems.EMPTY_JAR), mode, light, OverlayTexture.DEFAULT_UV, matrices, immediate, world, 0);
            immediate.draw();

            if (mode == ModelTransformationMode.GUI) {
                DiffuseLighting.enableGuiDepthLighting();
            }
            matrices.push();

        });
        PolearmRenderer.register(UItems.WOODEN_POLEARM);
        PolearmRenderer.register(UItems.STONE_POLEARM);
        PolearmRenderer.register(UItems.IRON_POLEARM);
        PolearmRenderer.register(UItems.GOLDEN_POLEARM);
        PolearmRenderer.register(UItems.DIAMOND_POLEARM);
        PolearmRenderer.register(UItems.NETHERITE_POLEARM);
        ModelPredicateProviderRegistry.register(UItems.GEMSTONE, new Identifier("affinity"), (stack, world, entity, seed) -> {
            return EnchantableItem.isEnchanted(stack) ? EnchantableItem.getSpellKey(stack).getAffinity().getAlignment() : 0;
        });
        ColorProviderRegistry.ITEM.register((stack, i) -> {
            return i > 0 || !EnchantableItem.isEnchanted(stack) ? -1 : EnchantableItem.getSpellKey(stack).getColor();
        }, UItems.GEMSTONE);
        ColorProviderRegistry.ITEM.register((stack, i) -> {
            if (i == 1 && EnchantableItem.isEnchanted(stack)) {
                return EnchantableItem.getSpellKey(stack).getColor();
            }
            return -1;
        }, UItems.MAGIC_STAFF);

        BlockColorProvider tintedProvider = (state, view, pos, color) -> {
            if (view == null || pos == null) {
                color = FoliageColors.getDefaultColor();
            } else {
                color = BiomeColors.getFoliageColor(view, pos);
            }

            if (state.getBlock() instanceof TintedBlock block) {
                return block.getTint(state, view, pos, color);
            }

            return color;
        };

        ColorProviderRegistry.BLOCK.register(tintedProvider, TintedBlock.REGISTRY.stream().toArray(Block[]::new));
        ColorProviderRegistry.ITEM.register((stack, i) -> {
            return tintedProvider.getColor(Block.getBlockFromItem(stack.getItem()).getDefaultState(), null, null, i);
        }, TintedBlock.REGISTRY.stream().map(Block::asItem).filter(i -> i != Items.AIR).toArray(Item[]::new));

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), UBlocks.TRANSLUCENT_BLOCKS.stream().toArray(Block[]::new));
        // for lava boats
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), Fluids.LAVA, Fluids.FLOWING_LAVA);

        TerraformBoatClientHelper.registerModelLayers(Unicopia.id("palm"), false);
    }

    static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    interface ParticleSupplier<T extends ParticleEffect> {
        Particle get(T effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz);
    }
}
