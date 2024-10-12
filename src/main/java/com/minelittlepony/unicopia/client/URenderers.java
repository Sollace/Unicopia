package com.minelittlepony.unicopia.client;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.*;
import com.minelittlepony.unicopia.block.cloud.CloudChestBlock;
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
import com.minelittlepony.unicopia.client.render.shader.UShaders;
import com.minelittlepony.unicopia.client.render.spell.SpellRendererFactory;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.FancyBedItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.Appearance;
import com.minelittlepony.unicopia.item.component.BalloonDesignComponent;
import com.minelittlepony.unicopia.item.component.BufferflyVariantComponent;
import com.minelittlepony.unicopia.particle.UParticles;
import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.FoliageColors;

public interface URenderers {
    BlockEntity CHEST_RENDER_ENTITY = new CloudChestBlock.TileData(BlockPos.ORIGIN, UBlocks.CLOUD_CHEST.getDefaultState());

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

        BlockEntityRendererFactories.register(UBlockEntities.WEATHER_VANE, WeatherVaneBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(UBlockEntities.FANCY_BED, CloudBedBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(UBlockEntities.CLOUD_CHEST, CloudChestBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(UBlockEntities.ITEM_JAR, ItemJarBlockEntityRenderer::new);

        register(URenderers::renderJarItem, UItems.FILLED_JAR);
        register(URenderers::renderBedItem, UItems.CLOTH_BED, UItems.CLOUD_BED);
        register(URenderers::renderChestItem, UBlocks.CLOUD_CHEST.asItem());
        PolearmRenderer.register(UItems.WOODEN_POLEARM, UItems.STONE_POLEARM, UItems.IRON_POLEARM, UItems.GOLDEN_POLEARM, UItems.DIAMOND_POLEARM, UItems.NETHERITE_POLEARM);
        ModelPredicateProviderRegistry.register(UItems.GEMSTONE, Identifier.ofVanilla("affinity"), (stack, world, entity, seed) -> EnchantableItem.getSpellKey(stack).getAffinity().getAlignment());
        ModelPredicateProviderRegistry.register(UItems.GEMSTONE, Identifier.ofVanilla("shape"), (stack, world, entity, seed) -> EnchantableItem.getSpellKey(stack).getGemShape().getId());
        ModelPredicateProviderRegistry.register(UItems.ROCK_CANDY, Identifier.ofVanilla("count"), (stack, world, entity, seed) -> stack.getCount() / (float)stack.getMaxCount());
        ModelPredicateProviderRegistry.register(UItems.BUTTERFLY, Identifier.ofVanilla("variant"), (stack, world, entity, seed) -> {
            // float operations do be weird like
            return BufferflyVariantComponent.get(stack).variant().ordinal() * (1F / ButterflyEntity.Variant.VALUES.length);
        });
        ModelPredicateProviderRegistry.register(UItems.GIANT_BALLOON, Identifier.ofVanilla("design"), (stack, world, entity, seed) -> {
            // float operations do be weird like
            return BalloonDesignComponent.get(stack).design().ordinal() * (1F / AirBalloonEntity.BalloonDesign.VALUES.length);
        });
        ModelPredicateProviderRegistry.register(Unicopia.id("zap_cycle"), new ClampedModelPredicateProvider() {
            private double targetAngle;
            private double lastAngle;
            private long lastTick;

            @Override
            public float unclampedCall(ItemStack stack, ClientWorld world, @Nullable LivingEntity holder, int seed) {
                @Nullable
                Entity entity = holder != null ? holder : stack.getHolder();
                if (entity == null) {
                    return 0;
                }
                if (world == null && entity.getWorld() instanceof ClientWorld cw) {
                    world = cw;
                }

                if (world == null) {
                    return 0;
                }
                if (world.getTime() != lastTick) {
                    targetAngle = world.getDimension().natural() ? UnicopiaClient.getInstance().getZapAppleStage().getCycleProgress(world) : Math.random();
                    lastAngle = lastAngle + (targetAngle - lastAngle) * 0.1;
                }

                return (float)lastAngle;
            }
        });

        ColorProviderRegistry.BLOCK.register(URenderers::getTintedBlockColor, TintedBlock.REGISTRY.stream().toArray(Block[]::new));
        ColorProviderRegistry.ITEM.register((stack, i) -> getTintedBlockColor(Block.getBlockFromItem(stack.getItem()).getDefaultState(), null, null, i), TintedBlock.REGISTRY.stream().map(Block::asItem).filter(i -> i != Items.AIR).toArray(Item[]::new));
        ColorProviderRegistry.ITEM.register((stack, i) -> i > 0 ? -1 : DyedColorComponent.getColor(stack, DyedColorComponent.DEFAULT_COLOR), UItems.FRIENDSHIP_BRACELET);
        ColorProviderRegistry.ITEM.register((stack, i) -> i > 0 || !EnchantableItem.isEnchanted(stack) ? Colors.WHITE : EnchantableItem.getSpellKey(stack).getColor() | 0xFF000000, UItems.GEMSTONE);
        ColorProviderRegistry.ITEM.register((stack, i) -> i == 1 && EnchantableItem.isEnchanted(stack) ? EnchantableItem.getSpellKey(stack).getColor() | 0xFF000000 : Colors.WHITE, UItems.MAGIC_STAFF);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), UBlocks.TRANSLUCENT_BLOCKS.stream().toArray(Block[]::new));
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), UBlocks.SEMI_TRANSPARENT_BLOCKS.stream().toArray(Block[]::new));
        // for lava boats
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), Fluids.LAVA, Fluids.FLOWING_LAVA);
        LeavesAdditionsModel.bootstrap();

        TerraformBoatClientHelper.registerModelLayers(Unicopia.id("palm"), false);

        SpellRendererFactory.bootstrap();
        UShaders.bootstrap();
    }

    private static void register(DynamicItemRenderer renderer, ItemConvertible...items) {
        for (ItemConvertible item : items) {
            BuiltinItemRendererRegistry.INSTANCE.register(item, renderer);
        }
    }

    @SuppressWarnings("unchecked")
    private static void renderBedItem(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        BlockEntity entity = ((Supplier<BlockEntity>)stack.getItem()).get();
        ((FancyBedBlock.Tile)entity).setPattern(FancyBedItem.getPattern(stack));
        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(entity, matrices, vertices, light, overlay);
    }

    private static void renderChestItem(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(CHEST_RENDER_ENTITY, matrices, vertices, light, overlay);
    }

    private static void renderJarItem(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();

        // Reset stuff done in the beforelands
        matrices.pop();

        if (mode == ModelTransformationMode.GUI) {
            DiffuseLighting.disableGuiDepthLighting();
        }

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        ClientWorld world = MinecraftClient.getInstance().world;

        if (Appearance.hasAppearance(stack)) {
            matrices.push();
            if (mode.isFirstPerson()) {
                matrices.translate(0.05, 0.06, 0.06);
            } else if (mode == ModelTransformationMode.HEAD) {
                matrices.translate(0, 0.4, 0);
            } else if (mode == ModelTransformationMode.GROUND
                    || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
                matrices.translate(0, 0.06, 0);
            }
            // GUI, FIXED, NONE - translate(0, 0, 0)

            float scale = 0.5F;
            matrices.scale(scale, scale, scale);
            renderer.renderItem(Appearance.upwrapAppearance(stack), mode, light, overlay, matrices, immediate, world, 0);
            matrices.pop();
        }
        renderer.renderItem(UItems.EMPTY_JAR.getDefaultStack(), mode, light, overlay, matrices, vertices, world, 0);

        if (mode == ModelTransformationMode.GUI) {
            if (vertices instanceof Immediate i) {
                i.draw();
            }

            DiffuseLighting.enableGuiDepthLighting();
        }

        matrices.push();
    }

    private static int getTintedBlockColor(BlockState state, @Nullable BlockRenderView view, @Nullable BlockPos pos, int color) {
        color = view == null || pos == null ? FoliageColors.getDefaultColor() : BiomeColors.getFoliageColor(view, pos);

        if (state.getBlock() instanceof TintedBlock block) {
            return block.getTint(state, view, pos, color) | 0xFF000000;
        }

        return color;
    }

    static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    interface ParticleSupplier<T extends ParticleEffect> {
        Particle get(T effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz);
    }
}
