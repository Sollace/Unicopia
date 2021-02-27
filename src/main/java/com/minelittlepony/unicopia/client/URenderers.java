package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.UEntities;
import com.minelittlepony.unicopia.client.particle.ChangelingMagicParticle;
import com.minelittlepony.unicopia.client.particle.CloudsEscapingParticle;
import com.minelittlepony.unicopia.client.particle.DiskParticle;
import com.minelittlepony.unicopia.client.particle.GroundPoundParticle;
import com.minelittlepony.unicopia.client.particle.HealthDrainParticle;
import com.minelittlepony.unicopia.client.particle.MagicParticle;
import com.minelittlepony.unicopia.client.particle.RainboomParticle;
import com.minelittlepony.unicopia.client.particle.RainbowTrailParticle;
import com.minelittlepony.unicopia.client.particle.RaindropsParticle;
import com.minelittlepony.unicopia.client.particle.SphereParticle;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer;
import com.minelittlepony.unicopia.client.render.AmuletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.FloatingArtefactEntityRenderer;
import com.minelittlepony.unicopia.client.render.WingsFeatureRenderer;
import com.minelittlepony.unicopia.item.ChameleonItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.particle.UParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.DyeableItem;
import net.minecraft.particle.ParticleEffect;

public interface URenderers {
    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(UParticles.UNICORN_MAGIC, createFactory(MagicParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.CHANGELING_MAGIC, createFactory(ChangelingMagicParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.RAIN_DROPS, createFactory(RaindropsParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.HEALTH_DRAIN, createFactory(HealthDrainParticle::new));
        ParticleFactoryRegistry.getInstance().register(UParticles.RAINBOOM_RING, RainboomParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.RAINBOOM_TRAIL, RainbowTrailParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.SPHERE, SphereParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.DISK, DiskParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.GROUND_POUND, GroundPoundParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.CLOUDS_ESCAPING, CloudsEscapingParticle::new);

        AccessoryFeatureRenderer.register(BraceletFeatureRenderer::new);
        AccessoryFeatureRenderer.register(AmuletFeatureRenderer::new);
        AccessoryFeatureRenderer.register(WingsFeatureRenderer::new);

        EntityRendererRegistry.INSTANCE.register(UEntities.THROWN_ITEM, (manager, context) -> new FlyingItemEntityRenderer<>(manager, context.getItemRenderer()));
        EntityRendererRegistry.INSTANCE.register(UEntities.FLOATING_ARTEFACT, FloatingArtefactEntityRenderer::new);

        ColorProviderRegistry.ITEM.register((stack, i) -> i > 0 ? -1 : ((DyeableItem)stack.getItem()).getColor(stack), UItems.FRIENDSHIP_BRACELET);
        BuiltinItemRendererRegistry.INSTANCE.register(UItems.FILLED_JAR, (stack, mode, matrices, vertexConsumers, light, overlay) -> {

            ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();

            ChameleonItem item = (ChameleonItem)stack.getItem();

            // Reset stuff done in the beforelands
            matrices.pop();

            if (mode == ModelTransformation.Mode.GUI) {
                DiffuseLighting.disableGuiDepthLighting();
            }

            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

            if (item.hasAppearance(stack)) {
                matrices.push();
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.translate(0.0125, 0.1, 0);
                renderer.renderItem(item.getAppearanceStack(stack), mode, light, overlay, matrices, immediate);
                matrices.pop();
            }
            renderer.renderItem(item.createAppearanceStack(stack, UItems.EMPTY_JAR), mode, light, OverlayTexture.DEFAULT_UV, matrices, immediate);
            immediate.draw();

            if (mode == ModelTransformation.Mode.GUI) {
                DiffuseLighting.enableGuiDepthLighting();
            }
            matrices.push();

        });
    }

    static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    interface ParticleSupplier<T extends ParticleEffect> {
        Particle get(T effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz);
    }
}
